package com.example.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyData(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = TransactionRepository(database.transactionDao)

    private val sharedPrefs = application.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    // UI States
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val startDate = MutableStateFlow<Long?>(null)
    val endDate = MutableStateFlow<Long?>(null)

    // Current Screen Tab (0: Dashboard, 1: Transactions, 2: Reports)
    val currentTab = MutableStateFlow(0)

    // Persistent Settings
    val isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("is_dark_mode", false))
    val currency = MutableStateFlow(sharedPrefs.getString("currency", "₹") ?: "₹")

    // Categories
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Salary", "Other")

    // Active screen toast message state (हम UI में toast दिखने के लिए इसे use करेंगे)
    val toastMessage = MutableStateFlow<String?>(null)

    init {
        // First run checks: database mein dummy data dalein agar empty hai
        viewModelScope.launch {
            repository.allTransactions.collect { list ->
                if (list.isEmpty()) {
                    insertSampleData()
                }
            }
        }
    }

    // Reactively filter transactions
    // यहाँ हम user की search query, category, और date range filters use कर रहे हैं
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        repository.allTransactions,
        searchQuery,
        selectedCategory,
        startDate,
        endDate
    ) { list, query, category, start, end ->
        list.filter { tx ->
            val matchesQuery = query.isEmpty() ||
                    tx.description.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true)
            
            val matchesCategory = category == "All" || tx.category.equals(category, ignoreCase = true)
            
            val matchesDate = (start == null || tx.date >= start) &&
                    (end == null || tx.date <= end)
            
            matchesQuery && matchesCategory && matchesDate
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Overall metrics for display cards
    // कुल आय (Total Income), कुल खर्च (Total Expense), और शेष राशि (Remaining Balance)
    val overallMetrics = repository.allTransactions.map { list ->
        val income = list.filter { it.type == "income" }.sumOf { it.amount }
        val expense = list.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = income - expense
        Triple(balance, income, expense)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Triple(0.0, 0.0, 0.0)
    )

    // Expense breakdown by categories for reports
    // Pie chart के लिए Category Wise Expense का logic
    val expenseBreakdown: StateFlow<Map<String, Double>> = filteredTransactions.map { list ->
        list.filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Monthly analytics grouped by month for report bars
    // Bar chart के लिए हर महीने आय बनाम खर्च (Income vs Expense)
    val monthlyAnalytics: StateFlow<List<MonthlyData>> = filteredTransactions.map { list ->
        val sdf = SimpleDateFormat("MMM yy", Locale.getDefault())
        val groupedMap = list.groupBy {
            sdf.format(Date(it.date))
        }

        groupedMap.map { (monthLabel, txs) ->
            val income = txs.filter { it.type == "income" }.sumOf { it.amount }
            val expense = txs.filter { it.type == "expense" }.sumOf { it.amount }
            MonthlyData(monthLabel, income, expense)
        }.sortedBy {
            try {
                sdf.parse(it.monthLabel)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Operations
    fun addTransaction(type: String, amount: Double, category: String, description: String, date: Long) {
        viewModelScope.launch {
            val tx = Transaction(
                type = type,
                amount = amount,
                category = category,
                description = description,
                date = date
            )
            repository.insert(tx)
            showToast("Added: ${tx.category} ($amount)")
        }
    }

    fun updateTransaction(id: Int, type: String, amount: Double, category: String, description: String, date: Long) {
        viewModelScope.launch {
            val tx = Transaction(
                id = id,
                type = type,
                amount = amount,
                category = category,
                description = description,
                date = date
            )
            repository.update(tx)
            showToast("Updated transaction successfully")
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
            showToast("Transaction deleted")
        }
    }

    fun toggleTheme() {
        val nextVal = !isDarkMode.value
        isDarkMode.value = nextVal
        sharedPrefs.edit().putBoolean("is_dark_mode", nextVal).apply()
        showToast("Theme changed to ${if (nextVal) "Dark" else "Light"}")
    }

    fun setCurrency(newSymbol: String) {
        currency.value = newSymbol
        sharedPrefs.edit().putString("currency", newSymbol).apply()
        showToast("Currency updated to $newSymbol")
    }

    private fun showToast(msg: String) {
        toastMessage.value = msg
    }

    fun clearToast() {
        toastMessage.value = null
    }

    // Insert sample entries spanning last 3 months
    // डेमो के लिए प्रारंभिक डेटा डालना, ताकि ग्राफ खाली न दिखे
    private suspend fun insertSampleData() {
        val cal = Calendar.getInstance()

        // Month 0 (Current)
        val t0 = cal.timeInMillis
        repository.insert(Transaction(type = "income", amount = 45000.0, category = "Salary", description = "Monthly Salary Credit", date = t0))
        repository.insert(Transaction(type = "expense", amount = 12000.0, category = "Bills", description = "House Rent Paid", date = t0 - 20000000))
        repository.insert(Transaction(type = "expense", amount = 450.0, category = "Food", description = "Swiggy Dinner", date = t0 - 30000000))
        repository.insert(Transaction(type = "expense", amount = 1800.0, category = "Shopping", description = "Bluetooth Earphones", date = t0 - 45000000))
        repository.insert(Transaction(type = "expense", amount = 600.0, category = "Transport", description = "Ola Ride to office", date = t0 - 64000000))

        // Month -1 (Previous Month)
        cal.add(Calendar.MONTH, -1)
        val t1 = cal.timeInMillis
        repository.insert(Transaction(type = "income", amount = 45000.0, category = "Salary", description = "Previous Month Salary", date = t1))
        repository.insert(Transaction(type = "expense", amount = 12000.0, category = "Bills", description = "House Rent", date = t1 - 10000000))
        repository.insert(Transaction(type = "expense", amount = 2400.0, category = "Entertainment", description = "Movie & Dinner", date = t1 - 40000000))
        repository.insert(Transaction(type = "expense", amount = 850.0, category = "Food", description = "Supermarket Groceries", date = t1 - 65000000))
        repository.insert(Transaction(type = "expense", amount = 3500.0, category = "Shopping", description = "T-shirts and Jeans", date = t1 - 85000000))

        // Month -2 (2 Months Ago)
        cal.add(Calendar.MONTH, -1)
        val t2 = cal.timeInMillis
        repository.insert(Transaction(type = "income", amount = 42000.0, category = "Salary", description = "Salary", date = t2))
        repository.insert(Transaction(type = "expense", amount = 11000.0, category = "Bills", description = "House Rent", date = t2 - 5000000))
        repository.insert(Transaction(type = "expense", amount = 1500.0, category = "Transport", description = "Fuel Refill", date = t2 - 15000000))
        repository.insert(Transaction(type = "expense", amount = 3200.0, category = "Entertainment", description = "Weekend Getaway Tour", date = t2 - 35000000))
        repository.insert(Transaction(type = "expense", amount = 980.0, category = "Food", description = "Zomato Pizza Party", date = t2 - 55000000))
    }
}

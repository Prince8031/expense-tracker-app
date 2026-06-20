package com.example

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyData
import com.example.data.Transaction
import com.example.data.TransactionViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            
            MyApplicationTheme(darkTheme = isDark) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: TransactionViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val currencySetting by viewModel.currency.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    // Show Dialog States
    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    // Custom Date Formatter
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Toast handling: flow notification
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "App Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Expense Tracker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Currency Selector dropdown
                    var showCurrencyMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showCurrencyMenu = true }) {
                        Text(
                            text = currencySetting,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false }
                    ) {
                        listOf("₹", "$", "€", "£", "¥").forEach { symbol ->
                            DropdownMenuItem(
                                text = { Text(text = "$symbol - Currency") },
                                onClick = {
                                    viewModel.setCurrency(symbol)
                                    showCurrencyMenu = false
                                }
                            )
                        }
                    }

                    // Night mode toggle
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.Star else Icons.Default.Refresh,
                            contentDescription = "Toggle Dark Mode",
                            tint = if (isDark) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.currentTab.value = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.currentTab.value = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Transactions") },
                    label = { Text("Transactions") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.currentTab.value = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0 || currentTab == 1) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add New", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onEditTransaction = { transaction ->
                        transactionToEdit = transaction
                    }
                )
                1 -> TransactionsScreen(
                    viewModel = viewModel,
                    onEditTransaction = { transaction ->
                        transactionToEdit = transaction
                    }
                )
                2 -> ReportsScreen(viewModel = viewModel)
            }

            // Dialog for Add Transaction
            if (showAddDialog) {
                AddEditTransactionDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { type, amount, category, description, date ->
                        viewModel.addTransaction(type, amount, category, description, date)
                        showAddDialog = false
                    },
                    viewModel = viewModel
                )
            }

            // Dialog for Edit Transaction
            transactionToEdit?.let { existingTx ->
                AddEditTransactionDialog(
                    transactionToEdit = existingTx,
                    onDismiss = { transactionToEdit = null },
                    onConfirm = { type, amount, category, description, date ->
                        viewModel.updateTransaction(existingTx.id, type, amount, category, description, date)
                        transactionToEdit = null
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

// -----------------------------------------------------
// 1. DASHBOARD SCREEN
// -----------------------------------------------------
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    onEditTransaction: (Transaction) -> Unit
) {
    val overallMetrics by viewModel.overallMetrics.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val currencySym by viewModel.currency.collectAsState()
    val moneyFormatter = remember { DecimalFormat("#,##,##0.00") }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Dashboard content vertical scroll
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcoming Title
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Finance Overview",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Balance Card
        // मुख्य बैलेंस कार्ड : जहाँ user को उसकी कुल बची हुई राशि दिखती है
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL BALANCE",
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val balanceColor = if (overallMetrics.first >= 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    Color(0xFFEF5350) // Coral red for negatives
                }

                Text(
                    text = "$currencySym${moneyFormatter.format(overallMetrics.first)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = balanceColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row of Income & Expense Summary
        // आय (Income) और खर्च (Expense) को अलग करने के लिए दो खूबसूरत Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Income card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)), // Clean light green background
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Income Icon",
                            tint = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySym${moneyFormatter.format(overallMetrics.second)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // Expense card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)), // Clean light red background
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "Expense Icon",
                            tint = Color(0xFFC62828)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySym${moneyFormatter.format(overallMetrics.third)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ratio Bar Visualization
        // एक सुंदर प्रोग्रेस बार जो आय और खर्च का अनुपात दिखाता है
        if (overallMetrics.second > 0 || overallMetrics.third > 0) {
            Text(
                text = "Expense to Income Ratio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val totalTurnover = overallMetrics.second + overallMetrics.third
            val incomeRatio = (overallMetrics.second / totalTurnover).toFloat()

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFC62828)) // Base represents Expenses
                ) {
                    // Overlapping green represent Income ratio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = incomeRatio)
                            .fillVertical()
                            .background(Color(0xFF2E7D32))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Income: ${(incomeRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Expense: ${((1f - incomeRatio) * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recent Transactions Section
        // हाल के ५ लेनदेन की सुसज्जित सूची
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { viewModel.currentTab.value = 1 }) {
                Text("See All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            EmptyStateView(
                title = "No Transactions Yet",
                desc = "Tap 'Add New' to kickstart your tracking!"
            )
        } else {
            // Limit to original top 5 transactions for dashboard display
            transactions.take(5).forEach { transaction ->
                TransactionRow(
                    tx = transaction,
                    currencySym = currencySym,
                    dateFormatter = dateFormatter,
                    onEditClick = { onEditTransaction(transaction) },
                    onDeleteClick = { viewModel.deleteTransaction(transaction.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Full modifier height fill helper
fun Modifier.fillVertical(): Modifier = this.then(Modifier.height(16.dp))

// -----------------------------------------------------
// 2. TRANSACTIONS LIST & FILTERS SCREEN
// -----------------------------------------------------
@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    onEditTransaction: (Transaction) -> Unit
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategory.collectAsState()
    val startDateFilter by viewModel.startDate.collectAsState()
    val endDateFilter by viewModel.endDate.collectAsState()
    val currencySym by viewModel.currency.collectAsState()

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var showAdvancedFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter header
        // लेनदेन सर्च, फ़िल्टर और केटेगरी कस्टमाइज़ करने का सेक्शन
        Surface(
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search Text Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by description...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Chips list or show advanced
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Selection Horizontal Scroll
                    // यहाँ user categories को horizontal scroll कर के filter कर सकता है
                    Text(
                        text = "Category Filter:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(onClick = { showAdvancedFilters = !showAdvancedFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Expand date filters",
                            tint = if (startDateFilter != null || endDateFilter != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterCategories = listOf("All") + viewModel.categories
                    filterCategories.forEach { category ->
                        val isSelected = selectedCategoryFilter == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable { viewModel.selectedCategory.value = category }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Advanced Date Filters (Collapsible)
                // तारीख सीमा (Date Range) के आधार पर खर्चे फ़िल्टर करना
                AnimatedVisibility(
                    visible = showAdvancedFilters,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start date selection
                            OutlinedButton(
                                onClick = {
                                    val currentNow = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                            }
                                            viewModel.startDate.value = cal.timeInMillis
                                        },
                                        currentNow.get(Calendar.YEAR),
                                        currentNow.get(Calendar.MONTH),
                                        currentNow.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Start Date", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = startDateFilter?.let { dateFormatter.format(Date(it)) } ?: "Start Date",
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // End Date selection
                            OutlinedButton(
                                onClick = {
                                    val currentNow = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                                set(Calendar.HOUR_OF_DAY, 23)
                                                set(Calendar.MINUTE, 59)
                                                set(Calendar.SECOND, 59)
                                            }
                                            viewModel.endDate.value = cal.timeInMillis
                                        },
                                        currentNow.get(Calendar.YEAR),
                                        currentNow.get(Calendar.MONTH),
                                        currentNow.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = "End Date", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = endDateFilter?.let { dateFormatter.format(Date(it)) } ?: "End Date",
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Reset Date Range
                        if (startDateFilter != null || endDateFilter != null) {
                            TextButton(
                                onClick = {
                                    viewModel.startDate.value = null
                                    viewModel.endDate.value = null
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Reset Dates", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // Transactions Column
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = "No Matches Found",
                    desc = "Try adjustments in search keys or filters!"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionRow(
                        tx = tx,
                        currencySym = currencySym,
                        dateFormatter = dateFormatter,
                        onEditClick = { onEditTransaction(tx) },
                        onDeleteClick = { viewModel.deleteTransaction(tx.id) }
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------
// 3. REPORTS & VISUALIZATIONS SCREEN (Canvas Graph logic)
// -----------------------------------------------------
@Composable
fun ReportsScreen(viewModel: TransactionViewModel) {
    val expenseBreakdown by viewModel.expenseBreakdown.collectAsState()
    val monthlyAnalytics by viewModel.monthlyAnalytics.collectAsState()
    val currencySym by viewModel.currency.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Visual Insights",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Graphical analysis of income versus expense flows",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Card containing Pie Chart for expense distribution
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Expenses by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "A breakdown of spending categories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                if (expenseBreakdown.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses recorded to draw a charts.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    CustomPieChart(
                        categoryExpenses = expenseBreakdown,
                        currencySym = currencySym
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card containing Bar Chart for monthly summaries
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Monthly Flow",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Comparison of Income vs Expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (monthlyAnalytics.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No historical transactions found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    CustomBarChart(
                        monthlyData = monthlyAnalytics,
                        currencySym = currencySym
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------
// 4. CUSTOM CANVAS DRAWINGS (Pie & Bar charts)
// -----------------------------------------------------

@Composable
fun CustomPieChart(
    categoryExpenses: Map<String, Double>,
    currencySym: String
) {
    val totalAmount = categoryExpenses.values.sum()
    val sortedCategories = categoryExpenses.toList().sortedByDescending { it.second }

    // Colors mapping list
    val listColors = remember {
        listOf(
            Color(0xFFFFA726), // Food - Orange
            Color(0xFF29B6F6), // Transport - Light Blue
            Color(0xFFEC407A), // Shopping - Pink
            Color(0xFFAB47BC), // Bills - Purple
            Color(0xFFFFD54F), // Entertainment - Amber
            Color(0xFF26A69A), // Salary/Income color
            Color(0xFF78909C)  // Other - Grey
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Canvas Drawing Block of Circular Pie Chart
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .weight(1.1f)
        ) {
            var currentAngle = 0f
            sortedCategories.forEachIndexed { idx, pair ->
                val sweep = ((pair.second / totalAmount) * 360f).toFloat()
                val colorIndex = idx % listColors.size
                drawArc(
                    color = listColors[colorIndex],
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                currentAngle += sweep
            }
        }

        // Details / Index column alongside
        Column(
            modifier = Modifier.weight(1.3f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sortedCategories.forEachIndexed { idx, pair ->
                val colorIndex = idx % listColors.size
                val percentage = ((pair.second / totalAmount) * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(listColors[colorIndex])
                    )
                    Text(
                        text = "${pair.first}: $currencySym${pair.second.toInt()} ($percentage%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBarChart(
    monthlyData: List<MonthlyData>,
    currencySym: String
) {
    // scale matching target height, finding max amount
    val maxIncome = monthlyData.maxOfOrNull { it.income } ?: 1.0
    val maxExpense = monthlyData.maxOfOrNull { it.expense } ?: 1.0
    val maxLimitVal = maxOf(maxIncome, maxExpense, 100.0)

    val gridlinesCount = 3
    val greenColor = Color(0xFF2E7D32)
    val redColor = Color(0xFFC62828)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Legends markers
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(greenColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(redColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Drawing custom scale inside Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val chartW = size.width
            val chartH = size.height - 24f // reserve space for text X-labels
            val barsSectionH = chartH

            // Draw Y-axis grid lines (Behind elements)
            for (i in 0..gridlinesCount) {
                val yVal = (barsSectionH / gridlinesCount) * i
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, yVal),
                    end = Offset(chartW, yVal),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw individual monthly double bar columns
            val groupCount = monthlyData.size
            val groupW = chartW / groupCount
            val barW = groupW * 0.25f // single column bar thin percent width

            monthlyData.forEachIndexed { index, data ->
                val groupStartX = groupW * index
                val centerX = groupStartX + (groupW / 2)

                // Scaling heights
                val incomeH = ((data.income / maxLimitVal) * barsSectionH).toFloat()
                val expenseH = ((data.expense / maxLimitVal) * barsSectionH).toFloat()

                // Drawing Left Bar: Income
                drawRect(
                    color = greenColor,
                    topLeft = Offset(centerX - barW - 4f, barsSectionH - incomeH),
                    size = Size(barW, incomeH)
                )

                // Drawing Right Bar: Expense
                drawRect(
                    color = redColor,
                    topLeft = Offset(centerX + 4f, barsSectionH - expenseH),
                    size = Size(barW, expenseH)
                )

                // Note: Standard native text drawing is easiest on canvas block or through overlay composables.
                // Draw Month labels simply by overlays, or standard simple draw parameters.
            }
        }

        // Beautiful month labels mapped dynamically using native Row aligned under columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            monthlyData.forEach { data ->
                Text(
                    text = data.monthLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// -----------------------------------------------------
// 5. HELPER VIEW COMPOSABLES
// -----------------------------------------------------

@Composable
fun TransactionRow(
    tx: Transaction,
    currencySym: String,
    dateFormatter: SimpleDateFormat,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isIncome = tx.type == "income"
    val colorAccent = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val cardBg = if (isIncome) Color(0xFFE8F5E9).copy(alpha = 0.3f) else Color(0xFFFFEBEE).copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Category Circular visual emblem indicator
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(cardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(tx.category),
                    contentDescription = tx.category,
                    tint = colorAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text parameters description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description.ifEmpty { tx.category },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tx.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormatter.format(Date(tx.date)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount column & delete action triggers
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"}$currencySym${tx.amount}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = colorAccent
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Edit trigger
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Transaction",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Quick Delete trigger
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Transaction",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = "Empty state icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// Map corresponding category strings into beautiful vector landmarks
fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase(Locale.getDefault()).trim()) {
        "food" -> Icons.Default.Fastfood
        "transport" -> Icons.Default.Commute
        "shopping" -> Icons.Default.LocalMall
        "bills" -> Icons.Default.Receipt
        "entertainment" -> Icons.Default.SportsEsports
        "salary" -> Icons.Default.Payments
        else -> Icons.Default.Category
    }
}

// -----------------------------------------------------
// 6. ADD / EDIT TRANSACTION DIALOG (MODAL OVERLAY)
// -----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
    transactionToEdit: Transaction? = null,
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, category: String, description: String, date: Long) -> Unit,
    viewModel: TransactionViewModel
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var type by remember { mutableStateOf(transactionToEdit?.type ?: "expense") }
    var amountStr by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(transactionToEdit?.description ?: "") }
    var category by remember { mutableStateOf(transactionToEdit?.category ?: "Food") }
    var selectedDateMillis by remember { mutableStateOf(transactionToEdit?.date ?: System.currentTimeMillis()) }

    val isEditMode = transactionToEdit != null

    // सेगमेंटेड बटन का कलर्स जो आय (Income) के लिए हरा और खर्च (Expense) के लिए लाल होगा
    val submitButtonColor = animateColorAsState(
        targetValue = if (type == "income") Color(0xFF2E7D32) else Color(0xFFC62828),
        animationSpec = tween(durationMillis = 300), label = "btnColor"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Edit Transaction" else "Add Transaction",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Segment toggle: Income vs Expense
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Expense Tab Selection
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (type == "expense") Color(0xFFC62828) else Color.Transparent)
                            .clickable {
                                type = "expense"
                                if (category == "Salary") category = "Food" // adjust salary check
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Expense",
                            fontWeight = FontWeight.Bold,
                            color = if (type == "expense") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Income Tab Selection
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (type == "income") Color(0xFF2E7D32) else Color.Transparent)
                            .clickable {
                                type = "income"
                                category = "Salary"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Income",
                            fontWeight = FontWeight.Bold,
                            color = if (type == "income") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount Text Field (decimal only)
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Description Title
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Category Chips Selector list
                Text(
                    text = "Select Category:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val availableCats = if (type == "income") listOf("Salary", "Other") else viewModel.categories.filter { it != "Salary" }
                    availableCats.forEach { cat ->
                        val isSelected = category == cat
                        val catBg = if (isSelected) submitButtonColor.value else MaterialTheme.colorScheme.surfaceVariant
                        val catText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(catBg)
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = cat,
                                modifier = Modifier.size(14.dp),
                                tint = catText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(cat, fontSize = 11.sp, color = catText, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Date Picker Action Trigger
                OutlinedButton(
                    onClick = {
                        val currentNow = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }
                                selectedDateMillis = cal.timeInMillis
                            },
                            currentNow.get(Calendar.YEAR),
                            currentNow.get(Calendar.MONTH),
                            currentNow.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date: ${dateFormatter.format(Date(selectedDateMillis))}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Choose Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        Toast.makeText(context, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val descFilled = description.ifEmpty { category }
                    onConfirm(type, amt, category, descFilled, selectedDateMillis)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = submitButtonColor.value,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isEditMode) "Save Changes" else "Add Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

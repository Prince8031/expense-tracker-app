package com.example.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()

    suspend fun insert(transaction: Transaction) {
        dao.insertTransaction(transaction)
    }

    suspend fun update(transaction: Transaction) {
        dao.updateTransaction(transaction)
    }

    suspend fun delete(id: Int) {
        dao.deleteTransaction(id)
    }
}

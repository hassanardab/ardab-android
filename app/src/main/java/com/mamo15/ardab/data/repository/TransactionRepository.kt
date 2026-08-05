package com.mamo15.ardab.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mamo15.ardab.data.entity.TransactionEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("transactions")

    fun getAllTransactions(): Flow<List<TransactionEntity>> = callbackFlow {
        val listener = collection.orderBy("date").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject<TransactionEntity>()?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    fun getTransactionsForProject(projectId: String): Flow<List<TransactionEntity>> = callbackFlow {
        val listener = collection
            .whereEqualTo("projectId", projectId)
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<TransactionEntity>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertTransaction(transaction: TransactionEntity): String {
        val docRef = collection.document()
        val entity = transaction.copy(id = docRef.id)
        docRef.set(entity).await()
        return docRef.id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        collection.document(transaction.id).set(transaction).await()
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        collection.document(transaction.id).delete().await()
    }

    // Per‑project balances
    suspend fun getCashBalanceForProject(projectId: String): Double =
        collection.whereEqualTo("projectId", projectId)
            .whereEqualTo("paymentMethod", "CASH")
            .get().await()
            .documents.sumOf { it.getDouble("amount") ?: 0.0 }

    suspend fun getBankBalanceForProject(projectId: String): Double =
        collection.whereEqualTo("projectId", projectId)
            .whereEqualTo("paymentMethod", "BANK")
            .get().await()
            .documents.sumOf { it.getDouble("amount") ?: 0.0 }

    suspend fun getLoanBalanceForProject(projectId: String): Double {
        val snapshot = collection.whereEqualTo("projectId", projectId)
            .whereIn("type", listOf("LOAN", "LOAN_REPAYMENT"))
            .get().await()
        return snapshot.documents.sumOf { doc ->
            val amount = doc.getDouble("amount") ?: 0.0
            val type = doc.getString("type")
            if (type == "LOAN") amount else -amount
        }
    }

    // Total (all projects) balances
    suspend fun getTotalCashBalance(): Double =
        collection.whereEqualTo("paymentMethod", "CASH").get().await()
            .documents.sumOf { it.getDouble("amount") ?: 0.0 }

    suspend fun getTotalBankBalance(): Double =
        collection.whereEqualTo("paymentMethod", "BANK").get().await()
            .documents.sumOf { it.getDouble("amount") ?: 0.0 }

    suspend fun getTotalLoanBalance(): Double {
        val snapshot = collection.whereIn("type", listOf("LOAN", "LOAN_REPAYMENT")).get().await()
        return snapshot.documents.sumOf { doc ->
            val amount = doc.getDouble("amount") ?: 0.0
            val type = doc.getString("type")
            if (type == "LOAN") amount else -amount
        }
    }
}
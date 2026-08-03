package com.mamo15.ardab.data.entity

import com.mamo15.ardab.data.PaymentMethod
import com.mamo15.ardab.data.TransactionType

data class TransactionEntity(
    val id: String = "",
    val projectId: String = "",   // Firestore document ID of the project
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.INCOME,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val description: String = "",
    val date: Long = System.currentTimeMillis()
)
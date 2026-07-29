package com.mamo15.ardab.data

data class Project(
    val id: String,
    val name: String,
    val currentCash: Double,
    val currentBank: Double,
    val createdAt: String
)

enum class PaymentMethod {
    CASH, BANK, CREDIT
}

enum class TransactionType {
    INCOME, EXPENSE, BILL, PAYROLL, LOAN, TRANSFER
}

data class Transaction(
    val id: String,
    val projectId: String,
    val type: TransactionType,
    val method: PaymentMethod,
    val amount: Double,
    val description: String,
    val date: String,
    val employeeId: String? = null, // If payroll
    val vendorId: String? = null    // If bill/expense
)

data class Employee(
    val id: String,
    val name: String,
    val role: String
)

data class Vendor(
    val id: String,
    val name: String,
    val balance: Double
)
package com.mamo15.ardab.data

enum class PaymentMethod {
    CASH, BANK, CREDIT
}

enum class ReportType {
    FINANCIAL, EMPLOYEE, VENDOR
}

enum class TransactionType {
    INCOME, EXPENSE, BILL, PAYROLL, LOAN, LOAN_REPAYMENT, TRANSFER
}

// Optional – keep if you use them
data class Employee(val id: String, val name: String, val role: String)
data class Vendor(val id: String, val name: String, val balance: Double)
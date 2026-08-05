package com.mamo15.ardab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mamo15.ardab.data.PaymentMethod
import com.mamo15.ardab.data.TransactionType
import com.mamo15.ardab.data.entity.ProjectEntity
import com.mamo15.ardab.data.entity.TransactionEntity
import com.mamo15.ardab.data.repository.ProjectRepository
import com.mamo15.ardab.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectDetailsViewModel(
    private val projectRepo: ProjectRepository,
    private val transactionRepo: TransactionRepository,
    private val projectId: String
) : ViewModel() {

    private val _project = MutableStateFlow<ProjectEntity?>(null)
    val project: StateFlow<ProjectEntity?> = _project.asStateFlow()

    val transactions: StateFlow<List<TransactionEntity>> = transactionRepo
        .getTransactionsForProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balances: StateFlow<ProjectBalances> = transactions.map { list ->
        val cash = list.filter { it.paymentMethod == PaymentMethod.CASH }
            .sumOf { if (it.type in listOf(TransactionType.INCOME, TransactionType.LOAN)) it.amount else -it.amount }
        val bank = list.filter { it.paymentMethod == PaymentMethod.BANK }
            .sumOf { if (it.type in listOf(TransactionType.INCOME, TransactionType.LOAN)) it.amount else -it.amount }
        val loan = list.filter { it.type == TransactionType.LOAN || it.type == TransactionType.LOAN_REPAYMENT }
            .sumOf { if (it.type == TransactionType.LOAN) it.amount else -it.amount }
        ProjectBalances(cash, bank, loan)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProjectBalances(0.0, 0.0, 0.0))

    init {
        viewModelScope.launch {
            _project.value = projectRepo.getProjectById(projectId)
        }
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionRepo.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionRepo.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionRepo.deleteTransaction(transaction)
    }

    data class ProjectBalances(
        val cash: Double = 0.0,
        val bank: Double = 0.0,
        val loan: Double = 0.0
    )
}
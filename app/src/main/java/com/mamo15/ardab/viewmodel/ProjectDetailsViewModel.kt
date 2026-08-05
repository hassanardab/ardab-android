package com.mamo15.ardab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope  // <-- add this
import com.mamo15.ardab.data.PaymentMethod
import com.mamo15.ardab.data.TransactionType
import com.mamo15.ardab.data.entity.ProjectEntity
import com.mamo15.ardab.data.entity.TransactionEntity
import com.mamo15.ardab.data.repository.ProjectRepository
import com.mamo15.ardab.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch  // <-- add this

class ProjectDetailsViewModel(
    private val projectRepo: ProjectRepository,
    private val transactionRepo: TransactionRepository,
    private val projectId: String
) : ViewModel() {

    private val _project = MutableStateFlow<ProjectEntity?>(null)
    val project: StateFlow<ProjectEntity?> = _project.asStateFlow()

    val transactions: StateFlow<List<TransactionEntity>> = transactionRepo
        .getTransactionsForProject(projectId)  // now resolved
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
        viewModelScope.launch {   // now the coroutine is properly scoped
            _project.value = projectRepo.getProjectById(projectId)
        }
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionRepo.insertTransaction(transaction)  // now resolved
    }

    data class ProjectBalances(
        val cash: Double = 0.0,
        val bank: Double = 0.0,
        val loan: Double = 0.0
    )
}
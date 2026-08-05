package com.mamo15.ardab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope  // <-- add this import
import com.mamo15.ardab.data.entity.ProjectEntity
import com.mamo15.ardab.data.entity.TransactionEntity
import com.mamo15.ardab.data.repository.ProjectRepository
import com.mamo15.ardab.data.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch  // <-- add this import (optional, but good practice)

class DashboardViewModel(
    private val projectRepo: ProjectRepository,
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    val projects: StateFlow<List<ProjectEntity>> = projectRepo.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        projects,
        balanceFlow()
    ) { projects, (totalCash, totalBank, totalLoan) ->
        DashboardSummary(
            totalCash = totalCash,
            totalBank = totalBank,
            totalLoan = totalLoan,
            totalBalance = totalCash + totalBank,
            activeProjectsCount = projects.size,
            projects = projects
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardSummary()
    )

    private fun balanceFlow(): Flow<Triple<Double, Double, Double>> = flow {
        while (true) {
            val cash = transactionRepo.getTotalCashBalance()
            val bank = transactionRepo.getTotalBankBalance()
            val loan = transactionRepo.getTotalLoanBalance()
            emit(Triple(cash, bank, loan))
            delay(5000)
        }
    }.catch { emit(Triple(0.0, 0.0, 0.0)) }

    suspend fun addProject(name: String) {
        projectRepo.insertProject(ProjectEntity(name = name))
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionRepo.insertTransaction(transaction)  // now resolved
    }

    data class DashboardSummary(
        val totalCash: Double = 0.0,
        val totalBank: Double = 0.0,
        val totalLoan: Double = 0.0,
        val totalBalance: Double = 0.0,
        val activeProjectsCount: Int = 0,
        val projects: List<ProjectEntity> = emptyList()
    )
}
package com.mamo15.ardab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mamo15.ardab.data.entity.ProjectEntity
import com.mamo15.ardab.data.entity.TransactionEntity
import com.mamo15.ardab.data.repository.ProjectRepository
import com.mamo15.ardab.data.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val projectRepo: ProjectRepository,
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    // Live list of projects
    val projects: StateFlow<List<ProjectEntity>> = projectRepo.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined summary: projects + computed balances
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        projects,
        // We'll fetch balances each time projects change, but we need flows for combine.
        // To avoid making balances flows, we can use flatMapLatest or just use a separate state.
        // Simpler: use projects as source and compute balances on demand.
        // We'll create a separate flow for balances:
        balanceFlow()
    ) { projects, (totalCash, totalBank) ->
        DashboardSummary(
            totalCash = totalCash,
            totalBank = totalBank,
            totalBalance = totalCash + totalBank,
            activeProjectsCount = projects.size,
            projects = projects
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardSummary()
    )

    // Create a flow that emits balances periodically or on demand
    private fun balanceFlow(): Flow<Pair<Double, Double>> = flow {
        while (true) {
            val cash = transactionRepo.getTotalCashBalance()
            val bank = transactionRepo.getTotalBankBalance()
            emit(cash to bank)
            delay(5000) // Refresh every 5 seconds or use a trigger (e.g., when transactions change)
        }
    }.catch { emit(0.0 to 0.0) }

    suspend fun addProject(name: String) {
        projectRepo.insertProject(ProjectEntity(name = name))
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionRepo.insertTransaction(transaction)
    }

    data class DashboardSummary(
        val totalCash: Double = 0.0,
        val totalBank: Double = 0.0,
        val totalBalance: Double = 0.0,
        val activeProjectsCount: Int = 0,
        val projects: List<ProjectEntity> = emptyList()
    )
}
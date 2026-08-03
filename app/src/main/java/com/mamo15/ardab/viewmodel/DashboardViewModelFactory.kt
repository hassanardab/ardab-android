package com.mamo15.ardab.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mamo15.ardab.data.repository.ProjectRepository
import com.mamo15.ardab.data.repository.TransactionRepository

class DashboardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            val firestore = FirebaseFirestore.getInstance()
            val projectRepo = ProjectRepository(firestore)
            val transactionRepo = TransactionRepository(firestore)
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(projectRepo, transactionRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
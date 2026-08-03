package com.mamo15.ardab.data.entity

data class ProjectEntity(
    val id: String = "",          // Firestore document ID
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
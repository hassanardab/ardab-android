package com.mamo15.ardab.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mamo15.ardab.data.entity.ProjectEntity   // correct import
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProjectRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("projects")

    fun getAllProjects(): Flow<List<ProjectEntity>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val projects = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject<ProjectEntity>()?.copy(id = doc.id)
            } ?: emptyList()
            trySend(projects)
        }
        awaitClose { listener.remove() }
    }

    suspend fun insertProject(project: ProjectEntity): String {
        val docRef = collection.document()
        val entity = project.copy(id = docRef.id)
        docRef.set(entity).await()
        return docRef.id
    }

    suspend fun updateProject(project: ProjectEntity) {
        collection.document(project.id).set(project).await()  // no .toString()
    }

    suspend fun deleteProject(project: ProjectEntity) {
        collection.document(project.id).delete().await()      // no .toString()
    }
}
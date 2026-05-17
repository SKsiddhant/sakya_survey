package com.geosurvey.pro.utils

import com.geosurvey.pro.domain.model.User
import com.geosurvey.pro.domain.model.UserRole
import com.geosurvey.pro.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoAccountSeeder @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) {
    suspend fun createDemoAccounts() {
        val demoUsers = listOf(
            DemoUser("admin@geosurvey.com", "admin123", "Demo Admin", UserRole.ADMIN),
            DemoUser("user@geosurvey.com", "user123", "Demo User", UserRole.USER)
        )

        demoUsers.forEach { demo ->
            val result = authRepository.signUp(demo.email, demo.password, demo.name)
            
            result.onSuccess { user ->
                // 1. Created new account, now set the role in Firestore
                val userMap = hashMapOf(
                    "id" to user.id,
                    "email" to demo.email,
                    "displayName" to demo.name,
                    "role" to demo.role.value
                )
                firestore.collection("users").document(user.id).set(userMap).await()
                println("Created ${demo.role.value} account: ${demo.email}")
            }.onFailure { e ->
                // 2. Account likely already exists or other error
                println("Note: ${demo.email} sign up failed: ${e.message}. Ensuring role is correct.")
                updateExistingRole(demo)
            }
        }
    }

    private suspend fun updateExistingRole(demo: DemoUser) {
        try {
            // Find user by email and update role to ensure demo works
            val query = firestore.collection("users")
                .whereEqualTo("email", demo.email)
                .get()
                .await()
            
            if (!query.isEmpty) {
                val docId = query.documents[0].id
                firestore.collection("users").document(docId)
                    .update("role", demo.role.value)
                    .await()
                println("Updated role for existing user: ${demo.email}")
            } else {
                // User exists in Auth but not in Firestore? Let's try to sign in to get UID
                // or just skip if we can't find them easily by email in Firestore
                println("User ${demo.email} exists in Auth but not found in Firestore 'users' collection.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class DemoUser(
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole
)

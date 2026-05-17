package com.geosurvey.pro.data.repository

import timber.log.Timber
import com.geosurvey.pro.domain.model.User
import com.geosurvey.pro.domain.model.UserRole
import com.geosurvey.pro.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                // We initially send a partial user, but in a real app 
                // we might want to fetch the role here too if it's critical for UI
                trySend(User(id = firebaseUser.uid, email = firebaseUser.email ?: "", displayName = firebaseUser.displayName ?: ""))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, pass: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("User not found")
            
            // Fetch role from Firestore with offline fallback
            val user = try {
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = userDoc.getString("displayName") ?: firebaseUser.displayName ?: "",
                    role = userDoc.getString("role") ?: UserRole.USER.value
                )
            } catch (e: Exception) {
                Timber.w(e, "Firestore fetch failed (possibly offline)")
                // Fallback for demo accounts or default to user
                val fallbackRole = if (email.equals("admin@geosurvey.com", ignoreCase = true)) {
                    UserRole.ADMIN.value
                } else {
                    UserRole.USER.value
                }
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    role = fallbackRole
                )
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, pass: String, name: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")
            
            val user = User(id = firebaseUser.uid, email = firebaseUser.email ?: "", displayName = name, role = "user")
            
            // Non-blocking Firestore write to avoid hanging if DB is slow/not found
            try {
                firestore.collection("users").document(firebaseUser.uid).set(user)
            } catch (e: Exception) {
                Timber.e(e, "Pre-emptive Firestore failure")
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}

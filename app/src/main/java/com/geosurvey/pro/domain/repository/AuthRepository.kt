package com.geosurvey.pro.domain.repository

import com.geosurvey.pro.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signIn(email: String, pass: String): Result<User>
    suspend fun signUp(email: String, pass: String, name: String): Result<User>
    suspend fun signOut()
}

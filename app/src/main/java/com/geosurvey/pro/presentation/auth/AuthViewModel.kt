package com.geosurvey.pro.presentation.auth

import androidx.lifecycle.viewModelScope
import com.geosurvey.pro.domain.model.UserRole
import com.geosurvey.pro.domain.repository.AuthRepository
import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.geosurvey.pro.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : BaseViewModel<AuthState, AuthEffect, AuthEvent>(AuthState()) {

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                setState { copy(user = user) }
            }
        }
    }

    override fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.Login -> login(event.email, event.pass, event.isAdmin)
            is AuthEvent.Register -> register(event.email, event.pass, event.name)
            is AuthEvent.ForgotPassword -> forgotPassword(event.email)
            AuthEvent.Logout -> logout()
        }
    }

    private fun login(email: String, pass: String, isAdmin: Boolean) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            setState { copy(isLoading = true, error = null) }
            val result = authRepository.signIn(trimmedEmail, pass)
            result.onSuccess { user ->
                // Fetch full user data from Firestore to check role
                // For simplicity, we assume role is part of user object returned by repo
                // In a real app, you'd fetch the document from 'users' collection
                if (isAdmin && user.role != UserRole.ADMIN.value) {
                    setState { copy(isLoading = false, error = "Access denied: Not an Admin") }
                } else {
                    setState { copy(isLoading = false, isSuccess = true) }
                    if (user.role == UserRole.ADMIN.value) {
                        sendEffect(AuthEffect.NavigateToAdminDashboard)
                    } else {
                        sendEffect(AuthEffect.NavigateToUserDashboard)
                    }
                }
            }.onFailure { e ->
                setState { copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            setState { copy(isLoading = true, error = null) }
            val result = authRepository.signUp(trimmedEmail, pass, name)
            result.onSuccess { user ->
                setState { copy(isLoading = false, isSuccess = true) }
                sendEffect(AuthEffect.NavigateToUserDashboard)
            }.onFailure { e ->
                setState { copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun forgotPassword(email: String) {
        // Implement forgot password logic in AuthRepository first
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            setState { AuthState() }
        }
    }
}

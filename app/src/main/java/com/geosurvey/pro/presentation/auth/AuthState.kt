package com.geosurvey.pro.presentation.auth

import com.geosurvey.pro.domain.model.User

data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

sealed class AuthEffect {
    object NavigateToUserDashboard : AuthEffect()
    object NavigateToAdminDashboard : AuthEffect()
    data class ShowError(val message: String) : AuthEffect()
}

sealed class AuthEvent {
    data class Login(val email: String, val pass: String, val isAdmin: Boolean = false) : AuthEvent()
    data class Register(val email: String, val pass: String, val name: String) : AuthEvent()
    data class ForgotPassword(val email: String) : AuthEvent()
    object Logout : AuthEvent()
}

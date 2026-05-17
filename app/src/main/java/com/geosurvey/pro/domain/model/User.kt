package com.geosurvey.pro.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val role: String = "user"
)

enum class UserRole(val value: String) {
    ADMIN("admin"),
    USER("user")
}

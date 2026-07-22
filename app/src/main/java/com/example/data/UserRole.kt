package com.example.data

enum class UserRole(val label: String) {
    USER("User"),
    ADMIN("Admin")
}

data class UserSession(
    val uid: String,
    val email: String,
    val role: UserRole
)

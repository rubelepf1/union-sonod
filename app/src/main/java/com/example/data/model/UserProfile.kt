package com.example.data.model

data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String = "operator", // super_admin, union_admin, operator
    val unionId: String? = null,
    val phone: String? = null,
    val isActive: Boolean = true,
    val accessToken: String? = null
) {
    val isSuperAdmin: Boolean get() = role == "super_admin"
    val isUnionAdmin: Boolean get() = role == "union_admin"
    val isOperator: Boolean get() = role == "operator"

    val roleTitleBn: String
        get() = when (role) {
            "super_admin" -> "সুপার অ্যাডমিন"
            "union_admin" -> "ইউপি প্রশাসক"
            "operator" -> "ইউপি অপারেটর"
            else -> "ব্যবহারকারী"
        }
}

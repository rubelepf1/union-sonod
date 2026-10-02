package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("up_sonod_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile?>(loadSession())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isOfflineGuestMode = MutableStateFlow(prefs.getBoolean("is_guest_mode", false))
    val isOfflineGuestMode: StateFlow<Boolean> = _isOfflineGuestMode.asStateFlow()

    fun saveSession(user: UserProfile) {
        prefs.edit()
            .putString("user_id", user.id)
            .putString("email", user.email)
            .putString("full_name", user.fullName)
            .putString("role", user.role)
            .putString("union_id", user.unionId)
            .putString("phone", user.phone)
            .putBoolean("is_active", user.isActive)
            .putString("access_token", user.accessToken)
            .putBoolean("is_guest_mode", false)
            .apply()
        _isOfflineGuestMode.value = false
        _currentUser.value = user
    }

    fun setOfflineGuestMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_guest_mode", enabled).apply()
        _isOfflineGuestMode.value = enabled
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _currentUser.value = null
        _isOfflineGuestMode.value = false
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun isLoggedIn(): Boolean = _currentUser.value != null && !_currentUser.value?.accessToken.isNullOrBlank()

    private fun loadSession(): UserProfile? {
        val id = prefs.getString("user_id", null) ?: return null
        val email = prefs.getString("email", "") ?: ""
        val fullName = prefs.getString("full_name", "") ?: ""
        val role = prefs.getString("role", "operator") ?: "operator"
        val unionId = prefs.getString("union_id", null)
        val phone = prefs.getString("phone", null)
        val isActive = prefs.getBoolean("is_active", true)
        val token = prefs.getString("access_token", null)

        return UserProfile(
            id = id,
            email = email,
            fullName = fullName,
            role = role,
            unionId = unionId,
            phone = phone,
            isActive = isActive,
            accessToken = token
        )
    }
}

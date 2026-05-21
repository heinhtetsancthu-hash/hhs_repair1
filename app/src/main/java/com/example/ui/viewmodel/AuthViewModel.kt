package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ServiceLocator
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = ServiceLocator.getUserRepository(application)
    private val settings = ServiceLocator.getSettingsManager(application)

    val loggedInUser: StateFlow<UserEntity?> = userRepository.loggedInUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentView = MutableStateFlow(AuthView.LOGIN)
    val currentView: StateFlow<AuthView> = _currentView.asStateFlow()

    enum class AuthView {
        LOGIN, REGISTER
    }

    fun switchView(view: AuthView) {
        _error.value = null
        _currentView.value = view
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and password cannot be empty."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                userRepository.login(email, password)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Authentication failed."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, fullName: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || fullName.isBlank() || password.isBlank()) {
            _error.value = "All fields are required."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                userRepository.register(email, fullName, password)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Registration failed."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            userRepository.logout()
            settings.clearSyncTime()
            onComplete()
        }
    }

    fun clearError() {
        _error.value = null
    }
}

package com.example.pgfinderapp.presentation.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pgfinderapp.data.model.AuthRepository
import com.example.pgfinderapp.data.model.AuthResult
import com.example.pgfinderapp.data.model.Role
import com.example.pgfinderapp.data.model.User
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val isLoggedIn: Boolean = false,
    val passwordResetSent: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    var authState by mutableStateOf(AuthState())
        private set
    
    init {
        checkCurrentUser()
    }
    
    private fun checkCurrentUser() {
        viewModelScope.launch {
            Log.d(TAG, "Checking current user...")
            val user = authRepository.getCurrentUser()
            Log.d(TAG, "Current user: ${user?.email}")
            authState = authState.copy(
                currentUser = user,
                isLoggedIn = user != null
            )
        }
    }
    
    fun login(email: String, password: String) {
        Log.d(TAG, "login() called with email: $email")
        viewModelScope.launch {
            authState = authState.copy(isLoading = true, error = null)
            Log.d(TAG, "Starting login...")
            
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Login successful: ${result.user.email}")
                    authState = authState.copy(
                        isLoading = false,
                        currentUser = result.user,
                        isLoggedIn = true,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "Login failed: ${result.message}")
                    authState = authState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                AuthResult.Loading -> {
                    authState = authState.copy(isLoading = true)
                }
            }
        }
    }
    
    fun signup(name: String, age: Int, email: String, password: String, role: Role) {
        Log.d(TAG, "signup() called with email: $email, name: $name, role: $role")
        viewModelScope.launch {
            authState = authState.copy(isLoading = true, error = null)
            Log.d(TAG, "Starting signup...")
            
            when (val result = authRepository.signup(name, age, email, password, role)) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Signup successful: ${result.user.email}")
                    authState = authState.copy(
                        isLoading = false,
                        currentUser = result.user,
                        isLoggedIn = true,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "Signup failed: ${result.message}")
                    authState = authState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                AuthResult.Loading -> {
                    authState = authState.copy(isLoading = true)
                }
            }
        }
    }
    
    fun logout() {
        Log.d(TAG, "logout() called")
        authRepository.logout()
        authState = AuthState()
    }
    
    fun sendPasswordResetEmail(email: String) {
        Log.d(TAG, "sendPasswordResetEmail() called with email: $email")
        viewModelScope.launch {
            authState = authState.copy(isLoading = true, error = null)
            
            authRepository.sendPasswordResetEmail(email).fold(
                onSuccess = {
                    Log.d(TAG, "Password reset email sent")
                    authState = authState.copy(
                        isLoading = false,
                        passwordResetSent = true
                    )
                },
                onFailure = {
                    Log.e(TAG, "Password reset failed: ${it.message}")
                    authState = authState.copy(
                        isLoading = false,
                        error = it.message ?: "Failed to send reset email"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        authState = authState.copy(error = null)
    }
    
    fun clearPasswordResetSent() {
        authState = authState.copy(passwordResetSent = false)
    }
}

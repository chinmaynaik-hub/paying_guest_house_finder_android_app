package com.example.pgfinderapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgfinderapp.data.model.Role
import com.example.pgfinderapp.data.model.User

@Composable
fun WelcomeScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("PG Finder", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Find your perfect stay", color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))

        Button(
            onClick = { onNavigate("login") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Started", fontSize = 18.sp)
        }
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (User) -> Unit,
    onNavigate: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState = authViewModel.authState
    
    // Handle successful login
    LaunchedEffect(authState.isLoggedIn, authState.currentUser) {
        if (authState.isLoggedIn && authState.currentUser != null) {
            onLoginSuccess(authState.currentUser)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = { onNavigate("guest_home") },
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Welcome Back", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Login to your account to continue", color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))

            if (authState.error != null) {
                Text(authState.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
            }

            OutlinedTextField(
                value = email, onValueChange = { email = it.trim() },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !authState.isLoading,
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onNavigate("forgot_password") }) {
                    Text("Forgot Password?")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        authViewModel.login(email.trim().lowercase(), password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !authState.isLoading
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login", fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = { onNavigate("signup") }) {
                Text("Don't have an account? Create Account")
            }
        }
    }
}

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onSignupSuccess: (User) -> Unit,
    onNavigate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.GUEST) }
    val authState = authViewModel.authState
    
    // Handle successful signup
    LaunchedEffect(authState.isLoggedIn, authState.currentUser) {
        if (authState.isLoggedIn && authState.currentUser != null) {
            onSignupSuccess(authState.currentUser)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = { onNavigate("login") }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        
        if (authState.error != null) {
            Text(authState.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it.trim() },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = age, onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("I am a...", fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { role = Role.GUEST },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (role == Role.GUEST) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ),
                enabled = !authState.isLoading
            ) { Text("PG Finder") }
            OutlinedButton(
                onClick = { role = Role.OWNER },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (role == Role.OWNER) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ),
                enabled = !authState.isLoading
            ) { Text("PG Owner") }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && age.isNotBlank()) {
                    authViewModel.signup(name.trim(), age.toIntOrNull() ?: 18, email.trim().lowercase(), password, role)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !authState.isLoading
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val authState = authViewModel.authState
    
    // Handle successful password reset email
    LaunchedEffect(authState.passwordResetSent) {
        if (authState.passwordResetSent) {
            authViewModel.clearPasswordResetSent()
            onNavigate("login")
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = { onNavigate("login") }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Reset Password", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(
            "Enter your email and we'll send you a link to reset your password",
            color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp)
        )

        if (authState.error != null) {
            Text(authState.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
        }

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (email.isNotBlank()) {
                    authViewModel.sendPasswordResetEmail(email)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !authState.isLoading
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send Reset Email", fontSize = 18.sp)
            }
        }
    }
}

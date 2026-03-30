package com.example.pgfinderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pgfinderapp.data.model.*
import com.example.pgfinderapp.presentation.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PGFinderApp()
                }
            }
        }
    }
}

@Composable
fun PGFinderApp() {
    // ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val pgViewModel: PGViewModel = viewModel()
    
    var currentScreen by remember { mutableStateOf("guest_home") }
    val authState = authViewModel.authState
    val pgState = pgViewModel.pgState
    
    // Use Firebase user instead of local state
    val currentUser = authState.currentUser
    val pgs = pgState.pgs
    
    var selectedPG by remember { mutableStateOf<PG?>(null) }
    var editingPG by remember { mutableStateOf<PG?>(null) }
    var showLoginDialog by remember { mutableStateOf(false) }
    
    // For adding review from card
    var reviewingPG by remember { mutableStateOf<PG?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }
    
    // For map picker
    var pendingLatitude by remember { mutableStateOf<Double?>(null) }
    var pendingLongitude by remember { mutableStateOf<Double?>(null) }
    var mapPickerReturnScreen by remember { mutableStateOf("add_pg") }
    var mapPickerInitialLat by remember { mutableStateOf<Double?>(null) }
    var mapPickerInitialLng by remember { mutableStateOf<Double?>(null) }

    val isLoggedIn = currentUser != null

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Login Required") },
            text = { Text("Please login to proceed.") },
            confirmButton = {
                Button(onClick = {
                    showLoginDialog = false
                    currentScreen = "login"
                }) {
                    Text("Go to Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReviewDialog && reviewingPG != null) {
        var rating by remember { mutableIntStateOf(5) }
        var comment by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Add Review for ${reviewingPG?.name}") },
            text = {
                Column {
                    Text("Rating:")
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < rating) Color(0xFFFFC107) else Color.LightGray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { rating = index + 1 }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your Comment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (comment.isNotBlank() && currentUser != null) {
                        val review = Review(
                            id = System.currentTimeMillis().toString(),
                            userId = currentUser.id,
                            userName = currentUser.name,
                            rating = rating,
                            comment = comment
                        )
                        pgViewModel.addReview(reviewingPG!!.id, review)
                        showReviewDialog = false
                        reviewingPG = null
                    }
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false; reviewingPG = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (currentScreen !in listOf("welcome", "login", "signup", "forgot_password", "add_pg", "edit_pg", "manage_pgs", "pg_details", "map_picker")) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentScreen.contains("home"),
                        onClick = { currentScreen = if (currentUser?.role == Role.OWNER) "owner_home" else "guest_home" }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add PG") },
                        label = { Text("Add PG") },
                        selected = currentScreen == "add_pg",
                        onClick = {
                            if (!isLoggedIn) {
                                showLoginDialog = true
                            } else {
                                pendingLatitude = null
                                pendingLongitude = null
                                currentScreen = "add_pg"
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentScreen == "profile",
                        onClick = {
                            if (!isLoggedIn) {
                                currentScreen = "login"
                            } else {
                                currentScreen = "profile"
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentScreen) {
                "welcome" -> WelcomeScreen(
                    onNavigate = { currentScreen = it }
                )
                "login" -> LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { user ->
                        currentScreen = if (user.role == Role.OWNER) "owner_home" else "guest_home"
                    },
                    onNavigate = { currentScreen = it }
                )
                "signup" -> SignupScreen(
                    authViewModel = authViewModel,
                    onSignupSuccess = { user ->
                        currentScreen = if (user.role == Role.OWNER) "owner_home" else "guest_home"
                    },
                    onNavigate = { currentScreen = it }
                )
                "forgot_password" -> ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigate = { currentScreen = it }
                )
                "guest_home" -> GuestHomeScreen(
                    pgs = pgs,
                    onSelectPG = { pg -> selectedPG = pg; currentScreen = "pg_details" }
                )
                "owner_home" -> OwnerHomeScreen(
                    pgs = pgs.filter { it.ownerId == currentUser?.id },
                    onAdd = { 
                        pendingLatitude = null
                        pendingLongitude = null
                        currentScreen = "add_pg" 
                    },
                    onSelectPG = { pg -> selectedPG = pg; currentScreen = "pg_details" }
                )
                "add_pg" -> AddPGScreenWithCoordinates(
                    selectedLatitude = pendingLatitude,
                    selectedLongitude = pendingLongitude,
                    onSave = { newPg ->
                        val pgToSave = newPg.copy(
                            id = System.currentTimeMillis().toString(),
                            ownerId = currentUser!!.id,
                            ownerName = currentUser.name,
                            ownerEmail = currentUser.email,
                            isVerified = currentUser.role == Role.OWNER
                        )
                        pgViewModel.addPG(pgToSave)
                        pendingLatitude = null
                        pendingLongitude = null
                        currentScreen = if (currentUser.role == Role.OWNER) "owner_home" else "guest_home"
                    },
                    onBack = { 
                        pendingLatitude = null
                        pendingLongitude = null
                        currentScreen = if (currentUser?.role == Role.OWNER) "owner_home" else "guest_home" 
                    },
                    onPickLocation = { lat, lng ->
                        mapPickerReturnScreen = "add_pg"
                        mapPickerInitialLat = lat
                        mapPickerInitialLng = lng
                        currentScreen = "map_picker"
                    }
                )
                "edit_pg" -> AddPGScreenWithCoordinates(
                    initialPG = editingPG,
                    selectedLatitude = pendingLatitude ?: editingPG?.latitude,
                    selectedLongitude = pendingLongitude ?: editingPG?.longitude,
                    onSave = { updatedPg ->
                        val pgToUpdate = updatedPg.copy(
                            id = editingPG!!.id,
                            ownerId = editingPG!!.ownerId,
                            isVerified = editingPG!!.isVerified
                        )
                        pgViewModel.updatePG(pgToUpdate)
                        pendingLatitude = null
                        pendingLongitude = null
                        currentScreen = "manage_pgs"
                    },
                    onBack = { 
                        pendingLatitude = null
                        pendingLongitude = null
                        currentScreen = "manage_pgs" 
                    },
                    onPickLocation = { lat, lng ->
                        mapPickerReturnScreen = "edit_pg"
                        mapPickerInitialLat = lat
                        mapPickerInitialLng = lng
                        currentScreen = "map_picker"
                    }
                )
                "map_picker" -> MapPickerScreen(
                    initialLatitude = mapPickerInitialLat,
                    initialLongitude = mapPickerInitialLng,
                    onLocationSelected = { lat, lng ->
                        pendingLatitude = lat
                        pendingLongitude = lng
                        currentScreen = mapPickerReturnScreen
                    },
                    onBack = {
                        currentScreen = mapPickerReturnScreen
                    }
                )
                "manage_pgs" -> ManagePGsScreen(
                    pgs = pgs,
                    currentUser = currentUser,
                    onEdit = { pg -> 
                        editingPG = pg
                        pendingLatitude = null
                        pendingLongitude = null
                        currentScreen = "edit_pg" 
                    },
                    onBack = { currentScreen = "profile" }
                )
                "pg_details" -> selectedPG?.let { pg ->
                    // Get the latest version of the PG from the list
                    val latestPG = pgs.find { it.id == pg.id } ?: pg
                    PGDetailScreen(
                        pg = latestPG,
                        currentUser = currentUser,
                        onBack = { currentScreen = if (currentUser?.role == Role.OWNER) "owner_home" else "guest_home" },
                        onAddReview = { review ->
                            pgViewModel.addReview(latestPG.id, review)
                        },
                        onDeleteReview = { reviewId ->
                            pgViewModel.deleteReview(latestPG.id, reviewId)
                        },
                        onLoginRequired = { showLoginDialog = true }
                    )
                }
                "profile" -> ProfileScreen(
                    user = currentUser,
                    onLogout = {
                        authViewModel.logout()
                        currentScreen = "welcome"
                    },
                    onNavigate = { currentScreen = it }
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PGFinderAppPreview() {
    MaterialTheme {
        PGFinderApp()
    }
}

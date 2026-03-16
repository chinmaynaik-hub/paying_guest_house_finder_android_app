package com.example.pgfinderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// ==========================================
// MODELS & DATA
// ==========================================

enum class Role { GUEST, OWNER }
enum class FoodType { VEG, NON_VEG, BOTH, NONE }
enum class ACType { AC, NON_AC, BOTH }

data class User(
    val id: String,
    val name: String,
    val age: Int,
    val role: Role,
    val email: String = "",
    val password: String = ""
)

data class Review(
    val id: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String
)

data class PG(
    val id: String,
    val ownerId: String,
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String,
    val alternatePhone: String?,
    val name: String,
    val address: String,
    val location: String,
    val capacity: Int,
    val availableBeds: Int,
    val costPerMonth: Int,
    val foodType: FoodType,
    val acType: ACType,
    val bedsInRoom: Int,
    val rating: Double,
    val images: List<String>,
    val reviews: List<Review>,
    val isVerified: Boolean
)

val initialPGs = listOf(
    PG(
        id = "1", ownerId = "owner1", ownerName = "Rahul Sharma", ownerPhone = "9876543210", ownerEmail = "rahul@example.com", alternatePhone = null, name = "Sunrise PG for Men",
        address = "123 Main St, Koramangala", location = "Koramangala, Bangalore",
        capacity = 50, availableBeds = 10, costPerMonth = 8000, foodType = FoodType.BOTH, acType = ACType.BOTH, bedsInRoom = 2,
        rating = 4.5, images = listOf(), reviews = listOf(
            Review("r1", "u1", "Rahul", 4, "Good food and clean rooms.")
        ), isVerified = true
    ),
    PG(
        id = "2", ownerId = "owner1", ownerName = "Rahul Sharma", ownerPhone = "9876543210", ownerEmail = "rahul@example.com", alternatePhone = null, name = "Comfort Stay Women PG",
        address = "456 Cross Rd, HSR Layout", location = "HSR Layout, Bangalore",
        capacity = 30, availableBeds = 5, costPerMonth = 10000, foodType = FoodType.VEG, acType = ACType.AC, bedsInRoom = 1,
        rating = 4.8, images = listOf(), reviews = listOf(), isVerified = true
    ),
    PG(
        id = "3", ownerId = "guest1", ownerName = "Amit Kumar", ownerPhone = "9123456780", ownerEmail = "amit@example.com", alternatePhone = "9988776655", name = "Budget PG",
        address = "789 2nd Main, BTM Layout", location = "BTM Layout, Bangalore",
        capacity = 100, availableBeds = 20, costPerMonth = 5000, foodType = FoodType.NONE, acType = ACType.NON_AC, bedsInRoom = 4,
        rating = 3.2, images = listOf(), reviews = listOf(), isVerified = false
    )
)

val initialUsers = listOf(
    User("guest1", "Guest User", 22, Role.GUEST, "guest@test.com", "password123"),
    User("owner1", "Owner User", 35, Role.OWNER, "owner@test.com", "password123")
)

// ==========================================
// MAIN ACTIVITY
// ==========================================

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

// ==========================================
// APP NAVIGATION & STATE
// ==========================================

@Composable
fun PGFinderApp() {
    var currentScreen by remember { mutableStateOf("guest_home") }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var users by remember { mutableStateOf(initialUsers) }
    var pgs by remember { mutableStateOf(initialPGs) }
    var selectedPG by remember { mutableStateOf<PG?>(null) }
    var editingPG by remember { mutableStateOf<PG?>(null) }
    var showLoginDialog by remember { mutableStateOf(false) }

    val isLoggedIn = currentUser != null

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Login Required") },
            text = { Text("Please login to add a new property.") },
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

    Scaffold(
        bottomBar = {
            if (currentScreen !in listOf("welcome", "login", "signup", "forgot_password", "add_pg", "edit_pg", "manage_pgs", "pg_details")) {
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
                    users = users,
                    onLogin = { user ->
                        currentUser = user
                        currentScreen = if (user.role == Role.OWNER) "owner_home" else "guest_home"
                    },
                    onNavigate = { currentScreen = it }
                )
                "signup" -> SignupScreen(
                    onSignup = { user ->
                        users = users + user
                        currentUser = user
                        currentScreen = if (user.role == Role.OWNER) "owner_home" else "guest_home"
                    },
                    onNavigate = { currentScreen = it }
                )
                "forgot_password" -> ForgotPasswordScreen(
                    users = users,
                    onUpdatePassword = { email, newPass ->
                        users = users.map { if (it.email == email) it.copy(password = newPass) else it }
                    },
                    onNavigate = { currentScreen = it }
                )
                "guest_home" -> GuestHomeScreen(
                    pgs = pgs,
                    onSelectPG = { pg -> selectedPG = pg; currentScreen = "pg_details" }
                )
                "owner_home" -> OwnerHomeScreen(
                    pgs = pgs.filter { it.ownerId == currentUser?.id },
                    onAdd = { currentScreen = "add_pg" },
                    onSelectPG = { pg -> selectedPG = pg; currentScreen = "pg_details" }
                )
                "add_pg" -> AddPGScreen(
                    onSave = { newPg ->
                        pgs = pgs + newPg.copy(
                            id = System.currentTimeMillis().toString(),
                            ownerId = currentUser!!.id,
                            isVerified = currentUser?.role == Role.OWNER
                        )
                        currentScreen = if (currentUser?.role == Role.OWNER) "owner_home" else "guest_home"
                    },
                    onBack = { currentScreen = if (currentUser?.role == Role.OWNER) "owner_home" else "guest_home" }
                )
                "edit_pg" -> AddPGScreen(
                    initialPG = editingPG,
                    onSave = { updatedPg ->
                        pgs = pgs.map { if (it.id == editingPG?.id) updatedPg.copy(id = it.id, ownerId = it.ownerId, isVerified = it.isVerified) else it }
                        currentScreen = "manage_pgs"
                    },
                    onBack = { currentScreen = "manage_pgs" }
                )
                "manage_pgs" -> ManagePGsScreen(
                    pgs = pgs,
                    currentUser = currentUser,
                    onEdit = { pg -> editingPG = pg; currentScreen = "edit_pg" },
                    onBack = { currentScreen = "profile" }
                )
                "pg_details" -> selectedPG?.let { pg ->
                    PGDetailScreen(
                        pg = pg,
                        currentUser = currentUser,
                        onBack = { currentScreen = if (currentUser?.role == Role.OWNER) "owner_home" else "guest_home" },
                        onAddReview = { review ->
                            val updatedPg = pg.copy(
                                reviews = pg.reviews + review,
                                rating = (pg.reviews.sumOf { it.rating } + review.rating).toDouble() / (pg.reviews.size + 1)
                            )
                            pgs = pgs.map { if (it.id == pg.id) updatedPg else it }
                            selectedPG = updatedPg
                        }
                    )
                }
                "profile" -> ProfileScreen(
                    user = currentUser,
                    onLogout = {
                        currentUser = null
                        currentScreen = "welcome"
                    },
                    onNavigate = { currentScreen = it }
                )
            }
        }
    }
}

// ==========================================
// SCREENS
// ==========================================

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
fun LoginScreen(users: List<User>, onLogin: (User) -> Unit, onNavigate: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

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

            if (error.isNotEmpty()) {
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
            }

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onNavigate("forgot_password") }) {
                    Text("Forgot Password?")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val user = users.find { it.email == email && it.password == password }
                    if (user != null) {
                        onLogin(user)
                    } else {
                        error = "Invalid email or password"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Login", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = { onNavigate("signup") }) {
                Text("Don't have an account? Create Account")
            }
        }
    }
}

@Composable
fun SignupScreen(onSignup: (User) -> Unit, onNavigate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.GUEST) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = { onNavigate("login") }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = age, onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("I am a...", fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { role = Role.GUEST },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (role == Role.GUEST) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) { Text("PG Finder") }
            OutlinedButton(
                onClick = { role = Role.OWNER },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (role == Role.OWNER) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) { Text("PG Owner") }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && age.isNotBlank()) {
                    onSignup(User(System.currentTimeMillis().toString(), name, age.toIntOrNull() ?: 18, role, email, password))
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Up", fontSize = 18.sp)
        }
    }
}

@Composable
fun ForgotPasswordScreen(users: List<User>, onUpdatePassword: (String, String) -> Unit, onNavigate: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = { onNavigate("login") }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Reset Password", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(
            if (step == 1) "Enter your email to verify your account" else "Enter your new password",
            color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp)
        )

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
        }

        if (step == 1) {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (users.any { it.email == email }) {
                        step = 2
                        error = ""
                    } else {
                        error = "Email not found in our records"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Verify Email", fontSize = 18.sp)
            }
        } else {
            OutlinedTextField(
                value = newPassword, onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (newPassword.isNotBlank()) {
                        onUpdatePassword(email, newPassword)
                        onNavigate("login")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Password", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun GuestHomeScreen(pgs: List<PG>, onSelectPG: (PG) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Find PG", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location, name...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Using device location", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        val filtered = pgs.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
        }.sortedByDescending { it.rating }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filtered) { pg -> PGCard(pg, onClick = { onSelectPG(pg) }) }
        }
    }
}

@Composable
fun OwnerHomeScreen(pgs: List<PG>, onAdd: () -> Unit, onSelectPG: (PG) -> Unit) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add PG")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Your Properties", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            if (pgs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No properties yet. Add one!")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(pgs) { pg -> PGCard(pg, onClick = { onSelectPG(pg) }) }
                }
            }
        }
    }
}

@Composable
fun PGCard(pg: PG, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray)) {
                // Image placeholder
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(48.dp), tint = Color.Gray)
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Text(if (pg.rating > 0) String.format(Locale.US, "%.1f", pg.rating) else "New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pg.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (pg.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                    }
                }
                Text(pg.location, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${pg.bedsInRoom} Sharing • ${pg.foodType}", fontSize = 14.sp, color = Color.DarkGray)
                    Text("₹${pg.costPerMonth}/mo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ManagePGsScreen(pgs: List<PG>, currentUser: User?, onEdit: (PG) -> Unit, onBack: () -> Unit) {
    val myPgs = pgs.filter { it.ownerId == currentUser?.id }

    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text("Manage Your PGs") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
        )
        if (myPgs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No properties yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(myPgs) { pg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pg.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(pg.location, color = Color.Gray, fontSize = 14.sp)
                            }
                            Button(onClick = { onEdit(pg) }) {
                                Text("Edit")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPGScreen(initialPG: PG? = null, onSave: (PG) -> Unit, onBack: () -> Unit) {
    var ownerName by remember { mutableStateOf(initialPG?.ownerName ?: "") }
    var ownerPhone by remember { mutableStateOf(initialPG?.ownerPhone ?: "") }
    var ownerEmail by remember { mutableStateOf(initialPG?.ownerEmail ?: "") }
    var alternatePhone by remember { mutableStateOf(initialPG?.alternatePhone ?: "") }
    var name by remember { mutableStateOf(initialPG?.name ?: "") }
    var location by remember { mutableStateOf(initialPG?.location ?: "") }
    var address by remember { mutableStateOf(initialPG?.address ?: "") }
    var capacity by remember { mutableStateOf(initialPG?.capacity?.toString() ?: "") }
    var availableBeds by remember { mutableStateOf(initialPG?.availableBeds?.toString() ?: "") }
    var cost by remember { mutableStateOf(initialPG?.costPerMonth?.toString() ?: "") }
    var acType by remember { mutableStateOf(initialPG?.acType?.name ?: "Both") }
    var beds by remember { mutableStateOf(initialPG?.bedsInRoom?.toString() ?: "") }
    var images by remember { mutableStateOf(if (initialPG != null && initialPG.images.isNotEmpty()) initialPG.images else listOf("https://picsum.photos/seed/newpg/600/400")) }

    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text(if (initialPG != null) "Edit PG" else "Add New PG") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
        )
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Contact Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name *") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("Phone No. *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = ownerEmail, onValueChange = { ownerEmail = it }, label = { Text("Email ID *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = alternatePhone, onValueChange = { alternatePhone = it }, label = { Text("Alternate Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text("Property Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("PG Name *") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location *") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Full Address *") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Total Capacity *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = availableBeds, onValueChange = { availableBeds = it }, label = { Text("Available Beds *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost per Month *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = acType, onValueChange = { acType = it }, label = { Text("AC / Non-AC / Both") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = beds, onValueChange = { beds = it }, label = { Text("Beds per Room *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Image URLs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = { images = images + "" }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Image")
                }
            }
            images.forEachIndexed { index, url ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { newUrl ->
                            val newImages = images.toMutableList()
                            newImages[index] = newUrl
                            images = newImages
                        },
                        label = { Text("Image URL ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    if (images.size > 1) {
                        IconButton(onClick = {
                            val newImages = images.toMutableList()
                            newImages.removeAt(index)
                            images = newImages
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Image", tint = Color.Red)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (ownerName.isNotBlank() && ownerPhone.isNotBlank() && ownerEmail.isNotBlank() && name.isNotBlank() && location.isNotBlank() && address.isNotBlank() && capacity.isNotBlank() && availableBeds.isNotBlank() && cost.isNotBlank() && beds.isNotBlank()) {
                        onSave(PG(
                            id = "", ownerId = "", ownerName = ownerName, ownerPhone = ownerPhone, ownerEmail = ownerEmail, alternatePhone = alternatePhone.takeIf { it.isNotBlank() },
                            name = name, address = address, location = location,
                            capacity = capacity.toIntOrNull() ?: 10, availableBeds = availableBeds.toIntOrNull() ?: 0, costPerMonth = cost.toIntOrNull() ?: 0, foodType = FoodType.BOTH,
                            acType = when(acType.uppercase()) { "AC" -> ACType.AC; "NON-AC", "NON AC" -> ACType.NON_AC; else -> ACType.BOTH },
                            bedsInRoom = beds.toIntOrNull() ?: 1, rating = 0.0, images = images.filter { it.isNotBlank() }, reviews = listOf(), isVerified = false
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Save Property") }
        }
    }
}

@Composable
fun PGDetailScreen(pg: PG, currentUser: User?, onBack: () -> Unit, onAddReview: (Review) -> Unit) {
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }
    var showMoreImages by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text("PG Details") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(pg.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        if (pg.isVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF2196F3))
                        }
                    }
                    Text(pg.location, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("₹${pg.costPerMonth} / month", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${pg.bedsInRoom} Sharing • ${pg.foodType} Food • ${pg.acType.name.replace("_", "-")}", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${pg.availableBeds} / ${pg.capacity} Beds Available", fontSize = 16.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Address", fontWeight = FontWeight.Bold)
                    Text(pg.address, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Contact Owner", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Name: ${pg.ownerName}")
                            Text("Phone: ${pg.ownerPhone}")
                            if (!pg.alternatePhone.isNullOrBlank()) {
                                Text("Alt Phone: ${pg.alternatePhone}")
                            }
                            Text("Email: ${pg.ownerEmail}")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Reviews", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    if (currentUser?.role == Role.GUEST) {
                        OutlinedTextField(
                            value = comment, onValueChange = { comment = it },
                            label = { Text("Write a review...") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Button(onClick = {
                            if (comment.isNotBlank()) {
                                onAddReview(Review(System.currentTimeMillis().toString(), currentUser.id, currentUser.name, rating, comment))
                                comment = ""
                            }
                        }) { Text("Post Review") }
                    }
                }
            }
            items(pg.reviews) { review ->
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(review.userName, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Text(review.rating.toString(), fontSize = 12.sp)
                    }
                    Text(review.comment, color = Color.DarkGray)
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                }
            }

            if (pg.images.size > 1) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    TextButton(
                        onClick = { showMoreImages = !showMoreImages },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null) // Changed from Icons.Default.Image
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showMoreImages) "Hide images" else "Show more images")
                    }
                }

                if (showMoreImages) {
                    val additionalImages = pg.images.drop(1)
                    items(additionalImages.chunked(2)) { rowImages ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (img in rowImages) {
                                Box(
                                    modifier = Modifier.weight(1f).height(120.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Face, contentDescription = null, tint = Color.Gray) // Changed from Icons.Default.Image
                                }
                            }
                            if (rowImages.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(user: User?, onLogout: () -> Unit, onNavigate: (String) -> Unit) {
    if (user == null) return
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Text(user.name.take(1), fontSize = 40.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(user.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (user.role == Role.OWNER) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = "Verified Owner", tint = Color(0xFF2196F3))
            }
        }
        Text("${user.role.name} • ${user.age} years old", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        if (user.role == Role.OWNER) {
            Button(
                onClick = { onNavigate("manage_pgs") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Text("Manage Your PGs", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Log Out", fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBar(title: @Composable () -> Unit, navigationIcon: @Composable () -> Unit) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon
    )
}

@Preview(showBackground = true)
@Composable
fun PGFinderAppPreview() {
    MaterialTheme {
        PGFinderApp()
    }
}

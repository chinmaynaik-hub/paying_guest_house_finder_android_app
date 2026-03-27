package com.example.pgfinderapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgfinderapp.data.model.Role
import com.example.pgfinderapp.data.model.User

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

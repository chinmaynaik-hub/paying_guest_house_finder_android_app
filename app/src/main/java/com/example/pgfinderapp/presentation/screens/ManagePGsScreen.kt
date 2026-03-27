package com.example.pgfinderapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgfinderapp.data.model.PG
import com.example.pgfinderapp.data.model.User
import com.example.pgfinderapp.presentation.components.SmallTopAppBar

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

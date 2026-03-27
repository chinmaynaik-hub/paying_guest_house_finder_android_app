package com.example.pgfinderapp.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgfinderapp.data.model.PG
import com.example.pgfinderapp.data.model.Review
import com.example.pgfinderapp.data.model.Role
import com.example.pgfinderapp.data.model.User
import com.example.pgfinderapp.presentation.components.SmallTopAppBar

@Composable
fun PGDetailScreen(pg: PG, currentUser: User?, onBack: () -> Unit, onAddReview: (Review) -> Unit) {
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }
    var showAddReview by remember { mutableStateOf(false) }
    var showMoreImages by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    Text("${pg.bedsInRoom} Sharing • ${pg.foodType.displayName} Food • ${pg.acType.displayName}", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${pg.availableBeds} / ${pg.capacity} Beds Available", fontSize = 16.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Address", fontWeight = FontWeight.Bold)
                    Text(pg.address, color = Color.DarkGray)

                    pg.mapLink?.let { link ->
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View on Maps")
                        }
                    }

                    if (!pg.rules.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("PG Rules", fontWeight = FontWeight.Bold)
                        Text(pg.rules, color = Color.DarkGray)
                    }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reviews", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (currentUser?.role == Role.GUEST) {
                            IconButton(onClick = { showAddReview = !showAddReview }) {
                                Icon(
                                    imageVector = if (showAddReview) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = "Add Review"
                                )
                            }
                        }
                    }

                    if (showAddReview && currentUser?.role == Role.GUEST) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text("Your Rating:", fontWeight = FontWeight.Medium)
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
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
                            OutlinedTextField(
                                value = comment, onValueChange = { comment = it },
                                label = { Text("Write a review...") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                            Button(onClick = {
                                if (comment.isNotBlank()) {
                                    onAddReview(Review(System.currentTimeMillis().toString(), currentUser.id, currentUser.name, rating, comment))
                                    comment = ""
                                    showAddReview = false
                                }
                            }) { Text("Post Review") }
                        }
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

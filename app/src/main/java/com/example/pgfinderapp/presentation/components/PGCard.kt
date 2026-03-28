package com.example.pgfinderapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgfinderapp.data.model.DistanceUtils
import com.example.pgfinderapp.data.model.PG
import java.util.Locale

@Composable
fun PGCard(
    pg: PG, 
    onClick: () -> Unit,
    onAddReviewClick: () -> Unit = {},
    userLatitude: Double? = null,
    userLongitude: Double? = null
) {
    // Calculate distance if user location and PG coordinates are available
    val distanceText = if (userLatitude != null && userLongitude != null && 
                          pg.latitude != null && pg.longitude != null) {
        val distance = DistanceUtils.calculateDistance(
            userLatitude, userLongitude, pg.latitude, pg.longitude
        )
        DistanceUtils.formatDistance(distance)
    } else null
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                // Distance badge
                if (distanceText != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn, 
                                null, 
                                tint = MaterialTheme.colorScheme.primary, 
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                distanceText, 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
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
                    Text("${pg.bedsInRoom} Sharing • ${pg.foodType.displayName} • ${pg.acType.displayName}", fontSize = 14.sp, color = Color.DarkGray)
                    Text("₹${pg.costPerMonth}/mo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reviews", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    // The + button for reviews
                    FilledIconButton(
                        onClick = { onAddReviewClick() },
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "Add Review", 
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                if (pg.reviews.isNotEmpty()) {
                    pg.reviews.takeLast(1).forEach { review ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                            Text(text = review.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 2.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = review.comment,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Text("No reviews yet", fontSize = 12.sp, color = Color.LightGray, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

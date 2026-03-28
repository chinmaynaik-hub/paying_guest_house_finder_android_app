package com.example.pgfinderapp.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgfinderapp.data.model.DistanceUtils
import com.example.pgfinderapp.data.model.LocationHelper
import com.example.pgfinderapp.data.model.PG
import com.example.pgfinderapp.presentation.components.PGCard
import kotlinx.coroutines.launch

@Composable
fun GuestHomeScreen(
    pgs: List<PG>, 
    onSelectPG: (PG) -> Unit,
    onAddReviewClick: (PG) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }
    
    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }
    var locationStatus by remember { mutableStateOf("Fetching location...") }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                     permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch {
                val location = locationHelper.getCurrentLocation()
                    ?: locationHelper.getLastKnownLocation()
                location?.let {
                    userLatitude = it.latitude
                    userLongitude = it.longitude
                    locationStatus = "Location found"
                } ?: run {
                    locationStatus = "Could not get location"
                }
            }
        } else {
            locationStatus = "Location permission denied"
        }
    }
    
    LaunchedEffect(Unit) {
        if (locationHelper.hasLocationPermission()) {
            val location = locationHelper.getCurrentLocation()
                ?: locationHelper.getLastKnownLocation()
            location?.let {
                userLatitude = it.latitude
                userLongitude = it.longitude
                locationStatus = "Location found"
            } ?: run {
                locationStatus = "Could not get location"
            }
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Find PG", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location, name, address...") },
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
                    Text(
                        if (userLatitude != null) "Using device location" else locationStatus, 
                        color = Color.White, 
                        fontSize = 12.sp
                    )
                }
            }
        }

        val filtered = pgs.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
        }.let { list ->
            // Sort by distance if user location is available
            if (userLatitude != null && userLongitude != null) {
                list.sortedBy { pg ->
                    if (pg.latitude != null && pg.longitude != null) {
                        DistanceUtils.calculateDistance(userLatitude!!, userLongitude!!, pg.latitude, pg.longitude)
                    } else {
                        Double.MAX_VALUE
                    }
                }
            } else {
                list.sortedByDescending { it.rating }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filtered) { pg -> 
                PGCard(
                    pg = pg, 
                    onClick = { onSelectPG(pg) },
                    onAddReviewClick = { onAddReviewClick(pg) },
                    userLatitude = userLatitude,
                    userLongitude = userLongitude
                ) 
            }
        }
    }
}

@Composable
fun OwnerHomeScreen(
    pgs: List<PG>, 
    onAdd: () -> Unit, 
    onSelectPG: (PG) -> Unit
) {
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
                    items(pgs) { pg -> 
                        PGCard(
                            pg = pg, 
                            onClick = { onSelectPG(pg) }
                        ) 
                    }
                }
            }
        }
    }
}

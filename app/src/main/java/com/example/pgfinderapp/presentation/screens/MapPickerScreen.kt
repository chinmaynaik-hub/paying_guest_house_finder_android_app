package com.example.pgfinderapp.presentation.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgfinderapp.data.model.LocationHelper
import com.example.pgfinderapp.presentation.components.SmallTopAppBar
import kotlinx.coroutines.launch

@Composable
fun MapPickerScreen(
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }
    
    // Default to Bangalore center if no initial location
    var selectedLatitude by remember { mutableStateOf(initialLatitude ?: 12.9716) }
    var selectedLongitude by remember { mutableStateOf(initialLongitude ?: 77.5946) }
    var latitudeText by remember { mutableStateOf((initialLatitude ?: 12.9716).toString()) }
    var longitudeText by remember { mutableStateOf((initialLongitude ?: 77.5946).toString()) }
    var locationFetching by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                     permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationFetching = true
            scope.launch {
                val location = locationHelper.getCurrentLocation()
                    ?: locationHelper.getLastKnownLocation()
                location?.let {
                    selectedLatitude = it.latitude
                    selectedLongitude = it.longitude
                    latitudeText = it.latitude.toString()
                    longitudeText = it.longitude.toString()
                }
                locationFetching = false
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text("Pick Location") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = {
                    onLocationSelected(selectedLatitude, selectedLongitude)
                }) {
                    Icon(Icons.Default.Check, "Confirm")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Enter Location Coordinates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Enter latitude and longitude manually, or use your current location.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            OutlinedTextField(
                value = latitudeText,
                onValueChange = { 
                    latitudeText = it
                    it.toDoubleOrNull()?.let { lat -> selectedLatitude = lat }
                },
                label = { Text("Latitude") },
                placeholder = { Text("e.g., 12.9716") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = longitudeText,
                onValueChange = { 
                    longitudeText = it
                    it.toDoubleOrNull()?.let { lng -> selectedLongitude = lng }
                },
                label = { Text("Longitude") },
                placeholder = { Text("e.g., 77.5946") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Get current location button
            OutlinedButton(
                onClick = {
                    if (locationHelper.hasLocationPermission()) {
                        locationFetching = true
                        scope.launch {
                            val location = locationHelper.getCurrentLocation()
                                ?: locationHelper.getLastKnownLocation()
                            location?.let {
                                selectedLatitude = it.latitude
                                selectedLongitude = it.longitude
                                latitudeText = "%.6f".format(it.latitude)
                                longitudeText = "%.6f".format(it.longitude)
                            }
                            locationFetching = false
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !locationFetching
            ) {
                if (locationFetching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Getting location...")
                } else {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use My Current Location")
                }
            }
            
            // View on map button
            OutlinedButton(
                onClick = {
                    val uri = Uri.parse("geo:$selectedLatitude,$selectedLongitude?q=$selectedLatitude,$selectedLongitude")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No maps app
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Preview on Maps")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Selected location display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selected Location:", fontWeight = FontWeight.Medium)
                    Text(
                        "Lat: %.6f".format(selectedLatitude),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Lng: %.6f".format(selectedLongitude),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Confirm button
            Button(
                onClick = { onLocationSelected(selectedLatitude, selectedLongitude) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Location")
            }
        }
    }
}

package com.example.pgfinderapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgfinderapp.data.model.ACType
import com.example.pgfinderapp.data.model.FoodType
import com.example.pgfinderapp.data.model.PG
import com.example.pgfinderapp.presentation.components.SmallTopAppBar

@Composable
fun AddPGScreen(
    initialPG: PG? = null, 
    onSave: (PG) -> Unit, 
    onBack: () -> Unit,
    onPickLocation: (currentLat: Double?, currentLng: Double?) -> Unit = { _, _ -> }
) {
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
    var acType by remember { mutableStateOf(initialPG?.acType?.name ?: "BOTH") }
    var foodType by remember { mutableStateOf(initialPG?.foodType?.name ?: "BOTH") }
    var beds by remember { mutableStateOf(initialPG?.bedsInRoom?.toString() ?: "") }
    var images by remember { mutableStateOf(if (initialPG != null && initialPG.images.isNotEmpty()) initialPG.images else listOf("https://picsum.photos/seed/newpg/600/400")) }
    var mapLink by remember { mutableStateOf(initialPG?.mapLink ?: "") }
    var rules by remember { mutableStateOf(initialPG?.rules ?: "") }
    var latitude by remember { mutableStateOf(initialPG?.latitude) }
    var longitude by remember { mutableStateOf(initialPG?.longitude) }

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
            
            Text("Food Available", fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FoodType.entries.forEach { type ->
                    OutlinedButton(
                        onClick = { foodType = type.name },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (foodType == type.name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(type.displayName, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("AC Type", fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ACType.entries.forEach { type ->
                    OutlinedButton(
                        onClick = { acType = type.name },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (acType == type.name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(type.displayName, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = beds, onValueChange = { beds = it }, label = { Text("Beds per Room *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Location on Map", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            
            OutlinedButton(
                onClick = { onPickLocation(latitude, longitude) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (latitude != null && longitude != null) 
                        "Location Selected ✓" 
                    else 
                        "Pick Location on Map"
                )
            }
            
            if (latitude != null && longitude != null) {
                Text(
                    "Lat: %.4f, Lng: %.4f".format(latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = mapLink, onValueChange = { mapLink = it }, label = { Text("Google Map Link (optional)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = rules,
                onValueChange = { rules = it },
                label = { Text("PG Rules") },
                placeholder = { Text("Enter rules (e.g. No smoking, Curfew time...)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

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
                            latitude = latitude, longitude = longitude,
                            capacity = capacity.toIntOrNull() ?: 10, availableBeds = availableBeds.toIntOrNull() ?: 0, costPerMonth = cost.toIntOrNull() ?: 0, foodType = FoodType.valueOf(foodType),
                            acType = ACType.valueOf(acType),
                            bedsInRoom = beds.toIntOrNull() ?: 1, rating = 0.0, images = images.filter { it.isNotBlank() }, reviews = listOf(), isVerified = false,
                            mapLink = mapLink.takeIf { it.isNotBlank() },
                            rules = rules.takeIf { it.isNotBlank() }
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Save Property") }
        }
    }
}

// Variant that receives coordinates back from map picker
@Composable
fun AddPGScreenWithCoordinates(
    initialPG: PG? = null,
    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null,
    onSave: (PG) -> Unit,
    onBack: () -> Unit,
    onPickLocation: (currentLat: Double?, currentLng: Double?) -> Unit
) {
    var latitude by remember(selectedLatitude) { mutableStateOf(selectedLatitude ?: initialPG?.latitude) }
    var longitude by remember(selectedLongitude) { mutableStateOf(selectedLongitude ?: initialPG?.longitude) }
    
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
    var acType by remember { mutableStateOf(initialPG?.acType?.name ?: "BOTH") }
    var foodType by remember { mutableStateOf(initialPG?.foodType?.name ?: "BOTH") }
    var beds by remember { mutableStateOf(initialPG?.bedsInRoom?.toString() ?: "") }
    var images by remember { mutableStateOf(if (initialPG != null && initialPG.images.isNotEmpty()) initialPG.images else listOf("https://picsum.photos/seed/newpg/600/400")) }
    var mapLink by remember { mutableStateOf(initialPG?.mapLink ?: "") }
    var rules by remember { mutableStateOf(initialPG?.rules ?: "") }

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
            
            Text("Food Available", fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FoodType.entries.forEach { type ->
                    OutlinedButton(
                        onClick = { foodType = type.name },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (foodType == type.name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(type.displayName, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("AC Type", fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ACType.entries.forEach { type ->
                    OutlinedButton(
                        onClick = { acType = type.name },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (acType == type.name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(type.displayName, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = beds, onValueChange = { beds = it }, label = { Text("Beds per Room *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Location on Map", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            
            OutlinedButton(
                onClick = { onPickLocation(latitude, longitude) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (latitude != null && longitude != null) 
                        "Location Selected ✓" 
                    else 
                        "Pick Location on Map"
                )
            }
            
            if (latitude != null && longitude != null) {
                Text(
                    "Lat: %.4f, Lng: %.4f".format(latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = mapLink, onValueChange = { mapLink = it }, label = { Text("Google Map Link (optional)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = rules,
                onValueChange = { rules = it },
                label = { Text("PG Rules") },
                placeholder = { Text("Enter rules (e.g. No smoking, Curfew time...)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

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
                            latitude = latitude, longitude = longitude,
                            capacity = capacity.toIntOrNull() ?: 10, availableBeds = availableBeds.toIntOrNull() ?: 0, costPerMonth = cost.toIntOrNull() ?: 0, foodType = FoodType.valueOf(foodType),
                            acType = ACType.valueOf(acType),
                            bedsInRoom = beds.toIntOrNull() ?: 1, rating = 0.0, images = images.filter { it.isNotBlank() }, reviews = listOf(), isVerified = false,
                            mapLink = mapLink.takeIf { it.isNotBlank() },
                            rules = rules.takeIf { it.isNotBlank() }
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Save Property") }
        }
    }
}

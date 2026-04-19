package com.example.pgfinderapp.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pgfinderapp.data.model.ACType
import com.example.pgfinderapp.data.model.FoodType
import com.example.pgfinderapp.data.model.ImageUtils
import com.example.pgfinderapp.data.model.PG
import com.example.pgfinderapp.data.model.initialPGs
import com.example.pgfinderapp.presentation.components.SmallTopAppBar
import com.example.pgfinderapp.ui.theme.PgFinderAppTheme
import kotlinx.coroutines.launch
import java.util.UUID

// Main Add/Edit PG Screen with coordinate support
@Composable
fun AddPGScreenWithCoordinates(
    initialPG: PG? = null,
    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null,
    onSave: (PG) -> Unit,
    onBack: () -> Unit,
    onPickLocation: (currentLat: Double?, currentLng: Double?, formData: PG) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
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
    var foodType by remember {
        mutableStateOf(
            initialPG?.foodType
                ?.takeIf { it != FoodType.NONE }
                ?.name
                ?: FoodType.BOTH.name
        )
    }
    var beds by remember { mutableStateOf(initialPG?.bedsInRoom?.toString() ?: "") }
    
    // Image handling - store both URIs (local) and URLs (uploaded)
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedImageUrls by remember { mutableStateOf(if (initialPG != null && initialPG.images.isNotEmpty()) initialPG.images else emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf("") }
    
    var mapLink by remember { mutableStateOf(initialPG?.mapLink ?: "") }
    var rules by remember { mutableStateOf(initialPG?.rules ?: "") }
    var latitude by remember(selectedLatitude) { mutableStateOf(selectedLatitude ?: initialPG?.latitude) }
    var longitude by remember(selectedLongitude) { mutableStateOf(selectedLongitude ?: initialPG?.longitude) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = selectedImageUris + uris
    }
    
    // Helper function to create PG from current form data
    fun createPGFromForm(images: List<String>) = PG(
        id = "",
        ownerId = "",
        ownerName = ownerName,
        ownerPhone = ownerPhone,
        ownerEmail = ownerEmail,
        alternatePhone = alternatePhone.takeIf { it.isNotBlank() },
        name = name,
        address = address,
        location = location,
        latitude = latitude,
        longitude = longitude,
        capacity = capacity.toIntOrNull() ?: 10,
        availableBeds = availableBeds.toIntOrNull() ?: 0,
        costPerMonth = cost.toIntOrNull() ?: 0,
        foodType = FoodType.valueOf(foodType),
        acType = ACType.valueOf(acType),
        bedsInRoom = beds.toIntOrNull() ?: 1,
        rating = 0.0,
        images = images,
        reviews = listOf(),
        isVerified = false,
        mapLink = mapLink.takeIf { it.isNotBlank() },
        rules = rules.takeIf { it.isNotBlank() }
    )

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
                FoodType.entries.filter { it != FoodType.NONE }.forEach { type ->
                    OutlinedButton(
                        onClick = { foodType = type.name },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (foodType == type.name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(
                            text = type.displayName,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("AC Type", fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ACType.entries.forEach { type ->
                    OutlinedButton(
                        onClick = { acType = type.name },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (acType == type.name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(
                            text = type.displayName,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = beds, onValueChange = { beds = it }, label = { Text("Beds per Room *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Location on Map", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            
            OutlinedButton(
                onClick = { onPickLocation(latitude, longitude, createPGFromForm(uploadedImageUrls)) },
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
            Text("Images", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Image picker section
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Show already uploaded images (URLs)
                items(uploadedImageUrls) { url ->
                    ImageThumbnail(
                        model = url,
                        contentDescription = "PG Image",
                        onRemove = { uploadedImageUrls = uploadedImageUrls - url }
                    )
                }
                
                // Show selected local images (URIs)
                items(selectedImageUris) { uri ->
                    ImageThumbnail(
                        model = uri,
                        contentDescription = "Selected Image",
                        onRemove = { selectedImageUris = selectedImageUris - uri }
                    )
                }
                
                // Add image button
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Image",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text("Add", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                }
            }
            
            if (isUploading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(uploadProgress, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (ownerName.isNotBlank() && ownerPhone.isNotBlank() && ownerEmail.isNotBlank() && name.isNotBlank() && location.isNotBlank() && address.isNotBlank() && capacity.isNotBlank() && availableBeds.isNotBlank() && cost.isNotBlank() && beds.isNotBlank()) {
                        if (selectedImageUris.isNotEmpty()) {
                            isUploading = true
                            scope.launch {
                                val tempPgId = UUID.randomUUID().toString()
                                uploadProgress = "Compressing and uploading images..."
                                val newUrls = ImageUtils.uploadMultipleImages(context, selectedImageUris, tempPgId)
                                val allImages = uploadedImageUrls + newUrls
                                isUploading = false
                                onSave(createPGFromForm(allImages))
                            }
                        } else {
                            onSave(createPGFromForm(uploadedImageUrls))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isUploading
            ) { Text(if (isUploading) "Uploading..." else "Save Property") }
        }
    }
}

@Composable
private fun ImageThumbnail(
    model: Any,
    contentDescription: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.background(Color.Red.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPGScreenPreview() {
    PgFinderAppTheme {
        AddPGScreenWithCoordinates(
            onSave = {},
            onBack = {},
            onPickLocation = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditPGScreenPreview() {
    PgFinderAppTheme {
        AddPGScreenWithCoordinates(
            initialPG = initialPGs[0],
            onSave = {},
            onBack = {},
            onPickLocation = { _, _, _ -> }
        )
    }
}

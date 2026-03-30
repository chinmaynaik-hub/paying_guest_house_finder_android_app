package com.example.pgfinderapp.data.model

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class PGResult<out T> {
    data class Success<T>(val data: T) : PGResult<T>()
    data class Error(val message: String) : PGResult<Nothing>()
    object Loading : PGResult<Nothing>()
}

class PGRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val pgsCollection = firestore.collection("pgs")
    
    companion object {
        private const val TAG = "PGRepository"
    }
    
    // Get all PGs as a Flow (real-time updates)
    fun getAllPGs(): Flow<List<PG>> = callbackFlow {
        Log.d(TAG, "Setting up PGs listener")
        val listener = pgsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to PGs: ${error.message}", error)
                close(error)
                return@addSnapshotListener
            }
            
            Log.d(TAG, "Received snapshot with ${snapshot?.documents?.size ?: 0} documents")
            val pgs = snapshot?.documents?.mapNotNull { doc ->
                documentToPG(doc)
            } ?: emptyList()
            
            Log.d(TAG, "Parsed ${pgs.size} PGs")
            trySend(pgs)
        }
        
        awaitClose { 
            Log.d(TAG, "Removing PGs listener")
            listener.remove() 
        }
    }
    
    // Get PGs by owner ID
    fun getPGsByOwner(ownerId: String): Flow<List<PG>> = callbackFlow {
        val listener = pgsCollection
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val pgs = snapshot?.documents?.mapNotNull { doc ->
                    documentToPG(doc)
                } ?: emptyList()
                
                trySend(pgs)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Get single PG by ID
    suspend fun getPGById(pgId: String): PG? {
        return try {
            val doc = pgsCollection.document(pgId).get().await()
            if (doc.exists()) documentToPG(doc) else null
        } catch (e: Exception) {
            null
        }
    }
    
    // Add new PG
    suspend fun addPG(pg: PG): PGResult<String> {
        return try {
            val pgData = pgToMap(pg)
            val docRef = pgsCollection.add(pgData).await()
            PGResult.Success(docRef.id)
        } catch (e: Exception) {
            PGResult.Error(e.message ?: "Failed to add PG")
        }
    }
    
    // Update existing PG
    suspend fun updatePG(pg: PG): PGResult<Unit> {
        return try {
            val pgData = pgToMap(pg)
            pgsCollection.document(pg.id).set(pgData).await()
            PGResult.Success(Unit)
        } catch (e: Exception) {
            PGResult.Error(e.message ?: "Failed to update PG")
        }
    }
    
    // Delete PG
    suspend fun deletePG(pgId: String): PGResult<Unit> {
        return try {
            pgsCollection.document(pgId).delete().await()
            PGResult.Success(Unit)
        } catch (e: Exception) {
            PGResult.Error(e.message ?: "Failed to delete PG")
        }
    }
    
    // Add review to PG
    suspend fun addReview(pgId: String, review: Review): PGResult<Unit> {
        return try {
            val pg = getPGById(pgId) ?: return PGResult.Error("PG not found")
            
            // Check if user already has a review
            if (pg.reviews.any { it.userId == review.userId }) {
                return PGResult.Error("You have already reviewed this PG")
            }
            
            val updatedReviews = pg.reviews + review
            val newRating = updatedReviews.map { it.rating }.average()
            
            pgsCollection.document(pgId).update(
                mapOf(
                    "reviews" to updatedReviews.map { reviewToMap(it) },
                    "rating" to newRating
                )
            ).await()
            
            PGResult.Success(Unit)
        } catch (e: Exception) {
            PGResult.Error(e.message ?: "Failed to add review")
        }
    }
    
    // Delete review from PG
    suspend fun deleteReview(pgId: String, reviewId: String): PGResult<Unit> {
        return try {
            val pg = getPGById(pgId) ?: return PGResult.Error("PG not found")
            val updatedReviews = pg.reviews.filter { it.id != reviewId }
            val newRating = if (updatedReviews.isNotEmpty()) {
                updatedReviews.map { it.rating }.average()
            } else {
                0.0
            }
            
            pgsCollection.document(pgId).update(
                mapOf(
                    "reviews" to updatedReviews.map { reviewToMap(it) },
                    "rating" to newRating
                )
            ).await()
            
            PGResult.Success(Unit)
        } catch (e: Exception) {
            PGResult.Error(e.message ?: "Failed to delete review")
        }
    }
    
    // Upload initial sample data to Firestore
    suspend fun uploadSampleData(): PGResult<Unit> {
        return try {
            initialPGs.forEach { pg ->
                val pgData = pgToMap(pg)
                pgsCollection.document(pg.id).set(pgData).await()
            }
            PGResult.Success(Unit)
        } catch (e: Exception) {
            PGResult.Error(e.message ?: "Failed to upload sample data")
        }
    }
    
    private fun documentToPG(doc: com.google.firebase.firestore.DocumentSnapshot): PG? {
        return try {
            @Suppress("UNCHECKED_CAST")
            val reviewsList = (doc.get("reviews") as? List<Map<String, Any>>)?.map { reviewMap ->
                Review(
                    id = reviewMap["id"] as? String ?: "",
                    userId = reviewMap["userId"] as? String ?: "",
                    userName = reviewMap["userName"] as? String ?: "",
                    rating = (reviewMap["rating"] as? Long)?.toInt() ?: 0,
                    comment = reviewMap["comment"] as? String ?: ""
                )
            } ?: emptyList()
            
            @Suppress("UNCHECKED_CAST")
            PG(
                id = doc.id,
                ownerId = doc.getString("ownerId") ?: "",
                ownerName = doc.getString("ownerName") ?: "",
                ownerPhone = doc.getString("ownerPhone") ?: "",
                ownerEmail = doc.getString("ownerEmail") ?: "",
                alternatePhone = doc.getString("alternatePhone"),
                name = doc.getString("name") ?: "",
                address = doc.getString("address") ?: "",
                location = doc.getString("location") ?: "",
                latitude = doc.getDouble("latitude"),
                longitude = doc.getDouble("longitude"),
                capacity = doc.getLong("capacity")?.toInt() ?: 0,
                availableBeds = doc.getLong("availableBeds")?.toInt() ?: 0,
                costPerMonth = doc.getLong("costPerMonth")?.toInt() ?: 0,
                foodType = FoodType.valueOf(doc.getString("foodType") ?: "NONE"),
                acType = ACType.valueOf(doc.getString("acType") ?: "NON_AC"),
                bedsInRoom = doc.getLong("bedsInRoom")?.toInt() ?: 1,
                rating = doc.getDouble("rating") ?: 0.0,
                images = (doc.get("images") as? List<String>) ?: emptyList(),
                reviews = reviewsList,
                isVerified = doc.getBoolean("isVerified") ?: false,
                mapLink = doc.getString("mapLink"),
                rules = doc.getString("rules")
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun pgToMap(pg: PG): Map<String, Any?> {
        return mapOf(
            "ownerId" to pg.ownerId,
            "ownerName" to pg.ownerName,
            "ownerPhone" to pg.ownerPhone,
            "ownerEmail" to pg.ownerEmail,
            "alternatePhone" to pg.alternatePhone,
            "name" to pg.name,
            "address" to pg.address,
            "location" to pg.location,
            "latitude" to pg.latitude,
            "longitude" to pg.longitude,
            "capacity" to pg.capacity,
            "availableBeds" to pg.availableBeds,
            "costPerMonth" to pg.costPerMonth,
            "foodType" to pg.foodType.name,
            "acType" to pg.acType.name,
            "bedsInRoom" to pg.bedsInRoom,
            "rating" to pg.rating,
            "images" to pg.images,
            "reviews" to pg.reviews.map { reviewToMap(it) },
            "isVerified" to pg.isVerified,
            "mapLink" to pg.mapLink,
            "rules" to pg.rules,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )
    }
    
    private fun reviewToMap(review: Review): Map<String, Any> {
        return mapOf(
            "id" to review.id,
            "userId" to review.userId,
            "userName" to review.userName,
            "rating" to review.rating,
            "comment" to review.comment
        )
    }
}

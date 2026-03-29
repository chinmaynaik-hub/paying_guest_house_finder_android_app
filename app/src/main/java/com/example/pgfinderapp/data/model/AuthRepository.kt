package com.example.pgfinderapp.data.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser
    
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Attempting login for email: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                Log.d(TAG, "Firebase login successful, uid: ${firebaseUser.uid}")
                val user = getUserFromFirestore(firebaseUser.uid)
                if (user != null) {
                    Log.d(TAG, "User data fetched successfully")
                    AuthResult.Success(user)
                } else {
                    Log.e(TAG, "User data not found in Firestore")
                    AuthResult.Error("User data not found")
                }
            } ?: AuthResult.Error("Login failed")
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Login failed")
        }
    }
    
    suspend fun signup(
        name: String,
        age: Int,
        email: String,
        password: String,
        role: Role
    ): AuthResult {
        return try {
            Log.d(TAG, "Attempting signup for email: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                Log.d(TAG, "Firebase account created, uid: ${firebaseUser.uid}")
                val user = User(
                    id = firebaseUser.uid,
                    name = name,
                    age = age,
                    role = role,
                    email = email,
                    password = ""
                )
                Log.d(TAG, "Saving user to Firestore...")
                saveUserToFirestore(user)
                Log.d(TAG, "User saved to Firestore successfully")
                AuthResult.Success(user)
            } ?: run {
                Log.e(TAG, "Signup failed - no user returned")
                AuthResult.Error("Signup failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Signup error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Signup failed")
        }
    }
    
    fun logout() {
        Log.d(TAG, "Logging out")
        auth.signOut()
    }
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "Sending password reset email to: $email")
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        Log.d(TAG, "Getting current user: ${firebaseUser.uid}")
        return getUserFromFirestore(firebaseUser.uid)
    }
    
    private suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            Log.d(TAG, "Fetching user from Firestore: $uid")
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                Log.d(TAG, "User document found")
                User(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    age = doc.getLong("age")?.toInt() ?: 0,
                    role = Role.valueOf(doc.getString("role") ?: "GUEST"),
                    email = doc.getString("email") ?: "",
                    password = ""
                )
            } else {
                Log.d(TAG, "User document not found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user: ${e.message}", e)
            null
        }
    }
    
    private suspend fun saveUserToFirestore(user: User) {
        Log.d(TAG, "Saving user ${user.id} to Firestore")
        val userData = hashMapOf(
            "name" to user.name,
            "age" to user.age,
            "role" to user.role.name,
            "email" to user.email,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        firestore.collection("users").document(user.id).set(userData).await()
        Log.d(TAG, "User saved successfully")
    }
}

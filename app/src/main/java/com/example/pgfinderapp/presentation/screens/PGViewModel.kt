package com.example.pgfinderapp.presentation.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pgfinderapp.data.model.PG
import com.example.pgfinderapp.data.model.PGRepository
import com.example.pgfinderapp.data.model.PGResult
import com.example.pgfinderapp.data.model.Review
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class PGState(
    val pgs: List<PG> = emptyList(),
    val selectedPG: PG? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val addPGSuccess: Boolean = false,
    val reviewAddedSuccess: Boolean = false
)

class PGViewModel : ViewModel() {
    private val pgRepository = PGRepository()
    
    companion object {
        private const val TAG = "PGViewModel"
    }
    
    var pgState by mutableStateOf(PGState())
        private set
    
    init {
        Log.d(TAG, "PGViewModel initialized")
        loadAllPGs()
    }
    
    fun loadAllPGs() {
        Log.d(TAG, "loadAllPGs() called")
        viewModelScope.launch {
            pgState = pgState.copy(isLoading = true)
            try {
                pgRepository.getAllPGs()
                    .catch { e ->
                        Log.e(TAG, "Error loading PGs: ${e.message}", e)
                        pgState = pgState.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                    .collectLatest { pgs ->
                        Log.d(TAG, "Loaded ${pgs.size} PGs")
                        pgState = pgState.copy(
                            pgs = pgs,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadAllPGs: ${e.message}", e)
                pgState = pgState.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun loadPGsByOwner(ownerId: String) {
        viewModelScope.launch {
            pgState = pgState.copy(isLoading = true)
            pgRepository.getPGsByOwner(ownerId).collectLatest { pgs ->
                pgState = pgState.copy(
                    pgs = pgs,
                    isLoading = false
                )
            }
        }
    }
    
    fun selectPG(pg: PG) {
        pgState = pgState.copy(selectedPG = pg)
    }
    
    fun clearSelectedPG() {
        pgState = pgState.copy(selectedPG = null)
    }
    
    fun addPG(pg: PG) {
        viewModelScope.launch {
            pgState = pgState.copy(isLoading = true, error = null)
            
            when (val result = pgRepository.addPG(pg)) {
                is PGResult.Success -> {
                    pgState = pgState.copy(
                        isLoading = false,
                        addPGSuccess = true
                    )
                }
                is PGResult.Error -> {
                    pgState = pgState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                PGResult.Loading -> {
                    pgState = pgState.copy(isLoading = true)
                }
            }
        }
    }
    
    fun updatePG(pg: PG) {
        viewModelScope.launch {
            pgState = pgState.copy(isLoading = true, error = null)
            
            when (val result = pgRepository.updatePG(pg)) {
                is PGResult.Success -> {
                    pgState = pgState.copy(
                        isLoading = false,
                        addPGSuccess = true
                    )
                }
                is PGResult.Error -> {
                    pgState = pgState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                PGResult.Loading -> {
                    pgState = pgState.copy(isLoading = true)
                }
            }
        }
    }
    
    fun deletePG(pgId: String) {
        viewModelScope.launch {
            when (val result = pgRepository.deletePG(pgId)) {
                is PGResult.Success -> {
                    // PG will be removed from the list via the Flow
                }
                is PGResult.Error -> {
                    pgState = pgState.copy(error = result.message)
                }
                PGResult.Loading -> {}
            }
        }
    }
    
    fun addReview(pgId: String, review: Review) {
        viewModelScope.launch {
            pgState = pgState.copy(isLoading = true, error = null)
            
            when (val result = pgRepository.addReview(pgId, review)) {
                is PGResult.Success -> {
                    // Refresh the selected PG to show new review
                    val updatedPG = pgRepository.getPGById(pgId)
                    pgState = pgState.copy(
                        isLoading = false,
                        selectedPG = updatedPG,
                        reviewAddedSuccess = true
                    )
                }
                is PGResult.Error -> {
                    pgState = pgState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                PGResult.Loading -> {
                    pgState = pgState.copy(isLoading = true)
                }
            }
        }
    }
    
    fun uploadSampleData() {
        viewModelScope.launch {
            pgRepository.uploadSampleData()
        }
    }
    
    fun clearError() {
        pgState = pgState.copy(error = null)
    }
    
    fun clearAddPGSuccess() {
        pgState = pgState.copy(addPGSuccess = false)
    }
    
    fun clearReviewAddedSuccess() {
        pgState = pgState.copy(reviewAddedSuccess = false)
    }
}

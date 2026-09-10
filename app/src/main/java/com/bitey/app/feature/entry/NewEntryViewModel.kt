package com.bitey.app.feature.entry

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.image.ImagePreprocessor
import com.bitey.app.core.image.ProcessedImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class NewEntryUiState(
    val selectedImage: ProcessedImage? = null,
    val isLoading: Boolean = false,
    val isCameraActive: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    private val imagePreprocessor: ImagePreprocessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewEntryUiState())
    val uiState: StateFlow<NewEntryUiState> = _uiState.asStateFlow()

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = imagePreprocessor.processUri(uri)
            result.fold(
                onSuccess = { processed ->
                    _uiState.update {
                        it.copy(
                            selectedImage = processed,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to process selected image."
                        )
                    }
                }
            )
        }
    }

    fun onPhotoCaptured(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isCameraActive = false, errorMessage = null) }
            val result = imagePreprocessor.processFile(file)
            result.fold(
                onSuccess = { processed ->
                    _uiState.update {
                        it.copy(
                            selectedImage = processed,
                            isLoading = false,
                            isCameraActive = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCameraActive = false,
                            errorMessage = error.localizedMessage ?: "Failed to process captured photo."
                        )
                    }
                }
            )
        }
    }

    fun openCamera() {
        _uiState.update { it.copy(isCameraActive = true, errorMessage = null) }
    }

    fun closeCamera() {
        _uiState.update { it.copy(isCameraActive = false) }
    }

    fun clearSelectedImage() {
        _uiState.update { it.copy(selectedImage = null, errorMessage = null) }
    }
}

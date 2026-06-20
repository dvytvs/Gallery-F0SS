package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaItem
import com.example.data.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class GalleryState {
    object Loading : GalleryState()
    data class Success(
        val mediaByDate: Map<String, List<MediaItem>>,
        val albums: Map<String, List<MediaItem>>,
        val allMediaCount: Int
    ) : GalleryState()
    data class Error(val message: String) : GalleryState()
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaRepository(application)

    private val _uiState = MutableStateFlow<GalleryState>(GalleryState.Loading)
    val uiState: StateFlow<GalleryState> = _uiState.asStateFlow()

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.value = GalleryState.Loading
            try {
                val mediaItems = repository.getMediaItems()
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                
                // Group by date (ignoring time)
                val groupedByDate = mediaItems.groupBy { item ->
                    dateFormat.format(Date(item.dateAdded))
                }

                // Group by album name
                val groupedByAlbum = mediaItems.groupBy { item ->
                    item.albumName ?: "Camera"
                }

                _uiState.value = GalleryState.Success(
                    mediaByDate = groupedByDate,
                    albums = groupedByAlbum,
                    allMediaCount = mediaItems.size
                )
            } catch (e: Exception) {
                _uiState.value = GalleryState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun getMediaItemById(id: Long): MediaItem? {
        val state = _uiState.value
        if (state is GalleryState.Success) {
            return state.mediaByDate.values.flatten().find { it.id == id }
        }
        return null
    }
}

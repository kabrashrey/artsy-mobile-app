package com.example.artsy_mobile_app

import android.util.Log
import androidx.compose.runtime.*

import kotlinx.coroutines.launch
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@Serializable
data class ArtistDetailsResponse(
    val statusCode: Int,
    val data: ArtistDetails,
    val message: String,
    val success: Boolean
)

@Serializable
data class ArtistDetails(
    val title: String,
    val id: String,
    val thumbnail: String? = null,
    val birthyear: String? = null,
    val nationality: String? = null,
    val biography: String? = null,
    val deathyear: String? = null
)

sealed class ArtistDetailsState {
    object Loading : ArtistDetailsState()
    data class Success(val artistDetails: ArtistDetails?) : ArtistDetailsState()
    data class Error(val message: String) : ArtistDetailsState()
}

class ArtistDetailsViewModel : ViewModel() {
    private val _state = mutableStateOf<ArtistDetailsState>(ArtistDetailsState.Loading)
    val state: State<ArtistDetailsState> get() = _state

    private val client = HttpClientProvider.client

    fun fetchArtistDetails(artistId: String) {
        viewModelScope.launch {
            _state.value = ArtistDetailsState.Loading
            try {
                val result = fetchArtistDetailsFromAPI(artistId)
                _state.value = ArtistDetailsState.Success(result)
            } catch (e: Exception) {
                _state.value = ArtistDetailsState.Error("Error fetching artist details: ${e.message}")
            }
        }
    }

    private suspend fun fetchArtistDetailsFromAPI(artistId: String): ArtistDetails? {
        val url = "https://artsy-shrey-3.wl.r.appspot.com/api/artists?id=$artistId"
        return try {
            val response: String = client.get(url).bodyAsText()

            val artistResponse = json.decodeFromString<ArtistDetailsResponse>(response)

            if (artistResponse.success) {
                artistResponse.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ArtistDetailsAPI", "Error fetching artist details: ${e.message}")
            null
        }
    }
}

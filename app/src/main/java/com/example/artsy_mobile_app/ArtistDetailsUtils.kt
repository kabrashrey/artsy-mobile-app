package com.example.artsy_mobile_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import kotlinx.serialization.Serializable

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
            Log.i("ArtistDetailsAPI", "Raw JSON: $response")

            val artistResponse = json.decodeFromString<ArtistDetailsResponse>(response)

            if (artistResponse.success) {
                Log.d("ArtistDetailsAPI", "Fetched artist details: ${artistResponse.data}")
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
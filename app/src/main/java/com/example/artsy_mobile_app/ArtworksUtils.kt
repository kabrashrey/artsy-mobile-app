package com.example.artsy_mobile_app

import android.util.Log
import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import androidx.lifecycle.ViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ArtworksResponse(
    val statusCode: Int,
    val data: List<Artworks>,
    val message: String,
    val success: Boolean
)

@Serializable
data class Artworks(
    val id: String,
    val title: String,
    val date: String,
    @SerialName("thumbnail_href") val thumbnail: String? = null
)

sealed class ArtworksState {
    object Loading : ArtworksState()
    data class Success(val results: List<Artworks>) : ArtworksState()
    data class Error(val message: String) : ArtworksState()
}

class ArtworksViewModel : ViewModel() {
    var artworksState by mutableStateOf<ArtworksState>(ArtworksState.Loading)
//    var artworksQuery by mutableStateOf("")

    private val client = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    suspend fun fetchArtworks(artistId: String) {
        artworksState = ArtworksState.Loading
        val results = fetchArtworksResultsFromAPI(artistId)
        artworksState = if (results.isNotEmpty()) {
            Log.d("ArtworksViewModel", "Artworks successful with ${results.size} results")
            ArtworksState.Success(results)
        } else {
            Log.d("ArtworksViewModel", "No results found for: $artistId")
            ArtworksState.Error("No results found")
        }
    }

    private suspend fun fetchArtworksResultsFromAPI(artistId: String): List<Artworks> {
        val url =
            "https://artsy-shrey-3.wl.r.appspot.com/api/artworks?artist_id=$artistId&size=10"
        return try {
            val response: String = client.get(url).bodyAsText()
            Log.i("ArtworksAPI", "Raw JSON: $response")

            val artworksResponse = jsonParser.decodeFromString<ArtworksResponse>(response)
            Log.d("ArtworksAPI", "Parsed Artworks: ${artworksResponse.data}")
            artworksResponse.data
        } catch (e: Exception) {
            Log.e("ArtworksError", "Error fetching Artworks results: ${e.message}")
            emptyList()
        }
    }
}

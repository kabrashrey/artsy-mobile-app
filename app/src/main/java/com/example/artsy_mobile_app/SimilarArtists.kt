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
data class SimilarArtistsResponse(
    val statusCode: Int,
    val data: List<SimilarArtists>,
    val message: String,
    val success: Boolean
)

@Serializable
data class SimilarArtists(
    val id: String,
    @SerialName("name") val name: String,
    val thumbnail: String? = null
)

sealed class SimilarArtistsState {
    object Loading : SimilarArtistsState()
    data class Success(val results: List<SimilarArtists>) : SimilarArtistsState()
    data class Error(val message: String) : SimilarArtistsState()
}

class SimilarArtistsViewModel : ViewModel() {
    var similarArtistState by mutableStateOf<SimilarArtistsState>(SimilarArtistsState.Loading)

    private val client = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    suspend fun fetchSimilarArtists(artistId: String) {
        similarArtistState = SimilarArtistsState.Loading
        val results = fetchSimilarArtistsResultsFromAPI(artistId)
        similarArtistState = if (results.isNotEmpty()) {
            Log.d("SimilarArtistsViewModel", "SimilarArtists successful with ${results.size} results")
            SimilarArtistsState.Success(results)
        } else {
            Log.d("SimilarArtistsViewModel", "No results found for: $artistId")
            SimilarArtistsState.Error("No results found")
        }
    }

    private suspend fun fetchSimilarArtistsResultsFromAPI(artistId: String): List<SimilarArtists> {
        val url =
            "https://artsy-shrey-3.wl.r.appspot.com/api/artists/similar/?id=$artistId"
        return try {
            val response: String = client.get(url).bodyAsText()
            Log.i("SimilarArtistsAPI", "Raw JSON: $response")

            val similarArtistResponse = jsonParser.decodeFromString<SimilarArtistsResponse>(response)
            Log.d("SimilarArtistsAPI", "Parsed SimilarArtists: ${similarArtistResponse.data}")
            similarArtistResponse.data
        } catch (e: Exception) {
            Log.e("SimilarArtistsError", "Error fetching SimilarArtists results: ${e.message}")
            emptyList()
        }
    }
}

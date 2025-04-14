package com.example.artsy_mobile_app

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
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
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage

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

    private val client = HttpClientProvider.client

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

            val artworksResponse = json.decodeFromString<ArtworksResponse>(response)
            Log.d("ArtworksAPI", "Parsed Artworks: ${artworksResponse.data}")
            artworksResponse.data
        } catch (e: Exception) {
            Log.e("ArtworksError", "Error fetching Artworks results: ${e.message}")
            emptyList()
        }
    }
}



@Composable
fun ArtworksCard(artist: Artworks, onClick: (Artworks) -> Unit )
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = artist.thumbnail,
                contentDescription = artist.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.artsy_logo),
                error = painterResource(id = R.drawable.artsy_logo),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${artist.title}, ${artist.date}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Button(
                onClick = { onClick(artist) },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(text = "View Categories")
            }
        }
    }
}

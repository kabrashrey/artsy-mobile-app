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

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage


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

    private val client = HttpClientProvider.client

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

            val similarArtistResponse = json.decodeFromString<SimilarArtistsResponse>(response)
            Log.d("SimilarArtistsAPI", "Parsed SimilarArtists: ${similarArtistResponse.data}")
            similarArtistResponse.data
        } catch (e: Exception) {
            Log.e("SimilarArtistsError", "Error fetching SimilarArtists results: ${e.message}")
            emptyList()
        }
    }
}


@Composable
fun SimilarArtistCard(SimilarArtists: SimilarArtists, onClick: (SimilarArtists) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(SimilarArtists) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = SimilarArtists.thumbnail,
                contentDescription = SimilarArtists.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.artsy_logo),
                error = painterResource(id = R.drawable.artsy_logo),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp)
                    .clickable { onClick(SimilarArtists) }
            ) {
                Text(
                    text = SimilarArtists.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go to Artist Details",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
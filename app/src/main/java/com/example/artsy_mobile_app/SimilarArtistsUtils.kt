package com.example.artsy_mobile_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
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
            SimilarArtistsState.Success(results)
        } else {
            SimilarArtistsState.Error("No results found")
        }
    }

    private suspend fun fetchSimilarArtistsResultsFromAPI(artistId: String): List<SimilarArtists> {
        val url =
            "https://artsy-shrey-3.wl.r.appspot.com/api/artists/similar/?id=$artistId"
        return try {
            val response: String = client.get(url).bodyAsText()

            val similarArtistResponse = json.decodeFromString<SimilarArtistsResponse>(response)
            similarArtistResponse.data
        } catch (e: Exception) {
            Log.e("SimilarArtistsError", "Error fetching SimilarArtists results: ${e.message}")
            emptyList()
        }
    }
}


@Composable
fun SimilarArtistCard(
    similarArtists: SimilarArtists,
    onClick: (SimilarArtists) -> Unit,
    isLoggedIn: Boolean,
    isFavorited: Boolean,
    onToggleFavorite: (SimilarArtists, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(similarArtists) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = similarArtists.thumbnail,
                contentDescription = similarArtists.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.artsy_logo),
                error = painterResource(id = R.drawable.artsy_logo),
            )
            if (isLoggedIn) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .clickable {
                            onToggleFavorite(similarArtists, !isFavorited)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isFavorited) R.drawable.star_filled else R.drawable.star_outline
                        ),
                        contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorited) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp)
                    .clickable { onClick(similarArtists) }
            ) {
                Text(
                    text = similarArtists.name,
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

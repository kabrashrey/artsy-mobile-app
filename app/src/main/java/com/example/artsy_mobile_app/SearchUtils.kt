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

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import io.ktor.client.statement.bodyAsText

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight


@Serializable
data class SearchResponse(
    val statusCode: Int,
    val data: List<Artist>,
    val message: String,
    val success: Boolean
)

@Serializable
data class Artist(
    val title: String,
    val id: String,
    val thumbnail: String? = null
)

sealed class SearchState {
    object Loading : SearchState()
    data class Success(val results: List<Artist>) : SearchState()
    data class Error(val message: String) : SearchState()
}

class SearchViewModel : ViewModel() {
    var searchState by mutableStateOf<SearchState>(SearchState.Loading)
    var searchQuery by mutableStateOf("")

    private val client = HttpClientProvider.client

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
        if (newQuery.length >= 3) {
            viewModelScope.launch {
                performSearch(newQuery)
            }
        } else {
            searchState = SearchState.Error("Type at least 3 characters")
        }
    }

    private suspend fun performSearch(query: String) {
        searchState = SearchState.Loading

        val results = fetchSearchResultsFromAPI(query)

        searchState = if (results.isNotEmpty()) {
            SearchState.Success(results)
        } else {
            SearchState.Error("No results found")
        }
    }

    private suspend fun fetchSearchResultsFromAPI(query: String): List<Artist> {
        val url = "https://artsy-shrey-3.wl.r.appspot.com/api/search?q=$query&size=10&type=artist"
        return try {
            val response: String = client.get(url).bodyAsText()
            val searchResponse = json.decodeFromString<SearchResponse>(response)
            searchResponse.data
        } catch (e: Exception) {
            Log.e("SearchError", "Error fetching search results: ${e.message}")
            emptyList()
        }
    }
}


@Composable
fun ArtistCard(
    artist: Artist,
    onClick: (Artist) -> Unit,
    isLoggedIn: Boolean,
    isFavorited: Boolean,
    onToggleFavorite: (Artist, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(artist) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = artist.thumbnail,
                contentDescription = artist.title,
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
                            onToggleFavorite(artist, !isFavorited)
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp)
                    .clickable { onClick(artist) }
            ) {
                Text(
                    text = artist.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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

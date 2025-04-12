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

    private val client = HttpClient(Android){
        install(Logging){
            level = LogLevel.ALL
        }
    }

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
        Log.d("SearchViewModel", "Searching for: $query")

        val results = fetchSearchResultsFromAPI(query)

        searchState = if (results.isNotEmpty()) {
            Log.d("SearchViewModel", "Search successful with ${results.size} results")
            SearchState.Success(results)
        } else {
            Log.d("SearchViewModel", "No results found for: $query")
            SearchState.Error("No results found")
        }
    }

    private suspend fun fetchSearchResultsFromAPI(query: String): List<Artist> {
        val url = "https://artsy-shrey-3.wl.r.appspot.com/api/search?q=$query&size=10&type=artist"
        return try {
            val response: String = client.get(url).bodyAsText()
            Log.i("SearchAPI", "Raw JSON: $response")

            val searchResponse = jsonParser.decodeFromString<SearchResponse>(response)

            Log.d("SearchAPI", "Parsed Artists: ${searchResponse.data}")
            searchResponse.data
        } catch (e: Exception) {
            Log.e("SearchError", "Error fetching search results: ${e.message}")
            emptyList()
        }
    }
}



@Composable
fun ArtistCard(artist: Artist, onClick: (Artist) -> Unit) {
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
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp)
                    .clickable { onClick(artist) }
            ) {
                Text(
                    text = artist.title,
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
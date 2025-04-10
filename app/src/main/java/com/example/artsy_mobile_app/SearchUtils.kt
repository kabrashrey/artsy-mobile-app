package com.example.artsy_mobile_app

import android.util.Log
import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

            val searchResponse = Json {
                ignoreUnknownKeys = true
            }.decodeFromString<SearchResponse>(response)

            Log.d("SearchAPI", "Parsed Artists: ${searchResponse.data}")
            searchResponse.data
        } catch (e: Exception) {
            Log.e("SearchError", "Error fetching search results: ${e.message}")
            emptyList()
        }
    }
}
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

// Data class to hold the search results response
data class SearchResponse(val results: List<String>)

sealed class SearchState {
    object Loading : SearchState()
    data class Success(val results: List<String>) : SearchState()
    data class Error(val message: String) : SearchState()
}

class SearchViewModel : ViewModel() {
    var searchState by mutableStateOf<SearchState>(SearchState.Loading)
//    var searchResults = mutableStateOf<List<String>>(emptyList())
    var searchQuery = mutableStateOf("")

    private val client = HttpClient(Android){
        install(Logging){
            level = LogLevel.ALL
        }
    }

    fun search(query: String) {
        if (query.length >= 3) {
            searchState = SearchState.Loading
            searchQuery = query

            viewModelScope.launch {
                val results = fetchSearchResultsFromAPI(query)
                searchState =  if(results.isNotEmpty()){
                    SearchState.Success(results)
                }else{
                    SearchState.Error("No results found")
                }
            }
        } else {
            searchState =  SearchState.Error("Query must be 3 characters")
        }
    }

    private suspend fun fetchSearchResultsFromAPI(query: String): List<String> {
        val url = "https://artsy-shrey-3.wl.r.appspot.com/api/search?q=$query&size=10&type=artist"
        return try {
            val response: String = client.get(url).bodyAsText()
            val searchResponse = Json.decodeFromString<SearchResponse>(response)
            searchResponse.results
        } catch (e: Exception) {
            Log.e("SearchError", "Error fetching search results: ${e.message}")
            emptyList()
        }
    }

    override fun Oncleared(){
        super.onCleared()
        client.close()
    }
}
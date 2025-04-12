package com.example.artsy_mobile_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class FavoritesResponse(
    val statusCode: Int,
    val message: String,
    val success: Boolean,
    val data: List<FavoriteArtist>
)

@Serializable
data class FavoriteArtist(
    @SerialName("_id") val id: String,
    @SerialName("fav_id") val favId: String,
    val email: String,
    val name: String,
    val birthyear: String,
    val deathyear: String,
    val nationality: String,
    @SerialName("bg_img") val bgImg: String,
    @SerialName("added_at") val addedAt: String
)

sealed class GetFavoriteArtistState {
    object Loading : GetFavoriteArtistState()
    data class Success(val results: List<FavoriteArtist>) : GetFavoriteArtistState()
    data class Error(val message: String) : GetFavoriteArtistState()
}


class FavoriteArtistViewModel : ViewModel() {

    var getFavoritesState by mutableStateOf<GetFavoriteArtistState>(GetFavoriteArtistState.Loading)

    private val client = HttpClient()

    suspend fun fetchFavorites(email: String) {
        getFavoritesState = GetFavoriteArtistState.Loading
        val results = fetchFavoritesFromAPI(email)

        getFavoritesState = if (results.isNotEmpty()) {
            Log.d("FavViewModel", "Fav successful with ${results.size} results")
            GetFavoriteArtistState.Success(results)
        } else {
            Log.d("FavViewModel", "No results found for: $email")
            GetFavoriteArtistState.Error("No results found")
        }
    }

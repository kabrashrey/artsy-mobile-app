package com.example.artsy_mobile_app

import android.util.Log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.http.*
import io.ktor.client.request.setBody
import kotlin.String

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

sealed class AddFavoriteArtistState {
    object Loading : AddFavoriteArtistState()
    data class Success(val results: List<FavoriteArtist>) : AddFavoriteArtistState()
    data class Error(val message: String) : AddFavoriteArtistState()
}

sealed class RemoveFavoriteArtistState {
    object Loading : RemoveFavoriteArtistState()
    data class Success(val results: List<FavoriteArtist>) : RemoveFavoriteArtistState()
    data class Error(val message: String) : RemoveFavoriteArtistState()
}


class FavoriteArtistViewModel : ViewModel() {

    var addFavoriteArtistState: AddFavoriteArtistState by mutableStateOf(AddFavoriteArtistState.Loading)
    var removeFavoriteArtistState: RemoveFavoriteArtistState by mutableStateOf(RemoveFavoriteArtistState.Loading)
    var getFavoritesState: GetFavoriteArtistState by mutableStateOf(GetFavoriteArtistState.Loading)

    private val currentFavorites = mutableListOf<FavoriteArtist>()
    private val client = HttpClientProvider.client

    fun addFavorite(email: String, favId: String) {
        viewModelScope.launch {
            addToFavorites(email, favId)
            if (addFavoriteArtistState is AddFavoriteArtistState.Success) {
                SnackbarManager.showMessage("Added to favorites")
            }
            fetchFavorites(email)
        }
    }

    fun removeFavorite(email: String, favId: String) {
        viewModelScope.launch {
            removeFromFavorites(email, favId)
            if (removeFavoriteArtistState is RemoveFavoriteArtistState.Success) {
                SnackbarManager.showMessage("Removed from favorites")
            }
            fetchFavorites(email)
        }
    }

    suspend fun fetchFavorites(email: String) {
        getFavoritesState = GetFavoriteArtistState.Loading
        val results = fetchFavoritesFromAPI(email)

        getFavoritesState = if (results.isNotEmpty()) {
            GetFavoriteArtistState.Success(results)
        } else {
            GetFavoriteArtistState.Error("No results found")
        }
    }

    private suspend fun fetchFavoritesFromAPI(email: String): List<FavoriteArtist> {
        val url = "https://artsy-shrey-3.wl.r.appspot.com/api/favourites/"
        return try {
            val response: String = client.get(url){
                url{
                    parameters.append("email", email)
                }
            }.body()

            val getFavResponse = json.decodeFromString<FavoritesResponse>(response)
            getFavResponse.data
        } catch (e: Exception) {
            Log.e("GetFavAPIError", "Error fetching Fav results: ${e.message}")
            emptyList()
        }
    }

    suspend fun addToFavorites(email: String, favId: String) {
        addFavoriteArtistState = AddFavoriteArtistState.Loading
        val newFavorite = addToFavoritesAPI(email, favId)

        if (newFavorite != null) {
            currentFavorites.add(newFavorite)
            addFavoriteArtistState = AddFavoriteArtistState.Success(currentFavorites)
        } else {
            addFavoriteArtistState = AddFavoriteArtistState.Error("Failed to add to favorites")
        }
    }

    private suspend fun addToFavoritesAPI(email: String, favId: String): FavoriteArtist? {
        val url = "https://artsy-shrey-3.wl.r.appspot.com/api/favourites/add"
        return try {
            val response: String = client.post(url) {
                setBody(FormDataContent(Parameters.build {
                    append("email", email)
                    append("fav_id", favId)
                }))
            }.body()

            val responseJson = json.parseToJsonElement(response).jsonObject
            val dataJson = responseJson["data"]?.toString()
            dataJson?.let { json.decodeFromString<FavoriteArtist>(it) }
        } catch (e: Exception) {
            Log.e("AddFavAPIError", "Error adding to favorites: ${e.message}")
            null
        }
    }

    suspend fun removeFromFavorites(email: String, favId: String) {
        removeFavoriteArtistState = RemoveFavoriteArtistState.Loading
        val removedId = removeFavoritesFromAPI(email, favId)

        if (removedId != null) {
            currentFavorites.removeAll { it.favId == removedId }
            removeFavoriteArtistState = RemoveFavoriteArtistState.Success(currentFavorites)
        } else {
            removeFavoriteArtistState = RemoveFavoriteArtistState.Error("Failed to remove from favorites")
        }
    }

    private suspend fun removeFavoritesFromAPI(email: String, favId: String): String? {
        val url = "https://artsy-shrey-3.wl.r.appspot.com/api/favourites/delete"
        return try {
            val response: String = client.delete(url) {
                setBody(FormDataContent(Parameters.build {
                    append("email", email)
                    append("fav_id", favId)
                }))
            }.body()

            val responseJson = json.parseToJsonElement(response).jsonObject
            responseJson["data"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            Log.e("RemoveFavAPIError", "Error removing from favorites: ${e.message}")
            null
        }
    }
}



@Composable
fun FavoriteArtistItem(artist: FavoriteArtist, onClick: () -> Unit) {
    val addedAtMillis = remember(artist.addedAt) {
        parseTimestamp(artist.addedAt)
    }

    val elapsedTime by produceState(initialValue = formatElapsedTime(System.currentTimeMillis() - addedAtMillis)) {
        while (true) {
            delay(1000)
            value = formatElapsedTime(System.currentTimeMillis() - addedAtMillis)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column (modifier = Modifier.weight(1f)
        ) {
            Text(artist.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("${artist.nationality}, ${artist.birthyear}", fontSize = 10.sp)
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = elapsedTime,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "View Details",
            modifier = Modifier
                .padding(start = 8.dp)
                .size(20.dp)
        )
    }
}


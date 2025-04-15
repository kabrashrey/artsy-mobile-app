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
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.delay


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

    val favoriteArtistIds: Set<String>
        get() = (getFavoritesState as? GetFavoriteArtistState.Success)
            ?.results
            ?.map { it.favId }
            ?.toSet()
            ?: emptySet()

    private val client = HttpClientProvider.client

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


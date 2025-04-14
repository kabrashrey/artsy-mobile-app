package com.example.artsy_mobile_app

import PersistentCookieJar
import android.content.Context
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import kotlinx.serialization.json.Json
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

import android.util.Log
import java.util.TimeZone

@Composable
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Loading...")
        }
    }
}


//
//object KtorClientProvider {
//    private var client: HttpClient? = null
//
//    fun getClient(context: Context): HttpClient {
//        if (client == null) {
//            val cookieJar = PersistentCookieJar(context)
//
//            val okHttpClient = OkHttpClient.Builder()
//                .cookieJar(cookieJar)
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .build()
//
//            client = HttpClient(OkHttp) {
//                engine {
//                    preconfigured = okHttpClient
//                }
//                install(ContentNegotiation) {
//                    json(Json { ignoreUnknownKeys = true })
//                }
//            }
//        }
//        return client!!
//    }
//}

object SnackbarManager {
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    suspend fun showMessage(message: String) {
        _messages.emit(message)
    }
}

object HttpClientProvider {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }
}

fun parseTimestamp(timestamp: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//        format.timeZone = TimeZone.getTimeZone("UTC")
        format.timeZone = TimeZone.getDefault()
        format.parse(timestamp)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

fun formatElapsedTime(diffInMillis: Long): String {
    return when {
        diffInMillis < 0 -> "just now"
        diffInMillis < 60_000 -> "${diffInMillis/1000} secs ago"
        diffInMillis < 3_600_000 -> "${diffInMillis / 60_000} min ago"
        diffInMillis < 86_400_000 -> "${diffInMillis / 3_600_000} hours ago"
        diffInMillis < 2_592_000_000 -> "${diffInMillis / 86_400_000} days ago"
        diffInMillis < 31_536_000_000 -> "${diffInMillis / 2_592_000_000} months ago"
        else -> "${diffInMillis / 31_536_000_000} years ago"
    }
}

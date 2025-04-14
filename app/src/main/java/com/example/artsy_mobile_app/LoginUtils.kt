package com.example.artsy_mobile_app
import PersistentCookieJar
import com.example.artsy_mobile_app.LoginRepository.loginUser

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.CancellationException

import io.ktor.client.engine.okhttp.*

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope

val json = Json { ignoreUnknownKeys = true }

@Serializable
data class User(
    val _id: String,
    val name: String,
    val email: String,
    val avatar: String
)

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val message: String) : LoginState()
    data class Error(val error: String) : LoginState()
}

sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    data class Success(val message: String) : LogoutState()
    data class Error(val error: String) : LogoutState()
}

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(context: Context, email: String, password: String) {
        if (_loginState.value is LoginState.Loading) {
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try{
                val result = loginUser(context, email, password)
                _loginState.value = result.fold(
                    onSuccess = { LoginState.Success("Logged in successfully!") },
                    onFailure = {
                        Log.e("LoginVM", "Login failed", it)
                        LoginState.Error(it.message ?: "Unknown error")
                    }
                )
            } catch (e: CancellationException) {
                _loginState.value = LoginState.Error("Operation cancelled")
            } catch (e: Exception) {
                Log.e("LoginVM", "Login failed", e)
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}


object LoginRepository {
    suspend fun loginUser(context: Context, email: String, password: String): Result<Unit> {
        return runCatching {
            val client = HttpClientProvider.client

            val response = client.post("https://artsy-shrey-3.wl.r.appspot.com/api/users/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email, "password" to password))
            }

            if (response.status != HttpStatusCode.OK) {
                throw Exception("Login failed with status: ${response.status}")
            }

            val responseBody = response.body<JsonObject>()
            val data = responseBody["data"]?.jsonObject ?: throw Exception("Invalid response")

            val userJson = data["user"]?.toString() ?: error("User not found")
            val user = json.decodeFromString<User>(userJson)

            // Extract tokens
            val accessToken = data["accessToken"]?.jsonPrimitive?.content ?: ""

            UserSessionManager.init(context)
            UserSessionManager.saveSession(user, accessToken)

//            client.close()
        }
    }
}


class LogoutViewModel : ViewModel() {
    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState

    fun logout(context: Context) {
        _logoutState.value = LogoutState.Loading

        viewModelScope.launch {
            try{
                val result = LogoutRepository.logoutUser(context)
                _logoutState.value = result.fold(
                    onSuccess = {
                        LogoutState.Success("Logged out successfully!")
                    },
                    onFailure = {
                        LogoutState.Error(it.message ?: "Unknown error during logout")
                    }
                )
            } catch(e: Exception){
                Log.e("LoginViewModel", "Login failed: ${e.message}")
                _logoutState.value = LogoutState.Error(e.message ?: "Unknown error")

            }
        }
    }

}


object LogoutRepository{
    suspend fun logoutUser(context: Context): Result<Unit> {
        return runCatching {
            val client = HttpClientProvider.client

            UserSessionManager.init(context)
            val email = UserSessionManager.getUser()?.email ?: throw Exception("No user found")

            val accessToken = UserSessionManager.getAccessToken()

            if (accessToken == null) {
                throw Exception("No tokens available for logout")
            }

            client.post("https://artsy-shrey-3.wl.r.appspot.com/api/users/logout") {
                contentType(ContentType.Application.Json)

                // Add the cookie headers
                header(HttpHeaders.Cookie, "accessToken=$accessToken;")

                setBody(mapOf("email" to email))
            }.also { response ->
                if (response.status != HttpStatusCode.OK) {
                    throw Exception("Logout failed with status: ${response.status}")
                }
            }

            // Clear stored session
            val cookieJar = PersistentCookieJar(context)
            cookieJar.clearCookies()
            UserSessionManager.clearSession()


//            client.close()
        }
    }
}

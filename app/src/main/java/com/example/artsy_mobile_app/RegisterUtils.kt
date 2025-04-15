package com.example.artsy_mobile_app

import PersistentCookieJar
import com.example.artsy_mobile_app.LoginRepository.loginUser

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CancellationException
import android.util.Log



sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val message: String) : RegisterState()
    data class Error(val error: String) : RegisterState()
}


@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class UserData(
    val name: String,
    val email: String,
    val password: String,
    val avatar: String,
    val _id: String
)


object RegisterRepository {
    suspend fun registerUser(context: Context, name: String, email: String, password: String): Result<Unit> {
        return runCatching {
            val client = HttpClientProvider.client

            val response = client.post("https://artsy-shrey-3.wl.r.appspot.com/api/users/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(name, email, password))
            }

            if (response.status != HttpStatusCode.Created) {
                throw Exception("Registration failed with status: ${response.status}")
            }

            val responseBody = response.body<JsonObject>()
            val data = responseBody["data"]?.jsonObject ?: throw Exception("Invalid response")

            val user = User(
                _id = data["_id"]?.jsonPrimitive?.content ?: "",
                name = data["name"]?.jsonPrimitive?.content ?: "",
                email = data["email"]?.jsonPrimitive?.content ?: "",
                avatar = data["avatar"]?.jsonPrimitive?.content ?: ""
            )

            // Fake login after registration or retrieve token
            // Here, you could call the login API or just simulate login
            val fakeAccessToken = "dummy-token-if-not-returned" // Replace if backend sends actual token

            UserSessionManager.init(context)
            UserSessionManager.saveSession(user, fakeAccessToken)
        }
    }
}


class RegisterViewModel() : ViewModel() {

    private val client = HttpClientProvider.client

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun registerUser(context: Context, name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = RegisterRepository.registerUser(context, name, email, password)
                _registerState.value = result.fold(
                    onSuccess = { RegisterState.Success("Registered successfully!") },
                    onFailure = {
                        Log.e("RegisterVM", "Registration failed", it)
                        RegisterState.Error(it.message ?: "Unknown error")
                    }
                )
            } catch (e: CancellationException) {
                _registerState.value = RegisterState.Error("Operation cancelled")
            } catch (e: Exception) {
                Log.e("RegisterVM", "Registration failed", e)
                _registerState.value = RegisterState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed class DeleteAccountState {
    object Idle : DeleteAccountState()
    object Loading : DeleteAccountState()
    data class Success(val message: String) : DeleteAccountState()
    data class Error(val error: String) : DeleteAccountState()
}

object DeleteAccountRepository {

    suspend fun deleteUserAccount(context: Context): Result<Unit> {
        return runCatching {
            val client = HttpClientProvider.client

            UserSessionManager.init(context)
            val email = UserSessionManager.getUser()?.email ?: throw Exception("No user found")

            val accessToken = UserSessionManager.getAccessToken()

            if (accessToken == null) {
                throw Exception("No tokens available for account deletion")
            }

            val response = client.delete("https://artsy-shrey-3.wl.r.appspot.com/api/users/delete") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Cookie, "accessToken=$accessToken;")
                setBody(mapOf("email" to email))
            }

            if (response.status != HttpStatusCode.OK) {
                throw Exception("Account deletion failed: ${response.status}")
            }

            // Clear session and cookies after successful deletion
            val cookieJar = PersistentCookieJar(context)
            cookieJar.clearCookies()
            UserSessionManager.clearSession()
        }
    }
}

class DeleteAccountViewModel : ViewModel() {

    private val _deleteState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteState: StateFlow<DeleteAccountState> = _deleteState

    fun deleteAccount(context: Context) {
        _deleteState.value = DeleteAccountState.Loading

        viewModelScope.launch {
            try {
                val result = DeleteAccountRepository.deleteUserAccount(context)
                _deleteState.value = result.fold(
                    onSuccess = {
                        DeleteAccountState.Success("Account deleted successfully")
                    },
                    onFailure = {
                        Log.e("DeleteAccountVM", "Deletion failed", it)
                        DeleteAccountState.Error(it.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                Log.e("DeleteAccountVM", "Unexpected error", e)
                _deleteState.value = DeleteAccountState.Error(e.message ?: "Unknown error")
            }
        }
    }
}


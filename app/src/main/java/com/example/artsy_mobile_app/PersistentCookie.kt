import android.content.Context
import android.content.SharedPreferences
import com.example.artsy_mobile_app.SerializableCookie
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

import androidx.core.content.edit

class PersistentCookieJar(context: Context) : CookieJar {
    private val prefs: SharedPreferences = context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val cookieMap = mutableMapOf<String, List<Cookie>>()

    init {
        loadCookies()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieMap[url.host] = cookies
        persistCookies()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieMap[url.host].orEmpty().filter { it.expiresAt > System.currentTimeMillis() }
        cookieMap[url.host] = cookies
        return cookies
    }

    private fun persistCookies() {
        val map = cookieMap.mapValues { entry -> entry.value.map { SerializableCookie.from(it) } }
        prefs.edit {
            putString("cookies", json.encodeToString(map))
        }
    }

    private fun loadCookies() {
        val saved = prefs.getString("cookies", null) ?: return
        val map = runCatching {
            json.decodeFromString<Map<String, List<SerializableCookie>>>(saved)
        }.getOrNull()?.mapValues { it.value.map { sc -> sc.toOkHttpCookie() } }

        if (map != null) cookieMap.putAll(map)
    }

    fun clearCookies() {
        prefs.edit { remove("cookies") }
        cookieMap.clear()
    }
}
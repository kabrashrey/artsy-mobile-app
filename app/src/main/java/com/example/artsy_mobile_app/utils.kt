package com.example.artsy_mobile_app

import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


import kotlinx.serialization.json.Json

@Composable
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}


val jsonParser: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}
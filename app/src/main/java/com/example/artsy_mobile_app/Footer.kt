package com.example.artsy_mobile_app

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun Footer() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Powered by Artsy",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray,
                fontStyle = FontStyle.Italic),
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, "https://www.artsy.net".toUri())
                context.startActivity(intent)
            }
        )
    }
}

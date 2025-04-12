package com.example.artsy_mobile_app

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.navigation.compose.*
import androidx.navigation.NavHostController

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.Composable

import android.util.Log

import com.example.artsy_mobile_app.ui.theme.ArtsyMobileAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        UserSessionManager.init(applicationContext)

        setContent {
            ArtsyMobileAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("search") {
                        SearchScreen(navController)
                    }
                    composable("login") {
                        LoginScreen(navController)
                    }
                    composable ("register") {
                        RegisterScreen(navController)
                    }
                    composable("artistDetails/{artistId}") { backStackEntry ->
                        val artistId = backStackEntry.arguments?.getString("artistId")
                        if (artistId != null) {
                            ArtistDetailsScreen(artistId = artistId, navController)
                        }
                    }

                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    ArtsyMobileAppTheme {
        HomeScreen(navController = rememberNavController())
    }
}
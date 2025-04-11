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

import com.example.artsy_mobile_app.ui.theme.ArtsyMobileAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ArtsyMobileAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController)
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

@Composable
fun MainContent(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // Today's Date
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = getCurrentDate(),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = Color.Gray
            )
        }

        // Favorite
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0))
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login to see favorites")
        }

        Footer()
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArtsyMobileAppTheme {
        HomeScreen(navController = rememberNavController())
    }
}
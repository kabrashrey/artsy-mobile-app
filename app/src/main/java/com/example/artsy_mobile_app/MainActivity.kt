package com.example.artsy_mobile_app
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.rememberCoroutineScope

import com.example.artsy_mobile_app.ui.theme.ArtsyMobileAppTheme
import kotlinx.coroutines.coroutineScope


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        UserSessionManager.init(applicationContext)

        setContent {
            ArtsyMobileAppTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    SnackbarManager.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }
                Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { _ ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                    ) {
                        composable("home") {
                        HomeScreen(navController)
                        }
                        composable("search") {
                            SearchScreen(navController)
                        }
                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                snackbarHostState = snackbarHostState,
                                scope = scope)
                        }
                        composable("register") {
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
}


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    ArtsyMobileAppTheme {
        HomeScreen(navController = rememberNavController())
    }
}
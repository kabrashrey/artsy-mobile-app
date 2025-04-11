package com.example.artsy_mobile_app

import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar


import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel





@Composable
fun ArtistDetailsScreen( artistId: String, navController: NavHostController){
    val viewModel: ArtistDetailsViewModel = viewModel()
    val state =  viewModel.state

    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(artistId) {
        viewModel.fetchArtistDetails(artistId)
    }

    val artistName = when (val currentState = state.value) {
        is ArtistDetailsState.Success -> currentState.artistDetails?.title ?: ""
        else -> null
    }

    Scaffold(
        topBar = {
            ArtistDetailsTopBar(navController = navController, artistName = artistName.toString())
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {

                // --- Tabs below TopBar ---
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        icon = { Icon(Icons.Filled.Info, contentDescription = "Info") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        icon = { Icon(Icons.Filled.AccountBox, contentDescription = "Artworks") }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Similar Artists") }
                    )
                }

                when (selectedTabIndex){
                    0 -> {
                        when (val currentState = state.value) {
                            is ArtistDetailsState.Loading -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),

                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            is ArtistDetailsState.Success -> {
                                val artistDetails = currentState.artistDetails
                                val scrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .verticalScroll(scrollState)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${artistDetails?.title}",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = "${artistDetails?.nationality}, ${artistDetails?.birthyear} - ${artistDetails?.deathyear}",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally),
                                    )
                                    Text(text = "${artistDetails?.biography}")
                                }
                            }
                            is ArtistDetailsState.Error -> {
                                Text(text = (state.value as ArtistDetailsState.Error).message)
                            }
                        }
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Artworks will be shown here.")
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Similar Artists will be shown here.")
                        }
                    }
                }

            }
        },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailsTopBar(navController: NavHostController, artistName: String) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

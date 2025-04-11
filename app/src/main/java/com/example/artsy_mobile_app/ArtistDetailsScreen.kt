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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Person

import androidx.compose.material3.*
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
import androidx.compose.ui.draw.clip

import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


@Composable
fun ArtistDetailsScreen( artistId: String, navController: NavHostController){
    val viewModel: ArtistDetailsViewModel = viewModel()
    val artworksViewModel: ArtworksViewModel = viewModel()
    val similarArtistsViewModel: SimilarArtistsViewModel = viewModel()
    val state =  viewModel.state

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(artistId) {
        viewModel.fetchArtistDetails(artistId)
        artworksViewModel.fetchArtworks(artistId)
        similarArtistsViewModel.fetchSimilarArtists(artistId)
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
                        icon = { Icon(Icons.Outlined.Info, contentDescription = "Details") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        icon = { Icon(Icons.Outlined.AccountBox, contentDescription = "Artworks") }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        icon = { Icon(Icons.Outlined.Person, contentDescription = "Similar Artists") }
                    )
                }

                when (selectedTabIndex){
                    0 -> {
                        when (val currentState = state.value) {
                            is ArtistDetailsState.Loading -> {
                                    CircularProgressIndicator()
                            }

                            is ArtistDetailsState.Error -> {
                                Text(text = (state.value as ArtistDetailsState.Error).message)
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
                        }
                    }
                    1 -> {
                        val artworksState = artworksViewModel.artworksState
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (artworksState) {
                                is ArtworksState.Loading -> {
                                    CircularProgressIndicator()
                                }

                                is ArtworksState.Error -> {
                                    Text(text = artworksState.message)
                                }
                                is ArtworksState.Success -> {
                                    LazyColumn {
                                        items(artworksState.results) { artist ->
                                            ArtworksCard(artist = artist, onClick = {})
                                        }
                                    }
                                }

                            }
                        }
                    }
                    2 -> {
                        val similarArtistsState = similarArtistsViewModel.similarArtistState

                        when (similarArtistsState) {
                            is SimilarArtistsState.Loading -> {
                                    CircularProgressIndicator()
                            }

                            is SimilarArtistsState.Success -> {
                                LazyColumn {
                                    items(similarArtistsState.results) { artist ->
                                        SimilarArtistCard(artist = artist, onClick = {})
                                    }
                                }
                            }
                            is SimilarArtistsState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = similarArtistsState.message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
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

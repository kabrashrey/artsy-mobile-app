package com.example.artsy_mobile_app

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect


@Composable
fun ArtistDetailsScreen( artistId: String, navController: NavHostController){
    val viewModel: ArtistDetailsViewModel = viewModel()
    val state =  viewModel.state

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
                when (val currentState = state.value) {
                    is ArtistDetailsState.Loading -> {
                        Text(text = "Loading artist details...")
                    }
                    is ArtistDetailsState.Success -> {
                        val artistDetails = currentState.artistDetails
                        Text(text = "Name: ${artistDetails?.title}")
                        Text(text = "Birth Year: ${artistDetails?.birthyear}")
                        Text(text = "Nationality: ${artistDetails?.nationality}")
                        Text(text = "Biography: ${artistDetails?.biography}")
                        Text(text = "Death Year: ${artistDetails?.deathyear}")
                    }
                    is ArtistDetailsState.Error -> {
                        Text(text = (state.value as ArtistDetailsState.Error).message)
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

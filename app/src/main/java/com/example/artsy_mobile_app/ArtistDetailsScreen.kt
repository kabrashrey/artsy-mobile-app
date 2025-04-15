package com.example.artsy_mobile_app

import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.AccountBox

import androidx.compose.material3.*
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalConfiguration

import android.util.Log
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

data class TabItem(val title: String, val icon: Any)


@Composable
fun ArtistDetailsScreen( artistId: String, navController: NavHostController){
    val viewModel: ArtistDetailsViewModel = viewModel()
    val artworksViewModel: ArtworksViewModel = viewModel()
    val similarArtistsViewModel: SimilarArtistsViewModel = viewModel()
    val state =  viewModel.state
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val categoriesViewModel: CategoriesViewModel = viewModel()
    var showCarousel by remember { mutableStateOf(false) }
    var selectedArtworkId by remember { mutableStateOf<String?>(null) }
    val categoryState = categoriesViewModel.categoriesState

    val isLoggedIn = UserSessionManager.isLoggedIn()

    val tabs = mutableListOf(
        TabItem("Details", Icons.Outlined.Info),
        TabItem("Artworks", Icons.Outlined.AccountBox)
    )

    if (isLoggedIn) {
        tabs.add(TabItem("Similar", painterResource(id = R.drawable.person_search)))
    }

    LaunchedEffect(artistId) {
        selectedTabIndex = 0
        viewModel.fetchArtistDetails(artistId)
        artworksViewModel.fetchArtworks(artistId)
        similarArtistsViewModel.fetchSimilarArtists(artistId)
    }

    LaunchedEffect(selectedArtworkId) {
        selectedArtworkId?.let { artworkId ->
            categoriesViewModel.fetchCategories(artworkId)
            showCarousel = true
        }
    }

    if (showCarousel && categoryState is CategoriesState.Success) {
        Dialog(onDismissRequest = {
            showCarousel = false
            selectedArtworkId = null
        }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 650.dp)
                    .padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .align(Alignment.Start)
                        )

                        CategoriesCarousel(categories = categoryState.results)

                        Spacer(modifier = Modifier.weight(1f))

                        // Pill-shaped close button at bottom
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Button(
                                onClick = {
                                    showCarousel = false
                                    selectedArtworkId = null
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
    else if (showCarousel && categoryState is CategoriesState.Error) {
        Dialog(onDismissRequest = {
            showCarousel = false
            selectedArtworkId = null
        }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        )

                        Text(
                            text = "No Categories available",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                showCarousel = false
                                selectedArtworkId = null
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
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
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                when (tab.icon) {
                                    is ImageVector -> Icon(tab.icon, contentDescription = tab.title)
                                    is Painter -> Icon(tab.icon, contentDescription = tab.title)
                                }
                            },
                            text = { Text(tab.title) }
                        )
                    }
                }

                when (tabs[selectedTabIndex].title) {
                    "Details" -> {
                        when (val currentState = state.value) {
                            is ArtistDetailsState.Loading -> LoadingIndicator()
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
                    "Artworks" -> {
                        val artworksState = artworksViewModel.artworksState
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (artworksState) {
                                is ArtworksState.Loading -> LoadingIndicator()
                                is ArtworksState.Error -> {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .wrapContentSize()
                                            .fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No Artworks",
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                is ArtworksState.Success -> {
                                    LazyColumn {
                                        items(artworksState.results) { artist ->
                                            ArtworksCard(
                                                artist = artist,
                                                onClick = {
                                                    if (selectedArtworkId != artist.id) {
                                                        selectedArtworkId = artist.id
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Similar" -> {
                        val similarArtistsState = similarArtistsViewModel.similarArtistState
                        when (similarArtistsState) {
                            is SimilarArtistsState.Loading -> LoadingIndicator()
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

                            is SimilarArtistsState.Success -> {
                                LazyColumn {
                                    items(similarArtistsState.results) { artist ->
                                        SimilarArtistCard(
                                            SimilarArtists = artist,
                                            onClick = { selectedArtist ->
                                                navController.navigate("artistDetails/${selectedArtist.id}") {
                                                    popUpTo("artistDetails/{artistId}") {
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                        )
                                    }
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

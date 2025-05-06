package com.example.artsy_mobile_app

import androidx.compose.runtime.*

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.AccountBox


import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color

import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel

import android.util.Log


data class TabItem(val title: String, val icon: Any)


@Composable
fun ArtistDetailsScreen( artistId: String, navController: NavHostController){
    val viewModel: ArtistDetailsViewModel = viewModel()
    val artworksViewModel: ArtworksViewModel = viewModel()
    val similarArtistsViewModel: SimilarArtistsViewModel = viewModel()
    val favoriteViewModel: FavoriteArtistViewModel = viewModel()
    val categoriesViewModel: CategoriesViewModel = viewModel()

    val state =  viewModel.state

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showCarousel by remember { mutableStateOf(false) }
    var selectedArtworkId by remember { mutableStateOf<String?>(null) }
    val categoryState = categoriesViewModel.categoriesState

    val isLoggedIn = UserSessionManager.isLoggedIn()
    val email = UserSessionManager.getUser()?.email ?: ""

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
        favoriteViewModel.fetchFavorites(email)
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
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth()
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

    val getFavoritesState = favoriteViewModel.getFavoritesState
//    val isFavoritesLoading = getFavoritesState is GetFavoriteArtistState.Loading
    val favoriteArtistIds = remember(getFavoritesState) {
        (getFavoritesState as? GetFavoriteArtistState.Success)
            ?.results
            ?.map { it.favId }
            ?.toSet()
            ?: emptySet()
    }

    val artistDetails = (state.value as? ArtistDetailsState.Success)?.artistDetails
    val isFavourited = remember(artistDetails, favoriteArtistIds) {
        artistDetails?.id in favoriteArtistIds
    }

    Scaffold(
        topBar = {
            if (artistDetails != null) {
            ArtistDetailsTopBar(
                navController = navController,
                artistDetails = artistDetails,
                isLoggedIn = isLoggedIn,
                isFavorited = isFavourited,
                onToggleFavorite = { artist, nowFavourited ->
                    if (nowFavourited) {
                        favoriteViewModel.addFavorite(email, artist.id)
                    } else {
                        favoriteViewModel.removeFavorite(email, artist.id)
                    }
                }
            )
        }
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
                                    val yearDisplay = if (!artistDetails?.deathyear.isNullOrBlank()) "${artistDetails?.birthyear} - ${artistDetails?.deathyear}"
                                                        else artistDetails?.birthyear
                                    val nationalityLine = if(!yearDisplay.isNullOrBlank() && !artistDetails?.nationality.isNullOrBlank()) "${artistDetails?.nationality}, $yearDisplay"
                                                        else if(artistDetails?.nationality.isNullOrBlank() && !yearDisplay.isNullOrBlank()) yearDisplay
                                                        else artistDetails?.nationality
                                    Text(
                                        text = nationalityLine.toString(),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally),
                                    )
                                    Text(text = artistDetails?.biography ?: "")
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
                                        val isFavourited = favoriteArtistIds.contains(artist.id)
                                        SimilarArtistCard(
                                            similarArtists = artist,
                                            onClick = { selectedArtist ->
                                                navController.navigate("artistDetails/${selectedArtist.id}") {
                                                    popUpTo("artistDetails/{artistId}") {
                                                        inclusive = true
                                                    }
                                                }
                                            },
                                            isLoggedIn = isLoggedIn,
                                            isFavorited = isFavourited,
                                            onToggleFavorite = { similarArtists, nowFavourited ->
                                                if (nowFavourited) {
                                                    favoriteViewModel.addFavorite(email, similarArtists.id)
                                                } else {
                                                    favoriteViewModel.removeFavorite(email, similarArtists.id)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailsTopBar(
    navController: NavHostController,
    artistDetails: ArtistDetails,
    isLoggedIn: Boolean,
    isFavorited: Boolean,
    onToggleFavorite: (ArtistDetails, Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = artistDetails.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (isLoggedIn) {
                    IconButton(
                        onClick = {
                            onToggleFavorite(artistDetails, !isFavorited)
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (isFavorited) R.drawable.star_filled else R.drawable.star_outline
                            ),
                            contentDescription = if (isFavorited) "Unfavorite" else "Favorite",
                            tint = if (isFavorited) Color.Black else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

package com.example.artsy_mobile_app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    val searchViewModel: SearchViewModel = viewModel()
    val favoriteViewModel: FavoriteArtistViewModel = viewModel()

    val email = UserSessionManager.getUser()?.email ?: ""

    LaunchedEffect(Unit) {
        if (UserSessionManager.isLoggedIn()) {
            favoriteViewModel.fetchFavorites(email)
        }
    }

    val searchState = searchViewModel.searchState
    val query = searchViewModel.searchQuery
    val getFavoritesState = favoriteViewModel.getFavoritesState

    val favoriteArtistIds = remember(getFavoritesState) {
        (getFavoritesState as? GetFavoriteArtistState.Success)
            ?.results
            ?.map { it.favId }
            ?.toSet()
            ?: emptySet()
    }

    val isLoggedIn = UserSessionManager.isLoggedIn()

    Scaffold(
        topBar = {
            ArtistSearchBar(
                searchQuery = query,
                onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
                onClearQuery = { searchViewModel.onSearchQueryChange("") },
                navController = navController
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (searchState) {
                is SearchState.Loading -> {}
                is SearchState.Error -> {
//                    Surface(
//                        shape = RoundedCornerShape(50),
//                        color = MaterialTheme.colorScheme.primaryContainer,
//                        modifier = Modifier
//                            .padding(8.dp)
//                            .wrapContentSize()
//                            .fillMaxWidth()
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 16.dp, vertical = 12.dp),
//                            contentAlignment = Alignment.Center
//                        ){
//                            Text(
//                                text = "No Favorites",
//                                color = MaterialTheme.colorScheme.onErrorContainer,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                        }
//                    }
                }

                is SearchState.Success -> {
                    val results = searchState.results
                    if (results.isEmpty()) {
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
                                    .padding(horizontal = 16.dp, vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    text = "No Result Found",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyColumn {
                            items(results) { artist ->
                                val isFavourited = favoriteArtistIds.contains(artist.id)
                                ArtistCard(
                                    artist = artist,
                                    onClick = { navController.navigate("artistDetails/${artist.id}") },
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
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    navController: NavHostController
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        SearchBar(
            inputField = {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(text = "Search artists...",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
                                  },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        IconButton(onClick = {
                            onClearQuery()
                            expanded = false
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = SearchBarDefaults.colors(
                containerColor = Color.Transparent,
                dividerColor = Color.Transparent
            )
        ) {
        }
    }
}

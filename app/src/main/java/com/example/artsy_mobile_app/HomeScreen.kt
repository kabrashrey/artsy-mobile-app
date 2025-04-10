package com.example.artsy_mobile_app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.util.Log

@Composable
fun HomeScreen(navController: NavHostController) {
    var showSearch by remember { mutableStateOf(false) }

    val viewModel = remember { SearchViewModel() }
    val searchState by viewModel::searchState

    Scaffold(
        topBar = {
            AppBar(
                onSearchClick = { showSearch = !showSearch },
                showSearch = showSearch,
                onCloseSearch = { showSearch = false },
                navController = navController,
                viewModel = viewModel,
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                if (!showSearch) {
                    MainContent(navController)
                } else {
                    when (searchState) {
                        is SearchState.Loading -> {
                        }

                        is SearchState.Error -> {
                        }

                        is SearchState.Success -> {
                            val results = (searchState as SearchState.Success).results
                            Log.d("HomeScreen", "Displaying ${results.size} artist cards.")
                            if (results.isEmpty()) {
                                Text(
                                    text = ""
                                )
                            } else {
                                LazyColumn {
                                    items(results) { artist ->
                                        ArtistCard(artist = artist, onClick = { /* Handle click */ })
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
fun AppBar(
    onSearchClick: () -> Unit,
    showSearch: Boolean,
    onCloseSearch: () -> Unit,
    navController: NavHostController,
    viewModel: SearchViewModel
) {
    val searchState by viewModel::searchState
    val query by viewModel::searchQuery

    TopAppBar(
        title = {
            if (showSearch) {
                CustomizableSearchBar(
                    searchQuery = query,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.onSearchQueryChange(it) },
                    searchResults = if (searchState is SearchState.Success) {
                        (searchState as SearchState.Success).results
                    } else {
                        emptyList()
                    },
                    onResultClick = { selected ->
                        // You can handle click here (e.g., navigate to artist detail)
                       //navController.navigate("artistDetail/${selected.id}")
                        onCloseSearch()
                    },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    trailingIcon = {
                        IconButton(onClick = { onCloseSearch() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close Search")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Artist Search",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        actions = {
            if (!showSearch) {
                IconButton(onClick = { onSearchClick() }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
                IconButton(onClick = { navController.navigate("login") }) {
                    Icon(Icons.Filled.Person, contentDescription = "User")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
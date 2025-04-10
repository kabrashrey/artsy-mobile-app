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


@Composable
fun HomeScreen(navController: NavHostController) {
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppBar(
                onSearchClick = { showSearch = !showSearch },
                showSearch = showSearch,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onCloseSearch = { showSearch = false },
                navController = navController
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                MainContent(navController)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    onSearchClick: () -> Unit,
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    navController: NavHostController
) {
    TopAppBar(
        title = {
            if (showSearch) {
                CustomizableSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearch = { query -> /* Perform search logic */ },
                    searchResults = listOf("Van Gogh", "Monet", "Da Vinci", "Picasso"),
                    onResultClick = { /* Handle result click */ },
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
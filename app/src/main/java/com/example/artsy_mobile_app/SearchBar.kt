package com.example.artsy_mobile_app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close

import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    val searchViewModel: SearchViewModel = viewModel()
    val searchState = searchViewModel.searchState
    val query = searchViewModel.searchQuery

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
                is SearchState.Error -> {}

                is SearchState.Success -> {
                    val results = searchState.results
                    if (results.isEmpty()) {
                        Text(
                            text = ""
                        )
                    } else {
                        LazyColumn {
                            items(results) { artist ->
                                ArtistCard(
                                    artist = artist,
                                    onClick = { navController.navigate("artistDetails/${artist.id}") })
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

    SearchBar(
        inputField = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search artists...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    IconButton(onClick = {
                        onClearQuery()
                        expanded = false
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        expanded = expanded,
        onExpandedChange = {expanded = it},
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        content = {},
        colors = SearchBarDefaults.colors(
            containerColor = Color(0xFFBB86FC),
            dividerColor = Color(0xFFBB86FC)
        )
    )
}


@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen(navController = rememberNavController())
}

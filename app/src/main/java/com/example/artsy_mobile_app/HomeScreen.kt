package com.example.artsy_mobile_app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavHostController) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            backStackEntry.savedStateHandle.get<String>("snackbar")?.let { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short)
                    backStackEntry.savedStateHandle.remove<String>("snackbar")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                    MainContent(navController)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState)
{
    var menuExpanded by remember { mutableStateOf(false) }
    val logoutViewModel: LogoutViewModel = viewModel()
    val deleteAccountViewModel: DeleteAccountViewModel = viewModel()
//    val logoutState by logoutViewModel.logoutState.collectAsState()
//    val registerState by deleteAccountViewModel.deleteState.collectAsState()
    val context = LocalContext.current

    TopAppBar(
        title = {
            Text(
                text = "Artist Search",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
                IconButton(onClick = { navController.navigate("search") }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            if (UserSessionManager.isLoggedIn()) {
                val avatarUrl = UserSessionManager.getUser()?.avatar ?: ""

                Column {
                    Image(
                        painter = rememberAsyncImagePainter(avatarUrl),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable { menuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Log Out") },
                            onClick = {
                                menuExpanded = false
                                UserSessionManager.clearSession()
                                logoutViewModel.logout(context)
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }.also {
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("snackbar", "Logged out successfully")
                                }
//                                coroutineScope.launch {
//                                    snackbarHostState.showSnackbar("Logged out successfully!")
//                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Delete Account",
                                color = MaterialTheme.colorScheme.error
                                )
                                   },
                            onClick = {
                                menuExpanded = false
                                deleteAccountViewModel.deleteAccount(context)
                                navController.navigate("home"){
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }.also{
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("snackbar", "Deleted user successfully")
                                }
//                                coroutineScope.launch {
//                                    snackbarHostState.showSnackbar("Account deleted!")
//                                }
                                UserSessionManager.clearSession()
                            }
                        )
                    }
                }
            } else {
                IconButton(onClick = { navController.navigate("login") }) {
                    Icon(Icons.Outlined.Person, contentDescription = "User")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}



@Composable
fun MainContent(navController: NavHostController) {

    val isLoggedIn = UserSessionManager.isLoggedIn()
    val email = UserSessionManager.getUser()?.email ?: ""
    val getFavArtistViewModel: FavoriteArtistViewModel = viewModel()

    val favArtistState = getFavArtistViewModel.getFavoritesState

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) getFavArtistViewModel.fetchFavorites(email)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // Today's Date
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = getCurrentDate(),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Favorite
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        when {
            !isLoggedIn -> {
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.padding(horizontal = 110.dp)
                ) {
                    Text("Login to see favorites")
                }
            }
            favArtistState is GetFavoriteArtistState.Loading -> LoadingIndicator()

            favArtistState is GetFavoriteArtistState.Success -> {
                LazyColumn {
                    items(favArtistState.results.size) { index ->
                        val artist = favArtistState.results[index]
                        FavoriteArtistItem(
                            artist = artist,
                            onClick = { navController.navigate("artistDetails/${artist.favId}")}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            favArtistState is GetFavoriteArtistState.Error -> {
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
                    ){
                        Text(
                            text = "No Favorites",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Footer()
    }
}
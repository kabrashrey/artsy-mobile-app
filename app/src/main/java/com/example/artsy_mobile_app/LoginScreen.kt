package com.example.artsy_mobile_app

import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton

import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import android.util.Log


@Composable
fun LoginScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
){
    Scaffold(
        topBar = {
            LoginTopBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                LoginMainContent(navController, snackbarHostState, scope)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start, // Align items to the start
                verticalAlignment = Alignment.CenterVertically // Center vertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
    ),
)}

@Composable
fun LoginMainContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
){
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel()
    val loginState by viewModel.loginState.collectAsState()

    Log.i("LoginScreen", "loginState: $loginState")

    fun clearFieldErrors() {
        emailError = null
        passwordError = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Email and Password Fields
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = if (it.isBlank()) "Email cannot be empty" else null
                clearFieldErrors()
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && email.isBlank()) {
                        emailError =  "Email cannot be empty"
                    }
                },
            isError = emailError != null
        )
        if (emailError != null){
            Text(
                text = emailError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = if (it.isBlank()) "Password cannot be empty" else null
                clearFieldErrors()
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && password.isBlank()) {
                        passwordError = "Password cannot be empty"
                    }
                },
            isError = passwordError != null
        )
        if (passwordError != null){
            Text(
                text = passwordError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                var valid = true
                if (email.isBlank()){
                    emailError = "Email cannot be empty"
                    valid = false
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailError = "Invalid email format"
                    valid = false
                }
                if (password.isBlank()){
                    passwordError = "Password cannot be empty"
                    valid = false
                }
                if(valid) {
                    viewModel.login(context, email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is LoginState.Loading
        ) {
            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
            }
        }

        when (loginState) {
            is LoginState.Error -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Username or password is incorrect",
                    color = MaterialTheme.colorScheme.error
                )
            }

            is LoginState.Success -> {
                LaunchedEffect(Unit) {
                    email = ""
                    password = ""
                    scope.launch {
                        snackbarHostState.showSnackbar("Logged in successfully")
                    }
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                    viewModel.resetState()
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an account yet?",
                modifier = Modifier.alignByBaseline()
            )
            TextButton(
                onClick = { navController.navigate("register") },
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.alignByBaseline()
            ) {
                Text(text = "Register",
                )
            }
        }
    }
    }


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LoginScreen(
        navController = rememberNavController(),
        snackbarHostState = snackbarHostState,
        scope = scope)
}


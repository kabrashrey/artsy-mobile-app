package com.example.artsy_mobile_app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.navigation.compose.*
import androidx.navigation.NavHostController

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
){
    Scaffold(
        topBar = {
            RegisterTopBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                RegisterMainContent(navController, snackbarHostState, scope)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTopBar(navController: NavHostController) {
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
                    text = "Register",
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
fun RegisterMainContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
){
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var fullNameError by remember { mutableStateOf<String?>(null) }

    val registerViewModel: RegisterViewModel = viewModel()
    val registerState by registerViewModel.registerState.collectAsState()


    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Full Name, Email and Password Fields
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                fullNameError = null
            },
            label = { Text("Enter full name") },
            modifier = Modifier.fillMaxWidth(),
            isError = fullNameError != null
        )
        if (fullNameError != null){
            Text(
                text = fullNameError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Enter Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null
        )
        if (emailError != null){
            Text(
                text = emailError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null
        )
        if (passwordError != null){
            Text(
                text = passwordError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                if (fullName.isBlank()){
                    fullNameError = "Full name cannot be empty"
                    valid = false
                }
                if(valid) { registerViewModel.registerUser(context, fullName, email, password) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = registerState !is RegisterState.Loading
        ) {
            if(registerState is RegisterState.Loading){
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Register")
            }
        }

        when(registerState) {
            is RegisterState.Error -> {
                Text(
                    text = "Username or password is incorrect",
                    color = MaterialTheme.colorScheme.error
                )
            }

            is RegisterState.Success -> {
                LaunchedEffect(Unit) {
                    email = ""
                    password = ""
                    scope.launch {
                        snackbarHostState.showSnackbar("Logged in successfully")
                    }
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
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
            Text(text = "Already have an account?",
                modifier = Modifier.alignByBaseline()
            )
            TextButton(
                onClick = { navController.popBackStack() },
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.alignByBaseline()
            ) {
                Text(text = "Login",
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    RegisterScreen(
        navController = rememberNavController(),
        snackbarHostState = snackbarHostState,
        scope = scope)
}


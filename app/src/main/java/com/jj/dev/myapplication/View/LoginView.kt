package com.jj.dev.myapplication.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import com.jj.dev.myapplication.viewmodel.LoginUiState



@Composable
fun LoginView(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel()
) {
    val loginState by authenticationViewModel.loginState.observeAsState(LoginUiState.Idle)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Email & pass inputs
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Trigger login
        Button(onClick = {
            authenticationViewModel.login(email.trim(), password)
        }) {
            Text("Login")
        }

        when (loginState) {
            LoginUiState.Idle -> { /*no-op*/ }
            LoginUiState.Loading -> {
                Text("Logging in…")
            }
            is LoginUiState.Error -> {
                Text((loginState as LoginUiState.Error).message, color = Color.Red)
            }
            is LoginUiState.Success -> {
                val (user, role) = loginState as LoginUiState.Success

                // THIS LaunchedEffect only runs once, immediately after Success
                LaunchedEffect(loginState) {
                    val dest = when (role) {
                        "Customer" -> "customer_home_screen"
                        "Admin"    -> "admin_home_screen"
                        "Mechanic" -> "mechanic_home_screen"
                        else       -> "login_screen"
                    }
                    navController.navigate(dest) {
                        popUpTo("login_screen") { inclusive = true }
                    }
                }

                Text("Welcome, ${user.email}", color = Color.Green)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Don't have an account? Register", color = Color.Blue,
            modifier = Modifier.clickable {
                navController.navigate("register_screen")
            })
    }
}

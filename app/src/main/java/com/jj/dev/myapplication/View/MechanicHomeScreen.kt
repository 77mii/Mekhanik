package com.jj.dev.myapplication.View

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController



import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MechanicHomeScreen(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel()
) {
    // Observe logout result (optional – you can show a toast/snackbar on failure)
    val logoutSuccess by authenticationViewModel.logoutResult.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, Mechanic",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Your mechanic‐specific UI goes here…

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        Button(
            onClick = {
                // Trigger the logout call
                authenticationViewModel.logout()

                // Immediately navigate back to login and clear back stack
                navController.navigate("login_screen") {
                    popUpTo(0) // removes everything from backstack
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Logout")
        }

        // Optional: Show feedback if logout fails
        logoutSuccess?.let { success ->
            if (!success) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Logout failed—please try again.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

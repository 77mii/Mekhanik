package com.jj.dev.myapplication.View

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel

/*@Composable
fun AdminHomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Welcome, Admin", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = { navController.navigate("admin_appointments_list") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View All Appointments")
        }
        // Add other admin-specific functionality buttons here
    }
}*/

@Composable
fun AdminHomeScreen(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel() // Inject AuthenticationViewModel
) {
    val logoutResult by authenticationViewModel.logoutResult.observeAsState()

    // Navigate to login screen after successful logout
    LaunchedEffect(logoutResult) {
        if (logoutResult == true) {
            // Reset the LiveData to prevent re-navigation on config change
            // (Though ViewModel might handle this internally or be cleared)
            // For robustness, ensure navigation logic is clear.
            navController.navigate("login_screen") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true } // Clear back stack to the start of the graph
                launchSingleTop = true // Avoid multiple instances of login screen
            }
            // Optionally, reset logoutResult in ViewModel if it's a one-shot event type
            // For now, AuthenticationViewModel clears its states internally on logout success.
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Pushes logout button to bottom
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome, Admin", style = MaterialTheme.typography.headlineSmall)

            Button(
                onClick = { navController.navigate("admin_appointments_list") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View All Appointments")
            }
            // Add other admin-specific functionality buttons here
        }

        Button(
            onClick = { authenticationViewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add some padding at the bottom
        ) {
            Text("Logout")
        }
    }
}

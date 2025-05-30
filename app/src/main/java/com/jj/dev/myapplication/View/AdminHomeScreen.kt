package com.jj.dev.myapplication.View

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
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
}

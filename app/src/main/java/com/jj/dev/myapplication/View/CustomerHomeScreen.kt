package com.jj.dev.myapplication.View

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.jj.dev.myapplication.viewmodel.StartupState
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import com.jj.dev.myapplication.viewmodel.CustomerViewModel
import com.jj.dev.myapplication.viewmodel.CustomerProfileState
import com.jj.dev.myapplication.viewmodel.StartupState as AuthStartupState


@Composable
fun CustomerHomeScreen(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val authStartupState by authenticationViewModel.startupState.observeAsState()
    val currentFirebaseUser by authenticationViewModel.currentFirebaseUser.observeAsState()

    val customerProfileState by customerViewModel.customerProfileState.observeAsState()
    val customerVehicles by customerViewModel.customerVehicles.observeAsState()
    val customerProfile by customerViewModel.customerProfile.observeAsState()


    LaunchedEffect(authStartupState, currentFirebaseUser) {
        val authState = authStartupState
        val fbUser = currentFirebaseUser
        if (authState is AuthStartupState.Authenticated && authState.role == "Customer" && fbUser != null) {
            if (customerProfileState !is CustomerProfileState.Loading && customerProfileState !is CustomerProfileState.Loaded) {
                Log.d("CustomerHomeScreen", "Auth state is Customer. Loading customer profile for ${fbUser.uid}")
                customerViewModel.loadCustomerProfile(fbUser.uid)
            }
        } else if (authState is AuthStartupState.Unauthenticated) {
            customerViewModel.clearCustomerData()
        }
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val displayName = customerProfile?.name ?: currentFirebaseUser?.email ?: "Guest"
        Text("Welcome, $displayName", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        when (val profileState = customerProfileState) {
            is CustomerProfileState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 8.dp))
                Text("Loading your profile...", style = MaterialTheme.typography.bodySmall)
            }
            is CustomerProfileState.Loaded -> {
                if (profileState.customer != null) {
                    Text("You have ${customerVehicles?.size ?: 0} vehicle(s) registered.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 16.dp))
                } else if (authStartupState is AuthStartupState.Authenticated && (authStartupState as AuthStartupState.Authenticated).role == "Customer"){
                    Text("Could not load detailed customer profile.", color = Color.DarkGray, modifier = Modifier.padding(bottom = 16.dp))
                }
            }
            is CustomerProfileState.Error -> Text("Error loading profile: ${profileState.message}", color = Color.Red, modifier = Modifier.padding(bottom = 16.dp))
            else -> {
                if (authStartupState is AuthStartupState.Authenticated && (authStartupState as AuthStartupState.Authenticated).role == "Customer") {
                    Text("Profile not loaded yet.", modifier = Modifier.padding(bottom = 16.dp))
                }
            }
        }

        Button(onClick = { navController.navigate("booking_screen") }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) { Text("Book Service") }
        Button(
            onClick = { navController.navigate("customer_appointments_list") }, // Updated route
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) { Text("View Appointments") }
        Button(onClick = { navController.navigate("customer_profile_screen") }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) { Text("View Profile") }
        Button(
            onClick = { Log.d("CustomerHomeScreen", "Current vehicles in CustomerVM: ${customerViewModel.customerVehicles.value?.joinToString { it.vehicleModel }}") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) { Text("My Vehicles (Test Log)") }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { authenticationViewModel.logout() }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text("Logout") }

        val logoutResult by authenticationViewModel.logoutResult.observeAsState()
        LaunchedEffect(logoutResult) {
            if (logoutResult == true) {
                customerViewModel.clearCustomerData()
                navController.navigate("login_screen") { popUpTo(navController.graph.startDestinationId) { inclusive = true }; launchSingleTop = true }
            }
        }
    }
}
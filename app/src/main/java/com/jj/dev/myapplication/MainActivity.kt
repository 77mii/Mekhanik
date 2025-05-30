package com.jj.dev.myapplication







import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jj.dev.myapplication.View.*
import com.jj.dev.myapplication.ui.theme.MekhanikTheme
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import com.jj.dev.myapplication.viewmodel.StartupState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // TODO: Replace "YourAppTheme" with the actual theme composable from your project.
            MekhanikTheme  {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppEntry()
                }
            }
        }
    }
}

@Composable
fun AppEntry(
    authenticationViewModel: AuthenticationViewModel = hiltViewModel()
) {
    val startupState by authenticationViewModel.startupState.observeAsState()
    val navController = rememberNavController()
    val finalStartupState = startupState ?: StartupState.Loading

    when (finalStartupState) {
        is StartupState.Loading -> {
            FullScreenLoadingIndicator()
        }
        else -> {
            val startDest = when (finalStartupState) {
                is StartupState.Unauthenticated -> "login_screen"
                is StartupState.Authenticated -> when (finalStartupState.role) {
                    "Admin"    -> "admin_home_screen"
                    "Mechanic" -> "mechanic_home_screen"
                    "Customer" -> "customer_home_screen"
                    else       -> "login_screen"
                }
                is StartupState.Error -> "login_screen"
                else -> "login_screen"
            }
            MainAppNavHost(navController = navController, startDestination = startDest)
        }
    }
}

@Composable
fun FullScreenLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


@Composable
fun MainAppNavHost(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login_screen") { LoginView(navController = navController) }
        composable("register_screen") { RegisterView(navController = navController) }
        composable("customer_home_screen") { CustomerHomeScreen(navController = navController) }
        composable("admin_home_screen") { AdminHomeScreen(navController = navController) }
        composable("mechanic_home_screen") { MechanicHomeScreen(navController = navController) }
        composable("booking_screen") { BookingScreen(navController = navController) }
        composable("customer_appointments_list") {
            CustomerAppointmentsView(navController = navController)
        }
        composable(
            route = "customer_appointment_detail/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            CustomerAppointmentDetailView(
                navController = navController,
                appointmentIdFromNav = backStackEntry.arguments?.getString("appointmentId")
            )
        }
        // Define other routes like "customer_profile_screen" etc.
        // Admin Appointment Routes
        composable("admin_appointments_list") {
            AdminAppointmentListView(navController = navController)
        }
        composable(
            route = "admin_appointment_detail/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            AdminAppointmentDetailView(
                navController = navController,
                appointmentIdFromNav = backStackEntry.arguments?.getString("appointmentId")
            )
        }
    }
}


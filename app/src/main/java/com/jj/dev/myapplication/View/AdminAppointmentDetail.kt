package com.jj.dev.myapplication.View

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jj.dev.myapplication.Model.AppointmentStatusEnum
import com.jj.dev.myapplication.viewmodel.AppointmentViewModel
import com.jj.dev.myapplication.viewmodel.CustomerViewModel // To fetch customer/vehicle details
import com.jj.dev.myapplication.viewmodel.CustomerProfileState
import com.jj.dev.myapplication.viewmodel.SingleAppointmentLoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentDetailView(
    navController: NavHostController,
    appointmentIdFromNav: String?,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel() // For fetching customer/vehicle details
) {
    val appointmentToDisplay by appointmentViewModel.selectedAppointment.observeAsState()
    val singleAppointmentLoadState by appointmentViewModel.singleAppointmentLoadState.observeAsState(SingleAppointmentLoadState.Idle)

    // Observe customer profile to get vehicle details if needed
    val customerProfile by customerViewModel.customerProfile.observeAsState()
    val customerProfileLoadingState by customerViewModel.customerProfileState.observeAsState()


    LaunchedEffect(key1 = appointmentIdFromNav) {
        if (!appointmentIdFromNav.isNullOrBlank()) {
            if (appointmentViewModel.selectedAppointment.value?.appointmentId != appointmentIdFromNav ||
                (appointmentToDisplay == null && singleAppointmentLoadState == SingleAppointmentLoadState.Idle) ||
                singleAppointmentLoadState is SingleAppointmentLoadState.NotFound ||
                singleAppointmentLoadState is SingleAppointmentLoadState.Error) {
                appointmentViewModel.loadSelectedAppointmentDetails(appointmentIdFromNav)
            }
        }
    }

    // Load customer profile once appointment (and thus userId) is available
    LaunchedEffect(appointmentToDisplay) {
        appointmentToDisplay?.userId?.let { userId ->
            if (userId.isNotBlank() && (customerProfile == null || customerProfile?.userId != userId) && customerProfileLoadingState !is CustomerProfileState.Loading) {
                Log.d("AdminApptDetailView", "Appointment selected, loading customer profile for user ID: $userId")
                customerViewModel.loadCustomerProfile(userId)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("AdminApptDetailView", "Disposing AdminAppointmentDetailView.")
            // appointmentViewModel.clearSelectedAppointment() // Optional: clear if desired
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details (Admin)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val isLoadingAppointment = appointmentToDisplay == null || singleAppointmentLoadState is SingleAppointmentLoadState.Loading
        val isLoadingCustomer = appointmentToDisplay != null && customerProfileLoadingState is CustomerProfileState.Loading && customerProfile?.userId != appointmentToDisplay?.userId

        if (isLoadingAppointment || isLoadingCustomer) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text(if(isLoadingAppointment) "Loading appointment..." else "Loading customer/vehicle info...")
            }
        } else if (appointmentToDisplay != null) {
            val appointment = appointmentToDisplay!!
            // Find vehicle from the loaded customer's vehicles list
            val vehicle = customerProfile?.vehicles?.find { it.vehicleId == appointment.vehicleId }

            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "Appointment ID:", value = appointment.appointmentId)
                DetailRow(label = "Date:", value = appointment.date)
                DetailRow(label = "Customer ID:", value = appointment.userId)
                customerProfile?.let { DetailRow(label = "Customer Name:", value = it.name) }
                Divider()
                Text("Vehicle Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DetailRow(label = "Model:", value = vehicle?.vehicleModel ?: "N/A")
                DetailRow(label = "Plate:", value = vehicle?.vehiclePlate ?: "N/A")
                DetailRow(label = "Vehicle ID:", value = appointment.vehicleId)
                Divider()
                Text("Service Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DetailRow(label = "Service Type:", value = appointment.serviceType ?: "Not specified")
                DetailRow(label = "Description:", value = appointment.description, isBlock = true)
                Divider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = appointment.appointmentStatus.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (appointment.appointmentStatus) {
                            AppointmentStatusEnum.Booked -> MaterialTheme.colorScheme.primary
                            AppointmentStatusEnum.InProgress -> Color(0xFFFFA500) // Orange
                            AppointmentStatusEnum.Completed -> Color(0xFF2E7D32) // Darker Green
                        }
                    )
                }
                // TODO: Add Admin specific actions here (e.g., change status, assign mechanic)
            }
        } else if (singleAppointmentLoadState is SingleAppointmentLoadState.NotFound) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Appointment not found.")
            }
        } else if (singleAppointmentLoadState is SingleAppointmentLoadState.Error) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Error loading appointment: ${(singleAppointmentLoadState as SingleAppointmentLoadState.Error).message}")
            }
        } else if (appointmentIdFromNav == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No appointment ID specified.")
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Please wait...") // Fallback
            }
        }
    }
}
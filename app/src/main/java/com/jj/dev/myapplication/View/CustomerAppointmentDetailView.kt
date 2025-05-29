package com.jj.dev.myapplication.View




/*
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
import com.jj.dev.myapplication.viewmodel.AppointmentsListState
import com.jj.dev.myapplication.viewmodel.CustomerViewModel
import com.jj.dev.myapplication.viewmodel.CustomerProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAppointmentDetailView(
    navController: NavHostController,
    appointmentIdFromNav: String?,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    // Primary source of truth for the appointment to display
    val appointmentToDisplay by appointmentViewModel.selectedAppointment.observeAsState()

    val customerVehicles by customerViewModel.customerVehicles.observeAsState()
    val customerProfileState by customerViewModel.customerProfileState.observeAsState()

    Log.d("CustAppDetailView", "Composing. Nav Arg ID: $appointmentIdFromNav. VM Selected Appt (observed): ${appointmentToDisplay?.appointmentId}")

    // This LaunchedEffect ensures that if the ViewModel's selectedAppointment isn't the one
    // we navigated for (e.g., after process death or if direct observation is slow),
    // we try to set it from the loaded list or trigger a specific load if necessary.
    LaunchedEffect(key1 = appointmentIdFromNav) { // Key only on appointmentIdFromNav
        Log.d("CustAppDetailView", "LaunchedEffect(appointmentIdFromNav) triggered. NavArg: $appointmentIdFromNav")
        if (!appointmentIdFromNav.isNullOrBlank()) {
            val currentSelectedInVm = appointmentViewModel.selectedAppointment.value
            if (currentSelectedInVm == null || currentSelectedInVm.appointmentId != appointmentIdFromNav) {
                Log.d("CustAppDetailView", "VM selected appointment is null or doesn't match nav arg. Trying to find from list or fetch.")
                // Try to find it in the existing list first
                val appointmentsListState = appointmentViewModel.customerAppointmentsState.value
                if (appointmentsListState is AppointmentsListState.Loaded) {
                    val appointmentFromList = appointmentsListState.appointments.find { it.appointmentId == appointmentIdFromNav }
                    if (appointmentFromList != null) {
                        Log.d("CustAppDetailView", "Found appointment $appointmentIdFromNav in loaded list. Selecting it in VM.")
                        appointmentViewModel.selectAppointment(appointmentFromList)
                    } else {
                        Log.w("CustAppDetailView", "Appointment $appointmentIdFromNav not found in loaded list. Consider fetching by ID if this is a deep link.")
                        // TODO: Future - Implement fetchAppointmentById(appointmentIdFromNav) in ViewModel if direct fetch is needed
                    }
                } else {
                    Log.w("CustAppDetailView", "Appointments list not loaded ($appointmentsListState). Cannot use as fallback for $appointmentIdFromNav. Relies on direct observation of selectedAppointment for now.")
                    // If the list isn't loaded, and selectedAppointment is null, the UI will show loading.
                    // This scenario is less common if navigating from the list view itself.
                }
            } else {
                Log.d("CustAppDetailView", "VM's selectedAppointment already matches NavArg ID $appointmentIdFromNav.")
            }
        }
    }

    // Load customer profile (for vehicle details) once we have an appointment to display
    LaunchedEffect(appointmentToDisplay) {
        appointmentToDisplay?.userId?.let { userId ->
            val currentProfile = customerViewModel.customerProfile.value
            val vehiclesLoaded = customerVehicles != null
            if (userId.isNotBlank() &&
                (customerProfileState !is CustomerProfileState.Loaded || currentProfile?.userId != userId || !vehiclesLoaded) &&
                customerProfileState !is CustomerProfileState.Loading
            ) {
                Log.d("CustAppDetailView", "Appointment details available (userId: $userId). Ensuring customer profile is loaded.")
                customerViewModel.loadCustomerProfile(userId)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("CustAppDetailView", "Disposing CustomerAppointmentDetailView.")
            // Clearing selectedAppointment here could be problematic if the user quickly navigates back
            // and then forward again to the same detail. Let ViewModel manage its state.
            // appointmentViewModel.clearSelectedAppointment()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (appointmentToDisplay == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Text("Loading appointment details...")
                Log.d("CustAppDetailView", "UI: Showing loading because appointmentToDisplay is null.")
            }
        } else {
            val appointment = appointmentToDisplay!!
            val vehicle = customerVehicles?.find { it.vehicleId == appointment.vehicleId }
            Log.d("CustAppDetailView", "UI: Displaying details for appointment: ${appointment.appointmentId}. Vehicle found: ${vehicle != null}")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "Appointment ID:", value = appointment.appointmentId)
                DetailRow(label = "Date:", value = appointment.date)
                Divider()
                Text("Vehicle Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DetailRow(label = "Model:", value = vehicle?.vehicleModel ?: (if(customerProfileState is CustomerProfileState.Loading && vehicle == null) "Loading vehicle..." else "N/A"))
                DetailRow(label = "Plate:", value = vehicle?.vehiclePlate ?: (if(customerProfileState is CustomerProfileState.Loading && vehicle == null) "" else "N/A"))
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
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBlock: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        if (isBlock) {
            Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
        } else {
            Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
        }
    }
}*/

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
import com.jj.dev.myapplication.viewmodel.CustomerViewModel
import com.jj.dev.myapplication.viewmodel.CustomerProfileState
import com.jj.dev.myapplication.viewmodel.SingleAppointmentLoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAppointmentDetailView(
    navController: NavHostController,
    appointmentIdFromNav: String?,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val appointmentToDisplay by appointmentViewModel.selectedAppointment.observeAsState()
    val singleAppointmentLoadState by appointmentViewModel.singleAppointmentLoadState.observeAsState(SingleAppointmentLoadState.Idle)

    val customerVehicles by customerViewModel.customerVehicles.observeAsState()
    val customerProfileState by customerViewModel.customerProfileState.observeAsState()

    Log.d("CustAppDetailView", "Composing. Nav Arg ID: $appointmentIdFromNav. Current VM Selected Appt: ${appointmentToDisplay?.appointmentId}. Load State: $singleAppointmentLoadState")

    // Trigger loading the specific appointment if ID is provided from nav
    // and it's not already selected or being loaded.
    LaunchedEffect(key1 = appointmentIdFromNav) {
        if (!appointmentIdFromNav.isNullOrBlank()) {
            Log.d("CustAppDetailView", "LaunchedEffect(appointmentIdFromNav): $appointmentIdFromNav. Current selected in VM: ${appointmentViewModel.selectedAppointment.value?.appointmentId}")
            // Only load if it's not already the selected one or if the state implies it needs loading
            if (appointmentViewModel.selectedAppointment.value?.appointmentId != appointmentIdFromNav ||
                (appointmentToDisplay == null && singleAppointmentLoadState == SingleAppointmentLoadState.Idle) ||
                singleAppointmentLoadState is SingleAppointmentLoadState.NotFound ||
                singleAppointmentLoadState is SingleAppointmentLoadState.Error) { // Added error/notfound check
                Log.d("CustAppDetailView", "Calling loadSelectedAppointmentDetails for ID: $appointmentIdFromNav")
                appointmentViewModel.loadSelectedAppointmentDetails(appointmentIdFromNav)
            }
        } else {
            Log.w("CustAppDetailView", "appointmentIdFromNav is null or blank.")
            // Handle case where no ID is passed - perhaps show an error or pop back.
            // For now, it will show loading indefinitely if appointmentToDisplay remains null.
        }
    }

    // Load customer profile for vehicle details once we have an appointment
    LaunchedEffect(appointmentToDisplay) {
        appointmentToDisplay?.userId?.let { userId ->
            val currentProfile = customerViewModel.customerProfile.value
            val vehiclesLoaded = customerVehicles != null
            if (userId.isNotBlank() &&
                (customerProfileState !is CustomerProfileState.Loaded || currentProfile?.userId != userId || !vehiclesLoaded) &&
                customerProfileState !is CustomerProfileState.Loading
            ) {
                Log.d("CustAppDetailView", "Appointment available (userId: $userId). Ensuring customer profile for vehicle details is loaded.")
                customerViewModel.loadCustomerProfile(userId)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("CustAppDetailView", "Disposing CustomerAppointmentDetailView.")
            // Optionally clear selected appointment if navigating "up" means it should be reset
            // appointmentViewModel.clearSelectedAppointment()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Determine overall loading state
        val isLoading = appointmentToDisplay == null || (singleAppointmentLoadState is SingleAppointmentLoadState.Loading)

        if (isLoading && appointmentIdFromNav != null) { // Show loading only if we expect data
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Text("Loading appointment details...")
                Log.d("CustAppDetailView", "UI: Showing loading. appointmentToDisplay is ${if(appointmentToDisplay == null) "null" else "not null"}. singleAppointmentLoadState is $singleAppointmentLoadState")
            }
        } else if (appointmentToDisplay != null) {
            val appointment = appointmentToDisplay!!
            val vehicle = customerVehicles?.find { it.vehicleId == appointment.vehicleId }
            Log.d("CustAppDetailView", "UI: Displaying details for appointment: ${appointment.appointmentId}. Vehicle found: ${vehicle != null}")

            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "Appointment ID:", value = appointment.appointmentId)
                DetailRow(label = "Date:", value = appointment.date)
                Divider()
                Text("Vehicle Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DetailRow(label = "Model:", value = vehicle?.vehicleModel ?: (if(customerProfileState is CustomerProfileState.Loading && vehicle == null) "Loading vehicle..." else "N/A"))
                DetailRow(label = "Plate:", value = vehicle?.vehiclePlate ?: (if(customerProfileState is CustomerProfileState.Loading && vehicle == null) "" else "N/A"))
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
            // Handle case where no ID was passed and nothing is selected
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No appointment selected or specified.")
            }
        }
        else {
            // Fallback for any other unhandled state, though should be covered
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Please wait...")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBlock: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        if (isBlock) {
            Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
        } else {
            Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
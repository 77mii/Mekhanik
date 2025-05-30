package com.jj.dev.myapplication.View

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.jj.dev.myapplication.Model.Appointment
import com.jj.dev.myapplication.Model.AppointmentStatusEnum

import com.jj.dev.myapplication.viewmodel.CustomerViewModel
import com.jj.dev.myapplication.viewmodel.AppointmentViewModel
import com.jj.dev.myapplication.viewmodel.AppointmentsListState



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentListView(
    navController: NavHostController,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel() // Inject CustomerViewModel
) {
    val allAppointmentsState by appointmentViewModel.allAppointmentsState.observeAsState(AppointmentsListState.Idle)
    val cachedUserProfiles by customerViewModel.cachedUserProfiles.observeAsState(emptyMap())

    LaunchedEffect(Unit) { // Load all appointments on entry
        Log.d("AdminApptListView", "Loading all appointments.")
        if (allAppointmentsState == AppointmentsListState.Idle || allAppointmentsState is AppointmentsListState.Error) {
            appointmentViewModel.loadAllAppointments()
        }
    }

    // When appointments are loaded, fetch profiles for their user IDs
    LaunchedEffect(allAppointmentsState) {
        if (allAppointmentsState is AppointmentsListState.Loaded) {
            val appointments = (allAppointmentsState as AppointmentsListState.Loaded).appointments
            val userIdsToFetch = appointments.map { it.userId }.distinct()
            userIdsToFetch.forEach { userId ->
                if (userId.isNotBlank()) {
                    Log.d("AdminApptListView", "Requesting profile for user ID: $userId")
                    customerViewModel.fetchAndCacheCustomerProfileIfNeeded(userId)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("AdminApptListView", "Disposing. Clearing all appointments list state from VM.")
            appointmentViewModel.clearAllAppointmentsListState()
            // Consider if cachedUserProfiles should be cleared here or managed differently
            // customerViewModel.clearAllCachedProfiles()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All System Appointments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = allAppointmentsState) {
                is AppointmentsListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(); Text("Loading all appointments...")
                    }
                }
                is AppointmentsListState.Loaded -> {
                    if (state.appointments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No appointments found in the system.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.appointments) { appointment ->
                                val customerName = cachedUserProfiles[appointment.userId]?.name ?: "Loading..."
                                AdminAppointmentListItem(
                                    appointment = appointment,
                                    customerName = customerName, // Pass customer name
                                    onClick = {
                                        appointmentViewModel.selectAppointment(appointment)
                                        navController.navigate("admin_appointment_detail/${appointment.appointmentId}")
                                    }
                                )
                            }
                        }
                    }
                }
                is AppointmentsListState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No appointments in the system.")
                    }
                }
                is AppointmentsListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading appointments: ${state.message}")
                    }
                }
                is AppointmentsListState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(); Text("Initializing...")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAppointmentListItem(appointment: Appointment, customerName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date: ${appointment.date}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Customer: $customerName (ID: ${appointment.userId})", // Show customer name
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Vehicle ID: ${appointment.vehicleId}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${appointment.appointmentStatus}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = when (appointment.appointmentStatus) {
                    AppointmentStatusEnum.Booked -> MaterialTheme.colorScheme.primary
                    AppointmentStatusEnum.InProgress -> Color(0xFFFFA500) // Orange
                    AppointmentStatusEnum.Completed -> Color(0xFF2E7D32) // Darker Green
                }
            )
        }
    }
}
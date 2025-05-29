package com.jj.dev.myapplication.View


/*
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
import com.jj.dev.myapplication.Model.Vehicle
import com.jj.dev.myapplication.viewmodel.AppointmentViewModel
import com.jj.dev.myapplication.viewmodel.AppointmentsListState
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import com.jj.dev.myapplication.viewmodel.CustomerViewModel
import com.jj.dev.myapplication.viewmodel.CustomerProfileState
import com.jj.dev.myapplication.Model.AppointmentStatusEnum


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAppointmentsView(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val currentFirebaseUser by authenticationViewModel.currentFirebaseUser.observeAsState()
    val appointmentsState by appointmentViewModel.customerAppointmentsState.observeAsState(AppointmentsListState.Idle)
    val customerVehicles by customerViewModel.customerVehicles.observeAsState()
    val customerProfileState by customerViewModel.customerProfileState.observeAsState()


    LaunchedEffect(currentFirebaseUser) {
        currentFirebaseUser?.uid?.let { userId ->
            if (userId.isNotBlank()) {
                Log.d("CustomerAppointmentsView", "User ID available: $userId.")
                // Load appointments only if in Idle state or an error occurred, to allow manual refresh later if needed.
                if (appointmentsState == AppointmentsListState.Idle || appointmentsState is AppointmentsListState.Error) {
                    Log.d("CustomerAppointmentsView", "Loading appointments for user: $userId")
                    appointmentViewModel.loadCustomerAppointments(userId)
                }
                // Ensure customer profile (and thus vehicles) is loaded
                if (customerProfileState !is CustomerProfileState.Loaded && customerProfileState !is CustomerProfileState.Loading) {
                    Log.d("CustomerAppointmentsView", "Customer profile not loaded. Loading for user: $userId")
                    customerViewModel.loadCustomerProfile(userId)
                }
            }
        }
    }

    // DisposableEffect to clear selected appointment when navigating away from the LIST screen,
    // ensuring detail screen doesn't show stale data if re-entered without selection.
    // However, clearing the *list* state here was problematic.
    DisposableEffect(Unit) {
        onDispose {
            Log.d("CustomerAppointmentsView", "Disposing CustomerAppointmentsView.")
            // DO NOT clear _customerAppointmentsState here, as it might be needed if user navigates back.
            // It will be cleared if user logs out or navigates far away.
            // Consider clearing _selectedAppointment if navigating away from the appointments section entirely.
            // appointmentViewModel.clearSelectedAppointment() // Might be too aggressive here.
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Appointments") },
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
            if (customerProfileState is CustomerProfileState.Loading && appointmentsState is AppointmentsListState.Loaded) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text("Loading vehicle details...")
                    }
                }
            } else {
                when (val state = appointmentsState) {
                    is AppointmentsListState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(); Text("Loading appointments...")
                        }
                    }
                    is AppointmentsListState.Loaded -> {
                        if (state.appointments.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("You have no appointments.")
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(state.appointments) { appointment ->
                                    val vehicle = customerVehicles?.find { it.vehicleId == appointment.vehicleId }
                                    AppointmentItem(
                                        appointment = appointment,
                                        vehicle = vehicle,
                                        onClick = {
                                            appointmentViewModel.selectAppointment(appointment)
                                            navController.navigate("customer_appointment_detail/${appointment.appointmentId}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is AppointmentsListState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("You have no appointments booked yet.")
                        }
                    }
                    is AppointmentsListState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error loading appointments: ${state.message}")
                        }
                    }
                    is AppointmentsListState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                            Text("Initializing...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: Appointment, vehicle: Vehicle?, onClick: () -> Unit) {
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

                val vehicleModelText = vehicle?.vehicleModel ?: "Vehicle ID"
                val vehicleIdentifierText = vehicle?.vehiclePlate?.takeIf { it.isNotBlank() } ?: appointment.vehicleId
                Text(
                    text = "$vehicleModelText ($vehicleIdentifierText)",
                    style = MaterialTheme.typography.bodyLarge
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
}*/

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
import com.jj.dev.myapplication.Model.Vehicle
import com.jj.dev.myapplication.viewmodel.AppointmentViewModel
import com.jj.dev.myapplication.viewmodel.AppointmentsListState
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import com.jj.dev.myapplication.viewmodel.CustomerViewModel
import com.jj.dev.myapplication.viewmodel.CustomerProfileState
import com.jj.dev.myapplication.Model.AppointmentStatusEnum


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAppointmentsView(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val currentFirebaseUser by authenticationViewModel.currentFirebaseUser.observeAsState()
    val appointmentsState by appointmentViewModel.customerAppointmentsState.observeAsState(AppointmentsListState.Idle)
    val customerVehicles by customerViewModel.customerVehicles.observeAsState()
    val customerProfileState by customerViewModel.customerProfileState.observeAsState()

    // Load data when currentFirebaseUser is available
    LaunchedEffect(currentFirebaseUser) {
        currentFirebaseUser?.uid?.let { userId ->
            if (userId.isNotBlank()) {
                Log.d("CustomerAppointmentsView", "User ID available: $userId.")
                // Load appointments only if in Idle state or an error occurred.
                if (appointmentsState == AppointmentsListState.Idle || appointmentsState is AppointmentsListState.Error) {
                    Log.d("CustomerAppointmentsView", "Triggering loadCustomerAppointments for user: $userId")
                    appointmentViewModel.loadCustomerAppointments(userId)
                }
                // Ensure customer profile (and thus vehicles) is loaded
                if (customerProfileState !is CustomerProfileState.Loaded && customerProfileState !is CustomerProfileState.Loading) {
                    Log.d("CustomerAppointmentsView", "Customer profile not loaded. Loading for user: $userId")
                    customerViewModel.loadCustomerProfile(userId)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("CustomerAppointmentsView", "Disposing CustomerAppointmentsView.")
            // It's generally better not to clear the list state here if the user might navigate back.
            // The ViewModel should manage the lifecycle of this data.
            // appointmentViewModel.clearAppointmentsListState() // Keep commented unless specific need
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Appointments") },
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
            // Combined loading state check
            val showLoading = appointmentsState is AppointmentsListState.Loading ||
                    (appointmentsState is AppointmentsListState.Idle && currentFirebaseUser != null) ||
                    (customerProfileState is CustomerProfileState.Loading && appointmentsState is AppointmentsListState.Loaded)

            if (showLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        if (appointmentsState is AppointmentsListState.Loading || appointmentsState is AppointmentsListState.Idle) {
                            Text("Loading appointments...")
                        } else if (customerProfileState is CustomerProfileState.Loading) {
                            Text("Loading vehicle details...")
                        }
                    }
                }
            } else {
                when (val state = appointmentsState) {
                    is AppointmentsListState.Loaded -> {
                        if (state.appointments.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("You have no appointments.")
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(state.appointments) { appointment ->
                                    val vehicle = customerVehicles?.find { it.vehicleId == appointment.vehicleId }
                                    AppointmentItem(
                                        appointment = appointment,
                                        vehicle = vehicle,
                                        onClick = {
                                            Log.d("CustomerAppointmentsView", "Appointment clicked: ${appointment.appointmentId}")
                                            appointmentViewModel.selectAppointment(appointment) // This is key
                                            navController.navigate("customer_appointment_detail/${appointment.appointmentId}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is AppointmentsListState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("You have no appointments booked yet.")
                        }
                    }
                    is AppointmentsListState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error loading appointments: ${state.message}")
                        }
                    }
                    is AppointmentsListState.Idle -> {
                        if (currentFirebaseUser == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Please log in to see appointments.")
                            }
                        } else {
                            // This case should ideally be covered by the 'showLoading' condition above
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                                Text("Initializing...")
                            }
                        }
                    }

                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Unexpected state: $state")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: Appointment, vehicle: Vehicle?, onClick: () -> Unit) {
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

                val vehicleModelText = vehicle?.vehicleModel ?: "Vehicle ID"
                val vehicleIdentifierText = vehicle?.vehiclePlate?.takeIf { it.isNotBlank() } ?: appointment.vehicleId
                Text(
                    text = "$vehicleModelText ($vehicleIdentifierText)",
                    style = MaterialTheme.typography.bodyLarge
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
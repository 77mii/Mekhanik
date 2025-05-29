

package com.jj.dev.myapplication.View








import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
// import android.widget.Toast // Toast for general errors might still be useful or replaced by dialogs
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jj.dev.myapplication.Model.Appointment
import com.jj.dev.myapplication.Model.Vehicle
import com.jj.dev.myapplication.viewmodel.AppointmentViewModel
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import com.jj.dev.myapplication.viewmodel.CustomerViewModel
import com.jj.dev.myapplication.viewmodel.BookingUiStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel()
) {
    Log.d("BookingScreen", "Composing BookingScreen.")

    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val vehicles by customerViewModel.customerVehicles.observeAsState(initial = emptyList())
    var selectedVehicle: Vehicle? by remember { mutableStateOf(null) }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

    val bookingUiStatus by appointmentViewModel.bookingUiStatus.observeAsState(BookingUiStatus.Idle)
    val currentFirebaseUser by authenticationViewModel.currentFirebaseUser.observeAsState()

    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictingAppointmentDetails: Appointment? by remember { mutableStateOf(null) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successfulAppointmentDetails: Appointment? by remember { mutableStateOf(null) }


    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember(context, calendar) {
        DatePickerDialog(context, { _, y, m, d ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.set(y, m, d); selectedDate = sdf.format(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.minDate = System.currentTimeMillis() - 1000 }
    }

    LaunchedEffect(bookingUiStatus) {
        when (val status = bookingUiStatus) {
            is BookingUiStatus.Success -> {
                successfulAppointmentDetails = status.newAppointment // Store the new appointment
                showSuccessDialog = true
                // Don't pop backstack or reset status immediately, let dialog handle it
            }
            is BookingUiStatus.Error -> {
                if (status.conflictingAppointment != null) {
                    conflictingAppointmentDetails = status.conflictingAppointment
                    showConflictDialog = true
                } else {
                    // For generic errors, you might still use a Toast or another dialog
                    Toast.makeText(context, "Booking failed: ${status.message}", Toast.LENGTH_LONG).show()
                }
                // Reset status only if it's not a conflict dialog being shown,
                // as the dialog will handle resetting.
                if (status.conflictingAppointment == null) {
                    appointmentViewModel.resetBookingStatus()
                }
            }
            else -> {} // Idle or Loading
        }
    }

    // Dialog for Booking Conflict
    if (showConflictDialog && conflictingAppointmentDetails != null) {
        AlertDialog(
            onDismissRequest = {
                showConflictDialog = false
                appointmentViewModel.resetBookingStatus()
            },
            title = { Text("Booking Conflict") },
            text = { Text("This vehicle already has an active appointment. Go to appointment details?") },
            confirmButton = {
                Button(onClick = {
                    conflictingAppointmentDetails?.let {
                        appointmentViewModel.selectAppointment(it)
                        navController.navigate("customer_appointment_detail/${it.appointmentId}")
                    }
                    showConflictDialog = false
                    appointmentViewModel.resetBookingStatus()
                }) { Text("Yes") }
            },
            dismissButton = {
                Button(onClick = {
                    showConflictDialog = false
                    appointmentViewModel.resetBookingStatus()
                    navController.navigate("customer_home_screen") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }) { Text("No") }
            }
        )
    }

    // Dialog for Booking Success
    if (showSuccessDialog && successfulAppointmentDetails != null) {
        AlertDialog(
            onDismissRequest = { // If dismissed by clicking outside or back button
                showSuccessDialog = false
                appointmentViewModel.resetBookingStatus()
                navController.popBackStack() // Go back from booking screen
            },
            title = { Text("Booking Successful") },
            text = { Text("Your appointment has been booked. See details?") },
            confirmButton = {
                Button(onClick = {
                    successfulAppointmentDetails?.let {
                        appointmentViewModel.selectAppointment(it)
                        navController.navigate("customer_appointment_detail/${it.appointmentId}") {
                            // Pop booking screen off the stack as we are navigating forward
                            popUpTo("booking_screen") { inclusive = true }
                        }
                    }
                    showSuccessDialog = false
                    appointmentViewModel.resetBookingStatus()
                }) { Text("Yes, See Details") }
            },
            dismissButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    appointmentViewModel.resetBookingStatus()
                    navController.popBackStack() // Go back from booking screen
                }) { Text("No, Thanks") }
            }
        )
    }


    LaunchedEffect(currentFirebaseUser) {
        currentFirebaseUser?.uid?.let { uid ->
            if (customerViewModel.customerVehicles.value == null && customerViewModel.customerProfileState.value !is com.jj.dev.myapplication.viewmodel.CustomerProfileState.Loading) {
                Log.d("BookingScreen", "Current user available, ensuring vehicles are loaded for $uid")
                customerViewModel.loadCustomerProfile(uid)
            }
        }
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Book Appointment", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = selectedDate, onValueChange = { }, label = { Text("Date") }, readOnly = true,
            trailingIcon = { Icon(Icons.Filled.DateRange, "Select Date", Modifier.clickable { datePickerDialog.show() }) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(expanded = vehicleDropdownExpanded && !vehicles.isNullOrEmpty(),
            onExpandedChange = { if (!vehicles.isNullOrEmpty()) vehicleDropdownExpanded = !vehicleDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(value = selectedVehicle?.let { "${it.vehicleModel} (${it.vehiclePlate})" } ?: "Select Vehicle",
                onValueChange = {}, readOnly = true, label = { Text("Vehicle") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded && !vehicles.isNullOrEmpty()) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = vehicleDropdownExpanded && !vehicles.isNullOrEmpty(), onDismissRequest = { vehicleDropdownExpanded = false }) {
                vehicles?.forEach { vehicle ->
                    DropdownMenuItem(text = { Text("${vehicle.vehicleModel} (${vehicle.vehiclePlate})") },
                        onClick = { selectedVehicle = vehicle; vehicleDropdownExpanded = false }
                    )
                }
                if (vehicles.isNullOrEmpty()) {
                    DropdownMenuItem(text = { Text("No vehicles found or loading...") }, onClick = {})
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val userId = currentFirebaseUser?.uid
                if (userId == null) { /* Toast.makeText(context, "User not identified.", Toast.LENGTH_SHORT).show(); */ return@Button } // Handled by UI state
                if (selectedDate.isBlank()) { /* Toast.makeText(context, "Select a date.", Toast.LENGTH_SHORT).show(); */ return@Button }
                if (selectedVehicle == null) { /* Toast.makeText(context, "Select a vehicle.", Toast.LENGTH_SHORT).show(); */ return@Button }
                if (description.isBlank()) { /* Toast.makeText(context, "Enter a description.", Toast.LENGTH_SHORT).show(); */ return@Button }

                appointmentViewModel.bookNewAppointment(userId, selectedDate, description, selectedVehicle!!)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = bookingUiStatus != BookingUiStatus.Loading && currentFirebaseUser != null && !showConflictDialog && !showSuccessDialog
        ) {
            if (bookingUiStatus == BookingUiStatus.Loading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Book Appointment")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) { Text("Cancel") }
    }
}
package com.jj.dev.myapplication.View



/*import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import com.jj.dev.myapplication.viewmodel.UserViewModel // Import UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterView(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel() // Inject UserViewModel
) {
    val context = LocalContext.current
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val roles = listOf("Customer", "Mechanic", "Admin") // Admin might not need detailed profile setup here
    var selectedRole by remember { mutableStateOf(roles[0]) }
    var expanded by remember { mutableStateOf(false) }

    val registerResultPair by authenticationViewModel.registerResult.observeAsState()
    // Observe UserViewModel state if needed for feedback on profile creation
    val userProfileState by userViewModel.userProfileState.observeAsState()


    // Handle navigation after successful registration and profile creation
    LaunchedEffect(registerResultPair, userProfileState) {
        val (success, firebaseUser) = registerResultPair ?: Pair(false, null)
        if (success && firebaseUser != null) {
            if (selectedRole == "Customer") {
                // If customer registration was successful via AuthVM, trigger profile creation in UserVM
                // Check if profile creation is already loading or done to avoid re-triggering
                if (userProfileState !is com.jj.dev.myapplication.viewmodel.UserProfileState.Loading &&
                    userProfileState !is com.jj.dev.myapplication.viewmodel.UserProfileState.Loaded) { // Basic check
                    Log.d("RegisterView", "Auth successful for customer ${firebaseUser.uid}. Triggering profile creation.")
                    userViewModel.createInitialCustomerProfileWithDummyVehicle(firebaseUser.uid, nameState.value.trim(), emailState.value.trim())
                }
                // Wait for UserViewModel to confirm profile creation before navigating, or navigate immediately and let UserVM load later.
                // For simplicity, we can navigate after auth, and UserVM handles profile in background.
                // Or, observe userProfileState for UserProfileState.Loaded before navigating.
                if (userProfileState is com.jj.dev.myapplication.viewmodel.UserProfileState.Loaded) {
                    Toast.makeText(context, "Registration & Profile Setup Successful!", Toast.LENGTH_SHORT).show()
                    navController.navigate("login_screen") { popUpTo("register_screen") { inclusive = true } }
                } else if (userProfileState is com.jj.dev.myapplication.viewmodel.UserProfileState.Error) {
                    Toast.makeText(context, "Auth successful, but profile setup failed.", Toast.LENGTH_LONG).show()
                }

            } else { // For Admin/Mechanic or other roles not needing immediate detailed profile
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                navController.navigate("login_screen") { popUpTo("register_screen") { inclusive = true } }
            }
        } else if (registerResultPair?.first == false && registerResultPair?.second == null) { // Explicitly check for auth failure
            Toast.makeText(context, "Registration Failed. Please try again.", Toast.LENGTH_LONG).show()
        }
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        TextField(value = nameState.value, onValueChange = { nameState.value = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = emailState.value, onValueChange = { emailState.value = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = passwordState.value, onValueChange = { passwordState.value = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Role", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            TextField(value = selectedRole, onValueChange = {}, readOnly = true, label = { Text("Role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                roles.forEach { roleOption ->
                    DropdownMenuItem(text = { Text(roleOption) }, onClick = { selectedRole = roleOption; expanded = false }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Role-specific fields (contact, specialization) are removed for now as they are part of Customer/Mechanic models
        // which would be fully created by their respective ViewModels/Repositories after basic registration.
        // If these fields are needed at registration time, they should be passed to the UserViewModel's profile creation method.

        Button(
            onClick = {
                authenticationViewModel.register(
                    email = emailState.value.trim(),
                    password = passwordState.value,
                    name = nameState.value.trim(),
                    role = selectedRole
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nameState.value.isNotBlank() && emailState.value.isNotBlank() && passwordState.value.isNotBlank() &&
                    (userProfileState !is com.jj.dev.myapplication.viewmodel.UserProfileState.Loading && registerResultPair?.first != true) // Prevent multiple clicks while processing
        ) {
            if (registerResultPair?.first == true && userProfileState is com.jj.dev.myapplication.viewmodel.UserProfileState.Loading && selectedRole == "Customer") {
                Text("Setting up profile...")
            } else if (registerResultPair?.first == true && selectedRole != "Customer"){
                Text("Registered!")
            }
            else {
                Text("Register")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Already have an account? Login", modifier = Modifier.clickable { navController.navigate("login_screen") }, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary))
    }
}*/

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jj.dev.myapplication.viewmodel.AuthenticationViewModel
import com.jj.dev.myapplication.viewmodel.CustomerViewModel // Updated import
import com.jj.dev.myapplication.viewmodel.CustomerProfileState // Updated import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterView(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel() // Inject CustomerViewModel
) {
    val context = LocalContext.current
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val roles = listOf("Customer", "Mechanic", "Admin")
    var selectedRole by remember { mutableStateOf(roles[0]) }
    var expanded by remember { mutableStateOf(false) }

    val registerResultPair by authenticationViewModel.registerResult.observeAsState()
    val customerProfileState by customerViewModel.customerProfileState.observeAsState() // Use customerProfileState


    LaunchedEffect(registerResultPair, customerProfileState) {
        val (success, firebaseUser) = registerResultPair ?: Pair(false, null)
        if (success && firebaseUser != null) {
            if (selectedRole == "Customer") {
                if (customerProfileState !is CustomerProfileState.Loading &&
                    customerProfileState !is CustomerProfileState.Loaded) {
                    Log.d("RegisterView", "Auth successful for customer ${firebaseUser.uid}. Triggering profile creation.")
                    customerViewModel.createInitialCustomerProfileWithDummyVehicle(firebaseUser.uid, nameState.value.trim(), emailState.value.trim())
                }
                if (customerProfileState is CustomerProfileState.Loaded) {
                    val loadedCustomer = (customerProfileState as CustomerProfileState.Loaded).customer
                    if (loadedCustomer != null) { // Ensure customer object is actually loaded
                        Toast.makeText(context, "Registration & Profile Setup Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate("login_screen") { popUpTo("register_screen") { inclusive = true } }
                    } else if ((customerProfileState as CustomerProfileState.Loaded).customer == null && customerProfileState !is CustomerProfileState.Loading) {
                        // This case might indicate an issue if Loaded has a null customer when it shouldn't
                        Log.w("RegisterView", "Profile loaded but customer object is null.")
                    }
                } else if (customerProfileState is CustomerProfileState.Error) {
                    Toast.makeText(context, "Auth successful, but profile setup failed: ${(customerProfileState as CustomerProfileState.Error).message}", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                navController.navigate("login_screen") { popUpTo("register_screen") { inclusive = true } }
            }
        } else if (registerResultPair?.first == false && registerResultPair?.second == null) {
            Toast.makeText(context, "Registration Failed. Please try again.", Toast.LENGTH_LONG).show()
        }
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        TextField(value = nameState.value, onValueChange = { nameState.value = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = emailState.value, onValueChange = { emailState.value = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = passwordState.value, onValueChange = { passwordState.value = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Role", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            TextField(value = selectedRole, onValueChange = {}, readOnly = true, label = { Text("Role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                roles.forEach { roleOption ->
                    DropdownMenuItem(text = { Text(roleOption) }, onClick = { selectedRole = roleOption; expanded = false }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authenticationViewModel.register(
                    email = emailState.value.trim(),
                    password = passwordState.value,
                    name = nameState.value.trim(),
                    role = selectedRole
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nameState.value.isNotBlank() && emailState.value.isNotBlank() && passwordState.value.isNotBlank() &&
                    (customerProfileState !is CustomerProfileState.Loading && registerResultPair?.first != true)
        ) {
            if (registerResultPair?.first == true && customerProfileState is CustomerProfileState.Loading && selectedRole == "Customer") {
                Text("Setting up profile...")
            } else if (registerResultPair?.first == true && selectedRole != "Customer"){
                Text("Registered!")
            }
            else {
                Text("Register")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Already have an account? Login", modifier = Modifier.clickable { navController.navigate("login_screen") }, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary))
    }
}
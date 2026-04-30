package com.compose.yifeng2fa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.compose.yifeng2fa.viewmodel.TotpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    viewModel: TotpViewModel,
    onBack: () -> Unit
) {
    var issuer by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var algorithm by remember { mutableStateOf("SHA1") }
    var digits by remember { mutableStateOf("6") }
    var period by remember { mutableStateOf("30") }

    val algorithms = listOf("SHA1", "SHA256", "SHA512")
    val digitsOptions = listOf("6", "8")
    val periodOptions = listOf("30", "60")

    var expandedAlgorithm by remember { mutableStateOf(false) }
    var expandedDigits by remember { mutableStateOf(false) }
    var expandedPeriod by remember { mutableStateOf(false) }

    val isValid = secret.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form Section
            Text(
                text = "Account Information",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = issuer,
                onValueChange = { issuer = it },
                label = { Text("Issuer (e.g. Google)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Name / Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            OutlinedTextField(
                value = secret,
                onValueChange = { secret = it },
                label = { Text("Secret Key *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                supportingText = { Text("Enter the secret key provided by the service") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Algorithm Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedAlgorithm,
                onExpandedChange = { expandedAlgorithm = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = algorithm,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Algorithm") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlgorithm) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedAlgorithm,
                    onDismissRequest = { expandedAlgorithm = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    algorithms.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                algorithm = option
                                expandedAlgorithm = false
                            }
                        )
                    }
                }
            }

            // Digits Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedDigits,
                onExpandedChange = { expandedDigits = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = digits,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Digits") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDigits) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedDigits,
                    onDismissRequest = { expandedDigits = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    digitsOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                digits = option
                                expandedDigits = false
                            }
                        )
                    }
                }
            }

            // Period Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedPeriod,
                onExpandedChange = { expandedPeriod = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "$period seconds",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Period") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedPeriod,
                    onDismissRequest = { expandedPeriod = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    periodOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text("$option seconds") },
                            onClick = {
                                period = option
                                expandedPeriod = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (secret.isNotBlank()) {
                        viewModel.addAccount(
                            issuer = issuer,
                            accountName = accountName,
                            secret = secret,
                            algorithm = algorithm,
                            digits = digits.toInt(),
                            period = period.toInt()
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Account", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

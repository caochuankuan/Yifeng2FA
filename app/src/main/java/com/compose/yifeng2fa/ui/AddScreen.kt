package com.compose.yifeng2fa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Manual Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = issuer,
                onValueChange = { issuer = it },
                label = { Text("Issuer (e.g. Google)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = secret,
                onValueChange = { secret = it },
                label = { Text("Secret Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedAlgorithm,
                    onDismissRequest = { expandedAlgorithm = false }
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
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedDigits,
                    onDismissRequest = { expandedDigits = false }
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
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedPeriod,
                    onDismissRequest = { expandedPeriod = false }
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

            Spacer(modifier = Modifier.weight(1f))

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
                enabled = secret.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}

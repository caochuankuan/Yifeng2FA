package com.compose.yifeng2fa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.compose.yifeng2fa.data.TotpEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountDialog(
    account: TotpEntity,
    onDismiss: () -> Unit,
    onConfirm: (TotpEntity) -> Unit
) {
    var issuer by remember { mutableStateOf(account.issuer) }
    var accountName by remember { mutableStateOf(account.accountName) }
    var secret by remember { mutableStateOf(account.secret) }
    var algorithm by remember { mutableStateOf(account.algorithm) }
    var digits by remember { mutableStateOf(account.digits.toString()) }
    var period by remember { mutableStateOf(account.period.toString()) }

    val algorithms = listOf("SHA1", "SHA256", "SHA512")
    val digitsOptions = listOf("6", "8")
    val periodOptions = listOf("30", "60")

    var expandedAlgorithm by remember { mutableStateOf(false) }
    var expandedDigits by remember { mutableStateOf(false) }
    var expandedPeriod by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Account",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = issuer,
                    onValueChange = { issuer = it },
                    label = { Text("Issuer") },
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
                    label = { Text("Account Name") },
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
                    label = { Text("Secret Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        account.copy(
                            issuer = issuer,
                            accountName = accountName,
                            secret = secret.uppercase().replace(" ", ""),
                            algorithm = algorithm,
                            digits = digits.toIntOrNull() ?: 6,
                            period = period.toIntOrNull() ?: 30
                        )
                    )
                },
                enabled = secret.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

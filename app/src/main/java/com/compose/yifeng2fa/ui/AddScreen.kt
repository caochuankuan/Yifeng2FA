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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            
            Button(
                onClick = {
                    if (secret.isNotBlank()) {
                        viewModel.addAccount(issuer, accountName, secret)
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

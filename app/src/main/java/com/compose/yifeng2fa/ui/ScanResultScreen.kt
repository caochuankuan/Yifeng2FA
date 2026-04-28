package com.compose.yifeng2fa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.compose.yifeng2fa.utils.TotpUtils
import com.compose.yifeng2fa.viewmodel.TotpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    uri: String,
    viewModel: TotpViewModel,
    onBack: () -> Unit
) {
    val totpData = remember { TotpUtils.parseOtpAuthUri(uri) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanned Account") },
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
            if (totpData != null) {
                Text("Issuer: ${totpData.issuer}", style = MaterialTheme.typography.titleMedium)
                Text("Account: ${totpData.accountName}", style = MaterialTheme.typography.titleMedium)
                Text("Secret: ${totpData.secret}", style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        viewModel.addAccount(totpData.issuer, totpData.accountName, totpData.secret)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Account")
                }
            } else {
                Text("Invalid QR Code", color = MaterialTheme.colorScheme.error)
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Go Back")
                }
            }
        }
    }
}

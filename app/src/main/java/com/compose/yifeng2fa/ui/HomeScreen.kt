package com.compose.yifeng2fa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.yifeng2fa.data.TotpEntity
import com.compose.yifeng2fa.utils.TotpUtils
import com.compose.yifeng2fa.viewmodel.SortOrder
import com.compose.yifeng2fa.viewmodel.TotpViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TotpViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val showCodes by viewModel.showCodes.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yifeng 2FA") },
                actions = {
                    IconButton(onClick = { viewModel.toggleShowCodes() }) {
                        Icon(
                            if (showCodes) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility"
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Date (Asc)") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.DATE_ASC)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Date (Desc)") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.DATE_DESC)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Issuer") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.ISSUER_ASC)
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = onNavigateToScan,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                }
                FloatingActionButton(onClick = onNavigateToAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add Account")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts, key = { it.id }) { account ->
                TotpCard(
                    account = account,
                    showCode = showCodes,
                    onDelete = { viewModel.deleteAccount(account) },
                    onClick = {
                        val fragmentActivity = context as? androidx.fragment.app.FragmentActivity
                        if (fragmentActivity != null) {
                            val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
                            val biometricPrompt = androidx.biometric.BiometricPrompt(
                                fragmentActivity,
                                executor,
                                object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                                        super.onAuthenticationSucceeded(result)
                                        onNavigateToDetail(account.id)
                                    }
                                }
                            )

                            val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Verify Identity")
                                .setSubtitle("Authenticate to view account details")
                                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                                .build()

                            biometricPrompt.authenticate(promptInfo)
                        } else {
                            onNavigateToDetail(account.id)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotpCard(
    account: TotpEntity,
    showCode: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var currentCode by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(account.secret) {
        while (true) {
            currentCode = TotpUtils.generateTotp(account.secret)
            progress = TotpUtils.getProgress()
            delay(100L)
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.issuer.ifEmpty { "Unknown Issuer" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = account.accountName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (showCode) {
                        if (currentCode.length == 6) {
                            "${currentCode.substring(0, 3)} ${currentCode.substring(3)}"
                        } else {
                            currentCode
                        }
                    } else {
                        "••• •••"
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(36.dp),
                    color = if (progress < 0.15f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

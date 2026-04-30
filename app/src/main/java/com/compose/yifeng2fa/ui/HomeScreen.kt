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
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.compose.yifeng2fa.data.TotpEntity
import com.compose.yifeng2fa.utils.CryptoUtils
import com.compose.yifeng2fa.utils.TotpUtils
import com.compose.yifeng2fa.viewmodel.SortOrder
import com.compose.yifeng2fa.viewmodel.TotpViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var exportUri by remember { mutableStateOf<Uri?>(null) }
    var importUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            exportUri = uri
            showExportPasswordDialog = true
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            importUri = uri
            showImportPasswordDialog = true
        }
    }

    if (showExportPasswordDialog) {
        PasswordDialog(
            title = "Export Password",
            onConfirm = { password ->
                showExportPasswordDialog = false
                coroutineScope.launch {
                    try {
                        val json = Gson().toJson(accounts)
                        val encrypted = CryptoUtils.encrypt(json, password)
                        exportUri?.let { uri ->
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(encrypted.toByteArray())
                            }
                        }
                        Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showExportPasswordDialog = false }
        )
    }

    if (showImportPasswordDialog) {
        PasswordDialog(
            title = "Import Password",
            onConfirm = { password ->
                showImportPasswordDialog = false
                coroutineScope.launch {
                    try {
                        var encrypted = ""
                        importUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                encrypted = inputStream.bufferedReader().use { it.readText() }
                            }
                        }
                        val decryptedJson = CryptoUtils.decrypt(encrypted, password)
                        val type = object : TypeToken<List<TotpEntity>>() {}.type
                        val importedAccounts: List<TotpEntity> = Gson().fromJson(decryptedJson, type)
                        viewModel.importAccounts(importedAccounts)
                        Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Import failed: Check password or file", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showImportPasswordDialog = false }
        )
    }

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
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Export Data") },
                            onClick = {
                                exportLauncher.launch("2fa_backup.json")
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Data") },
                            onClick = {
                                importLauncher.launch(arrayOf("*/*"))
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
                        val activity = context as? androidx.fragment.app.FragmentActivity
                        if (activity != null) {
                            val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
                            val biometricPrompt = androidx.biometric.BiometricPrompt(
                                activity,
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

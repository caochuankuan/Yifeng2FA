package com.compose.yifeng2fa.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlin.math.abs

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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Yifeng 2FA",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
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
                        onDismissRequest = { showMenu = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Date (Asc)") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.DATE_ASC)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Date (Desc)") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.DATE_DESC)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Issuer") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.ISSUER_ASC)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        HorizontalDivider()
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
                    modifier = Modifier.padding(bottom = 12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                }
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Account")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (accounts.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Security,
                title = "No Accounts Yet",
                message = "Add your first 2FA account by scanning a QR code or entering details manually.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private fun issuerColor(issuer: String): Color {
    val colors = listOf(
        Color(0xFF006A6A), Color(0xFF4A6363), Color(0xFF4D6077),
        Color(0xFF6B4EA2), Color(0xFF8B4F74), Color(0xFF7D5648),
        Color(0xFF5D6E42), Color(0xFF00639B), Color(0xFF7D5260),
        Color(0xFF4A635D), Color(0xFF6A4A63), Color(0xFF635D4A)
    )
    val hash = abs(issuer.hashCode())
    return colors[hash % colors.size]
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(account.secret, account.algorithm, account.digits, account.period) {
        while (true) {
            currentCode = TotpUtils.generateTotp(
                secret = account.secret,
                algorithm = account.algorithm,
                digits = account.digits,
                period = account.period
            )
            progress = TotpUtils.getProgress(period = account.period)
            delay(100L)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account?") },
            text = { Text("This will permanently remove \"${account.issuer.ifEmpty { "Unknown" }}\" from your authenticator.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val progressColor = when {
        progress < 0.1f -> MaterialTheme.colorScheme.error
        progress < 0.3f -> Color(0xFFE6A817)
        else -> MaterialTheme.colorScheme.primary
    }

    val issuerName = account.issuer.ifEmpty { "Unknown" }
    val color = issuerColor(issuerName)

    val displayCode = if (showCode) {
        when (currentCode.length) {
            6 -> "${currentCode.substring(0, 3)} ${currentCode.substring(3)}"
            8 -> "${currentCode.substring(0, 4)} ${currentCode.substring(4)}"
            else -> currentCode
        }
    } else {
        if (currentCode.length == 8) "•••• ••••" else "••• •••"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top section: info + delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Issuer Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = issuerName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issuerName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = account.accountName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // TOTP Code
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = displayCode,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        fontSize = 36.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Linear progress bar at bottom
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {}
            )
        }
    }
}

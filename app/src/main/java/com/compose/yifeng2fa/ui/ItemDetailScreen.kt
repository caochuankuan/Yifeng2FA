package com.compose.yifeng2fa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.compose.yifeng2fa.utils.TotpUtils
import com.compose.yifeng2fa.viewmodel.TotpViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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
fun ItemDetailScreen(
    id: Long,
    viewModel: TotpViewModel,
    onBack: () -> Unit
) {
    val account by viewModel.getAccountById(id).collectAsState(initial = null)

    var currentCode by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }
    var showEditDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(account?.secret, account?.algorithm, account?.digits, account?.period) {
        val acc = account ?: return@LaunchedEffect
        while (true) {
            currentCode = TotpUtils.generateTotp(
                secret = acc.secret,
                algorithm = acc.algorithm,
                digits = acc.digits,
                period = acc.period
            )
            progress = TotpUtils.getProgress(period = acc.period)
            delay(100L)
        }
    }

    if (showEditDialog && account != null) {
        EditAccountDialog(
            account = account!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedAccount ->
                viewModel.updateAccount(updatedAccount)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (account != null) {
                val issuerName = account!!.issuer.ifEmpty { "Unknown" }
                val color = issuerColor(issuerName)

                // Issuer Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = issuerName.take(1).uppercase(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = issuerName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = account!!.accountName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // TOTP Code Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val progressColor = when {
                                progress < 0.1f -> MaterialTheme.colorScheme.error
                                progress < 0.3f -> Color(0xFFE6A817)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(160.dp),
                                color = progressColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 8.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (currentCode.length) {
                                        6 -> "${currentCode.substring(0, 3)}\n${currentCode.substring(3)}"
                                        8 -> "${currentCode.substring(0, 4)}\n${currentCode.substring(4)}"
                                        else -> currentCode
                                    },
                                    style = when (currentCode.length) {
                                        8 -> MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 4.sp
                                        )
                                        else -> MaterialTheme.typography.displayMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 4.sp
                                        )
                                    },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${(progress * account!!.period).toInt()}s remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (progress < 0.15f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Secret Key
                        DetailRow(
                            label = "Secret Key",
                            value = account!!.secret,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(account!!.secret))
                                Toast.makeText(context, "Secret copied", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Algorithm
                        DetailItem(label = "Algorithm", value = account!!.algorithm)

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem(
                                label = "Digits",
                                value = account!!.digits.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            DetailItem(
                                label = "Period",
                                value = "${account!!.period}s",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Added on ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(account!!.addedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onCopy) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

package com.compose.yifeng2fa.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.yifeng2fa.data.PasswordEntity
import com.compose.yifeng2fa.utils.CryptoUtils
import com.compose.yifeng2fa.viewmodel.PasswordViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(
    viewModel: PasswordViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val allAccountsCount by viewModel.allAccountsCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showPassword by viewModel.showPassword.collectAsState()
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var showDeleteConfirm by remember { mutableStateOf(false) }
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
            title = "导出密码",
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
                        Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showExportPasswordDialog = false }
        )
    }

    if (showImportPasswordDialog) {
        PasswordDialog(
            title = "导入密码",
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
                        val type = object : TypeToken<List<PasswordEntity>>() {}.type
                        val importedAccounts: List<PasswordEntity> = Gson().fromJson(decryptedJson, type)
                        viewModel.importAccounts(importedAccounts)
                        Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "导入失败: 请检查密码或文件", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showImportPasswordDialog = false }
        )
    }

    Scaffold(
        topBar = {
            if (isMultiSelectMode) {
                TopAppBar(
                    title = { Text("已选择 ${selectedIds.size} 项") },
                    navigationIcon = {
                        IconButton(onClick = {
                            isMultiSelectMode = false
                            selectedIds.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "取消")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除选中",
                                tint = if (selectedIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("密码管理") },
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "菜单")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (showPassword) "隐藏密码" else "显示密码") },
                                leadingIcon = {
                                    Icon(
                                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    viewModel.toggleShowPassword()
                                    showMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("导入数据") },
                                leadingIcon = {
                                    Icon(Icons.Default.FileUpload, contentDescription = null)
                                },
                                onClick = {
                                    importLauncher.launch(arrayOf("*/*"))
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("导出数据") },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                },
                                onClick = {
                                    exportLauncher.launch("password_backup.json")
                                    showMenu = false
                                }
                            )
                            if (accounts.isNotEmpty()) {
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("多选管理") },
                                    leadingIcon = {
                                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                                    },
                                    onClick = {
                                        isMultiSelectMode = true
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isMultiSelectMode) {
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加密码")
                }
            }
        }
    ) { padding ->
        if (showDeleteConfirm) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("确认删除") },
                text = { Text("确定要删除选中的 ${selectedIds.size} 条记录吗？此操作不可撤销。") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.deleteAccountsByIds(selectedIds.toList())
                            selectedIds.clear()
                            isMultiSelectMode = false
                            showDeleteConfirm = false
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isMultiSelectMode && allAccountsCount > 3) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("搜索网站、账号或备注") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }

            if (accounts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Lock,
                    title = "暂无密码记录",
                    message = "点击右下角按钮添加您的第一条密码记录。",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts, key = { it.id }) { account ->
                        val isSelected = selectedIds.contains(account.id)
                        PasswordItemCard(
                            account = account,
                            showPassword = showPassword,
                            isMultiSelectMode = isMultiSelectMode,
                            isSelected = isSelected,
                            onClick = {
                                if (isMultiSelectMode) {
                                    if (isSelected) selectedIds.remove(account.id)
                                    else selectedIds.add(account.id)
                                    if (selectedIds.isEmpty()) isMultiSelectMode = false
                                } else {
                                    onNavigateToDetail(account.id)
                                }
                            },
                            onLongClick = {
                                if (!isMultiSelectMode) {
                                    isMultiSelectMode = true
                                    selectedIds.add(account.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PasswordItemCard(
    account: PasswordEntity,
    showPassword: Boolean,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM-dd", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                val initial = account.websiteName.take(1).uppercase()
                Text(
                    text = initial.ifEmpty { "?" },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = account.websiteName.ifEmpty { "未命名" },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isMultiSelectMode) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateFormat.format(Date(account.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (account.account.isNotEmpty()) {
                    Text(
                        text = account.account,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = if (showPassword) account.password else "•".repeat(account.password.length.coerceIn(6, 12)),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

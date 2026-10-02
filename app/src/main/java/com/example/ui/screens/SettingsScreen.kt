package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.service.CsvExportImportService
import com.example.ui.ThemeMode
import com.example.ui.theme.FintechCoral
import com.example.ui.theme.FintechEmerald
import com.example.ui.theme.FintechGoldPrimary

@Composable
fun SettingsScreen(
    userName: String,
    onUserNameChange: (String) -> Unit,
    currencySymbol: String,
    onCurrencyChange: (String) -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    transactions: List<TransactionEntity>,
    onOpenCsvImport: () -> Unit,
    onLoadDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userName) }

    val currencies = listOf(
        "₹" to "₹ INR (Indian Rupee)",
        "$" to "$ USD (US Dollar)",
        "€" to "€ EUR (Euro)",
        "£" to "£ GBP (British Pound)"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Settings & Preferences",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure display currency, themes, and manage data export/import",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Profile Section
        item {
            SettingsCard(title = "Profile") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(FintechGoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = FintechGoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        if (editingName) {
                            OutlinedTextField(
                                value = tempName,
                                onValueChange = { tempName = it },
                                modifier = Modifier.width(160.dp),
                                singleLine = true
                            )
                        } else {
                            Column {
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Primary Account Holder",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (editingName) {
                        IconButton(onClick = {
                            onUserNameChange(tempName.ifBlank { "User" })
                            editingName = false
                            onShowToast("Name updated")
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = FintechEmerald)
                        }
                    } else {
                        IconButton(onClick = { editingName = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Currency Selector
        item {
            SettingsCard(title = "Currency Display") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { (symbol, label) ->
                        val isSelected = currencySymbol == symbol
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) FintechGoldPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { onCurrencyChange(symbol) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FintechGoldPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = FintechGoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Theme Mode Selector
        item {
            SettingsCard(title = "Theme Appearance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val isSel = themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) FintechGoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onThemeChange(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color(0xFF132238) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Data Management: Export & Import
        item {
            SettingsCard(title = "Data Management & Export") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Export CSV
                    DataActionRow(
                        title = "Export CSV",
                        description = "Export all ${transactions.size} transactions to expenses.csv format",
                        icon = Icons.Default.FileDownload,
                        accentColor = FintechEmerald,
                        onClick = {
                            val csv = CsvExportImportService.exportToCsv(transactions)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("expenses.csv", csv))

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "expenses.csv")
                                putExtra(Intent.EXTRA_TEXT, csv)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export transactions.csv"))
                            onShowToast("CSV copied to clipboard & shared")
                        }
                    )

                    // Export JSON
                    DataActionRow(
                        title = "Export JSON",
                        description = "Export transactions as structured JSON backup",
                        icon = Icons.Default.Code,
                        accentColor = FintechGoldPrimary,
                        onClick = {
                            val json = CsvExportImportService.exportToJson(transactions)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("expenses.json", json))
                            onShowToast("JSON copied to clipboard")
                        }
                    )

                    // Import CSV
                    DataActionRow(
                        title = "Import CSV",
                        description = "Validate and import external CSV records",
                        icon = Icons.Default.FileUpload,
                        accentColor = Color(0xFF38BDF8),
                        onClick = onOpenCsvImport
                    )

                    // Load Demo Data
                    DataActionRow(
                        title = "Reload Sample Demo Data",
                        description = "Repopulate with standard fintech benchmark data",
                        icon = Icons.Default.Refresh,
                        accentColor = FintechGoldPrimary,
                        onClick = onLoadDemoData
                    )

                    // Clear All Data
                    DataActionRow(
                        title = "Clear All Records",
                        description = "Reset database and purge all transactions and budgets",
                        icon = Icons.Default.DeleteForever,
                        accentColor = FintechCoral,
                        onClick = { showClearConfirm = true }
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Financial Data", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete all transactions, budgets, and recurring items?") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechCoral)
                ) {
                    Text("Delete Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun DataActionRow(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

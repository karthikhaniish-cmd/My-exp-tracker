package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.service.CsvExportImportService
import com.example.service.SmartCategoryDetector
import com.example.ui.theme.FintechCoral
import com.example.ui.theme.FintechEmerald
import com.example.ui.theme.FintechGoldPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: TransactionEntity?,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (id: Long, type: String, amount: Double, description: String, category: String, paymentMethod: String, date: Long, notes: String) -> Unit
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE.name) }
    var amountText by remember { mutableStateOf(initialTransaction?.amount?.toString()?.removeSuffix(".0") ?: "") }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(initialTransaction?.category ?: TransactionCategory.FOOD.name) }
    var selectedPaymentMethod by remember { mutableStateOf(initialTransaction?.paymentMethod ?: PaymentMethod.UPI.displayName) }
    var notes by remember { mutableStateOf(initialTransaction?.notes ?: "") }
    var dateMillis by remember { mutableStateOf(initialTransaction?.date ?: System.currentTimeMillis()) }

    var suggestedCategory by remember { mutableStateOf<Pair<TransactionCategory, TransactionType>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Smart keyword detection trigger when description changes
    LaunchedEffect(description) {
        if (initialTransaction == null && description.length >= 3) {
            val detected = SmartCategoryDetector.detectCategory(description)
            suggestedCategory = detected
        } else {
            suggestedCategory = null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with title and close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTransaction == null) "Add Transaction" else "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type Toggle (Expense / Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    // Expense tab
                    val isExpense = type == TransactionType.EXPENSE.name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExpense) FintechCoral else Color.Transparent)
                            .clickable { type = TransactionType.EXPENSE.name }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Income tab
                    val isIncome = type == TransactionType.INCOME.name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isIncome) FintechEmerald else Color.Transparent)
                            .clickable { type = TransactionType.INCOME.name }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountText = input
                            errorMessage = null
                        }
                    },
                    label = { Text("Amount") },
                    prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = FintechGoldPrimary) },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("amount_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechGoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        errorMessage = null
                    },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Swiggy dinner, Uber ride, Salary") },
                    modifier = Modifier.fillMaxWidth().testTag("description_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechGoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Smart Category Suggestion Chip
                AnimatedVisibility(visible = suggestedCategory != null) {
                    suggestedCategory?.let { (cat, suggestedType) ->
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechGoldPrimary.copy(alpha = 0.15f))
                                .border(1.dp, FintechGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedCategory = cat.name
                                    type = suggestedType.name
                                    suggestedCategory = null
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Smart Detection",
                                    tint = FintechGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Smart Suggestion: ${cat.displayName} (${suggestedType.name.lowercase().replaceFirstChar { it.uppercase() }}) — Tap to apply",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = FintechGoldPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val availableCategories = if (type == TransactionType.INCOME.name) {
                    TransactionCategory.entries.filter { !it.isExpenseOnly }
                } else {
                    TransactionCategory.entries.filter { !it.isIncomeOnly }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCategories) { cat ->
                        val isSelected = selectedCategory.equals(cat.name, ignoreCase = true) ||
                                selectedCategory.equals(cat.displayName, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) cat.getColor() else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedCategory = cat.name }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = cat.getIcon(),
                                    contentDescription = cat.displayName,
                                    tint = if (isSelected) Color.White else cat.getColor(),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method Chips
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PaymentMethod.entries) { method ->
                        val isSelected = selectedPaymentMethod == method.displayName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FintechGoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedPaymentMethod = method.displayName }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = method.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF132238) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date selection quick chips (Today, Yesterday, 2 Days Ago)
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
                val curDateStr = sdf.format(Date(dateMillis))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date: $curDateStr",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val now = System.currentTimeMillis()
                        Text(
                            text = "Today",
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { dateMillis = now }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Yesterday",
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { dateMillis = now - 86400000L }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Additional details...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechGoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = FintechCoral,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            errorMessage = "Please enter a valid amount greater than 0"
                            return@Button
                        }
                        if (description.isBlank()) {
                            errorMessage = "Please enter a transaction description"
                            return@Button
                        }

                        onSave(
                            initialTransaction?.id ?: 0L,
                            type,
                            amt,
                            description,
                            selectedCategory,
                            selectedPaymentMethod,
                            dateMillis,
                            notes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechGoldPrimary)
                ) {
                    Text(
                        text = "SAVE TRANSACTION",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF132238)
                    )
                }
            }
        }
    }
}

@Composable
fun AddBudgetDialog(
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (category: String, amount: Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(TransactionCategory.FOOD.name) }
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text(
                    text = "Set Monthly Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Track your monthly spending limits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val expenseCategories = TransactionCategory.entries.filter { !it.isIncomeOnly }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(expenseCategories) { cat ->
                        val isSelected = selectedCategory == cat.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) cat.getColor() else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedCategory = cat.name }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                            amountText = it
                            error = null
                        }
                    },
                    label = { Text("Monthly Budget Limit") },
                    prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = FintechGoldPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("budget_amount_input"),
                    singleLine = true
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(error ?: "", color = FintechCoral, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            error = "Please enter a valid budget amount"
                            return@Button
                        }
                        onSave(selectedCategory, amt)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_budget_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechGoldPrimary)
                ) {
                    Text("SAVE BUDGET", fontWeight = FontWeight.Bold, color = Color(0xFF132238))
                }
            }
        }
    }
}

@Composable
fun AddRecurringDialog(
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, category: String, frequency: String, startDate: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.BILLS.name) }
    var selectedFrequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY.name) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text(
                    text = "Add Recurring Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Subscriptions, bills & automated payments",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Netflix, Gym, Apartment Rent") },
                    modifier = Modifier.fillMaxWidth().testTag("recurring_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) amountText = it },
                    label = { Text("Amount") },
                    prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = FintechGoldPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("recurring_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecurrenceFrequency.entries.forEach { freq ->
                        val isSel = selectedFrequency == freq.name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) FintechGoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedFrequency = freq.name }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = freq.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color(0xFF132238) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error ?: "", color = FintechCoral, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (name.isBlank()) {
                            error = "Please enter recurring payment name"
                            return@Button
                        }
                        if (amt == null || amt <= 0) {
                            error = "Please enter a valid amount"
                            return@Button
                        }
                        onSave(name, amt, selectedCategory, selectedFrequency, System.currentTimeMillis())
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_recurring_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechGoldPrimary)
                ) {
                    Text("SAVE RECURRING", fontWeight = FontWeight.Bold, color = Color(0xFF132238))
                }
            }
        }
    }
}

@Composable
fun CsvImportDialog(
    onDismiss: () -> Unit,
    onConfirmImport: (List<TransactionEntity>) -> Unit
) {
    var rawCsvText by remember {
        mutableStateOf(
            "Date,Description,Category,Payment Method,Type,Amount,Notes\n" +
            "2026-10-01,Organic Groceries,Food,UPI,EXPENSE,1450,Weekly supermarket run\n" +
            "2026-10-02,Consulting Freelance,Freelance,Bank Transfer,INCOME,18000,Q3 design sprint payment\n" +
            "2026-10-02,Gym Annual Fee,Health,Credit Card,EXPENSE,6500,Annual fitness pass\n" +
            "2026-10-03,Fuel Petrol,Transport,UPI,EXPENSE,2000,Tank refill"
        )
    }

    val parseResult = remember(rawCsvText) {
        CsvExportImportService.parseCsv(rawCsvText)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Import Transactions CSV",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Paste CSV data to validate and import into your database",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = rawCsvText,
                    onValueChange = { rawCsvText = it },
                    label = { Text("CSV Text") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechGoldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Validation Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (parseResult.isValid) FintechEmerald.copy(alpha = 0.15f)
                            else FintechCoral.copy(alpha = 0.15f)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (parseResult.isValid)
                                "✓ Valid CSV: ${parseResult.validTransactions.size} transactions detected"
                            else "⚠ Validation Failed: No valid transactions detected",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (parseResult.isValid) FintechEmerald else FintechCoral
                        )

                        if (parseResult.errorMessages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            parseResult.errorMessages.take(3).forEach { err ->
                                Text(
                                    text = "• $err",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = FintechCoral
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (parseResult.isValid) {
                            onConfirmImport(parseResult.validTransactions)
                        }
                    },
                    enabled = parseResult.isValid,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_import_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechGoldPrimary)
                ) {
                    Text(
                        text = "CONFIRM IMPORT (${parseResult.validTransactions.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF132238)
                    )
                }
            }
        }
    }
}

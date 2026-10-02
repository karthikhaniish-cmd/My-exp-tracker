package com.example.service

import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CsvParseResult(
    val isValid: Boolean,
    val validTransactions: List<TransactionEntity>,
    val errorMessages: List<String>,
    val totalProcessedRows: Int
)

object CsvExportImportService {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    fun exportToCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("Date,Description,Category,Payment Method,Type,Amount,Notes\n")
        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.date))
            val desc = escapeCsv(tx.description)
            val cat = escapeCsv(tx.category)
            val pm = escapeCsv(tx.paymentMethod)
            val type = tx.type
            val amt = tx.amount
            val notes = escapeCsv(tx.notes)
            sb.append("$dateStr,$desc,$cat,$pm,$type,$amt,$notes\n")
        }
        return sb.toString()
    }

    fun exportToJson(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        transactions.forEachIndexed { index, tx ->
            val dateStr = dateFormat.format(Date(tx.date))
            sb.append("  {\n")
            sb.append("    \"id\": ${tx.id},\n")
            sb.append("    \"date\": \"$dateStr\",\n")
            sb.append("    \"description\": \"${escapeJson(tx.description)}\",\n")
            sb.append("    \"category\": \"${escapeJson(tx.category)}\",\n")
            sb.append("    \"paymentMethod\": \"${escapeJson(tx.paymentMethod)}\",\n")
            sb.append("    \"type\": \"${tx.type}\",\n")
            sb.append("    \"amount\": ${tx.amount},\n")
            sb.append("    \"notes\": \"${escapeJson(tx.notes)}\"\n")
            sb.append(if (index < transactions.size - 1) "  },\n" else "  }\n")
        }
        sb.append("]")
        return sb.toString()
    }

    fun parseCsv(csvContent: String): CsvParseResult {
        val lines = csvContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return CsvParseResult(
                isValid = false,
                validTransactions = emptyList(),
                errorMessages = listOf("CSV file is completely empty"),
                totalProcessedRows = 0
            )
        }

        val header = lines.first()
        val hasHeader = header.contains("Date", ignoreCase = true) && header.contains("Amount", ignoreCase = true)
        val dataRows = if (hasHeader) lines.drop(1) else lines

        val validList = mutableListOf<TransactionEntity>()
        val errors = mutableListOf<String>()

        for ((index, row) in dataRows.withIndex()) {
            val lineNum = index + (if (hasHeader) 2 else 1)
            val parts = parseCsvRow(row)

            if (parts.size < 6) {
                errors.add("Line $lineNum: Insufficient columns (expected at least Date, Description, Category, Payment Method, Type, Amount).")
                continue
            }

            val dateStr = parts[0].trim()
            val description = parts[1].trim()
            val category = parts[2].trim()
            val paymentMethod = parts[3].trim()
            val typeStr = parts[4].trim().uppercase()
            val amountStr = parts[5].trim().replace("$", "").replace("₹", "").replace(",", "")
            val notes = if (parts.size > 6) parts[6].trim() else ""

            if (description.isEmpty()) {
                errors.add("Line $lineNum: Description cannot be empty.")
                continue
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                errors.add("Line $lineNum: Invalid amount '$amountStr'. Must be positive number.")
                continue
            }

            val type = if (typeStr.contains("INCOME")) TransactionType.INCOME.name else TransactionType.EXPENSE.name

            val timestamp = parseFlexibleDate(dateStr)

            validList.add(
                TransactionEntity(
                    type = type,
                    amount = amount,
                    description = description,
                    category = if (category.isNotBlank()) category else TransactionCategory.OTHER.displayName,
                    paymentMethod = if (paymentMethod.isNotBlank()) paymentMethod else PaymentMethod.OTHER.displayName,
                    date = timestamp,
                    notes = notes
                )
            )
        }

        return CsvParseResult(
            isValid = validList.isNotEmpty(),
            validTransactions = validList,
            errorMessages = errors,
            totalProcessedRows = dataRows.size
        )
    }

    private fun parseFlexibleDate(dateStr: String): Long {
        val formats = listOf("yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "MM/dd/yyyy", "dd MMM yyyy", "yyyy/MM/dd")
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                val date = sdf.parse(dateStr)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    private fun parseCsvRow(row: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()

        for (ch in row) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    tokens.add(current.toString())
                    current.setLength(0)
                }
                else -> current.append(ch)
            }
        }
        tokens.add(current.toString())
        return tokens
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}

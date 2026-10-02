package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other")
}

enum class RecurrenceFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

enum class TransactionCategory(
    val displayName: String,
    val isExpenseOnly: Boolean = false,
    val isIncomeOnly: Boolean = false
) {
    FOOD("Food", isExpenseOnly = true),
    TRANSPORT("Transport", isExpenseOnly = true),
    SHOPPING("Shopping", isExpenseOnly = true),
    BILLS("Bills", isExpenseOnly = true),
    ENTERTAINMENT("Entertainment", isExpenseOnly = true),
    EDUCATION("Education", isExpenseOnly = true),
    HEALTH("Health", isExpenseOnly = true),
    TRAVEL("Travel", isExpenseOnly = true),
    SALARY("Salary", isIncomeOnly = true),
    FREELANCE("Freelance", isIncomeOnly = true),
    INVESTMENT("Investment"),
    OTHER("Other");

    fun getIcon(): ImageVector {
        return when (this) {
            FOOD -> Icons.Default.Restaurant
            TRANSPORT -> Icons.Default.DirectionsCar
            SHOPPING -> Icons.Default.ShoppingBag
            BILLS -> Icons.Default.Receipt
            ENTERTAINMENT -> Icons.Default.Movie
            EDUCATION -> Icons.Default.School
            HEALTH -> Icons.Default.MedicalServices
            TRAVEL -> Icons.Default.Flight
            SALARY -> Icons.Default.AccountBalanceWallet
            FREELANCE -> Icons.Default.Laptop
            INVESTMENT -> Icons.Default.TrendingUp
            OTHER -> Icons.Default.Category
        }
    }

    fun getColor(): Color {
        return when (this) {
            FOOD -> Color(0xFFF97316) // Orange
            TRANSPORT -> Color(0xFF3B82F6) // Blue
            SHOPPING -> Color(0xFFEC4899) // Pink
            BILLS -> Color(0xFFEAB308) // Yellow
            ENTERTAINMENT -> Color(0xFF8B5CF6) // Purple
            EDUCATION -> Color(0xFF06B6D4) // Cyan
            HEALTH -> Color(0xFFEF4444) // Red
            TRAVEL -> Color(0xFF14B8A6) // Teal
            SALARY -> Color(0xFF10B981) // Emerald
            FREELANCE -> Color(0xFF6366F1) // Indigo
            INVESTMENT -> Color(0xFFC9A24B) // Gold
            OTHER -> Color(0xFF64748B) // Slate
        }
    }

    companion object {
        fun fromString(name: String): TransactionCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: OTHER
        }
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val description: String,
    val category: String,
    val paymentMethod: String,
    val date: Long, // Epoch timestamp in ms
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val amount: Double,
    val month: Int, // 1 to 12
    val year: Int
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String,
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val startDate: Long,
    val nextDate: Long,
    val isActive: Boolean = true
)

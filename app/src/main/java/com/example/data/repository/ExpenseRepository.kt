package com.example.data.repository

import com.example.data.db.BudgetDao
import com.example.data.db.RecurringDao
import com.example.data.db.TransactionDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val recurringDao: RecurringDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allRecurring: Flow<List<RecurringTransactionEntity>> = recurringDao.getAllRecurring()

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteById(id)

    suspend fun insertBudget(budget: BudgetEntity): Long =
        budgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: BudgetEntity) =
        budgetDao.updateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        budgetDao.deleteBudget(budget)

    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long =
        recurringDao.insertRecurring(recurring)

    suspend fun updateRecurring(recurring: RecurringTransactionEntity) =
        recurringDao.updateRecurring(recurring)

    suspend fun deleteRecurring(recurring: RecurringTransactionEntity) =
        recurringDao.deleteRecurring(recurring)

    suspend fun clearAllData() {
        transactionDao.deleteAll()
        budgetDao.deleteAll()
        recurringDao.deleteAll()
    }

    suspend fun loadDemoData(clearExisting: Boolean = false) {
        if (clearExisting) {
            clearAllData()
        }

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        val oneDay = 86400000L

        val demoTransactions = listOf(
            TransactionEntity(
                type = TransactionType.INCOME.name,
                amount = 50000.0,
                description = "Monthly Salary Credited",
                category = TransactionCategory.SALARY.name,
                paymentMethod = PaymentMethod.BANK_TRANSFER.displayName,
                date = now - (oneDay * 1),
                notes = "Direct deposit from company"
            ),
            TransactionEntity(
                type = TransactionType.INCOME.name,
                amount = 10000.0,
                description = "UI Design Freelance Project",
                category = TransactionCategory.FREELANCE.name,
                paymentMethod = PaymentMethod.UPI.displayName,
                date = now - (oneDay * 4),
                notes = "Landing page design client payment"
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                amount = 5200.0,
                description = "Swiggy & Weekend Dinner with friends",
                category = TransactionCategory.FOOD.name,
                paymentMethod = PaymentMethod.UPI.displayName,
                date = now - (oneDay * 1),
                notes = "Italian dinner & takeout"
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                amount = 2100.0,
                description = "Uber commute & metro recharge",
                category = TransactionCategory.TRANSPORT.name,
                paymentMethod = PaymentMethod.UPI.displayName,
                date = now - (oneDay * 2),
                notes = "Weekly commute to client office"
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                amount = 3500.0,
                description = "Amazon headphones & tech accessories",
                category = TransactionCategory.SHOPPING.name,
                paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                date = now - (oneDay * 3),
                notes = "Noise cancelling earphones"
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                amount = 4000.0,
                description = "Electricity & High-speed Fiber Bill",
                category = TransactionCategory.BILLS.name,
                paymentMethod = PaymentMethod.BANK_TRANSFER.displayName,
                date = now - (oneDay * 5),
                notes = "Monthly utilities"
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                amount = 1500.0,
                description = "Netflix & Movie Tickets",
                category = TransactionCategory.ENTERTAINMENT.name,
                paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                date = now - (oneDay * 6),
                notes = "Weekend cinema & monthly 4k plan"
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                amount = 1200.0,
                description = "Gym supplements & pharmacy",
                category = TransactionCategory.HEALTH.name,
                paymentMethod = PaymentMethod.DEBIT_CARD.displayName,
                date = now - (oneDay * 8),
                notes = "Vitamins and protein"
            )
        )

        val demoBudgets = listOf(
            BudgetEntity(category = TransactionCategory.FOOD.name, amount = 7000.0, month = currentMonth, year = currentYear),
            BudgetEntity(category = TransactionCategory.TRANSPORT.name, amount = 3000.0, month = currentMonth, year = currentYear),
            BudgetEntity(category = TransactionCategory.SHOPPING.name, amount = 5000.0, month = currentMonth, year = currentYear),
            BudgetEntity(category = TransactionCategory.BILLS.name, amount = 4500.0, month = currentMonth, year = currentYear),
            BudgetEntity(category = TransactionCategory.ENTERTAINMENT.name, amount = 2000.0, month = currentMonth, year = currentYear)
        )

        val demoRecurring = listOf(
            RecurringTransactionEntity(
                name = "Netflix Premium",
                amount = 649.0,
                category = TransactionCategory.ENTERTAINMENT.name,
                frequency = RecurrenceFrequency.MONTHLY.name,
                startDate = now - (oneDay * 15),
                nextDate = now + (oneDay * 15)
            ),
            RecurringTransactionEntity(
                name = "Apartment Rent",
                amount = 12000.0,
                category = TransactionCategory.BILLS.name,
                frequency = RecurrenceFrequency.MONTHLY.name,
                startDate = now - (oneDay * 20),
                nextDate = now + (oneDay * 10)
            ),
            RecurringTransactionEntity(
                name = "Gym Membership",
                amount = 1500.0,
                category = TransactionCategory.HEALTH.name,
                frequency = RecurrenceFrequency.MONTHLY.name,
                startDate = now - (oneDay * 10),
                nextDate = now + (oneDay * 20)
            ),
            RecurringTransactionEntity(
                name = "High-speed Internet",
                amount = 999.0,
                category = TransactionCategory.BILLS.name,
                frequency = RecurrenceFrequency.MONTHLY.name,
                startDate = now - (oneDay * 5),
                nextDate = now + (oneDay * 25)
            )
        )

        transactionDao.insertAll(demoTransactions)
        budgetDao.insertAll(demoBudgets)
        recurringDao.insertAll(demoRecurring)
    }
}

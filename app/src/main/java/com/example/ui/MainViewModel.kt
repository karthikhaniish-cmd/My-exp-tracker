package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ExpenseDatabase
import com.example.data.model.*
import com.example.data.repository.ExpenseRepository
import com.example.service.InsightCardItem
import com.example.service.SmartInsightEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    TRANSACTIONS("Transactions"),
    BUDGETS("Budgets"),
    ANALYTICS("Analytics"),
    RECURRING("Recurring"),
    INSIGHTS("Smart Insights"),
    SETTINGS("Settings")
}

enum class DateFilter(val displayName: String) {
    ALL("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

enum class SortOrder(val displayName: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST_AMOUNT("Highest Amount"),
    LOWEST_AMOUNT("Lowest Amount")
}

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

data class FilterState(
    val query: String = "",
    val type: TransactionType? = null,
    val category: String? = "All",
    val dateFilter: DateFilter = DateFilter.ALL,
    val sortOrder: SortOrder = SortOrder.NEWEST
)

data class DashboardSummary(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSavings: Double = 0.0,
    val monthlySpending: Double = 0.0,
    val highestSpendingCategory: String = "None",
    val highestCategoryAmount: Double = 0.0,
    val savingsRate: Double = 0.0,
    val avgDailySpending: Double = 0.0,
    val avgMonthlySpending: Double = 0.0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    init {
        val db = ExpenseDatabase.getDatabase(application)
        repository = ExpenseRepository(
            transactionDao = db.transactionDao(),
            budgetDao = db.budgetDao(),
            recurringDao = db.recurringDao()
        )
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Settings State
    private val _currencySymbol = MutableStateFlow("₹")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _userName = MutableStateFlow("Alex")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Combined Filter State
    private val _filterState = MutableStateFlow(FilterState())

    val searchQuery: StateFlow<String> = _filterState.map { it.query }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val typeFilter: StateFlow<TransactionType?> = _filterState.map { it.type }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categoryFilter: StateFlow<String?> = _filterState.map { it.category }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "All")

    val dateFilter: StateFlow<DateFilter> = _filterState.map { it.dateFilter }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DateFilter.ALL)

    val sortOrder: StateFlow<SortOrder> = _filterState.map { it.sortOrder }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.NEWEST)

    // Spending Trend Timeframe (7D, 30D, 3M, 6M, 1Y)
    private val _trendTimeframe = MutableStateFlow("30 Days")
    val trendTimeframe: StateFlow<String> = _trendTimeframe.asStateFlow()

    // Toast / Feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Editing transaction state (null = add new)
    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction: StateFlow<TransactionEntity?> = _editingTransaction.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    private val _showAddBudgetDialog = MutableStateFlow(false)
    val showAddBudgetDialog: StateFlow<Boolean> = _showAddBudgetDialog.asStateFlow()

    private val _showAddRecurringDialog = MutableStateFlow(false)
    val showAddRecurringDialog: StateFlow<Boolean> = _showAddRecurringDialog.asStateFlow()

    private val _showCsvImportDialog = MutableStateFlow(false)
    val showCsvImportDialog: StateFlow<Boolean> = _showCsvImportDialog.asStateFlow()

    // Data streams from database
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurring: StateFlow<List<RecurringTransactionEntity>> = repository.allRecurring
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Auto-seed demo data if DB is empty on first launch
        viewModelScope.launch {
            val existing = repository.allTransactions.first()
            if (existing.isEmpty()) {
                repository.loadDemoData(clearExisting = false)
            }
        }
    }

    // Filtered transactions for the Transaction History screen
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        _filterState
    ) { txList, filter ->
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        val query = filter.query
        val type = filter.type
        val category = filter.category
        val dateFilter = filter.dateFilter
        val sort = filter.sortOrder

        var filtered = txList.filter { tx ->
            val matchesQuery = query.isBlank() ||
                    tx.description.contains(query, ignoreCase = true) ||
                    tx.notes.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.paymentMethod.contains(query, ignoreCase = true)

            val matchesType = type == null || tx.type == type.name

            val matchesCategory = category == null || category == "All" ||
                    tx.category.equals(category, ignoreCase = true)

            val matchesDate = when (dateFilter) {
                DateFilter.ALL -> true
                DateFilter.TODAY -> {
                    calendar.timeInMillis = now
                    val nowDay = calendar.get(Calendar.DAY_OF_YEAR)
                    val nowYear = calendar.get(Calendar.YEAR)
                    calendar.timeInMillis = tx.date
                    calendar.get(Calendar.DAY_OF_YEAR) == nowDay && calendar.get(Calendar.YEAR) == nowYear
                }
                DateFilter.THIS_WEEK -> {
                    calendar.timeInMillis = now
                    val nowWeek = calendar.get(Calendar.WEEK_OF_YEAR)
                    val nowYear = calendar.get(Calendar.YEAR)
                    calendar.timeInMillis = tx.date
                    calendar.get(Calendar.WEEK_OF_YEAR) == nowWeek && calendar.get(Calendar.YEAR) == nowYear
                }
                DateFilter.THIS_MONTH -> {
                    calendar.timeInMillis = now
                    val nowMonth = calendar.get(Calendar.MONTH)
                    val nowYear = calendar.get(Calendar.YEAR)
                    calendar.timeInMillis = tx.date
                    calendar.get(Calendar.MONTH) == nowMonth && calendar.get(Calendar.YEAR) == nowYear
                }
            }

            matchesQuery && matchesType && matchesCategory && matchesDate
        }

        filtered = when (sort) {
            SortOrder.NEWEST -> filtered.sortedByDescending { it.date }
            SortOrder.OLDEST -> filtered.sortedBy { it.date }
            SortOrder.HIGHEST_AMOUNT -> filtered.sortedByDescending { it.amount }
            SortOrder.LOWEST_AMOUNT -> filtered.sortedBy { it.amount }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated Dashboard Summary
    val dashboardSummary: StateFlow<DashboardSummary> = transactions.map { txList ->
        val totalIncome = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val totalExpenses = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses
        val savings = balance

        val calendar = Calendar.getInstance()
        val curMonth = calendar.get(Calendar.MONTH)
        val curYear = calendar.get(Calendar.YEAR)

        val thisMonthExpenses = txList.filter {
            calendar.timeInMillis = it.date
            it.type == TransactionType.EXPENSE.name &&
                    calendar.get(Calendar.MONTH) == curMonth &&
                    calendar.get(Calendar.YEAR) == curYear
        }

        val monthlySpending = thisMonthExpenses.sumOf { it.amount }

        val categoryGroups = thisMonthExpenses.groupBy { it.category }
            .mapValues { it.value.sumOf { item -> item.amount } }

        val highestEntry = categoryGroups.maxByOrNull { it.value }

        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpenses) / totalIncome) * 100 else 0.0

        val daysCount = if (txList.isNotEmpty()) {
            val minDate = txList.minOf { it.date }
            val diff = ((System.currentTimeMillis() - minDate) / 86400000L).coerceAtLeast(1)
            diff
        } else 1L

        val avgDaily = totalExpenses / daysCount
        val avgMonthly = totalExpenses / (daysCount / 30.0).coerceAtLeast(1.0)

        DashboardSummary(
            totalBalance = balance,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            totalSavings = savings,
            monthlySpending = monthlySpending,
            highestSpendingCategory = highestEntry?.key ?: "None",
            highestCategoryAmount = highestEntry?.value ?: 0.0,
            savingsRate = savingsRate,
            avgDailySpending = avgDaily,
            avgMonthlySpending = avgMonthly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Smart Spending Insights Flow
    val smartInsights: StateFlow<List<InsightCardItem>> = combine(
        transactions,
        budgets,
        recurring,
        _currencySymbol
    ) { txList, bList, rList, symbol ->
        SmartInsightEngine.generateInsights(txList, bList, rList, symbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setCurrency(symbol: String) {
        _currencySymbol.value = symbol
        showToast("Currency set to $symbol")
    }

    fun setTheme(mode: ThemeMode) {
        _themeMode.value = mode
        showToast("Theme updated to ${mode.name.lowercase().replaceFirstChar { it.uppercase() }}")
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(query = query) }
    }

    fun setTypeFilter(type: TransactionType?) {
        _filterState.update { it.copy(type = type) }
    }

    fun setCategoryFilter(category: String?) {
        _filterState.update { it.copy(category = category) }
    }

    fun setDateFilter(filter: DateFilter) {
        _filterState.update { it.copy(dateFilter = filter) }
    }

    fun setSortOrder(order: SortOrder) {
        _filterState.update { it.copy(sortOrder = order) }
    }

    fun setTrendTimeframe(timeframe: String) {
        _trendTimeframe.value = timeframe
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun openAddTransactionDialog() {
        _editingTransaction.value = null
        _showAddEditDialog.value = true
    }

    fun openEditTransactionDialog(transaction: TransactionEntity) {
        _editingTransaction.value = transaction
        _showAddEditDialog.value = true
    }

    fun closeAddEditDialog() {
        _showAddEditDialog.value = false
        _editingTransaction.value = null
    }

    fun openAddBudgetDialog() {
        _showAddBudgetDialog.value = true
    }

    fun closeAddBudgetDialog() {
        _showAddBudgetDialog.value = false
    }

    fun openAddRecurringDialog() {
        _showAddRecurringDialog.value = true
    }

    fun closeAddRecurringDialog() {
        _showAddRecurringDialog.value = false
    }

    fun openCsvImportDialog() {
        _showCsvImportDialog.value = true
    }

    fun closeCsvImportDialog() {
        _showCsvImportDialog.value = false
    }

    fun saveTransaction(
        id: Long = 0,
        type: String,
        amount: Double,
        description: String,
        category: String,
        paymentMethod: String,
        date: Long,
        notes: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id,
                type = type,
                amount = amount,
                description = description.trim(),
                category = category,
                paymentMethod = paymentMethod,
                date = date,
                notes = notes.trim()
            )
            if (id == 0L) {
                repository.insertTransaction(entity)
                showToast("Transaction added successfully")
            } else {
                repository.updateTransaction(entity)
                showToast("Transaction updated")
            }
            closeAddEditDialog()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
            showToast("Transaction deleted")
        }
    }

    fun saveBudget(category: String, amount: Double) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val budget = BudgetEntity(
                category = category,
                amount = amount,
                month = cal.get(Calendar.MONTH) + 1,
                year = cal.get(Calendar.YEAR)
            )
            repository.insertBudget(budget)
            showToast("Budget set for $category")
            closeAddBudgetDialog()
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(BudgetEntity(id = id, category = "", amount = 0.0, month = 0, year = 0))
            showToast("Budget removed")
        }
    }

    fun saveRecurring(
        name: String,
        amount: Double,
        category: String,
        frequency: String,
        startDate: Long
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.timeInMillis = startDate
            val nextDate = when (frequency) {
                RecurrenceFrequency.DAILY.name -> startDate + 86400000L
                RecurrenceFrequency.WEEKLY.name -> startDate + (7L * 86400000L)
                RecurrenceFrequency.MONTHLY.name -> {
                    cal.add(Calendar.MONTH, 1)
                    cal.timeInMillis
                }
                RecurrenceFrequency.YEARLY.name -> {
                    cal.add(Calendar.YEAR, 1)
                    cal.timeInMillis
                }
                else -> startDate + (30L * 86400000L)
            }

            val entity = RecurringTransactionEntity(
                name = name.trim(),
                amount = amount,
                category = category,
                frequency = frequency,
                startDate = startDate,
                nextDate = nextDate,
                isActive = true
            )
            repository.insertRecurring(entity)
            showToast("Recurring payment added: $name")
            closeAddRecurringDialog()
        }
    }

    fun deleteRecurring(recurringEntity: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(recurringEntity)
            showToast("Recurring payment removed")
        }
    }

    fun importTransactions(list: List<TransactionEntity>) {
        viewModelScope.launch {
            for (tx in list) {
                repository.insertTransaction(tx)
            }
            showToast("Imported ${list.size} transactions")
            closeCsvImportDialog()
        }
    }

    fun loadSampleData(clearExisting: Boolean = false) {
        viewModelScope.launch {
            repository.loadDemoData(clearExisting)
            showToast(if (clearExisting) "Sample demo data reloaded" else "Sample demo data added")
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllData()
            showToast("All financial data cleared")
        }
    }
}

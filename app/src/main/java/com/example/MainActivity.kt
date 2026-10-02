package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.ThemeMode
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SmartExpenseTrackerTheme(darkTheme = isDarkTheme) {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
                val allTransactions by viewModel.transactions.collectAsStateWithLifecycle()
                val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
                val budgets by viewModel.budgets.collectAsStateWithLifecycle()
                val recurring by viewModel.recurring.collectAsStateWithLifecycle()
                val insights by viewModel.smartInsights.collectAsStateWithLifecycle()
                val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
                val userName by viewModel.userName.collectAsStateWithLifecycle()
                val timeframe by viewModel.trendTimeframe.collectAsStateWithLifecycle()

                // Filter & Search states
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
                val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
                val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
                val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

                // Dialog states
                val showAddEdit by viewModel.showAddEditDialog.collectAsStateWithLifecycle()
                val editingTx by viewModel.editingTransaction.collectAsStateWithLifecycle()
                val showAddBudget by viewModel.showAddBudgetDialog.collectAsStateWithLifecycle()
                val showAddRecurring by viewModel.showAddRecurringDialog.collectAsStateWithLifecycle()
                val showCsvImport by viewModel.showCsvImportDialog.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(toastMessage) {
                    toastMessage?.let {
                        snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                        viewModel.clearToast()
                    }
                }

                // Handle system back navigation
                BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                    viewModel.navigateTo(AppScreen.DASHBOARD)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(FintechGoldPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "₹",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF132238)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Smart Expense Tracker",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = if (currentScreen == AppScreen.SETTINGS) FintechGoldPrimary
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.DASHBOARD,
                                onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF132238),
                                    indicatorColor = FintechGoldPrimary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.TRANSACTIONS,
                                onClick = { viewModel.navigateTo(AppScreen.TRANSACTIONS) },
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transactions") },
                                label = { Text("Transactions", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF132238),
                                    indicatorColor = FintechGoldPrimary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.BUDGETS,
                                onClick = { viewModel.navigateTo(AppScreen.BUDGETS) },
                                icon = { Icon(Icons.Default.Savings, contentDescription = "Budgets") },
                                label = { Text("Budgets", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF132238),
                                    indicatorColor = FintechGoldPrimary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.ANALYTICS,
                                onClick = { viewModel.navigateTo(AppScreen.ANALYTICS) },
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                                label = { Text("Analytics", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF132238),
                                    indicatorColor = FintechGoldPrimary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.RECURRING,
                                onClick = { viewModel.navigateTo(AppScreen.RECURRING) },
                                icon = { Icon(Icons.Default.Repeat, contentDescription = "Recurring") },
                                label = { Text("Recurring", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF132238),
                                    indicatorColor = FintechGoldPrimary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.INSIGHTS,
                                onClick = { viewModel.navigateTo(AppScreen.INSIGHTS) },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Insights") },
                                label = { Text("Insights", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF132238),
                                    indicatorColor = FintechGoldPrimary
                                )
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.TRANSACTIONS) {
                            FloatingActionButton(
                                onClick = { viewModel.openAddTransactionDialog() },
                                containerColor = FintechGoldPrimary,
                                contentColor = Color(0xFF132238),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("fab_add_transaction")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = FintechGoldPrimary,
                                contentColor = Color(0xFF132238),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (currentScreen) {
                            AppScreen.DASHBOARD -> DashboardScreen(
                                summary = summary,
                                transactions = allTransactions,
                                budgets = budgets,
                                insights = insights,
                                currencySymbol = currencySymbol,
                                userName = userName,
                                selectedTimeframe = timeframe,
                                onTimeframeSelected = { viewModel.setTrendTimeframe(it) },
                                onAddTransactionClick = { viewModel.openAddTransactionDialog() },
                                onViewAllTransactions = { viewModel.navigateTo(AppScreen.TRANSACTIONS) },
                                onEditTransaction = { viewModel.openEditTransactionDialog(it) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                onNavigateTo = { viewModel.navigateTo(it) }
                            )

                            AppScreen.TRANSACTIONS -> TransactionsScreen(
                                transactions = filteredTransactions,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                typeFilter = typeFilter,
                                onTypeFilterChange = { viewModel.setTypeFilter(it) },
                                categoryFilter = categoryFilter,
                                onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                                dateFilter = dateFilter,
                                onDateFilterChange = { viewModel.setDateFilter(it) },
                                sortOrder = sortOrder,
                                onSortOrderChange = { viewModel.setSortOrder(it) },
                                currencySymbol = currencySymbol,
                                onEditTransaction = { viewModel.openEditTransactionDialog(it) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                onAddTransactionClick = { viewModel.openAddTransactionDialog() }
                            )

                            AppScreen.BUDGETS -> BudgetsScreen(
                                budgets = budgets,
                                transactions = allTransactions,
                                currencySymbol = currencySymbol,
                                onAddBudgetClick = { viewModel.openAddBudgetDialog() },
                                onDeleteBudget = { viewModel.deleteBudget(it) }
                            )

                            AppScreen.ANALYTICS -> AnalyticsScreen(
                                summary = summary,
                                transactions = allTransactions,
                                currencySymbol = currencySymbol
                            )

                            AppScreen.RECURRING -> RecurringScreen(
                                recurringList = recurring,
                                currencySymbol = currencySymbol,
                                onAddRecurringClick = { viewModel.openAddRecurringDialog() },
                                onDeleteRecurring = { viewModel.deleteRecurring(it) }
                            )

                            AppScreen.INSIGHTS -> InsightsScreen(
                                insights = insights
                            )

                            AppScreen.SETTINGS -> SettingsScreen(
                                userName = userName,
                                onUserNameChange = { viewModel.setUserName(it) },
                                currencySymbol = currencySymbol,
                                onCurrencyChange = { viewModel.setCurrency(it) },
                                themeMode = themeMode,
                                onThemeChange = { viewModel.setTheme(it) },
                                transactions = allTransactions,
                                onOpenCsvImport = { viewModel.openCsvImportDialog() },
                                onLoadDemoData = { viewModel.loadSampleData(clearExisting = true) },
                                onClearAllData = { viewModel.clearAllUserData() },
                                onShowToast = { viewModel.showToast(it) }
                            )
                        }
                    }

                    // Dialogs
                    if (showAddEdit) {
                        AddEditTransactionDialog(
                            initialTransaction = editingTx,
                            currencySymbol = currencySymbol,
                            onDismiss = { viewModel.closeAddEditDialog() },
                            onSave = { id, type, amount, desc, category, pm, date, notes ->
                                viewModel.saveTransaction(id, type, amount, desc, category, pm, date, notes)
                            }
                        )
                    }

                    if (showAddBudget) {
                        AddBudgetDialog(
                            currencySymbol = currencySymbol,
                            onDismiss = { viewModel.closeAddBudgetDialog() },
                            onSave = { category, amount ->
                                viewModel.saveBudget(category, amount)
                            }
                        )
                    }

                    if (showAddRecurring) {
                        AddRecurringDialog(
                            currencySymbol = currencySymbol,
                            onDismiss = { viewModel.closeAddRecurringDialog() },
                            onSave = { name, amount, category, frequency, startDate ->
                                viewModel.saveRecurring(name, amount, category, frequency, startDate)
                            }
                        )
                    }

                    if (showCsvImport) {
                        CsvImportDialog(
                            onDismiss = { viewModel.closeCsvImportDialog() },
                            onConfirmImport = { list ->
                                viewModel.importTransactions(list)
                            }
                        )
                    }
                }
            }
        }
    }
}

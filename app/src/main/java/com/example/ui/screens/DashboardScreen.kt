package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.service.InsightCardItem
import com.example.service.InsightType
import com.example.ui.AppScreen
import com.example.ui.DashboardSummary
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    transactions: List<TransactionEntity>,
    budgets: List<BudgetEntity>,
    insights: List<InsightCardItem>,
    currencySymbol: String,
    userName: String,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    onAddTransactionClick: () -> Unit,
    onViewAllTransactions: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onNavigateTo: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning, $userName 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Here's your financial overview.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Add Transaction Button
                IconButton(
                    onClick = onAddTransactionClick,
                    modifier = Modifier
                        .testTag("dashboard_add_tx_button")
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(FintechGoldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = Color(0xFF132238),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Primary Stat Cards Grid (Total Balance, Total Income, Total Expenses, Savings)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Balance",
                        amount = summary.totalBalance,
                        currencySymbol = currencySymbol,
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = FintechGoldPrimary,
                        accentBrush = Brush.horizontalGradient(listOf(FintechGoldPrimary, FintechGoldLight)),
                        subText = if (summary.totalBalance >= 0) "Healthy liquidity" else "Attention required",
                        modifier = Modifier.weight(1f),
                        testTag = "total_balance_card"
                    )

                    StatCard(
                        title = "Net Savings",
                        amount = summary.totalSavings,
                        currencySymbol = currencySymbol,
                        icon = Icons.Default.Savings,
                        iconTint = FintechEmerald,
                        accentBrush = Brush.horizontalGradient(listOf(FintechEmerald, Color(0xFF34D399))),
                        subText = "Rate: ${String.format(Locale.getDefault(), "%.1f", summary.savingsRate)}%",
                        modifier = Modifier.weight(1f),
                        testTag = "net_savings_card"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Income",
                        amount = summary.totalIncome,
                        currencySymbol = currencySymbol,
                        icon = Icons.Default.TrendingUp,
                        iconTint = FintechEmerald,
                        modifier = Modifier.weight(1f),
                        testTag = "total_income_card"
                    )

                    StatCard(
                        title = "Total Expenses",
                        amount = summary.totalExpenses,
                        currencySymbol = currencySymbol,
                        icon = Icons.Default.TrendingDown,
                        iconTint = FintechCoral,
                        modifier = Modifier.weight(1f),
                        testTag = "total_expenses_card"
                    )
                }
            }
        }

        // 3. Secondary Metrics (Monthly Spending & Highest Category)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Current Month Spending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(summary.monthlySpending.toLong())}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Top Spending Category",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (summary.highestSpendingCategory != "None") {
                                "${summary.highestSpendingCategory} ($currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(summary.highestCategoryAmount.toLong())})"
                            } else "None",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FintechGoldPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 4. Chart 1 — Spending Trend (Line Chart with timeframe toggle)
        item {
            SpendingTrendCard(
                transactions = transactions,
                selectedTimeframe = selectedTimeframe,
                onTimeframeSelected = onTimeframeSelected,
                currencySymbol = currencySymbol
            )
        }

        // 5. Chart 2 — Expense Categories Doughnut Chart
        item {
            ExpenseCategoriesDoughnutCard(
                transactions = transactions,
                currencySymbol = currencySymbol
            )
        }

        // 6. Chart 3 — Income vs Expenses Bar comparison
        item {
            IncomeVsExpensesCard(
                totalIncome = summary.totalIncome,
                totalExpenses = summary.totalExpenses,
                currencySymbol = currencySymbol
            )
        }

        // 7. Chart 4 — Monthly Spending (Jan - Dec)
        item {
            MonthlyExpensesChartCard(
                transactions = transactions,
                currencySymbol = currencySymbol
            )
        }

        // 8. Smart Insights Snapshot Card
        if (insights.isNotEmpty()) {
            item {
                val topInsight = insights.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            when (topInsight.type) {
                                InsightType.ALERT -> FintechCoral.copy(alpha = 0.12f)
                                InsightType.WARNING -> FintechAmber.copy(alpha = 0.12f)
                                InsightType.POSITIVE -> FintechEmerald.copy(alpha = 0.12f)
                                InsightType.INFO -> FintechGoldPrimary.copy(alpha = 0.12f)
                            }
                        )
                        .border(
                            1.dp,
                            when (topInsight.type) {
                                InsightType.ALERT -> FintechCoral.copy(alpha = 0.4f)
                                InsightType.WARNING -> FintechAmber.copy(alpha = 0.4f)
                                InsightType.POSITIVE -> FintechEmerald.copy(alpha = 0.4f)
                                InsightType.INFO -> FintechGoldPrimary.copy(alpha = 0.4f)
                            },
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { onNavigateTo(AppScreen.INSIGHTS) }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (topInsight.type) {
                                        InsightType.ALERT -> FintechCoral.copy(alpha = 0.2f)
                                        InsightType.WARNING -> FintechAmber.copy(alpha = 0.2f)
                                        InsightType.POSITIVE -> FintechEmerald.copy(alpha = 0.2f)
                                        InsightType.INFO -> FintechGoldPrimary.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (topInsight.type) {
                                    InsightType.ALERT -> Icons.Default.Warning
                                    InsightType.WARNING -> Icons.Default.Info
                                    InsightType.POSITIVE -> Icons.Default.CheckCircle
                                    InsightType.INFO -> Icons.Default.Lightbulb
                                },
                                contentDescription = null,
                                tint = when (topInsight.type) {
                                    InsightType.ALERT -> FintechCoral
                                    InsightType.WARNING -> FintechAmber
                                    InsightType.POSITIVE -> FintechEmerald
                                    InsightType.INFO -> FintechGoldPrimary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = topInsight.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "View All →",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FintechGoldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = topInsight.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 9. Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onViewAllTransactions) {
                    Text(
                        text = "View All",
                        color = FintechGoldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 10. Recent Transactions Items (top 5)
        val recentTransactions = transactions.take(5)
        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap + to add your first transaction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                TransactionItemRow(
                    transaction = tx,
                    currencySymbol = currencySymbol,
                    onEdit = { onEditTransaction(tx) },
                    onDelete = { onDeleteTransaction(tx.id) }
                )
            }
        }
    }
}

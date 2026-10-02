package com.example.service

import com.example.data.model.BudgetEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

data class InsightCardItem(
    val title: String,
    val message: String,
    val type: InsightType, // POSITIVE, WARNING, ALERT, INFO
    val iconName: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class InsightType {
    POSITIVE,
    WARNING,
    ALERT,
    INFO
}

object SmartInsightEngine {

    fun generateInsights(
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        recurring: List<RecurringTransactionEntity>,
        currencySymbol: String = "₹"
    ): List<InsightCardItem> {
        val insights = mutableListOf<InsightCardItem>()

        if (transactions.isEmpty()) {
            insights.add(
                InsightCardItem(
                    title = "Getting Started",
                    message = "Add your first income or expense transaction to unlock personalized AI spending analytics.",
                    type = InsightType.INFO,
                    iconName = "info"
                )
            )
            return insights
        }

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val thisMonthTransactions = transactions.filter {
            calendar.timeInMillis = it.date
            calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
        }

        val lastMonth = if (currentMonth == 0) 11 else currentMonth - 1
        val lastMonthYear = if (currentMonth == 0) currentYear - 1 else currentYear
        val lastMonthTransactions = transactions.filter {
            calendar.timeInMillis = it.date
            calendar.get(Calendar.MONTH) == lastMonth && calendar.get(Calendar.YEAR) == lastMonthYear
        }

        val totalIncome = thisMonthTransactions
            .filter { it.type == TransactionType.INCOME.name }
            .sumOf { it.amount }

        val totalExpense = thisMonthTransactions
            .filter { it.type == TransactionType.EXPENSE.name }
            .sumOf { it.amount }

        val savings = totalIncome - totalExpense

        // 1. Savings Rate Analysis
        if (totalIncome > 0) {
            val savingsRate = (savings / totalIncome) * 100
            when {
                savingsRate >= 40 -> {
                    insights.add(
                        InsightCardItem(
                            title = "Exceptional Savings Rate",
                            message = "Your savings rate is ${String.format(Locale.getDefault(), "%.1f", savingsRate)}% this month! You are well on track for your financial goals.",
                            type = InsightType.POSITIVE,
                            iconName = "trending_up"
                        )
                    )
                }
                savingsRate >= 20 -> {
                    insights.add(
                        InsightCardItem(
                            title = "Healthy Savings Habit",
                            message = "You have saved ${currencySymbol}${formatAmount(savings)} (${String.format(Locale.getDefault(), "%.1f", savingsRate)}% of total income). Keep it up!",
                            type = InsightType.POSITIVE,
                            iconName = "savings"
                        )
                    )
                }
                savingsRate in 0.0..19.9 -> {
                    insights.add(
                        InsightCardItem(
                            title = "Low Savings Margin",
                            message = "Your savings rate is ${String.format(Locale.getDefault(), "%.1f", savingsRate)}%. Consider trimming non-essential subscriptions or dining out.",
                            type = InsightType.WARNING,
                            iconName = "warning"
                        )
                    )
                }
                else -> {
                    insights.add(
                        InsightCardItem(
                            title = "Expenses Exceed Income",
                            message = "You have spent ${currencySymbol}${formatAmount(totalExpense - totalIncome)} more than your income this month. Review your budget immediately.",
                            type = InsightType.ALERT,
                            iconName = "alert"
                        )
                    )
                }
            }
        }

        // 2. Highest Spending Category & Concentration
        val expensesByCategory = thisMonthTransactions
            .filter { it.type == TransactionType.EXPENSE.name }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        if (expensesByCategory.isNotEmpty() && totalExpense > 0) {
            val highestCategoryEntry = expensesByCategory.maxByOrNull { it.value }
            if (highestCategoryEntry != null) {
                val percentage = (highestCategoryEntry.value / totalExpense) * 100
                if (percentage >= 35) {
                    insights.add(
                        InsightCardItem(
                            title = "High Spending Concentration",
                            message = "Your largest spending category is ${highestCategoryEntry.key} (${currencySymbol}${formatAmount(highestCategoryEntry.value)}), accounting for ${String.format(Locale.getDefault(), "%.1f", percentage)}% of all expenses.",
                            type = if (percentage >= 50) InsightType.WARNING else InsightType.INFO,
                            iconName = "pie_chart"
                        )
                    )
                }
            }
        }

        // 3. Month-over-Month Comparison
        val lastMonthExpense = lastMonthTransactions
            .filter { it.type == TransactionType.EXPENSE.name }
            .sumOf { it.amount }

        if (lastMonthExpense > 0 && totalExpense > 0) {
            val changePercent = ((totalExpense - lastMonthExpense) / lastMonthExpense) * 100
            if (changePercent > 15) {
                insights.add(
                    InsightCardItem(
                        title = "Spending Spike Detected",
                        message = "Your monthly spending increased by ${String.format(Locale.getDefault(), "%.1f", changePercent)}% compared to last month.",
                        type = InsightType.WARNING,
                        iconName = "trending_up"
                    )
                )
            } else if (changePercent < -10) {
                insights.add(
                    InsightCardItem(
                        title = "Great Spending Reduction",
                        message = "You spent ${String.format(Locale.getDefault(), "%.1f", kotlin.math.abs(changePercent))}% less this month compared to last month. Excellent discipline!",
                        type = InsightType.POSITIVE,
                        iconName = "check_circle"
                    )
                )
            }
        }

        // 4. Weekend vs Weekday Spending Behavior
        var weekendExpense = 0.0
        var weekdayExpense = 0.0
        for (tx in thisMonthTransactions.filter { it.type == TransactionType.EXPENSE.name }) {
            calendar.timeInMillis = tx.date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                weekendExpense += tx.amount
            } else {
                weekdayExpense += tx.amount
            }
        }

        if (totalExpense > 0 && (weekendExpense / totalExpense) >= 0.40) {
            val weekendPct = (weekendExpense / totalExpense) * 100
            insights.add(
                InsightCardItem(
                    title = "Weekend Spending Pattern",
                    message = "Your spending is heavily concentrated on weekends (${String.format(Locale.getDefault(), "%.0f", weekendPct)}% of expenses on Sat & Sun).",
                    type = InsightType.INFO,
                    iconName = "event"
                )
            )
        }

        // 5. Budget Alerts
        for (budget in budgets) {
            val categorySpent = expensesByCategory[budget.category] ?: 0.0
            val ratio = if (budget.amount > 0) categorySpent / budget.amount else 0.0
            if (ratio >= 1.0) {
                insights.add(
                    InsightCardItem(
                        title = "Budget Exceeded: ${budget.category}",
                        message = "You exceeded your ${budget.category} budget of ${currencySymbol}${formatAmount(budget.amount)} by ${currencySymbol}${formatAmount(categorySpent - budget.amount)}.",
                        type = InsightType.ALERT,
                        iconName = "error"
                    )
                )
            } else if (ratio >= 0.90) {
                insights.add(
                    InsightCardItem(
                        title = "Budget Alert: ${budget.category}",
                        message = "${budget.category} spending is at ${String.format(Locale.getDefault(), "%.0f", ratio * 100)}% of limit. Only ${currencySymbol}${formatAmount(budget.amount - categorySpent)} remaining.",
                        type = InsightType.ALERT,
                        iconName = "warning"
                    )
                )
            } else if (ratio >= 0.75) {
                insights.add(
                    InsightCardItem(
                        title = "Budget Warning: ${budget.category}",
                        message = "You have used ${String.format(Locale.getDefault(), "%.0f", ratio * 100)}% of your ${budget.category} budget.",
                        type = InsightType.WARNING,
                        iconName = "info"
                    )
                )
            }
        }

        // 6. Upcoming Recurring Subscriptions
        val now = System.currentTimeMillis()
        val oneWeekFromNow = now + (7L * 86400000L)
        val upcomingRecurring = recurring.filter { it.isActive && it.nextDate in now..oneWeekFromNow }
        if (upcomingRecurring.isNotEmpty()) {
            val names = upcomingRecurring.joinToString(", ") { it.name }
            val sumDue = upcomingRecurring.sumOf { it.amount }
            insights.add(
                InsightCardItem(
                    title = "Upcoming Payments Due",
                    message = "${upcomingRecurring.size} recurring bill(s) totaling ${currencySymbol}${formatAmount(sumDue)} due this week: $names.",
                    type = InsightType.INFO,
                    iconName = "schedule"
                )
            )
        }

        return insights
    }

    private fun formatAmount(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(amount.toLong())
    }
}

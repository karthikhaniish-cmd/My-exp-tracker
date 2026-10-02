package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.FintechEmerald
import com.example.ui.theme.FintechCoral
import com.example.ui.theme.FintechGoldPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SpendingTrendCard(
    transactions: List<TransactionEntity>,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val timeframes = listOf("7 Days", "30 Days", "3 Months", "6 Months", "1 Year")

    // Filter transactions by timeframe
    val now = System.currentTimeMillis()
    val cutoffMillis = when (selectedTimeframe) {
        "7 Days" -> now - (7L * 86400000L)
        "30 Days" -> now - (30L * 86400000L)
        "3 Months" -> now - (90L * 86400000L)
        "6 Months" -> now - (180L * 86400000L)
        "1 Year" -> now - (365L * 86400000L)
        else -> now - (30L * 86400000L)
    }

    val expenseTransactions = transactions.filter {
        it.type == TransactionType.EXPENSE.name && it.date >= cutoffMillis
    }.sortedBy { it.date }

    // Aggregate into 6-8 interval buckets
    val bucketCount = 7
    val bucketDuration = (now - cutoffMillis) / bucketCount
    val points = mutableListOf<Double>()
    val bucketLabels = mutableListOf<String>()
    val sdf = when (selectedTimeframe) {
        "7 Days" -> SimpleDateFormat("EEE", Locale.US)
        "30 Days" -> SimpleDateFormat("d MMM", Locale.US)
        else -> SimpleDateFormat("MMM", Locale.US)
    }

    for (i in 0 until bucketCount) {
        val bStart = cutoffMillis + (i * bucketDuration)
        val bEnd = bStart + bucketDuration
        val sum = expenseTransactions.filter { it.date in bStart until bEnd }.sumOf { it.amount }
        points.add(sum)
        bucketLabels.add(sdf.format(Date(bStart + (bucketDuration / 2))))
    }

    val maxAmount = points.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time expense dynamics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val totalTrend = points.sum()
                Text(
                    text = "$currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(totalTrend.toLong())}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FintechGoldPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeframe Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timeframes.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) FintechGoldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onTimeframeSelected(tf) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF132238) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line Chart Canvas
            val primaryColor = FintechGoldPrimary
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val width = size.width
                val height = size.height
                val padY = 16f

                // Draw background grid lines (3 horizontal lines)
                for (step in 0..3) {
                    val y = padY + (height - 2 * padY) * (step / 3f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                if (points.isEmpty()) return@Canvas

                val stepX = width / (points.size - 1).coerceAtLeast(1)

                val linePath = Path()
                val fillPath = Path()

                fillPath.moveTo(0f, height - padY)

                points.forEachIndexed { i, value ->
                    val x = i * stepX
                    val normalizedY = ((maxAmount - value) / maxAmount).toFloat()
                    val y = padY + normalizedY * (height - 2 * padY)

                    if (i == 0) {
                        linePath.moveTo(x, y)
                        fillPath.lineTo(x, y)
                    } else {
                        // Smooth bezier curve
                        val prevX = (i - 1) * stepX
                        val prevVal = points[i - 1]
                        val prevNormalizedY = ((maxAmount - prevVal) / maxAmount).toFloat()
                        val prevY = padY + prevNormalizedY * (height - 2 * padY)

                        val cx1 = prevX + (x - prevX) / 2f
                        val cy1 = prevY
                        val cx2 = prevX + (x - prevX) / 2f
                        val cy2 = y
                        linePath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                        fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                    }
                }

                fillPath.lineTo((points.size - 1) * stepX, height - padY)
                fillPath.close()

                // Draw gradient area
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw main curve stroke
                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw glowing dots at points
                points.forEachIndexed { i, value ->
                    val x = i * stepX
                    val normalizedY = ((maxAmount - value) / maxAmount).toFloat()
                    val y = padY + normalizedY * (height - 2 * padY)

                    drawCircle(
                        color = Color(0xFF132238),
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-Axis bucket labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bucketLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseCategoriesDoughnutCard(
    transactions: List<TransactionEntity>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE.name }
    val totalExpense = expenseTransactions.sumOf { it.amount }

    val categoryTotals = expenseTransactions.groupBy { it.category }
        .mapValues { it.value.sumOf { item -> item.amount } }
        .toList()
        .sortedByDescending { it.second }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Expense Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Breakdown by spending category",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (totalExpense <= 0 || categoryTotals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expense data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Doughnut Canvas
                    Box(
                        modifier = Modifier
                            .size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(130.dp)) {
                            val strokeWidth = 24.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val arcSize = Size(diameter, diameter)
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                            var currentAngle = -90f

                            categoryTotals.forEach { (catName, amount) ->
                                val sweep = ((amount / totalExpense) * 360f).toFloat()
                                val category = TransactionCategory.fromString(catName)
                                val color = category.getColor()

                                drawArc(
                                    color = color,
                                    startAngle = currentAngle,
                                    sweepAngle = sweep - 1.5f, // slight gap between slices
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                currentAngle += sweep
                            }
                        }

                        // Center content
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(totalExpense.toLong())}",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend column (top 4 categories)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryTotals.take(4).forEach { (catName, amount) ->
                            val cat = TransactionCategory.fromString(catName)
                            val pct = ((amount / totalExpense) * 100).toInt()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(cat.getColor())
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeVsExpensesCard(
    totalIncome: Double,
    totalExpenses: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val totalCombined = (totalIncome + totalExpenses).coerceAtLeast(1.0)
    val incomePercent = (totalIncome / totalCombined).toFloat()
    val expensePercent = (totalExpenses / totalCombined).toFloat()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Income vs Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (totalIncome >= totalExpenses) "Net Positive" else "Deficit",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (totalIncome >= totalExpenses) FintechEmerald else FintechCoral
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Income row bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FintechEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(totalIncome.toLong())}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FintechEmerald
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(incomePercent.coerceIn(0.01f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(FintechEmerald)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expenses row bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FintechCoral)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(totalExpenses.toLong())}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FintechCoral
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(expensePercent.coerceIn(0.01f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(FintechCoral)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyExpensesChartCard(
    transactions: List<TransactionEntity>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    val curYear = calendar.get(Calendar.YEAR)
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    val monthlyData = DoubleArray(12) { 0.0 }
    for (tx in transactions.filter { it.type == TransactionType.EXPENSE.name }) {
        calendar.timeInMillis = tx.date
        if (calendar.get(Calendar.YEAR) == curYear) {
            val m = calendar.get(Calendar.MONTH)
            monthlyData[m] += tx.amount
        }
    }

    val maxVal = monthlyData.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Spending ($curYear)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bars canvas
            val barColor = FintechGoldPrimary
            val activeMonth = Calendar.getInstance().get(Calendar.MONTH)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val width = size.width
                val height = size.height
                val totalBars = 12
                val slotWidth = width / totalBars
                val barWidth = slotWidth * 0.55f

                monthlyData.forEachIndexed { i, valAmt ->
                    val norm = (valAmt / maxVal).toFloat()
                    val barHeight = (height * norm).coerceAtLeast(6f)
                    val x = i * slotWidth + (slotWidth - barWidth) / 2f
                    val y = height - barHeight

                    val color = if (i == activeMonth) barColor else barColor.copy(alpha = 0.45f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Month labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthNames.forEachIndexed { i, m ->
                    Text(
                        text = m,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        fontWeight = if (i == activeMonth) FontWeight.Bold else FontWeight.Normal,
                        color = if (i == activeMonth) FintechGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionCategory
import com.example.ui.theme.FintechAmber
import com.example.ui.theme.FintechCoral
import com.example.ui.theme.FintechEmerald
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetCard(
    budget: BudgetEntity,
    spentAmount: Double,
    currencySymbol: String = "₹",
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = TransactionCategory.fromString(budget.category)
    val ratio = if (budget.amount > 0) (spentAmount / budget.amount).toFloat() else 0f
    val percentage = (ratio * 100).toInt()
    val remaining = (budget.amount - spentAmount).coerceAtLeast(0.0)

    val (statusLabel, statusColor) = when {
        percentage >= 100 -> "Exceeded" to FintechCoral
        percentage >= 90 -> "Alert ($percentage%)" to FintechCoral
        percentage >= 75 -> "Warning ($percentage%)" to FintechAmber
        else -> "Safe ($percentage%)" to FintechEmerald
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (percentage >= 90) statusColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(category.getColor().copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.getIcon(),
                            contentDescription = budget.category,
                            tint = category.getColor(),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = budget.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Limit: $currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(budget.amount.toLong())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ratio.coerceIn(0.01f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spent vs Remaining row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: $currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(spentAmount.toLong())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (percentage >= 100) {
                        "Over by $currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format((spentAmount - budget.amount).toLong())}"
                    } else {
                        "Remaining: $currencySymbol${NumberFormat.getNumberInstance(Locale.getDefault()).format(remaining.toLong())}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (percentage >= 100) FintechCoral else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

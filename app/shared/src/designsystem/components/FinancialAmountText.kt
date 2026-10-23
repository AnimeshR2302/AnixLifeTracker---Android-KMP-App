package com.anix.android.anixlifetracker.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.model.Money
import com.anix.android.anixlifetracker.model.TransactionType
import com.anix.android.anixlifetracker.theme.LocalFinancialColors

private data class TypeConfig(
    val textColor: Color,
    val containerColor: Color,
    val symbol: String,
    val label: String
)

@Composable
fun FinancialAmountText(
    amount: Money,
    type: TransactionType? = null,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    showBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val financialColors = LocalFinancialColors.current

    val effectiveType = type ?: when {
        amount.isPositive -> TransactionType.INCOME
        amount.isNegative -> TransactionType.EXPENSE
        else -> TransactionType.TRANSFER
    }

    val config = when (effectiveType) {
        TransactionType.INCOME -> TypeConfig(
            financialColors.income,
            financialColors.incomeContainer,
            "↑",
            "Income"
        )
        TransactionType.EXPENSE -> TypeConfig(
            financialColors.expense,
            financialColors.expenseContainer,
            "↓",
            "Expense"
        )
        TransactionType.TRANSFER -> TypeConfig(
            financialColors.neutralTransfer,
            financialColors.neutralTransferContainer,
            "⇄",
            "Transfer"
        )
        else -> TypeConfig(
            financialColors.neutralTransfer,
            financialColors.neutralTransferContainer,
            "•",
            effectiveType.name
        )
    }

    val formatted = amount.formattedString(includeSign = true)
    val accessibleText = "${config.label}: $formatted"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics {
            contentDescription = accessibleText
        }
    ) {
        if (showBadge) {
            Box(
                modifier = Modifier
                    .background(config.containerColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = config.symbol,
                        style = MaterialTheme.typography.labelSmall,
                        color = config.textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = config.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = config.textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = config.symbol,
                style = style,
                color = config.textColor,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = formatted,
            style = style,
            color = config.textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

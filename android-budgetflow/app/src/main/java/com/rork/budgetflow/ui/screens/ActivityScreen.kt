package com.rork.budgetflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.data.Dates
import com.rork.budgetflow.data.IconCatalog
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.Transaction
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.IconChip
import com.rork.budgetflow.ui.components.androidClick
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

private enum class TxFilter { ALL, EXPENSE, INCOME }

@Composable
fun ActivityScreen(vm: BudgetViewModel) {
    val data by vm.data.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf(TxFilter.ALL) }

    val filtered = data.transactions.filter {
        when (filter) {
            TxFilter.ALL -> true
            TxFilter.EXPENSE -> !it.isIncome
            TxFilter.INCOME -> it.isIncome
        }
    }
    val grouped = filtered.groupBy { Dates.section(it.timestamp) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Activity",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip("All", filter == TxFilter.ALL) { filter = TxFilter.ALL }
                FilterChip("Expenses", filter == TxFilter.EXPENSE) { filter = TxFilter.EXPENSE }
                FilterChip("Income", filter == TxFilter.INCOME) { filter = TxFilter.INCOME }
            }
        }

        if (filtered.isEmpty()) {
            item { EmptyActivity() }
        }

        grouped.forEach { (section, txs) ->
            item(key = section) {
                Text(
                    section,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                )
            }
            items(txs, key = { it.id }) { tx ->
                val acc = vm.account(tx.accountId)
                val cat = vm.category(tx.categoryId)
                SwipeRow(
                    tx = tx,
                    subtitle = (acc?.name ?: "Account") + (cat?.let { " · ${it.name}" } ?: ""),
                    iconKey = cat?.iconKey ?: "pay",
                    color = cat?.colorArgb?.toColor() ?: Mint,
                    onDelete = { vm.deleteTransaction(tx.id) },
                )
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Mint else InkSurface)
            .androidClick(onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) com.rork.budgetflow.ui.theme.OnMint else TextSecondary,
        )
    }
}

@Composable
private fun SwipeRow(
    tx: Transaction,
    subtitle: String,
    iconKey: String,
    color: Color,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(InkElevated)
            .androidClick { expanded = !expanded }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconChip(color = if (tx.isIncome) Mint else color) {
            Icon(
                IconCatalog.icon(if (tx.isIncome) "pay" else iconKey),
                contentDescription = null,
                tint = if (tx.isIncome) Mint else color,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                tx.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = TextSecondary, maxLines = 1)
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Coral.copy(alpha = 0.16f))
                    .androidClick(onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = Coral, modifier = Modifier.size(20.dp))
            }
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (tx.isIncome) Money.formatSigned(tx.amount) else "-" + Money.format(tx.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (tx.isIncome) Mint else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(Dates.relative(tx.timestamp), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
    }
}

@Composable
private fun EmptyActivity() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconChip(color = Mint, size = 72.dp) {
            Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = Mint, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "No transactions yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap the + button to log your first one",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

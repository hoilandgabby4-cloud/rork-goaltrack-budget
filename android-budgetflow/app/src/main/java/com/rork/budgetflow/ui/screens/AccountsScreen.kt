package com.rork.budgetflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.data.Account
import com.rork.budgetflow.data.AccountType
import com.rork.budgetflow.data.IconCatalog
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.data.Money as MoneyFmt
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.GlassCard
import com.rork.budgetflow.ui.components.IconChip
import com.rork.budgetflow.ui.components.ProgressBar
import com.rork.budgetflow.ui.components.SectionHeader
import com.rork.budgetflow.ui.components.androidClick

import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

@Composable
fun AccountsScreen(
    vm: BudgetViewModel,
    onAddAccount: () -> Unit,
) {
    val data by vm.data.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "Accounts",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
        }
        item {
            SectionHeader(
                title = "Payment sources",
                action = { AddPill(onAddAccount) },
            )
        }
        items(data.accounts, key = { it.id }) { acc ->
            AccountCard(acc, onLongDelete = { vm.deleteAccount(acc.id) })
        }

    }
}

@Composable
private fun AddPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Mint.copy(alpha = 0.14f))
            .androidClick(onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("Add", style = MaterialTheme.typography.labelMedium, color = Mint)
    }
}

@Composable
private fun AccountCard(account: Account, onLongDelete: () -> Unit) {
    val color = account.colorArgb.toColor()
    val isCredit = account.type == AccountType.CREDIT
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(InkElevated, Ink))
            )
            .border(1.dp, color.copy(alpha = 0.20f), RoundedCornerShape(24.dp))
            .androidClick(onLongDelete)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(color = color) {
                    Icon(
                        IconCatalog.icon(accountIcon(account.type)),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        account.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(account.type.label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (isCredit) "Owed" else "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                    Text(
                        Money.format(account.balance),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isCredit) Coral else Mint,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (isCredit && account.creditLimit > 0) {
                Spacer(Modifier.height(16.dp))
                ProgressBar(
                    progress = (account.balance / account.creditLimit).toFloat(),
                    color = color,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        Money.format(account.creditLimit - account.balance) + " available",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "Limit " + Money.formatCompact(account.creditLimit),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                    )
                }
            }
        }
    }
}

private fun accountIcon(type: AccountType): String = when (type) {
    AccountType.CHECKING -> "bank"
    AccountType.SAVINGS -> "savings"
    AccountType.CREDIT -> "card"
    AccountType.CASH -> "wallet"
    AccountType.INCOME -> "pay"
    AccountType.RETIREMENT -> "retirement"
}

@Composable
private fun BudgetCard(
    name: String,
    iconKey: String,
    color: Color,
    used: Double,
    budget: Double,
) {
    val ratio = (used / budget).toFloat().coerceIn(0f, 1f)
    val over = used > budget
    GlassCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconChip(color = color) {
                Icon(IconCatalog.icon(iconKey), contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        MoneyFmt.format(used) + " / " + MoneyFmt.formatCompact(budget),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (over) Coral else TextSecondary,
                    )
                }
                Spacer(Modifier.height(8.dp))
                ProgressBar(
                    progress = ratio,
                    color = if (over) Coral else color,
                    modifier = Modifier.fillMaxWidth(),
                    height = 7.dp,
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    name: String,
    iconKey: String,
    color: Color,
    used: Double,
    budget: Double,
    suggestedPct: Double = 0.0,
    onEdit: () -> Unit,
    onLongDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(InkElevated)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(color = color) {
                Icon(IconCatalog.icon(iconKey), contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (budget > 0) "Budget " + Money.formatCompact(budget) + "/mo" else "No budget set",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                    if (suggestedPct > 0) {
                        Text(
                            " · ${suggestedPct.toInt()}% suggested",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                    }
                }
            }
            Text(
                Money.format(used),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Hairline)
                    .androidClick(onEdit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

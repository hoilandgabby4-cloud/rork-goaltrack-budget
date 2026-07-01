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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.rork.budgetflow.data.Dates
import com.rork.budgetflow.data.HouseholdProfile
import com.rork.budgetflow.data.IconCatalog
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.data.Category
import com.rork.budgetflow.ui.components.FinancialHealthDashboard
import com.rork.budgetflow.ui.components.GlassCard
import com.rork.budgetflow.ui.components.androidClick
import com.rork.budgetflow.ui.components.IconChip
import com.rork.budgetflow.ui.components.PieSlice
import com.rork.budgetflow.ui.components.ProgressBar
import com.rork.budgetflow.ui.components.SectionHeader
import com.rork.budgetflow.ui.components.SpendingPieChart
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Gold
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.MintBright
import com.rork.budgetflow.ui.theme.MintDeep
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

@Composable
fun DashboardScreen(
    vm: BudgetViewModel,
    onSeeAccounts: () -> Unit,
    onSeeActivity: () -> Unit,
    onAdd: () -> Unit,
    onEditHousehold: () -> Unit = {},
) {
    val data by vm.data.collectAsStateWithLifecycle()
    val net = vm.netWorth()
    val available = vm.totalAvailable()
    val owed = vm.totalCreditOwed()
    val spent = remember(data) { vm.spentThisMonth(data) }
    val income = remember(data) { vm.incomeThisMonth(data) }

    val health = remember(data) { vm.financialHealth(data) }
    val spentByCat = remember(data) { vm.spentByCategory(data) }


    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Header() }

        // Household summary card
        item {
            HouseholdCard(
                household = data.household,
                onEdit = onEditHousehold,
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowStat(
                    label = "Income",
                    value = Money.format(income),
                    tint = Mint,
                    up = true,
                    modifier = Modifier.weight(1f),
                )
                FlowStat(
                    label = "Spent",
                    value = Money.format(spent),
                    tint = Coral,
                    up = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        // Spending breakdown pie chart
        if (spentByCat.isNotEmpty()) {
            item {
                SectionHeader(title = "Spending breakdown")
            }
            item {
                SpendingPieChart(
                    slices = spentByCat.map { (cat, amount) ->
                        PieSlice(
                            label = cat.name,
                            amount = amount,
                            color = cat.colorArgb.toColor(),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(InkElevated)
                        .padding(20.dp),
                )
            }
        }

        // Recent transactions
        item {
            SectionHeader(
                title = "Recent purchases",
                action = {
                    Text(
                        "See all",
                        style = MaterialTheme.typography.labelLarge,
                        color = Mint,
                        modifier = Modifier.clip(CircleShape).padding(4.dp),
                    )
                },
            )
        }
        items(data.transactions.take(5)) { tx ->
            val acc = vm.account(tx.accountId)
            val cat = vm.category(tx.categoryId)
            TransactionRow(
                title = tx.title,
                subtitle = (acc?.name ?: "Account") + (cat?.let { " · ${it.name}" } ?: ""),
                amount = tx.amount,
                isIncome = tx.isIncome,
                time = Dates.relative(tx.timestamp),
                iconKey = cat?.iconKey ?: "pay",
                color = cat?.colorArgb?.toColor() ?: Mint,
            )
        }

        // Financial health dashboard — score + metrics
        item {
            FinancialHealthDashboard(health = health)
        }

        item {
            SectionHeader(
                title = "Accounts",
                action = {
                    Text(
                        "See all",
                        style = MaterialTheme.typography.labelLarge,
                        color = Mint,
                        modifier = Modifier.clip(CircleShape).padding(4.dp),
                    )
                },
            )
        }
        item { AccountCarousel(accounts = data.accounts, onSeeAccounts = onSeeAccounts) }

        item { NetWorthCard(net = net, available = available, owed = owed) }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                greeting(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Your money",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(MintDeep, Mint))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = Ink)
        }
    }
}

@Composable
private fun HouseholdCard(
    household: HouseholdProfile,
    onEdit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(InkElevated)
            .androidClick(onEdit)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Mint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.People,
                    contentDescription = null,
                    tint = Mint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Your household",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(2.dp))
                val parts = mutableListOf<String>()
                if (household.adultCount > 0) parts.add("${household.adultCount} adult${if (household.adultCount != 1) "s" else ""}")
                if (household.childCount > 0) parts.add("${household.childCount} child${if (household.childCount != 1) "ren" else ""}")
                if (household.petCount > 0) parts.add("${household.petCount} pet${if (household.petCount != 1) "s" else ""}")
                Text(
                    parts.joinToString(" · ").ifEmpty { "Not set" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Icon(
                Icons.Rounded.Edit,
                contentDescription = "Edit household",
                tint = Mint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun greeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
private fun NetWorthCard(net: Double, available: Double, owed: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(MintDeep, Color(0xFF0B3A2B), InkElevated)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                "Net worth",
                style = MaterialTheme.typography.labelLarge,
                color = MintBright.copy(alpha = 0.9f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                Money.format(net),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                MiniStat("Available", Money.format(available), Mint, Modifier.weight(1f))
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(Color.White.copy(alpha = 0.10f))
                )
                MiniStat("Owed", Money.format(owed), Coral, Modifier.weight(1f).padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = tint,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FlowStat(
    label: String,
    value: String,
    tint: Color,
    up: Boolean,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, cornerRadius = 22.dp) {
        Column(Modifier.padding(18.dp)) {
            IconChip(color = tint, size = 38.dp) {
                Icon(
                    if (up) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "this month",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
            )
        }
    }
}

@Composable
private fun AccountCarousel(accounts: List<Account>, onSeeAccounts: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        accounts.forEach { acc ->
            AccountMiniCard(acc, onClick = onSeeAccounts)
        }
    }
}

@Composable
private fun AccountMiniCard(account: Account, onClick: () -> Unit) {
    val color = account.colorArgb.toColor()
    val isCredit = account.type == AccountType.CREDIT
    Box(
        modifier = Modifier
            .width(168.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(InkElevated)
            .androidClick(onClick)
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    account.type.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                account.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                (if (isCredit) "-" else "") + Money.format(account.balance),
                style = MaterialTheme.typography.titleLarge,
                color = if (isCredit) Coral else Mint,
                fontWeight = FontWeight.Bold,
            )
            if (isCredit && account.creditLimit > 0) {
                Spacer(Modifier.height(10.dp))
                ProgressBar(
                    progress = (account.balance / account.creditLimit).toFloat(),
                    color = color,
                    modifier = Modifier.fillMaxWidth(),
                    height = 6.dp,
                )
            }
        }
    }
}

@Composable
private fun BudgetRow(
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
                        Money.format(used) + " / " + Money.formatCompact(budget),
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
fun TransactionRow(
    title: String,
    subtitle: String,
    amount: Double,
    isIncome: Boolean,
    time: String,
    iconKey: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(InkElevated)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconChip(color = if (isIncome) Mint else color) {
            Icon(
                IconCatalog.icon(if (isIncome) "pay" else iconKey),
                contentDescription = null,
                tint = if (isIncome) Mint else color,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = TextSecondary, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (isIncome) Money.formatSigned(amount) else "-" + Money.format(amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (isIncome) Mint else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(time, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
    }
}

package com.rork.budgetflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.TrendingDown
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.data.BuyingPower
import com.rork.budgetflow.data.BuyingPowerType
import com.rork.budgetflow.data.Debt
import com.rork.budgetflow.data.IconCatalog
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.SavingsGoal
import com.rork.budgetflow.data.Vehicle
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.IconChip
import com.rork.budgetflow.ui.components.ProgressBar
import com.rork.budgetflow.ui.components.RingGauge
import com.rork.budgetflow.ui.components.androidClick
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Gold
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.OnMint
import com.rork.budgetflow.ui.theme.Sky
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

private enum class GoalTab { GOALS, DEBTS, ASSETS }

@Composable
fun GoalsScreen(
    vm: BudgetViewModel,
    onAddGoal: () -> Unit,
    onAddDebt: () -> Unit,
    onContribute: (String) -> Unit,
    onPayDebt: (String) -> Unit,
    onAddVehicle: () -> Unit,
    onAddBuyingPower: () -> Unit,
) {
    val data by vm.data.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(GoalTab.GOALS) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "Goals & Assets",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(InkSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SegTab("Savings", tab == GoalTab.GOALS, Modifier.weight(1f)) { tab = GoalTab.GOALS }
                SegTab("Debts", tab == GoalTab.DEBTS, Modifier.weight(1f)) { tab = GoalTab.DEBTS }
                SegTab("Assets", tab == GoalTab.ASSETS, Modifier.weight(1f)) { tab = GoalTab.ASSETS }
            }
        }

        when (tab) {
            GoalTab.GOALS -> {
                item { AddRow("New savings goal", onAddGoal) }
                items(data.goals, key = { it.id }) { goal ->
                    GoalCard(goal, onContribute = { onContribute(goal.id) }, onLongDelete = { vm.deleteGoal(goal.id) })
                }
            }
            GoalTab.DEBTS -> {
                item {
                    val total = data.debts.sumOf { it.total - it.paid }
                    DebtSummary(total)
                }
                item { AddRow("New debt", onAddDebt) }
                items(data.debts, key = { it.id }) { debt ->
                    DebtCard(debt, onPay = { onPayDebt(debt.id) }, onLongDelete = { vm.deleteDebt(debt.id) })
                }
            }
            GoalTab.ASSETS -> {
                item {
                    VehicleWorthSummary(vm)
                }
                item {
                    Text(
                        "Vehicles",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item { AddRow("Add vehicle", onAddVehicle) }
                items(data.vehicles, key = { it.id }) { vehicle ->
                    VehicleCard(vehicle, vm, onLongDelete = { vm.deleteVehicle(vehicle.id) })
                }

                item {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Buying Power",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Always show computed car buying power
                item {
                    val bp = vm.carBuyingPower()
                    BuyingPowerCard(bp, modifier = Modifier.fillMaxWidth())
                }

                // Always show computed home buying power
                item {
                    val bp = vm.homeBuyingPower()
                    BuyingPowerCard(bp, modifier = Modifier.fillMaxWidth())
                }

                // Expandable section for other buying power items
                item {
                    OtherBuyingPowerSection(
                        customBps = data.buyingPowers,
                        onDelete = { vm.deleteBuyingPower(it.id) },
                        onAdd = onAddBuyingPower,
                    )
                }
            }
        }
    }
}

@Composable
private fun SegTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) Mint else Color.Transparent)
            .androidClick(onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) OnMint else TextSecondary,
        )
    }
}

@Composable
private fun AddRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Mint.copy(alpha = 0.10f))
            .androidClick(onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconChip(color = Mint, size = 38.dp) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = Mint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = Mint)
    }
}

@Composable
private fun GoalCard(goal: SavingsGoal, onContribute: () -> Unit, onLongDelete: () -> Unit) {
    val color = goal.colorArgb.toColor()
    val ratio = (goal.saved / goal.target).toFloat().coerceIn(0f, 1f)
    val done = goal.saved >= goal.target
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(InkElevated)
            .androidClick(onLongDelete)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RingGauge(
                    progress = ratio,
                    color = color,
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 7.dp,
                    center = {
                        if (done) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "${(ratio * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(IconCatalog.icon(goal.iconKey), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            goal.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Money.format(goal.saved) + " of " + Money.format(goal.target),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (done) "Goal reached" else Money.format(goal.target - goal.saved) + " to go",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (done) color else TextTertiary,
                    )
                }
            }
            if (!done) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.14f))
                        .androidClick(onContribute)
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Add money", style = MaterialTheme.typography.labelLarge, color = color)
                }
            }
        }
    }
}

@Composable
private fun DebtSummary(total: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(InkElevated)
            .padding(22.dp)
    ) {
        Column {
            Text("Total remaining debt", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Text(
                Money.format(total),
                style = MaterialTheme.typography.displayMedium,
                color = Coral,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DebtCard(debt: Debt, onPay: () -> Unit, onLongDelete: () -> Unit) {
    val color = debt.colorArgb.toColor()
    val remaining = debt.total - debt.paid
    val ratio = (debt.paid / debt.total).toFloat().coerceIn(0f, 1f)
    val cleared = remaining <= 0.0
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(InkElevated)
            .androidClick(onLongDelete)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        debt.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${debt.apr}% APR",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (cleared) "Cleared" else Money.format(remaining),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (cleared) Mint else Coral,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "of " + Money.format(debt.total),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            ProgressBar(
                progress = ratio,
                color = if (cleared) Mint else color,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!cleared) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.14f))
                        .androidClick(onPay)
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Make a payment", style = MaterialTheme.typography.labelLarge, color = color)
                }
            }
        }
    }
}

// --- Vehicles ----------------------------------------------------------------

@Composable
private fun VehicleWorthSummary(vm: BudgetViewModel) {
    val total = vm.totalVehicleWorth()
    val purchaseTotal = vm.data.value.vehicles.sumOf { it.purchasePrice }
    val change = total - purchaseTotal
    val changePercent = if (purchaseTotal > 0) (change / purchaseTotal * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(InkElevated)
            .padding(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Vehicle Worth", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text(
                    Money.format(total),
                    style = MaterialTheme.typography.displayMedium,
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${if (change >= 0) "+" else ""}${Money.format(change)} (${if (changePercent >= 0) "+" else ""}$changePercent%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (change >= 0) Mint else Coral,
                )
            }
            Icon(
                Icons.Rounded.DirectionsCar,
                contentDescription = null,
                tint = Gold.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun VehicleCard(vehicle: Vehicle, vm: BudgetViewModel, onLongDelete: () -> Unit) {
    val color = vehicle.colorArgb.toColor()
    val estimated = vm.estimatedVehicleValue(vehicle)
    val hasManualValue = vehicle.currentValue != null
    val depreciation = vehicle.purchasePrice - estimated
    val depreciationPercent = if (vehicle.purchasePrice > 0) (depreciation / vehicle.purchasePrice * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(InkElevated)
            .androidClick(onLongDelete)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                IconChip(color = color, size = 52.dp) {
                    Icon(
                        Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${vehicle.make} ${vehicle.model}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${vehicle.year} · Paid ${Money.format(vehicle.purchasePrice)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        Money.format(estimated),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (depreciation > 0) Gold else Mint,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "now",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.TrendingDown,
                    contentDescription = null,
                    tint = if (depreciation > 0) Coral.copy(alpha = 0.7f) else TextTertiary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (depreciation > 0) "-${Money.format(depreciation)} ($depreciationPercent%)" else "Value stable",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (depreciation > 0) Coral else TextTertiary,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (hasManualValue) "Manual value" else "Auto-estimated",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary,
                )
            }
        }
    }
}

// --- Other Buying Power (expandable) ----------------------------------------

@Composable
private fun OtherBuyingPowerSection(
    customBps: List<BuyingPower>,
    onDelete: (BuyingPower) -> Unit,
    onAdd: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Collapsed: tappable row to expand
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(InkElevated)
                .androidClick { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Check buying power on other items",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Motorcycles, boats, RVs, and more",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
            }
            Icon(
                if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp),
            )
        }

        // Expanded: custom buying power cards + add button
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Spacer(Modifier.height(2.dp))

                if (customBps.isNotEmpty()) {
                    customBps.forEach { bp ->
                        BuyingPowerCard(
                            bp,
                            modifier = Modifier.fillMaxWidth(),
                            onLongDelete = { onDelete(bp) },
                        )
                    }
                }

                AddRow("Add custom buying power", onAdd)
            }
        }
    }
}

// --- Buying Power ------------------------------------------------------------

@Composable
private fun BuyingPowerCard(
    bp: BuyingPower,
    modifier: Modifier = Modifier,
    onLongDelete: (() -> Unit)? = null,
) {
    val accent = when (bp.type) {
        BuyingPowerType.CAR -> Gold
        BuyingPowerType.HOME -> Sky
    }
    val icon = when (bp.type) {
        BuyingPowerType.CAR -> Icons.Rounded.DirectionsCar
        BuyingPowerType.HOME -> Icons.Rounded.Home
    }
    val label = when (bp.type) {
        BuyingPowerType.CAR -> "Car"
        BuyingPowerType.HOME -> "Home"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(InkElevated)
            .then(if (onLongDelete != null) Modifier.androidClick(onLongDelete) else Modifier)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(color = accent, size = 48.dp) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "$label buying power",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.10f))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "You can afford up to",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Money.format(bp.maxPurchase),
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 36.sp),
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text(
                            "Est. monthly: ",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                        Text(
                            Money.format(bp.monthlyPayment),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "/mo",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                    }
                }
            }
        }
    }
}

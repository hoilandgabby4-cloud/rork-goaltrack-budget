package com.rork.budgetflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.data.Category
import com.rork.budgetflow.data.IconCatalog
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.GlassCard
import com.rork.budgetflow.ui.components.IconChip
import com.rork.budgetflow.ui.components.MonthlyBarChart
import com.rork.budgetflow.ui.components.SectionHeader
import com.rork.budgetflow.ui.components.SpendingGuidelines
import com.rork.budgetflow.ui.components.androidClick
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.OnMint
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

private enum class BudgetTab(val label: String) {
    CATEGORIES("Budget"),
    MONTHLY("Monthly"),
}

@Composable
fun TrendsScreen(
    vm: BudgetViewModel,
    onAddCategory: () -> Unit = {},
    onEditCategory: (Category) -> Unit = {},
    onDeleteCategory: (String) -> Unit = {},
) {
    val data by vm.data.collectAsStateWithLifecycle()
    val months = vm.monthlyBreakdown()
    var tab by androidx.compose.runtime.mutableStateOf(BudgetTab.CATEGORIES)

    Column(modifier = Modifier.fillMaxSize().background(Ink)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Budget",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Plan your spending",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
        }

        // Tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(InkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BudgetTab.entries.forEach { t ->
                val isSel = t == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isSel) InkElevated else Color.Transparent)
                        .androidClick { tab = t }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        t.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSel) Mint else TextSecondary,
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        when (tab) {
            BudgetTab.CATEGORIES -> BudgetTab(
                vm = vm,
                onAddCategory = onAddCategory,
                onEditCategory = onEditCategory,
                onDeleteCategory = onDeleteCategory,
            )
            BudgetTab.MONTHLY -> MonthlyTab(months = months)
        }
    }
}

@Composable
private fun BudgetTab(
    vm: BudgetViewModel,
    onAddCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (String) -> Unit,
) {
    val data by vm.data.collectAsStateWithLifecycle()
    val guidelines = vm.spendingGuidelines(data)
    val totalPct = vm.totalSuggestedPct(data)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Spending guidelines card
        if (guidelines.isNotEmpty()) {
            item {
                SpendingGuidelines(
                    rows = guidelines,
                    totalSuggestedPct = totalPct,
                )
            }
        }

        // Category management header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(
                    title = "Categories",
                    modifier = Modifier.weight(1f),
                )
                // Add button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Mint)
                        .androidClick(onAddCategory),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add category",
                        tint = OnMint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        // Category cards
        items(data.categories, key = { it.id }) { cat ->
            CategoryCard(
                category = cat,
                spent = vm.spentInCategory(cat.id, data),
                monthlyIncome = vm.incomeThisMonth(data).let { if (it > 0) it else data.household.monthlyIncome },
                onEdit = { onEditCategory(cat) },
                onDelete = { onDeleteCategory(cat.id) },
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    spent: Double,
    monthlyIncome: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = category.colorArgb.toColor()
    val pct = if (monthlyIncome > 0) (spent / monthlyIncome * 100.0) else 0.0
    val isOverBudget = category.monthlyBudget > 0 && spent > category.monthlyBudget

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            IconChip(color = color, size = 42.dp) {
                Icon(
                    IconCatalog.icon(category.iconKey),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            // Name + details
            Column(Modifier.weight(1f)) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (category.suggestedPercentage > 0) {
                        Text(
                            "${category.suggestedPercentage.toInt()}% suggested",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                        Text(
                            " · ",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                    }
                    Text(
                        if (spent > 0) "${Money.formatCompact(spent)} spent" else "No spending",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverBudget) Coral else TextSecondary,
                    )
                }
            }

            // Edit button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f))
                    .androidClick(onEdit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit ${category.name}",
                    tint = color,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.width(8.dp))

            // Delete button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Coral.copy(alpha = 0.12f))
                    .androidClick(onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete ${category.name}",
                    tint = Coral,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun MonthlyTab(months: List<com.rork.budgetflow.data.MonthBar>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Summary cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val totalIncome = months.sumOf { it.income }
                val totalSpent = months.sumOf { it.spent }
                TrendSummaryCard(
                    label = "12-Month Income",
                    value = Money.format(totalIncome),
                    tint = Mint,
                    modifier = Modifier.weight(1f),
                )
                TrendSummaryCard(
                    label = "12-Month Spent",
                    value = Money.format(totalSpent),
                    tint = Coral,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Bar chart
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader(title = "Income vs Spending")
                    Spacer(Modifier.height(8.dp))
                    MonthlyBarChart(
                        months = months,
                        modifier = Modifier.fillMaxWidth(),
                        barHeight = 160.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendSummaryCard(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, cornerRadius = 20.dp) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(tint)
            )
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

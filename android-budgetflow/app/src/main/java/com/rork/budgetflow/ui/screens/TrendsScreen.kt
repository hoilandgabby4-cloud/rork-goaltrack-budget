package com.rork.budgetflow.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.data.Category
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.GlassCard
import com.rork.budgetflow.ui.components.IconChip
import com.rork.budgetflow.ui.components.MonthlyBarChart
import com.rork.budgetflow.ui.components.PieSlice
import com.rork.budgetflow.ui.components.ProgressBar
import com.rork.budgetflow.ui.components.SectionHeader
import com.rork.budgetflow.ui.components.SpendingPieChart
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.MintBright
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

private enum class TrendTab(val label: String) {
    BUDGET("Budget"),
    MONTHLY("Monthly"),
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TrendsScreen(vm: BudgetViewModel) {
    val data by vm.data.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { TrendTab.entries.size })
    val scope = rememberCoroutineScope()
    val months = vm.monthlyBreakdown()
    val spentByCat = vm.spentByCategory()

    // Tabs are swipeable and tappable

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
                    "Past 12 months",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
        }

        // Swipeable tabs
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Ink,
            contentColor = Mint,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = Mint,
                        height = 3.dp,
                    )
                }
            },
            divider = { Spacer(Modifier.padding(horizontal = 20.dp).height(1.dp).fillMaxWidth().background(Hairline)) },
        ) {
            TrendTab.entries.forEach { tab ->
                Tab(
                    selected = pagerState.currentPage == tab.ordinal,
                    onClick = { scope.launch { pagerState.animateScrollToPage(tab.ordinal) } },
                    selectedContentColor = Mint,
                    unselectedContentColor = TextTertiary,
                    modifier = Modifier.padding(vertical = 6.dp),
                ) {
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (pagerState.currentPage == tab.ordinal) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) { page ->
            when (TrendTab.entries[page]) {
                TrendTab.BUDGET -> CategoriesTab(
                    spentByCat = spentByCat,
                    categories = data.categories,
                )
                TrendTab.MONTHLY -> MonthlyTab(months = months)
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

@Composable
private fun CategoriesTab(
    spentByCat: List<Pair<Category, Double>>,
    categories: List<Category>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Pie chart
        if (spentByCat.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                    SpendingPieChart(
                        slices = spentByCat.map { (cat, amount) ->
                            PieSlice(label = cat.name, amount = amount, color = cat.colorArgb.toColor())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    )
                }
            }
        }

        // Budget progress list
        val withBudget = categories.filter { it.monthlyBudget > 0.0 }
        if (withBudget.isNotEmpty()) {
            item {
                SectionHeader(title = "Budget progress")
            }
            items(withBudget) { cat ->
                BudgetRow(
                    name = cat.name,
                    iconKey = cat.iconKey,
                    color = cat.colorArgb.toColor(),
                    used = spentByCat.firstOrNull { it.first.id == cat.id }?.second ?: 0.0,
                    budget = cat.monthlyBudget,
                    suggestedPct = cat.suggestedPercentage,
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
    suggestedPct: Double = 0.0,
) {
    val ratio = (used / budget).toFloat().coerceIn(0f, 1f)
    val over = used > budget
    GlassCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    com.rork.budgetflow.data.IconCatalog.icon(iconKey),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp),
                )
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
                Spacer(Modifier.height(4.dp))
                if (suggestedPct > 0) {
                    Text(
                        "Suggested: ${suggestedPct.toInt()}% of income",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                    Spacer(Modifier.height(4.dp))
                }
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

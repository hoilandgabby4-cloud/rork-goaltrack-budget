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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.GlassCard
import com.rork.budgetflow.ui.components.MonthlyBarChart
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.ui.components.SectionHeader
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.TextSecondary

@Composable
fun TrendsScreen(
    vm: BudgetViewModel,
) {
    val data by vm.data.collectAsStateWithLifecycle()
    val months = vm.monthlyBreakdown()

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

        MonthlyTab(months = months)
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



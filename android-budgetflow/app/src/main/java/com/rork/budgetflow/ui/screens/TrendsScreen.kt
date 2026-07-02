package com.rork.budgetflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.data.CalendarEvent
import com.rork.budgetflow.data.CalendarEventType
import com.rork.budgetflow.data.Category
import com.rork.budgetflow.data.IconCatalog
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.GlassCard
import com.rork.budgetflow.ui.components.IconChip
import com.rork.budgetflow.ui.components.MonthlyBarChart
import com.rork.budgetflow.ui.components.ProgressBar
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class BudgetTab(val label: String) {
    CATEGORIES("Budget"),
    CALENDAR("Calendar"),
    MONTHLY("Monthly"),
}

@Composable
fun TrendsScreen(
    vm: BudgetViewModel,
    onAddCategory: () -> Unit = {},
    onEditCategory: (Category) -> Unit = {},
    onDeleteCategory: (String) -> Unit = {},
    onAddCalendarEvent: () -> Unit = {},
    onEditCalendarEvent: (CalendarEvent) -> Unit = {},
    onDeleteCalendarEvent: (String) -> Unit = {},
) {
    val data by vm.data.collectAsStateWithLifecycle()
    val months = vm.monthlyBreakdown()
    var tab by remember { mutableStateOf(BudgetTab.CATEGORIES) }

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
            BudgetTab.CATEGORIES -> BudgetTabContent(
                vm = vm,
                onAddCategory = onAddCategory,
                onEditCategory = onEditCategory,
                onDeleteCategory = onDeleteCategory,
            )
            BudgetTab.CALENDAR -> CalendarTab(
                vm = vm,
                onAddEvent = onAddCalendarEvent,
                onEditEvent = onEditCalendarEvent,
                onDeleteEvent = onDeleteCalendarEvent,
            )
            BudgetTab.MONTHLY -> MonthlyTab(months = months)
        }
    }
}

// ─── Budget tab ─────────────────────────────────────────────────────────────

@Composable
private fun BudgetTabContent(
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
        if (guidelines.isNotEmpty()) {
            item {
                SpendingGuidelines(
                    rows = guidelines,
                    totalSuggestedPct = totalPct,
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(
                    title = "Categories",
                    modifier = Modifier.weight(1f),
                )
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

        if (data.categories.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(InkElevated)
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconChip(color = Mint, size = 56.dp) {
                        Icon(Icons.Rounded.Category, contentDescription = null, tint = Mint, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "No categories yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Add budget categories like Groceries, Housing, or Car to start tracking your spending",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
        } else {
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

    // Progress: fraction of monthly budget spent (or of suggested income share if no budget set)
    val budgetTarget = if (category.monthlyBudget > 0) {
        category.monthlyBudget
    } else if (category.suggestedPercentage > 0 && monthlyIncome > 0) {
        monthlyIncome * category.suggestedPercentage / 100.0
    } else {
        0.0
    }
    val progress = if (budgetTarget > 0) (spent / budgetTarget).coerceIn(0.0, 1.0).toFloat() else 0f
    val barColor = if (isOverBudget) Coral else color

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconChip(color = color, size = 42.dp) {
                    Icon(
                        IconCatalog.icon(category.iconKey),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(Modifier.width(14.dp))

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

            // Progress bar
            if (budgetTarget > 0) {
                Spacer(Modifier.height(10.dp))
                ProgressBar(
                    progress = progress,
                    color = barColor,
                    modifier = Modifier.fillMaxWidth(),
                    height = 6.dp,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "${(progress * 100).toInt()}% of ${Money.formatCompact(budgetTarget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverBudget) Coral.copy(alpha = 0.8f) else TextTertiary,
                    )
                    Text(
                        if (isOverBudget) "Over by ${Money.formatCompact(spent - budgetTarget)}"
                        else "${Money.formatCompact((budgetTarget - spent).coerceAtLeast(0.0))} left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverBudget) Coral.copy(alpha = 0.8f) else Mint.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

// ─── Monthly tab ─────────────────────────────────────────────────────────────

@Composable
private fun MonthlyTab(months: List<com.rork.budgetflow.data.MonthBar>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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

// ─── Calendar tab ────────────────────────────────────────────────────────────

@Composable
private fun CalendarTab(
    vm: BudgetViewModel,
    onAddEvent: () -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
    onDeleteEvent: (String) -> Unit,
) {
    val data by vm.data.collectAsStateWithLifecycle()

    // Navigable month/year state
    var calMonth by remember {
        val c = Calendar.getInstance()
        mutableStateOf(c)
    }

    val year = calMonth.get(Calendar.YEAR)
    val month = calMonth.get(Calendar.MONTH) // 0-based
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calMonth.time)

    val events = remember(data.calendarEvents, year, month) {
        vm.eventsForMonth(year, month)
    }

    // Days in month
    val daysInMonth = calMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    // First day-of-week (1=Sun, 2=Mon, ... 7=Sat)
    val tempCal = Calendar.getInstance().apply {
        set(year, month, 1)
    }
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1=Sun
    val offsetDays = (firstDayOfWeek + 5) % 7 // offset so Mon=0

    // Group events by day of month
    val eventsByDay = events.groupBy { it.dayOfMonth }

    // Selected day to show event list
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Month & year header — clearly separated above the calendar grid
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .androidClick {
                                calMonth = (calMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                                selectedDay = null
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            monthLabel,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .androidClick {
                                calMonth = (calMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                                selectedDay = null
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = "Next month",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        // Calendar grid — uniform square cells in their own card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Day-of-week header (Mon–Sun)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(
                                day,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Day grid — 6 rows max, each cell is a uniform square
                    val rows = (offsetDays + daysInMonth + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (row in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                for (col in 0..6) {
                                    val idx = row * 7 + col
                                    val day = idx - offsetDays + 1

                                    if (day in 1..daysInMonth) {
                                        val isToday = (year == Calendar.getInstance().get(Calendar.YEAR) &&
                                            month == Calendar.getInstance().get(Calendar.MONTH) &&
                                            day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                                        val isSelected = day == selectedDay
                                        val dayEvents = eventsByDay[day] ?: emptyList()

                                        DayCell(
                                            day = day,
                                            isToday = isToday,
                                            isSelected = isSelected,
                                            events = dayEvents,
                                            onClick = { selectedDay = if (isSelected) null else day },
                                            modifier = Modifier.weight(1f).aspectRatio(1f),
                                        )
                                    } else {
                                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(Mint))
                        Spacer(Modifier.width(6.dp))
                        Text("Bill", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        Spacer(Modifier.width(16.dp))
                        Box(Modifier.size(10.dp).clip(CircleShape).background(Coral))
                        Spacer(Modifier.width(6.dp))
                        Text("Vacation", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }
            }
        }

        // Events for the selected day
        if (selectedDay != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader(
                        title = "Events on $selectedDay",
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Mint)
                            .androidClick(onAddEvent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "Add event",
                            tint = OnMint,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            val dayEvents = eventsByDay[selectedDay] ?: emptyList()
            if (dayEvents.isEmpty()) {
                item {
                    Text(
                        "No events on this day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                items(dayEvents, key = { it.id }) { event ->
                    CalendarEventCard(
                        event = event,
                        onEdit = { onEditEvent(event) },
                        onDelete = { onDeleteEvent(event.id) },
                    )
                }
            }
        } else {
            // Show upcoming events summary when no day selected
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader(
                        title = "Upcoming events",
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Mint)
                            .androidClick(onAddEvent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "Add event",
                            tint = OnMint,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            if (events.isEmpty()) {
                item {
                    Text(
                        "No events this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                items(events, key = { it.id }) { event ->
                    CalendarEventCard(
                        event = event,
                        onEdit = { onEditEvent(event) },
                        onDelete = { onDeleteEvent(event.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    events: List<CalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasBill = events.any { it.type == CalendarEventType.BILL }
    val hasVacation = events.any { it.type == CalendarEventType.VACATION }

    val bgColor = when {
        isSelected -> Mint.copy(alpha = 0.3f)
        isToday -> Mint.copy(alpha = 0.15f)
        else -> InkSurface.copy(alpha = 0.6f)
    }
    val borderColor = when {
        isSelected -> Mint.copy(alpha = 0.6f)
        isToday -> Mint.copy(alpha = 0.35f)
        else -> Hairline
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .androidClick(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                "$day",
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isToday -> Mint
                    isSelected -> Mint
                    else -> MaterialTheme.colorScheme.onBackground
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            )
            // Event dots — consistent space so all cells align
            Spacer(Modifier.height(2.dp))
            if (hasBill || hasVacation) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (hasBill) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Mint)
                        )
                    }
                    if (hasVacation) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Coral)
                        )
                    }
                }
            } else {
                // Invisible spacer to keep cell height consistent
                Spacer(Modifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun CalendarEventCard(
    event: CalendarEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = event.colorArgb.toColor()
    val isBill = event.type == CalendarEventType.BILL
    val accentColor = if (isBill) Mint else Coral
    val dateStr: String
    if (isBill) {
        dateStr = "Every month on the ${event.dayOfMonth}${ordinalSuffix(event.dayOfMonth)}"
    } else {
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        val start = fmt.format(event.startTimestamp)
        val end = event.endTimestamp?.let { fmt.format(it) }
        dateStr = if (end != null) "$start – $end" else start
    }

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            if (isBill) "Bill" else "Vacation",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                if (isBill && event.amount > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        Money.format(event.amount),
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (event.notes.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        event.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        maxLines = 1,
                    )
                }
            }

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
                    contentDescription = "Edit event",
                    tint = color,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.width(8.dp))

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
                    contentDescription = "Delete event",
                    tint = Coral,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun ordinalSuffix(day: Int): String = when {
    day in 11..13 -> "th"
    day % 10 == 1 -> "st"
    day % 10 == 2 -> "nd"
    day % 10 == 3 -> "rd"
    else -> "th"
}

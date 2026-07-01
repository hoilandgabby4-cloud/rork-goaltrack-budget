package com.rork.budgetflow.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rork.budgetflow.data.FinancialHealth
import com.rork.budgetflow.data.GuidelineRow
import com.rork.budgetflow.data.HealthMetric
import com.rork.budgetflow.data.HealthStatus
import com.rork.budgetflow.data.IconCatalog
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.MonthBar
import com.rork.budgetflow.data.Recommendation
import com.rork.budgetflow.data.RecommendationType
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Gold
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.Rose
import com.rork.budgetflow.ui.theme.Sky
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary
import com.rork.budgetflow.ui.theme.Violet

/** A rounded translucent card surface used throughout the app. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    background: Color = InkElevated,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(background)
    ) {
        content()
    }
}

/** Small circular icon chip with a tinted background. */
@Composable
fun IconChip(
    color: Color,
    size: Dp = 44.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Animated rounded progress bar. */
@Composable
fun ProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    track: Color = Hairline,
    height: Dp = 8.dp,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "progress",
    )
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(color.copy(alpha = 0.75f), color)
                    )
                )
        )
    }
}

/** A circular ring gauge showing progress, with a label slot in the center. */
@Composable
fun RingGauge(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    track: Color = Hairline,
    center: @Composable () -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "ring",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f,
            )
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(color.copy(alpha = 0.6f), color)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        center()
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        androidx.compose.material3.Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        if (action != null) {
            Box(Modifier.align(Alignment.CenterEnd)) { action() }
        }
    }
}

fun roundedStroke(): RoundedCornerShape = RoundedCornerShape(24.dp)

/** A faint rounded rectangle outline used for empty / dashed states. */
fun Modifier.dashedOutline(color: Color = Hairline): Modifier = this

/** Simple clickable shorthand used across cards. */
fun Modifier.androidClick(onClick: () -> Unit): Modifier = this.clickable(onClick = onClick)

/** A slice of the spending pie chart. */
data class PieSlice(
    val label: String,
    val amount: Double,
    val color: Color,
)

/**
 * Animated donut chart showing spending breakdown by category.
 * Displays total spent in the center with colored segments and a legend below.
 */
@Composable
fun SpendingPieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    donutRadius: Dp = 80.dp,
    strokeWidth: Dp = 28.dp,
) {
    val total = slices.sumOf { it.amount }
    if (total <= 0.0 || slices.isEmpty()) return

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTriggered = true }

    val animatedSweep by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(800),
        label = "pie-sweep",
    )

    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Donut chart
        Box(
            modifier = Modifier.size(donutRadius * 2 + strokeWidth),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = size.minDimension - strokePx
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f,
                )
                val arcSize = Size(diameter, diameter)
                var startAngle = -90f

                slices.forEach { slice ->
                    val sweep = (slice.amount / total * 360f * animatedSweep).toFloat()
                    if (sweep > 0f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                        )
                        startAngle += sweep
                    }
                }
            }

            // Center label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    Money.formatCompact(total),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                slices.forEachIndexed { index, slice ->
                    val pct = (slice.amount / total * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            slice.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 100.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${pct}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            Money.formatCompact(slice.amount),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bar chart showing income (Mint) and spent (Coral) side-by-side for each
 * month in the past year. Bar heights are proportional to the maximum value
 * across both series.
 */
@Composable
fun MonthlyBarChart(
    months: List<MonthBar>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 180.dp,
) {
    if (months.isEmpty()) return

    val maxVal = months.maxOf { maxOf(it.income, it.spent) }.coerceAtLeast(10.0)

    // Allocate extra height so month labels sit above the bar area
    val labelArea = 28.dp

    Column(modifier = modifier) {
        // Legend — now shows wider bar = income, narrower on top = spent
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(Mint))
            Spacer(Modifier.width(6.dp))
            Text("Income", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.width(20.dp))
            Box(Modifier.size(10.dp).clip(CircleShape).background(Coral))
            Spacer(Modifier.width(6.dp))
            Text("Spent", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }

        Spacer(Modifier.height(16.dp))

        // Bars — taller Row to fit labels above each bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight + labelArea),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            months.forEach { month ->
                MonthBarColumn(
                    month = month,
                    maxVal = maxVal,
                    barHeight = barHeight,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MonthBarColumn(
    month: MonthBar,
    maxVal: Double,
    barHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val incomeH = (month.income / maxVal * barHeight.value).dp.coerceAtLeast(0.dp)
    val spentH = (month.spent / maxVal * barHeight.value).dp.coerceAtLeast(0.dp)
    val minH = 4.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxHeight(),
    ) {
        // Month label above the bars
        Text(
            month.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            maxLines = 1,
        )

        Spacer(Modifier.height(4.dp))

        // Bar area — fills remaining height, bars anchored to bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Compact value labels above the bars
                if (month.income > 0 || month.spent > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (month.income > 0) {
                            Text(
                                Money.formatCompact(month.income),
                                style = MaterialTheme.typography.labelSmall,
                                color = Mint,
                                maxLines = 1,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f,
                            )
                        }
                        if (month.spent > 0) {
                            Text(
                                Money.formatCompact(month.spent),
                                style = MaterialTheme.typography.labelSmall,
                                color = Coral,
                                maxLines = 1,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f,
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                }

                // Overlaid bars — income (wider, behind) and spent (narrower, on top)
                Box(contentAlignment = Alignment.BottomCenter) {
                    // Income bar — wide, muted gradient
                    if (month.income > 0) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(incomeH.coerceAtLeast(minH))
                                .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Mint.copy(alpha = 0.85f), Mint.copy(alpha = 0.35f))
                                    )
                                ),
                        )
                    }
                    // Spent bar — narrow, solid color, drawn on top
                    if (month.spent > 0) {
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(spentH.coerceAtLeast(minH))
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Coral, Coral.copy(alpha = 0.75f))
                                    )
                                ),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Spending guidelines panel showing suggested vs actual percentage
 * of income spent per category. Each row is a horizontal bar comparison.
 */
@Composable
fun SpendingGuidelines(
    rows: List<GuidelineRow>,
    totalSuggestedPct: Double,
    modifier: Modifier = Modifier,
) {
    if (rows.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(InkElevated)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Spending guidelines",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${totalSuggestedPct.toInt()}% total",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Suggested vs actual % of monthly income",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(16.dp))

        rows.forEachIndexed { index, row ->
            GuidelineRowItem(row)
            if (index < rows.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun GuidelineRowItem(row: GuidelineRow) {
    val isOver = row.actualPct > row.suggestedPct
    val ratio = (row.actualPct / row.suggestedPct.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 2f)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(color = row.category.colorArgb.toColor()) {
                Icon(
                    IconCatalog.icon(row.category.iconKey),
                    contentDescription = null,
                    tint = row.category.colorArgb.toColor(),
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    row.category.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${row.suggestedPct.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
                Text(
                    "suggested",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${row.actualPct.toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isOver) Coral else Mint,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "actual",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOver) Coral.copy(alpha = 0.8f) else Mint.copy(alpha = 0.8f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Comparison bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Hairline),
        ) {
            val suggestedFraction = (row.suggestedPct / (row.suggestedPct.coerceAtLeast(row.actualPct) * 1.4f)).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = ratio.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isOver) Coral.copy(alpha = 0.7f) else Mint.copy(alpha = 0.7f)
                    ),
            )
        }

        // Amount row
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                Money.formatCompact(row.suggestedAmount) + " suggested",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
            )
            Text(
                Money.formatCompact(row.spent) + " spent",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOver) Coral.copy(alpha = 0.8f) else Mint.copy(alpha = 0.8f),
            )
        }
    }
}

/**
 * A single actionable recommendation card. Displays an icon, title, body,
 * and an optional action button. Color-coded by recommendation type.
 */
@Composable
fun RecommendationCard(
    recommendation: Recommendation,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null,
) {
    val color = recommendation.colorArgb.toColor()
    val typeColor = when (recommendation.type) {
        RecommendationType.OVERSPENDING -> Coral
        RecommendationType.DEBT_REDUCTION -> Coral
        RecommendationType.MISSING_BILL -> Sky
        RecommendationType.SAVING_OPPORTUNITY -> Gold
        RecommendationType.EMERGENCY_FUND -> Gold
        RecommendationType.GENERAL_TIP -> Violet
    }
    val typeLabel = when (recommendation.type) {
        RecommendationType.OVERSPENDING -> "Spending alert"
        RecommendationType.DEBT_REDUCTION -> "Debt priority"
        RecommendationType.MISSING_BILL -> "Recommended bill"
        RecommendationType.SAVING_OPPORTUNITY -> "Saving tip"
        RecommendationType.EMERGENCY_FUND -> "Safety net"
        RecommendationType.GENERAL_TIP -> "Quick tip"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(typeColor.copy(alpha = 0.1f), InkElevated, InkElevated)
                )
            ),
    ) {
        Column(Modifier.padding(18.dp)) {
            // Header row: type badge + action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(typeColor.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (recommendation.actionLabel != null && onAction != null) {
                    Text(
                        recommendation.actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Mint,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .androidClick(onAction)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Icon + title
            Row(verticalAlignment = Alignment.Top) {
                IconChip(color = color, size = 40.dp) {
                    Icon(
                        IconCatalog.icon(recommendation.iconKey),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        recommendation.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        recommendation.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.45f,
                    )
                }
            }
        }
    }
}

/**
 * Financial health dashboard — a hero score gauge plus scannable metric cards
 * that show status across emergency fund, credit, savings, retirement, debt,
 * and bill payments. Each metric is color-coded with a status label.
 */
@Composable
fun FinancialHealthDashboard(
    health: FinancialHealth,
    modifier: Modifier = Modifier,
) {
    val statusColor = statusColor(health.status)
    val scoreColor = when {
        health.overallScore >= 80 -> Mint
        health.overallScore >= 55 -> Gold
        health.overallScore >= 30 -> Coral
        else -> Coral
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.radialGradient(
                    listOf(statusColor.copy(alpha = 0.06f), InkElevated, InkElevated)
                )
            )
            .padding(20.dp),
    ) {
        // Header row with title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Financial health",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    health.status.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Score gauge + summary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Score ring gauge
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center,
            ) {
                RingGauge(
                    progress = health.overallScore / 100f,
                    color = scoreColor,
                    strokeWidth = 10.dp,
                    track = Hairline,
                    modifier = Modifier.fillMaxSize(),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${health.overallScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "score",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            // Summary text
            Column(Modifier.weight(1f)) {
                Text(
                    healthSummary(health),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f,
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    health.metrics.take(3).forEach { metric ->
                        val dotColor = when (metric.status) {
                            HealthStatus.GOOD -> Mint
                            HealthStatus.ON_TRACK -> Gold
                            HealthStatus.NEEDS_ATTENTION -> Coral
                            HealthStatus.CRITICAL -> Coral
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Metric cards in a lazy row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(health.metrics) { metric ->
                HealthMetricCard(metric)
            }
        }
    }
}

@Composable
private fun HealthMetricCard(metric: HealthMetric) {
    val mColor = when (metric.status) {
        HealthStatus.GOOD -> Mint
        HealthStatus.ON_TRACK -> Gold
        HealthStatus.NEEDS_ATTENTION -> Coral
        HealthStatus.CRITICAL -> Coral
    }

    GlassCard(
        modifier = Modifier.width(152.dp),
        cornerRadius = 18.dp,
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(color = mColor, size = 34.dp) {
                    Icon(
                        IconCatalog.icon(metric.iconKey),
                        contentDescription = null,
                        tint = mColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(mColor),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                metric.label,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                metric.value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                metric.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                maxLines = 2,
                lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 1.2f,
            )
        }
    }
}

private fun statusColor(status: HealthStatus): Color = when (status) {
    HealthStatus.GOOD -> Mint
    HealthStatus.ON_TRACK -> Gold
    HealthStatus.NEEDS_ATTENTION -> Coral
    HealthStatus.CRITICAL -> Coral
}

private fun healthSummary(health: FinancialHealth): String {
    val good = health.metrics.count { it.status == HealthStatus.GOOD }
    val track = health.metrics.count { it.status == HealthStatus.ON_TRACK }
    val attention = health.metrics.count { it.status == HealthStatus.NEEDS_ATTENTION }
    val critical = health.metrics.count { it.status == HealthStatus.CRITICAL }

    return when {
        health.overallScore >= 80 ->
            "You're in great shape! $good areas are strong. Keep up the discipline and consider investing more."
        health.overallScore >= 55 ->
            "You're on the right track with $good strong areas. Focus on the ${attention + critical} area${if (attention + critical != 1) "s" else ""} needing work."
        health.overallScore >= 30 -> {
            val parts = mutableListOf<String>()
            if (attention > 0) parts.add("$attention need${if (attention == 1) "s" else ""} attention")
            if (critical > 0) parts.add("$critical critical")
            "${parts.joinToString(", ")}. Prioritize building emergency savings and reducing high-interest debt."
        }
        else ->
            "Your finances need immediate attention. Start small — build a \$1,000 emergency fund and tackle high-interest debt first."
    }
}

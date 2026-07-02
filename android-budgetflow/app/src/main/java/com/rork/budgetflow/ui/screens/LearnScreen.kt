package com.rork.budgetflow.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement as ComposeArrangement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.data.AccountType
import com.rork.budgetflow.data.RecommendationType
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.GlassCard
import com.rork.budgetflow.ui.components.RecommendationCard
import com.rork.budgetflow.ui.components.SectionHeader
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Gold
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.MintDeep
import com.rork.budgetflow.ui.theme.OnMint
import com.rork.budgetflow.ui.theme.Sky
import com.rork.budgetflow.ui.theme.TextPrimary
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary
import com.rork.budgetflow.ui.theme.Violet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    vm: BudgetViewModel,
    onNavigateToWallet: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
) {
    val data by vm.data.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Compute personalized insights
    val spent = vm.spentThisMonth()
    val income = vm.incomeThisMonth()
    val savingsRate = if (income > 0) ((income - spent) / income * 100).toInt().coerceIn(0, 100) else 0
    val hasRetirement = data.accounts.any { it.type == AccountType.RETIREMENT && it.balance > 0 }
    val totalDebt = data.debts.sumOf { it.total - it.paid } + vm.totalCreditOwed()
    val guidelines = vm.spendingGuidelines()
    val overBudgetCategories = guidelines.filter { it.actualPct > it.suggestedPct }
    val netWorth = vm.netWorth()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp),
    ) {
        // Header
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Financial GPS",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        Text(
                            "Learn & improve",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Ink),
        )

        Spacer(Modifier.height(8.dp))

        // --- Smart Recommendations ---
        val recs = remember(data) { vm.recommendations() }
        if (recs.isNotEmpty()) {
            SectionHeading(
                title = "Recommendations for you",
                subtitle = "Personalized based on your spending and household",
                icon = Icons.Rounded.Lightbulb,
                color = Gold,
            )

            Spacer(Modifier.height(12.dp))

            recs.forEachIndexed { index, rec ->
                RecommendationCard(
                    recommendation = rec,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onAction = {
                        when (rec.type) {
                            RecommendationType.MISSING_BILL -> onNavigateToWallet()
                            RecommendationType.OVERSPENDING -> onNavigateToWallet()
                            RecommendationType.DEBT_REDUCTION -> onNavigateToGoals()
                            RecommendationType.EMERGENCY_FUND -> onNavigateToGoals()
                            else -> {}
                        }
                    },
                )
                if (index < recs.lastIndex) {
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // --- Personalized Insights ---
        if (income > 0) {
            
            SectionHeading(
                title = "Your insights",
                subtitle = "Based on this month's activity",
                icon = Icons.Rounded.BarChart,
                color = Mint,
            )

            Spacer(Modifier.height(12.dp))

            // Insight cards
            if (overBudgetCategories.isNotEmpty()) {
                PersonalizedInsightCard(
                    icon = Icons.Rounded.Warning,
                    iconColor = Coral,
                    title = "Overspending detected",
                    body = "You're over the suggested budget in ${overBudgetCategories.size} categor${if (overBudgetCategories.size == 1) "y" else "ies"}: ${
                        overBudgetCategories.take(3).joinToString(", ") { it.category.name }
                    }${if (overBudgetCategories.size > 3) " and more" else ""}. Try setting a monthly budget cap for these.",
                    accentColor = Coral,
                )
                Spacer(Modifier.height(10.dp))
            }

            if (savingsRate < 20) {
                PersonalizedInsightCard(
                    icon = Icons.Rounded.Savings,
                    iconColor = Gold,
                    title = "Boost your savings",
                    body = "You're saving about $savingsRate% of your income. Financial experts recommend at least 20%. Consider cutting back on non-essential spending to build your emergency fund.",
                    accentColor = Gold,
                )
                Spacer(Modifier.height(10.dp))
            } else {
                PersonalizedInsightCard(
                    icon = Icons.Rounded.Star,
                    iconColor = Mint,
                    title = "Great savings rate",
                    body = "You're saving $savingsRate% of your income — that's excellent! Keep it up and consider investing the surplus into a retirement or brokerage account.",
                    accentColor = Mint,
                )
                Spacer(Modifier.height(10.dp))
            }

            if (!hasRetirement && income > 0) {
                PersonalizedInsightCard(
                    icon = Icons.Rounded.TrendingUp,
                    iconColor = Violet,
                    title = "Start retirement planning",
                    body = "You don't have a retirement account yet. Even small contributions to a 401k or IRA compound dramatically over time. Aim to invest at least 10–15% of your income.",
                    accentColor = Violet,
                )
                Spacer(Modifier.height(10.dp))
            }

            if (totalDebt > 0 && income > 0) {
                val debtRatio = (totalDebt / (income * 12).coerceAtLeast(1.0) * 100).toInt()
                if (debtRatio > 36) {
                    PersonalizedInsightCard(
                        icon = Icons.Rounded.TrendingDown,
                        iconColor = Coral,
                        title = "High debt load",
                        body = "Your total debt is about ${debtRatio}% of your annual income — above the recommended 36% maximum. Prioritize paying off high-interest debt first (debt avalanche method).",
                        accentColor = Coral,
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        // --- Retirement Milestones ---
        val milestones = remember(data) { vm.retirementMilestones() }
        if (milestones.isNotEmpty()) {
            SectionHeading(
                title = "Retirement milestones",
                subtitle = "Savings targets based on your age & income",
                icon = Icons.Rounded.Cake,
                color = Violet,
            )
            Spacer(Modifier.height(12.dp))

            RetirementMilestonesSection(
                milestones = milestones,
                userAge = data.household.userAge,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(24.dp))
        }

        // --- App Help / Usage Guide ---
        SectionHeading(
            title = "How to use Financial GPS",
            subtitle = "A guided tour of every tab in the app",
            icon = Icons.Rounded.HelpOutline,
            color = Sky,
        )
        Spacer(Modifier.height(12.dp))

        // Expandable help cards
        AppHelpSection()

        Spacer(Modifier.height(24.dp))

        // --- Budgeting Education ---
        SectionHeading(
            title = "Budgeting basics",
            subtitle = "Build a budget that works for you",
            icon = Icons.Rounded.BarChart,
            color = Mint,
        )
        Spacer(Modifier.height(12.dp))

        EducationCard(
            icon = Icons.Rounded.Star,
            iconBg = Mint.copy(alpha = 0.12f),
            iconTint = Mint,
            title = "The 50/30/20 rule",
            body = "A simple framework for any income level:\n\n" +
                    "• 50% for needs — housing, utilities, groceries, minimum debt payments, transport\n" +
                    "• 30% for wants — dining out, entertainment, shopping, hobbies, travel\n" +
                    "• 20% for savings & debt — emergency fund, retirement accounts, extra debt payments\n\n" +
                    "This rule is a starting point — adjust the percentages based on your cost of living and goals."
        )
        Spacer(Modifier.height(10.dp))

        EducationCard(
            icon = Icons.Rounded.ShoppingBag,
            iconBg = Sky.copy(alpha = 0.12f),
            iconTint = Sky,
            title = "Envelope budgeting",
            body = "Assign a fixed amount to each spending category at the start of the month. When the envelope is empty, you stop spending in that category. This creates hard limits that prevent overspending.\n\n" +
                    "Digital envelopes (like the budget feature in this app) work the same way: set a monthly budget per category and track progress throughout the month."
        )
        Spacer(Modifier.height(10.dp))

        EducationCard(
            icon = Icons.Rounded.Warning,
            iconBg = Coral.copy(alpha = 0.12f),
            iconTint = Coral,
            title = "Emergency fund first",
            body = "Before investing or making large purchases, build an emergency fund of 3–6 months of living expenses. Keep it in a high-yield savings account where it's accessible but earns interest.\n\n" +
                    "This prevents you from going into debt when unexpected expenses arise — car repairs, medical bills, or job loss."
        )
        Spacer(Modifier.height(20.dp))

        // --- Investing Education ---
        SectionHeading(
            title = "Investing 101",
            subtitle = "Grow your wealth over time",
            icon = Icons.Rounded.TrendingUp,
            color = Gold,
        )
        Spacer(Modifier.height(12.dp))

        EducationCard(
            icon = Icons.Rounded.Savings,
            iconBg = Gold.copy(alpha = 0.12f),
            iconTint = Gold,
            title = "Compound interest",
            body = "Money invested today grows exponentially over time. At a 7% average annual return:\n\n" +
                    "• \\$100/month for 30 years → \\$122,000\n" +
                    "• \\$500/month for 30 years → \\$610,000\n" +
                    "• \\$1,000/month for 30 years → \\$1,220,000\n\n" +
                    "The key is starting early and staying consistent — time in the market beats timing the market."
        )
        Spacer(Modifier.height(10.dp))

        EducationCard(
            icon = Icons.Rounded.School,
            iconBg = Violet.copy(alpha = 0.12f),
            iconTint = Violet,
            title = "Retirement accounts",
            body = "• 401k — employer-sponsored plan, often with matching contributions (free money!). Contribute at least enough to get the full match. 2025 limit: \\$23,500/year\n\n" +
                    "• IRA (Traditional or Roth) — individual retirement account with tax advantages. 2025 limit: \\$7,000/year\n\n" +
                    "• The earlier you start, the less you need to save each month thanks to compound growth."
        )
        Spacer(Modifier.height(10.dp))

        EducationCard(
            icon = Icons.Rounded.BarChart,
            iconBg = Mint.copy(alpha = 0.12f),
            iconTint = Mint,
            title = "Index funds vs individual stocks",
            body = "• Index funds (S&P 500, total market) — low-cost, diversified, and historically return ~7–10% annually over the long term. Recommended for most investors.\n\n" +
                    "• Individual stocks — higher risk and volatility. Consider limiting to 5–10% of your portfolio as \"fun money\" you're willing to lose.\n\n" +
                    "Warren Buffett's advice: \"Consistently buy an S&P 500 low-cost index fund. It's the thing that makes the most sense practically all of the time.\""
        )
        Spacer(Modifier.height(20.dp))

        // --- Smart Purchasing ---
        SectionHeading(
            title = "Smart purchasing",
            subtitle = "Make large purchases with confidence",
            icon = Icons.Rounded.ShoppingBag,
            color = Sky,
        )
        Spacer(Modifier.height(12.dp))

        EducationCard(
            icon = Icons.Rounded.DirectionsCar,
            iconBg = Coral.copy(alpha = 0.12f),
            iconTint = Coral,
            title = "Buying a car",
            body = "• The 20/4/10 rule: 20% down payment, 4-year loan max, total car costs under 10% of gross monthly income\n\n" +
                    "• New cars lose 15–20% of value in year one. Consider a 2–3 year old used car to avoid the steepest depreciation\n\n" +
                    "• Check your car buying power in the Goals → Assets tab to see what you can afford based on your real numbers"
        )
        Spacer(Modifier.height(10.dp))

        EducationCard(
            icon = Icons.Rounded.Home,
            iconBg = Sky.copy(alpha = 0.12f),
            iconTint = Sky,
            title = "Buying a home",
            body = "• The 28/36 rule: mortgage + taxes + insurance ≤ 28% of gross monthly income, total debt ≤ 36%\n\n" +
                    "• Aim for a 20% down payment to avoid PMI (private mortgage insurance), which adds ~0.5–1% to your annual costs\n\n" +
                    "• Don't forget closing costs (2–5% of purchase price), property taxes, insurance, maintenance (~1% of home value/year), and utilities\n\n" +
                    "• Check your home buying power in the Goals → Assets tab"
        )
        Spacer(Modifier.height(10.dp))

        EducationCard(
            icon = Icons.Rounded.Movie,
            iconBg = Violet.copy(alpha = 0.12f),
            iconTint = Violet,
            title = "The 30-day rule for impulse buys",
            body = "For any non-essential purchase over \\$100, wait 30 days before buying. Write it down and revisit the decision after a month.\n\n" +
                    "Most impulse purchases lose their appeal after a cooling-off period. The money you save can go toward your real goals — emergency fund, retirement, or that vacation you actually want.\n\n" +
                    "For purchases over \\$500, extend this to a 90-day rule and research alternatives during that time."
        )
        Spacer(Modifier.height(20.dp))

        // Bottom spacing
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            modifier = Modifier.padding(start = 42.dp),
        )
    }
}

@Composable
private fun EducationCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    body: String,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f,
            )
        }
    }
}

@Composable
private fun PersonalizedInsightCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    body: String,
    accentColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.08f), InkElevated)
                )
            ),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f,
                )
            }
        }
    }
}

private data class HelpItem(
    val icon: ImageVector,
    val title: String,
    val accentColor: Color,
    val steps: List<String>,
)

@Composable
private fun AppHelpSection() {
    val helpItems = remember {
        listOf(
            HelpItem(
                icon = Icons.Rounded.Home,
                title = "Home tab",
                accentColor = Mint,
                steps = listOf(
                    "See your total net worth, monthly income, and spending at a glance.",
                    "Tap \"See all accounts\" to jump to the Wallet tab and manage your payment methods.",
                    "Tap \"See all activity\" to jump to the Activity tab and review transactions.",
                    "The \"+\" button in the bottom bar opens the quick-add transaction sheet from any tab.",
                ),
            ),
            HelpItem(
                icon = Icons.Rounded.CreditCard,
                title = "Wallet tab",
                accentColor = Sky,
                steps = listOf(
                    "Add all your payment accounts — checking, savings, credit cards, investments, and retirement.",
                    "Tap any account to see its balance and recent activity.",
                    "Credit cards show a circular gauge of available credit vs. credit limit so you never max out.",
                    "Use the \"+\" button or the add button to create new accounts.",
                ),
            ),
            HelpItem(
                icon = Icons.Rounded.BarChart,
                title = "Budget tab",
                accentColor = Coral,
                steps = listOf(
                    "Budget sub-tab — set spending categories with suggested percentage guidelines and monthly budget caps.",
                    "Each category shows a progress bar tracking actual spending against your budget target.",
                    "Tap edit to change a category's name, budget, percentage, icon, or color.",
                    "Calendar sub-tab — add recurring bills by day-of-month and mark vacation dates.",
                    "When adding a transaction with the recurring bill toggle, a matching calendar event is created automatically.",
                    "Monthly sub-tab — view a 12-month bar chart comparing income vs. spending over time.",
                ),
            ),
            HelpItem(
                icon = Icons.Rounded.SwapVert,
                title = "Activity tab",
                accentColor = Mint,
                steps = listOf(
                    "See every transaction you've recorded, newest first.",
                    "Tap any transaction to see details: category, account, amount, date, and notes.",
                    "Swipe left on a transaction to delete it — linked recurring calendar events are also cleaned up.",
                    "Use the search bar or filter chips at the top to find specific transactions fast.",
                ),
            ),
            HelpItem(
                icon = Icons.Rounded.Flag,
                title = "Goals tab",
                accentColor = Gold,
                steps = listOf(
                    "Goals sub-tab — track savings goals with a target amount and progress ring. Tap \"Contribute\" to add money toward any goal.",
                    "Debts sub-tab — track debts with total owed, amount paid, and a payoff progress bar. Tap \"Pay debt\" to record a payment.",
                    "Assets sub-tab — add vehicles you own with purchase price and current value. See car and home buying-power estimates based on your real numbers.",
                    "Buying power calculators use your income, savings, and debts to estimate what you can afford.",
                ),
            ),
            HelpItem(
                icon = Icons.Rounded.Lightbulb,
                title = "Learn tab",
                accentColor = Violet,
                steps = listOf(
                    "You're here now! This tab gives you personalized financial insights and education.",
                    "Recommendations at the top are tailored to your spending patterns and life situation.",
                    "Scroll down for guides on budgeting, investing, and smart purchasing decisions.",
                    "Use the tips here to build better money habits and reach your financial goals faster.",
                ),
            ),
        )
    }

    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        helpItems.forEachIndexed { index, item ->
            val isExpanded = expandedIndex == index

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isExpanded) item.accentColor.copy(alpha = 0.08f) else InkElevated
                    )
                    .animateContentSize()
                    .clickable { expandedIndex = if (isExpanded) null else index },
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(item.accentColor.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = null,
                                tint = item.accentColor,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextTertiary,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    if (isExpanded) {
                        Spacer(Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(item.accentColor.copy(alpha = 0.12f)),
                        )
                        Spacer(Modifier.height(12.dp))
                        item.steps.forEachIndexed { stepIndex, step ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 7.dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(item.accentColor.copy(alpha = 0.6f)),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.45f,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RetirementMilestonesSection(
    milestones: List<com.rork.budgetflow.ui.BudgetViewModel.RetirementMilestone>,
    userAge: Int,
    modifier: Modifier = Modifier,
) {
    val retirementBalance = milestones.firstOrNull()?.currentBalance ?: 0.0
    val maxTarget = milestones.lastOrNull()?.targetAmount ?: 0.0
    val overallProgress = if (maxTarget > 0) {
        (retirementBalance / maxTarget).toFloat().coerceIn(0f, 1f)
    } else 0f

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            // Header with user's current age
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Violet.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Cake,
                        contentDescription = null,
                        tint = Violet,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "At age $userAge",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Fidelity savings benchmarks",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        com.rork.budgetflow.data.Money.formatCompact(retirementBalance),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (retirementBalance > 0) Mint else TextTertiary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Overall progress bar
            androidx.compose.material3.Text(
                text = if (retirementBalance > 0) {
                    "${(overallProgress * 100).toInt()}% toward your age-67 goal"
                } else {
                    "Start a retirement account to begin tracking progress"
                },
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(6.dp))
            com.rork.budgetflow.ui.components.ProgressBar(
                progress = overallProgress,
                color = Violet,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(18.dp))

            // Milestone timeline
            milestones.forEachIndexed { index, milestone ->
                val milestoneColor = when {
                    milestone.isReached -> Mint
                    milestone.yearsAway <= 0 -> Coral
                    milestone.yearsAway <= 5 -> Gold
                    else -> Violet
                }
                val progress = if (milestone.targetAmount > 0) {
                    (milestone.currentBalance / milestone.targetAmount).toFloat().coerceIn(0f, 1f)
                } else 0f

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Age circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(milestoneColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${milestone.targetAge}",
                            style = MaterialTheme.typography.titleSmall,
                            color = milestoneColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Milestone details
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${milestone.incomeMultiple.toInt()}x income",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(milestoneColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    milestone.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = milestoneColor,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Target: ${com.rork.budgetflow.data.Money.formatCompact(milestone.targetAmount)}" +
                                if (milestone.yearsAway > 0) "  •  ${milestone.yearsAway}y away" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                        Spacer(Modifier.height(6.dp))
                        com.rork.budgetflow.ui.components.ProgressBar(
                            progress = progress,
                            color = milestoneColor,
                            modifier = Modifier.fillMaxWidth(),
                            height = 6.dp,
                        )
                    }
                }

                if (index < milestones.lastIndex) {
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

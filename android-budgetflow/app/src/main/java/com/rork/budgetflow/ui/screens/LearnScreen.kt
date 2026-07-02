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
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SetMeal
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.TheaterComedy
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

        // --- Free & Low-Cost Activities ---
        SectionHeading(
            title = "Free & low-cost activities",
            subtitle = "Have fun without breaking the budget",
            icon = Icons.Rounded.Palette,
            color = Coral,
        )
        Spacer(Modifier.height(12.dp))

        FreeActivitiesSection()

        Spacer(Modifier.height(20.dp))

        // --- Low-Cost Healthy Food ---
        SectionHeading(
            title = "Low-cost healthy food",
            subtitle = "A grocery list you can take shopping",
            icon = Icons.Rounded.LocalGroceryStore,
            color = MintDeep,
        )
        Spacer(Modifier.height(12.dp))

        HealthyFoodSection(
            adultCount = data.household.adultCount,
            childCount = data.household.childCount,
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

private data class ActivityGroup(
    val icon: ImageVector,
    val title: String,
    val costLabel: String,
    val accentColor: Color,
    val activities: List<String>,
)

private data class FoodCategory(
    val icon: ImageVector,
    val title: String,
    val accentColor: Color,
    val items: List<String>,
    val estimatedCost: String,
)

@Composable
private fun HealthyFoodSection(
    adultCount: Int = 1,
    childCount: Int = 0,
) {
    // Recommended monthly grocery budget: $300-350 per adult, $100-150 per child
    val adultMin = adultCount * 300
    val adultMax = adultCount * 350
    val childMin = childCount * 100
    val childMax = childCount * 150
    val totalMin = adultMin + childMin
    val totalMax = adultMax + childMax

    val categories = remember {
        listOf(
            FoodCategory(
                icon = Icons.Rounded.SetMeal,
                title = "Proteins",
                accentColor = Coral,
                estimatedCost = "~\$0.15-0.50 / serving",
                items = listOf(
                    "Dried lentils — ~$1.50/lb, 11g protein per serving, cooks in 20 min",
                    "Dried beans (black, pinto, kidney) — ~$1.30/lb, 7g protein per serving",
                    "Split peas — ~$1.20/lb, great for hearty soups",
                    "Canned tuna — ~$0.75/can, 20g protein, stock up on sales",
                    "Canned chicken — ~$2.50/can, versatile for sandwiches and salads",
                    "Eggs — ~$2.50/dozen, 6g protein each, one of the cheapest proteins",
                    "Whole chicken — ~$1.50/lb, roast it for multiple meals, make stock from bones",
                    "Chicken thighs (bone-in) — ~$1.80/lb, cheaper and juicier than breasts",
                    "Ground turkey — ~$3.50/lb, leaner than beef and often cheaper",
                    "Peanut butter — ~$2.50/jar, 8g protein per 2 tbsp, no-stir preferred",
                    "Cottage cheese — ~$3.00/tub, 14g protein per half cup",
                    "Greek yogurt (plain, large tub) — ~$4.00/32oz, 15-20g protein per cup",
                    "Tofu (firm) — ~$2.00/block, 10g protein per serving",
                    "Sardines — ~$2.00/tin, packed with omega-3s and protein",
                ),
            ),
            FoodCategory(
                icon = Icons.Rounded.Restaurant,
                title = "Grains & starches",
                accentColor = Gold,
                estimatedCost = "~\$0.05-0.30 / serving",
                items = listOf(
                    "Brown rice — ~$1.50/lb, filling and fiber-rich, batch cook and freeze",
                    "White rice — ~$1.00/lb, cheapest staple calorie, pairs with anything",
                    "Rolled oats — ~$1.00/lb, 30+ servings per bag, breakfast or baking",
                    "Whole wheat pasta — ~$1.50/box, more fiber than white",
                    "Bulgur wheat — ~$2.00/lb, cooks fast, great in tabbouleh and pilafs",
                    "Pearl barley — ~$1.50/lb, hearty addition to soups and stews",
                    "Potatoes (russet) — ~$0.60/lb, vitamin C, potassium, very filling",
                    "Sweet potatoes — ~$0.90/lb, rich in vitamin A, versatile",
                    "Cornmeal / polenta — ~$1.00/lb, creamy side dish or baked into bread",
                    "Whole wheat flour — ~$2.50/5lb, bake your own bread or tortillas",
                    "Popcorn kernels — ~$1.50/lb, cheap whole-grain snack, air-pop for healthiest",
                ),
            ),
            FoodCategory(
                icon = Icons.Rounded.Grass,
                title = "Fruits & vegetables",
                accentColor = Mint,
                estimatedCost = "~\$0.30-1.00 / serving",
                items = listOf(
                    "Bananas — ~$0.30/lb, cheapest fresh fruit, great for smoothies and snacks",
                    "Apples (in season) — ~$1.20/lb, buy bags for savings",
                    "Oranges — ~$0.70/lb, vitamin C powerhouse",
                    "Cabbage — ~$0.40/lb, lasts weeks, shred for slaw or stir-fry",
                    "Carrots — ~$0.80/lb, or ~$1.50 for a 2lb bag, lasts a month",
                    "Onions — ~$0.60/lb, flavor base for almost every dish",
                    "Garlic — ~$3.00/lb, a little goes a long way",
                    "Potatoes — also here! Cheap, filling, nutrient-dense",
                    "Sweet potatoes — versatile and affordable",
                    "Frozen mixed vegetables — ~$1.50/bag, just as nutritious as fresh, no waste",
                    "Frozen spinach — ~$1.50/box, add to eggs, soups, smoothies",
                    "Frozen berries — ~$3.00/bag, cheaper than fresh for smoothies and oatmeal",
                    "Roma tomatoes — ~$1.00/lb, cheapest tomato variety",
                    "Kale or collard greens — ~$1.50/bunch, nutrient-dense and cheap",
                    "Celery — ~$1.50/bunch, lasts weeks, snacks and soup base",
                    "Seasonal produce — check farmers market end-of-day for deals",
                ),
            ),
            FoodCategory(
                icon = Icons.Rounded.LocalDining,
                title = "Dairy & eggs",
                accentColor = Sky,
                estimatedCost = "~\$0.20-0.60 / serving",
                items = listOf(
                    "Eggs — listed in proteins, but a dairy-section staple too",
                    "Milk (gallon) — ~$3.00, cheapest calcium source",
                    "Plain yogurt (large tub) — ~$3.00, cheaper and healthier than individual cups",
                    "Greek yogurt (plain) — ~$4.00/32oz, high protein, use in place of sour cream",
                    "Cottage cheese — ~$3.00/tub, high protein snack or breakfast",
                    "Block cheese (cheddar, mozzarella) — ~$4.00/lb, shred yourself — pre-shredded costs 2x",
                    "Parmesan (wedge) — ~$8.00/lb but a little lasts months",
                    "Butter — ~$4.00/lb, cook and bake with it",
                    "Powdered milk — ~$10.00/box, shelf-stable backup for baking and cooking",
                ),
            ),
            FoodCategory(
                icon = Icons.Rounded.Egg,
                title = "Pantry staples & legumes",
                accentColor = Violet,
                estimatedCost = "~\$0.02-0.50 / serving",
                items = listOf(
                    "Dried lentils — already in proteins, but a pantry hero",
                    "Dried beans — soak overnight, cook a big batch, freeze portions",
                    "Chickpeas (dried) — ~$1.50/lb, hummus, roasts, curries",
                    "Canned beans (any) — ~$0.80/can, convenient when you can't soak",
                    "Canned diced tomatoes — ~$1.00/can, soup, sauce, chili base",
                    "Canned tomato paste — ~$0.60/can, flavor booster for sauces",
                    "Olive oil — ~$8.00/bottle, use sparingly, lasts months",
                    "Canola or vegetable oil — ~$3.00/bottle, budget cooking oil",
                    "Vinegar (apple cider or white) — ~$2.00/bottle, salad dressing and cooking",
                    "Salt & pepper — buy in bulk, lasts forever",
                    "Cumin, paprika, garlic powder, chili powder — ~$2 each, build flavor cheaply",
                    "Chicken or vegetable bouillon — ~$3.00/jar, instant broth for soups",
                    "Canned corn — ~$0.80/can, add to soups, salsas, salads",
                    "Raisins or dried cranberries — ~$3.00/bag, snack or oatmeal topping",
                    "Pasta sauce (jar, basic) — ~$2.00/jar, or make your own from canned tomatoes",
                ),
            ),
            FoodCategory(
                icon = Icons.Rounded.Restaurant,
                title = "Frozen & bulk buys",
                accentColor = MintDeep,
                estimatedCost = "Best value per serving",
                items = listOf(
                    "Frozen chicken breasts (bulk bag) — ~$2.50/lb, cheaper than fresh",
                    "Frozen tilapia or cod — ~$4.00/lb, affordable fish option",
                    "Frozen mixed vegetables — ~$1.50/bag, perfect for stir-fries and soups",
                    "Frozen fruit (for smoothies) — ~$3.00/bag, cheaper than fresh out of season",
                    "Frozen peas — ~$1.50/bag, add protein and color to any dish",
                    "Frozen spinach — ~$1.50/box, sneaky nutrition booster",
                    "Frozen edamame — ~$3.00/bag, cheap protein snack",
                    "Rice (10-20lb bag) — ~$10-18, lowest cost per serving",
                    "Dried beans (bulk) — ~$1.00/lb in bulk bins, cheapest protein",
                    "Oats (bulk) — ~$0.80/lb, breakfast for pennies",
                    "Flour (bulk) — ~$2.00/5lb, bake bread to save even more",
                    "Peanut butter (large jar) — ~$5.00/28oz, cheapest per-ounce protein",
                ),
            ),
        )
    }

    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Grocery budget recommendation card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MintDeep.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Savings,
                            contentDescription = null,
                            tint = MintDeep,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Your recommended grocery budget",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Based on your household size",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Big total range
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "\$${totalMin}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MintDeep,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        " – \$${totalMax}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MintDeep,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "/ month",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Breakdown by adult / child
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (adultCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Mint.copy(alpha = 0.10f)),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.LocalDining,
                                        contentDescription = null,
                                        tint = Mint,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Per adult",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "\$300 – \$350",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "$adultCount adult${if (adultCount == 1) "" else "s"} = \$${adultMin} – \$${adultMax}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                )
                            }
                        }
                    }
                    if (childCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Sky.copy(alpha = 0.10f)),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.ChildCare,
                                        contentDescription = null,
                                        tint = Sky,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Per child",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "\$100 – \$150",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "$childCount child${if (childCount == 1) "" else "ren"} = \$${childMin} – \$${childMax}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "These estimates assume home-cooked meals using the budget-friendly foods listed below. Costs vary by region and dietary needs — adjust as needed for your situation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f,
                )
            }
        }

        // Main clickable card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MintDeep.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.LocalGroceryStore,
                            contentDescription = null,
                            tint = MintDeep,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Low-cost healthy grocery list",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Tap to expand the full shopping list",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                    }
                    Icon(
                        if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextTertiary,
                        modifier = Modifier.size(24.dp),
                    )
                }

                if (!isExpanded) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Eating healthy on a budget is absolutely possible. This list covers proteins, grains, produce, dairy, and pantry staples — most cost under \$1 per serving. Use it as your shopping guide and build meals around what's on sale.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f,
                    )
                }
            }
        }

        // Expanded grocery list
        AnimatedVisibility(visible = isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Shopping tips card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Smart shopping tips",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        val tips = listOf(
                            "Shop the perimeter — fresh produce, dairy, and proteins are usually on the outer aisles",
                            "Buy in bulk for staples like rice, beans, and oats — lowest cost per serving",
                            "Frozen fruits and vegetables are just as nutritious as fresh and won't spoil",
                            "Compare unit prices (price per lb/oz), not package prices",
                            "Plan meals around what's on sale each week",
                            "Cook once, eat twice — batch cook beans, rice, and proteins to save time and money",
                            "Shop seasonal produce for the best prices and flavor",
                            "Never shop hungry — you'll buy 23% more on average",
                        )
                        tips.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Gold.copy(alpha = 0.6f)),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                // Category cards
                categories.forEachIndexed { index, category ->
                    var categoryExpanded by remember { mutableStateOf(index == 0) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (categoryExpanded) category.accentColor.copy(alpha = 0.08f) else InkElevated
                            )
                            .animateContentSize()
                            .clickable { categoryExpanded = !categoryExpanded },
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(category.accentColor.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        category.icon,
                                        contentDescription = null,
                                        tint = category.accentColor,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        category.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        category.estimatedCost,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                    )
                                }
                                Icon(
                                    if (categoryExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = if (categoryExpanded) "Collapse" else "Expand",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }

                            if (categoryExpanded) {
                                Spacer(Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(category.accentColor.copy(alpha = 0.12f)),
                                )
                                Spacer(Modifier.height(12.dp))
                                category.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 7.dp)
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(category.accentColor.copy(alpha = 0.6f)),
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            item,
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

                // Budget meal ideas card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Restaurant,
                                contentDescription = null,
                                tint = Mint,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Budget meal ideas from this list",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        val meals = listOf(
                            "Lentil soup with carrots, onions, and spices — under \$1.50 for 4 servings",
                            "Rice and beans with salsa — complete protein, under \$0.75/serving",
                            "Oatmeal with bananas and peanut butter — under \$0.40/bowl",
                            "Egg fried rice with frozen mixed veg — under \$1.00/serving",
                            "Black bean tacos with cabbage slaw — under \$1.50 for 3 tacos",
                            "Pasta with canned tomatoes, garlic, and olive oil — under \$1.00/serving",
                            "Baked potato with cottage cheese and steamed broccoli — under \$1.20",
                            "Greek yogurt with frozen berries and oats — under \$0.80/bowl",
                            "Tuna salad on whole wheat toast — under \$1.50 for 2 sandwiches",
                            "Chickpea curry with rice — under \$1.00/serving, makes 4-6 servings",
                        )
                        meals.forEach { meal ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Mint.copy(alpha = 0.6f)),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    meal,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f,
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
private fun FreeActivitiesSection() {
    val groups = remember {
        listOf(
            ActivityGroup(
                icon = Icons.Rounded.Forest,
                title = "Outdoors & nature",
                costLabel = "FREE",
                accentColor = Mint,
                activities = listOf(
                    "Hiking local trails and nature preserves — most are free and open year-round",
                    "Visiting state and national parks (many are free; some charge a small day-use fee)",
                    "Walking or biking in your neighborhood — great exercise and costs nothing",
                    "Geocaching — a free real-world treasure hunt using your phone's GPS",
                    "Birdwatching at local wetlands, lakes, or parks — download a free app like Merlin to identify species",
                    "Stargazing on a clear night — find a dark spot away from city lights",
                    "Gardening — start with seeds or cuttings from a neighbor; grow herbs on a windowsill",
                    "Swimming at public beaches, lakes, or community pools (low-cost day passes)",
                ),
            ),
            ActivityGroup(
                icon = Icons.Rounded.ChildCare,
                title = "With kids",
                costLabel = "FREE",
                accentColor = Sky,
                activities = listOf(
                    "Taking kids to the playground or park — pack snacks and make a day of it",
                    "Library visits — borrow books, movies, audiobooks, and attend free story-time events",
                    "Building forts, obstacle courses, or cardboard-box creations at home",
                    "Free museum days — many museums offer free admission weekly or monthly",
                    "Craft projects using recycled materials, paper, and things you already have",
                    "Backyard camping or indoor blanket-tent sleepovers",
                    "Scavenger hunts around the house, yard, or neighborhood",
                    "Baking or cooking together with pantry ingredients",
                    "Community events — farmers markets, parades, outdoor concerts, and festivals",
                ),
            ),
            ActivityGroup(
                icon = Icons.Rounded.Movie,
                title = "Entertainment at home",
                costLabel = "FREE – $$",
                accentColor = Violet,
                activities = listOf(
                    "Movie nights — borrow DVDs free from the library or use a streaming service you already pay for",
                    "Board game or card game nights — dust off games you own or borrow from friends",
                    "Hosting a potluck dinner — everyone brings a dish, you provide the space",
                    "Reading — libraries are free and most offer digital e-books and audiobooks too",
                    "Learning a new skill on YouTube — drawing, cooking, guitar, origami, you name it",
                    "Podcast listening — thousands of free shows on every topic imaginable",
                    "Home workout videos — yoga, HIIT, dance — all free on YouTube",
                    "Starting a journal, writing stories, or learning to draw",
                ),
            ),
            ActivityGroup(
                icon = Icons.Rounded.TheaterComedy,
                title = "Out & about",
                costLabel = "FREE – $$",
                accentColor = Gold,
                activities = listOf(
                    "Free outdoor concerts and movies in the park — check your city's event calendar",
                    "First Friday / art walk gallery nights — many cities host free monthly art walks",
                    "Community theater and school performances — often free or donation-based",
                    "Free workshops at libraries, hardware stores, and community centers",
                    "Volunteering — meet people, give back, and feel great (animal shelters, food banks, habitat builds)",
                    "Farmers markets — free to browse, and you can buy affordable fresh produce",
                    "Exploring a new neighborhood or downtown area on foot",
                    "Free admission days at zoos, aquariums, and botanical gardens",
                ),
            ),
            ActivityGroup(
                icon = Icons.Rounded.SelfImprovement,
                title = "Health & wellness",
                costLabel = "FREE",
                accentColor = Coral,
                activities = listOf(
                    "Meditation and breathing exercises — free apps like Insight Timer or YouTube guided sessions",
                    "Home yoga or stretching routines — no gym membership needed",
                    "Running, walking, or jogging — just lace up your shoes",
                    "Meal-prepping and cooking at home — save money and eat healthier",
                    "Decluttering your home — therapeutic and you can sell items for extra cash",
                    "Free online courses — Coursera, Khan Academy, and edX offer thousands of free classes",
                ),
            ),
        )
    }

    var expandedIndex by remember { mutableStateOf<Int?>(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Intro card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Fun doesn't have to be expensive.",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "The average household spends over \$3,000/year on entertainment. Cutting just a third of that and redirecting it to savings can add up to an extra \$1,000+ a year toward your goals. Tap each category to explore ideas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f,
                )
            }
        }

        groups.forEachIndexed { index, group ->
            val isExpanded = expandedIndex == index

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isExpanded) group.accentColor.copy(alpha = 0.08f) else InkElevated
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
                                .background(group.accentColor.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                group.icon,
                                contentDescription = null,
                                tint = group.accentColor,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            group.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(group.accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                group.costLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = group.accentColor,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
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
                                .background(group.accentColor.copy(alpha = 0.12f)),
                        )
                        Spacer(Modifier.height(12.dp))
                        group.activities.forEachIndexed { activityIndex, activity ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 7.dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(group.accentColor.copy(alpha = 0.6f)),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    activity,
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

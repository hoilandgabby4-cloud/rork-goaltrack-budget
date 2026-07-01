package com.rork.budgetflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rork.budgetflow.data.Account
import com.rork.budgetflow.data.AccountType
import com.rork.budgetflow.data.BudgetData
import com.rork.budgetflow.data.BudgetRepository
import com.rork.budgetflow.data.BuyingPower
import com.rork.budgetflow.data.BuyingPowerType
import com.rork.budgetflow.data.Category
import com.rork.budgetflow.data.Dates
import com.rork.budgetflow.data.Debt
import com.rork.budgetflow.data.FinancialHealth
import com.rork.budgetflow.data.GuidelineRow
import com.rork.budgetflow.data.HealthMetric
import com.rork.budgetflow.data.HealthStatus
import com.rork.budgetflow.data.HouseholdProfile
import com.rork.budgetflow.data.MonthBar
import com.rork.budgetflow.data.Recommendation
import com.rork.budgetflow.data.RecommendationType
import com.rork.budgetflow.data.SavingsGoal
import com.rork.budgetflow.data.Transaction
import com.rork.budgetflow.data.Vehicle
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BudgetRepository(app)

    private val _data = MutableStateFlow(repo.load())
    val data: StateFlow<BudgetData> = _data.asStateFlow()

    private fun update(transform: (BudgetData) -> BudgetData) {
        val next = transform(_data.value)
        _data.value = next
        viewModelScope.launch { repo.save(next) }
    }

    // --- Derived helpers -----------------------------------------------------

    fun account(id: String?): Account? = _data.value.accounts.firstOrNull { it.id == id }
    fun category(id: String?): Category? = _data.value.categories.firstOrNull { it.id == id }

    /** Total available money: positive balances minus credit (debt) balances. */
    fun netWorth(): Double {
        return _data.value.accounts.sumOf { acc ->
            if (acc.type == AccountType.CREDIT) -acc.balance else acc.balance
        }
    }

    fun totalAvailable(): Double = _data.value.accounts
        .filter { it.type != AccountType.CREDIT }
        .sumOf { it.balance }

    fun totalCreditOwed(): Double = _data.value.accounts
        .filter { it.type == AccountType.CREDIT }
        .sumOf { it.balance }

    fun spentThisMonth(data: BudgetData = _data.value): Double = data.transactions
        .filter { !it.isIncome && Dates.isThisMonth(it.timestamp) }
        .sumOf { it.amount }

    fun incomeThisMonth(data: BudgetData = _data.value): Double = data.transactions
        .filter { it.isIncome && Dates.isThisMonth(it.timestamp) }
        .sumOf { it.amount }

    /** Amount spent against a category in the current month. */
    fun spentInCategory(categoryId: String, data: BudgetData = _data.value): Double = data.transactions
        .filter { !it.isIncome && it.categoryId == categoryId && Dates.isThisMonth(it.timestamp) }
        .sumOf { it.amount }

    /** Category + spending amount for the current month, sorted highest first. */
    fun spentByCategory(data: BudgetData = _data.value): List<Pair<Category, Double>> {
        return data.categories
            .map { cat -> cat to spentInCategory(cat.id, data) }
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }
    }

    // --- Transactions --------------------------------------------------------

    /**
     * Records a transaction and adjusts the chosen account's balance. Spending
     * lowers a cash/savings balance but raises a credit card's owed balance.
     */
    fun addTransaction(
        title: String,
        amount: Double,
        accountId: String,
        categoryId: String?,
        isIncome: Boolean,
    ) {
        if (amount <= 0.0) return
        val tx = Transaction(
            id = BudgetRepository.uuid(),
            title = title.trim().ifBlank { if (isIncome) "Income" else "Expense" },
            amount = amount,
            accountId = accountId,
            categoryId = if (isIncome) null else categoryId,
            isIncome = isIncome,
            timestamp = System.currentTimeMillis(),
        )
        update { d ->
            val accounts = d.accounts.map { acc ->
                if (acc.id != accountId) return@map acc
                val delta = applyDelta(acc.type, amount, isIncome)
                acc.copy(balance = acc.balance + delta)
            }
            d.copy(accounts = accounts, transactions = listOf(tx) + d.transactions)
        }
    }

    private fun applyDelta(type: AccountType, amount: Double, isIncome: Boolean): Double {
        return if (type == AccountType.CREDIT) {
            // Spending increases what is owed; income (a payment) reduces it.
            if (isIncome) -amount else amount
        } else {
            if (isIncome) amount else -amount
        }
    }

    fun deleteTransaction(id: String) {
        update { d ->
            val tx = d.transactions.firstOrNull { it.id == id } ?: return@update d
            val accounts = d.accounts.map { acc ->
                if (acc.id != tx.accountId) return@map acc
                // Reverse the original delta.
                val delta = -applyDelta(acc.type, tx.amount, tx.isIncome)
                acc.copy(balance = acc.balance + delta)
            }
            d.copy(accounts = accounts, transactions = d.transactions.filterNot { it.id == id })
        }
    }

    // --- Accounts ------------------------------------------------------------

    fun addAccount(name: String, type: AccountType, balance: Double, colorArgb: Long, creditLimit: Double) {
        update { d ->
            d.copy(
                accounts = d.accounts + Account(
                    id = BudgetRepository.uuid(),
                    name = name.trim().ifBlank { type.label },
                    type = type,
                    balance = balance,
                    colorArgb = colorArgb,
                    creditLimit = creditLimit,
                )
            )
        }
    }

    fun deleteAccount(id: String) {
        update { d -> d.copy(accounts = d.accounts.filterNot { it.id == id }) }
    }

    // --- Categories ----------------------------------------------------------

    fun addCategory(name: String, iconKey: String, colorArgb: Long, monthlyBudget: Double, suggestedPercentage: Double = 0.0) {
        update { d ->
            d.copy(
                categories = d.categories + Category(
                    id = BudgetRepository.uuid(),
                    name = name.trim().ifBlank { "Category" },
                    iconKey = iconKey,
                    colorArgb = colorArgb,
                    monthlyBudget = monthlyBudget,
                    suggestedPercentage = suggestedPercentage,
                )
            )
        }
    }

    fun updateCategory(id: String, name: String, iconKey: String, colorArgb: Long, monthlyBudget: Double, suggestedPercentage: Double = 0.0) {
        update { d ->
            d.copy(categories = d.categories.map {
                if (it.id == id) it.copy(
                    name = name.trim().ifBlank { "Category" },
                    iconKey = iconKey,
                    colorArgb = colorArgb,
                    monthlyBudget = monthlyBudget,
                    suggestedPercentage = suggestedPercentage,
                ) else it
            })
        }
    }

    fun deleteCategory(id: String) {
        update { d -> d.copy(categories = d.categories.filterNot { it.id == id }) }
    }

    // --- Goals ---------------------------------------------------------------

    fun addGoal(name: String, target: Double, colorArgb: Long, iconKey: String) {
        update { d ->
            d.copy(
                goals = d.goals + SavingsGoal(
                    id = BudgetRepository.uuid(),
                    name = name.trim().ifBlank { "Goal" },
                    target = target,
                    saved = 0.0,
                    colorArgb = colorArgb,
                    iconKey = iconKey,
                )
            )
        }
    }

    /** Move money from an account into a savings goal. */
    fun contributeToGoal(goalId: String, amount: Double, fromAccountId: String?) {
        if (amount <= 0.0) return
        update { d ->
            val goals = d.goals.map {
                if (it.id == goalId) it.copy(saved = (it.saved + amount).coerceAtMost(it.target)) else it
            }
            val accounts = if (fromAccountId != null) {
                d.accounts.map { acc ->
                    if (acc.id != fromAccountId) return@map acc
                    val delta = if (acc.type == AccountType.CREDIT) amount else -amount
                    acc.copy(balance = acc.balance + delta)
                }
            } else d.accounts
            d.copy(goals = goals, accounts = accounts)
        }
    }

    fun deleteGoal(id: String) {
        update { d -> d.copy(goals = d.goals.filterNot { it.id == id }) }
    }

    // --- Debts ---------------------------------------------------------------

    fun addDebt(name: String, total: Double, apr: Double, colorArgb: Long) {
        update { d ->
            d.copy(
                debts = d.debts + Debt(
                    id = BudgetRepository.uuid(),
                    name = name.trim().ifBlank { "Debt" },
                    total = total,
                    paid = 0.0,
                    apr = apr,
                    colorArgb = colorArgb,
                )
            )
        }
    }

    /** Pay down a debt, optionally pulling the payment from an account. */
    fun payDebt(debtId: String, amount: Double, fromAccountId: String?) {
        if (amount <= 0.0) return
        update { d ->
            val debts = d.debts.map {
                if (it.id == debtId) it.copy(paid = (it.paid + amount).coerceAtMost(it.total)) else it
            }
            val accounts = if (fromAccountId != null) {
                d.accounts.map { acc ->
                    if (acc.id != fromAccountId) return@map acc
                    val delta = if (acc.type == AccountType.CREDIT) amount else -amount
                    acc.copy(balance = acc.balance + delta)
                }
            } else d.accounts
            d.copy(debts = debts, accounts = accounts)
        }
    }

    fun deleteDebt(id: String) {
        update { d -> d.copy(debts = d.debts.filterNot { it.id == id }) }
    }

    // --- Vehicles ------------------------------------------------------------

    /** Estimated current value accounting for auto-depreciation when [Vehicle.currentValue] is null. */
    fun estimatedVehicleValue(v: Vehicle): Double {
        v.currentValue?.let { return it }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val age = (currentYear - v.year).coerceAtLeast(0)
        if (age <= 0) return v.purchasePrice
        // 15% first year, 10% each subsequent year
        val firstYear = 0.85
        val subsequent = Math.pow(0.90, (age - 1).toDouble())
        return v.purchasePrice * firstYear * subsequent
    }

    fun totalVehicleWorth(): Double = _data.value.vehicles.sumOf { estimatedVehicleValue(it) }

    fun addVehicle(make: String, model: String, year: Int, purchasePrice: Double, currentValue: Double?, colorArgb: Long) {
        update { d ->
            d.copy(
                vehicles = d.vehicles + Vehicle(
                    id = BudgetRepository.uuid(),
                    make = make.trim().ifBlank { "Vehicle" },
                    model = model.trim(),
                    year = year,
                    purchasePrice = purchasePrice,
                    currentValue = currentValue,
                    colorArgb = colorArgb,
                )
            )
        }
    }

    fun updateVehicleValue(id: String, currentValue: Double?) {
        update { d ->
            d.copy(vehicles = d.vehicles.map {
                if (it.id == id) it.copy(currentValue = currentValue) else it
            })
        }
    }

    fun deleteVehicle(id: String) {
        update { d -> d.copy(vehicles = d.vehicles.filterNot { it.id == id }) }
    }

    // --- Buying Power --------------------------------------------------------

    /**
     * Compute car buying power from real data:
     *  - 15% of monthly income for 48-month loan + available savings - remaining debt.
     */
    fun carBuyingPower(): BuyingPower {
        val monthlyIncome = incomeThisMonth()
        val savings = totalAvailable()
        val debts = _data.value.debts.sumOf { it.total - it.paid }
        val maxLoan = monthlyIncome * 0.15 * 48.0
        val maxPrice = (maxLoan + savings - debts).coerceAtLeast(0.0)
        val monthly = if (maxPrice > 0) (maxPrice * 0.02) else 0.0 // ~2% monthly on 48mo loan
        return BuyingPower(
            id = "car",
            type = BuyingPowerType.CAR,
            maxPurchase = maxPrice,
            monthlyPayment = monthly,
            colorArgb = _data.value.buyingPowers.firstOrNull { it.type == BuyingPowerType.CAR }?.colorArgb
                ?: 0xFFF5A623,
        )
    }

    /**
     * Compute home buying power using the 28/36 rule:
     *  - 28% of gross monthly income for mortgage payment
     *  - 30-year fixed at ~6.5%
     *  - Down payment from savings (assume 20%)
     */
    fun homeBuyingPower(): BuyingPower {
        val monthlyIncome = incomeThisMonth()
        val savings = totalAvailable()
        val maxMonthlyPayment = monthlyIncome * 0.28
        // Present value of 30-year annuity at 6.5% (360 payments)
        val monthlyRate = 0.065 / 12.0
        val months = 360
        val pvFactor = (1.0 - Math.pow(1.0 + monthlyRate, -months.toDouble())) / monthlyRate
        val maxLoan = maxMonthlyPayment * pvFactor
        val downPayment = savings.coerceAtMost(maxLoan * 0.25) // assume up to 25% down
        val maxPrice = (maxLoan + downPayment).coerceAtLeast(0.0)
        return BuyingPower(
            id = "home",
            type = BuyingPowerType.HOME,
            maxPurchase = maxPrice,
            monthlyPayment = maxMonthlyPayment,
            colorArgb = _data.value.buyingPowers.firstOrNull { it.type == BuyingPowerType.HOME }?.colorArgb
                ?: 0xFF4A90D9,
        )
    }

    /** Add a manual buying power override. */
    fun addBuyingPower(type: BuyingPowerType, maxPurchase: Double, monthlyPayment: Double, colorArgb: Long) {
        update { d ->
            val filtered = d.buyingPowers.filterNot { it.type == type }
            d.copy(
                buyingPowers = filtered + BuyingPower(
                    id = BudgetRepository.uuid(),
                    type = type,
                    maxPurchase = maxPurchase,
                    monthlyPayment = monthlyPayment,
                    colorArgb = colorArgb,
                )
            )
        }
    }

    fun deleteBuyingPower(id: String) {
        update { d -> d.copy(buyingPowers = d.buyingPowers.filterNot { it.id == id }) }
    }

    // --- Household & onboarding ----------------------------------------------

    fun completeOnboarding(profile: HouseholdProfile) {
        update { d ->
            val hh = d.household.copy(
                adultCount = profile.adultCount,
                childCount = profile.childCount,
                petCount = profile.petCount,
                monthlyIncome = if (profile.monthlyIncome > 0) profile.monthlyIncome else d.household.monthlyIncome,
            )
            // Auto-create child and pet categories if the user has dependents
            val extraCats = mutableListOf<Category>()
            if (hh.childCount > 0) {
                val existing = d.categories.any { it.name.equals("Children", ignoreCase = true) }
                if (!existing) {
                    extraCats.add(
                        Category(
                            id = BudgetRepository.uuid(),
                            name = "Children",
                            iconKey = "child",
                            colorArgb = 0xFFF5C451,
                            monthlyBudget = hh.monthlyIncome * 0.08,
                            suggestedPercentage = 8.0,
                        )
                    )
                }
            }
            if (hh.petCount > 0) {
                val existing = d.categories.any { it.name.equals("Pets", ignoreCase = true) }
                if (!existing) {
                    extraCats.add(
                        Category(
                            id = BudgetRepository.uuid(),
                            name = "Pets",
                            iconKey = "pets",
                            colorArgb = 0xFF5EC2FF,
                            monthlyBudget = hh.petCount * hh.monthlyIncome * 0.025,
                            suggestedPercentage = hh.petCount * 2.5,
                        )
                    )
                }
            }
            d.copy(household = hh, categories = d.categories + extraCats, onboarded = true)
        }
    }

    // --- Spending guidelines -------------------------------------------------

    /**
     * Returns a list of guideline rows — one per category that has a suggested
     * percentage set. The suggested percentage is adjusted based on household
     * composition (more people → higher groceries, lower fun; kids → childcare).
     */
    fun spendingGuidelines(data: BudgetData = _data.value): List<GuidelineRow> {
        // Use actual income this month, falling back to the household's stored monthly income
        // so guidelines still show even when no income transactions were logged this month.
        val monthlyIncome = incomeThisMonth(data).let { if (it > 0) it else data.household.monthlyIncome }
        if (monthlyIncome <= 0.0) return emptyList()
        val hh = data.household
        return data.categories
            .filter { it.suggestedPercentage > 0.0 }
            .map { cat ->
                val adjustedPct = householdAdjustedPct(cat, hh)
                val spent = spentInCategory(cat.id, data)
                val actualPct = (spent / monthlyIncome * 100.0).coerceAtMost(200.0)
                GuidelineRow(
                    category = cat,
                    suggestedPct = adjustedPct,
                    actualPct = actualPct,
                    spent = spent,
                    suggestedAmount = monthlyIncome * (adjustedPct / 100.0),
                )
            }
            .sortedByDescending { it.category.suggestedPercentage }
    }

    /** Adjust category suggested % based on household composition. */
    private fun householdAdjustedPct(cat: Category, hh: HouseholdProfile): Double {
        val base = cat.suggestedPercentage
        val name = cat.name.lowercase()
        return when {
            name.contains("groceries") || name.contains("grocer") -> {
                // More people = more food. Base covers 1 adult; +5% per extra adult, +3% per child
                (base + (hh.adultCount - 1).coerceAtLeast(0) * 5.0 + hh.childCount * 3.0).coerceAtMost(35.0)
            }
            name.contains("dining") || name.contains("restaurant") || name.contains("food") -> {
                // Dining out scales less aggressively — more people eat out less per person
                (base + (hh.totalPeople - 1) * 1.5).coerceAtMost(15.0)
            }
            name.contains("fun") || name.contains("entertainment") || name.contains("movie") -> {
                // Fun budget tightens with more dependents
                val reduction = hh.childCount * 1.5 + hh.petCount * 0.5
                (base - reduction).coerceAtLeast(2.0)
            }
            name.contains("shopping") || name.contains("clothing") -> {
                // Shopping scales with household size
                (base + (hh.totalPeople - 1) * 2.0).coerceAtMost(20.0)
            }
            name.contains("child") || name.contains("kid") -> {
                // Children category scales with number of kids
                (base + (hh.childCount - 1).coerceAtLeast(0) * 4.0).coerceAtMost(20.0)
            }
            name.contains("pet") || name.contains("animal") -> {
                // Pets scale with number of pets
                (base + (hh.petCount - 1).coerceAtLeast(0) * 2.0).coerceAtMost(15.0)
            }
            name.contains("bill") || name.contains("utilities") -> {
                // Utilities go up with more people
                (base + (hh.totalPeople - 1) * 2.0).coerceAtMost(40.0)
            }
            name.contains("transport") || name.contains("car") || name.contains("gas") -> {
                // Transport scales modestly
                (base + (hh.adultCount - 1).coerceAtLeast(0) * 2.0).coerceAtMost(18.0)
            }
            else -> base
        }
    }

    /** Total suggested allocation as a percentage (sum of all suggestedPercentage values). */
    fun totalSuggestedPct(data: BudgetData = _data.value): Double = data.categories.sumOf { it.suggestedPercentage }

    // --- Monthly breakdown ---------------------------------------------------

    /**
     * Returns the past 12 months of income vs spending, starting from the
     * current month and going back 11 months. Each entry has a short month
     * label, total income, and total spent.
     */
    fun monthlyBreakdown(): List<MonthBar> {
        val d = _data.value
        val cal = Calendar.getInstance()
        val months = mutableListOf<MonthBar>()
        for (i in 11 downTo 0) {
            val m = Calendar.getInstance().apply {
                add(Calendar.MONTH, -i)
            }
            val label = monthLabel(m)
            val income = d.transactions
                .filter { it.isIncome && Dates.isMonth(it.timestamp, m) }
                .sumOf { it.amount }
            val spent = d.transactions
                .filter { !it.isIncome && Dates.isMonth(it.timestamp, m) }
                .sumOf { it.amount }
            months.add(MonthBar(label = label, income = income, spent = spent))
        }
        return months
    }

    private fun monthLabel(cal: Calendar): String {
        val names = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return names[cal.get(Calendar.MONTH)]
    }

    // --- Smart recommendations -----------------------------------------------

    /**
     * Generates personalized recommendations by analyzing the user's spending
     * patterns, household composition, and financial situation. Returns a list
     * ordered by priority (most actionable first).
     */
    fun recommendations(data: BudgetData = _data.value): List<Recommendation> {
        val income = incomeThisMonth(data)
        val spent = spentThisMonth(data)
        val hh = data.household
        val result = mutableListOf<Recommendation>()
        var recIndex = 0

        fun nextId() = "rec_${recIndex++}"

        // 1. Missing bills based on household composition
        val catNames = data.categories.map { it.name.lowercase() }

        // Pet insurance — when user has pets but no pet insurance category
        if (hh.petCount > 0 && catNames.none { it.contains("pet") && (it.contains("insurance") || it.contains("vet")) }) {
            val estimate = hh.petCount * 35.0
            result.add(
                Recommendation(
                    id = nextId(),
                    type = RecommendationType.MISSING_BILL,
                    title = "Add pet insurance",
                    body = "You have ${hh.petCount} ${if (hh.petCount == 1) "pet" else "pets"} in your household but no pet insurance or vet care category. Accidents and illnesses can cost \$500–\$5,000. Pet insurance typically runs \$30–\$50/month per pet. Consider budgeting ~\$${rawAmount(estimate)}/month for coverage and routine care.",
                    actionLabel = "Add pet care budget",
                    iconKey = "pets",
                    colorArgb = 0xFF5EC2FF,
                )
            )
        }

        // Gym / fitness — when adults present but no health/gym category
        if (hh.adultCount > 0 && catNames.none { it.contains("gym") || it.contains("fitness") || it.contains("health") }) {
            result.add(
                Recommendation(
                    id = nextId(),
                    type = RecommendationType.MISSING_BILL,
                    title = "Consider a gym membership",
                    body = "Regular exercise reduces long-term healthcare costs and improves quality of life. Most gym memberships run \$25–\$60/month, and many employers offer wellness reimbursements. Even a budget gym at \$30/month is a high-return investment in your health.",
                    actionLabel = "Add health & fitness budget",
                    iconKey = "gym",
                    colorArgb = 0xFF34E0A1,
                )
            )
        }

        // Life insurance — when adults with dependents (children) present
        if (hh.adultCount > 0 && hh.childCount > 0 && catNames.none { it.contains("life") && it.contains("insurance") }) {
            result.add(
                Recommendation(
                    id = nextId(),
                    type = RecommendationType.MISSING_BILL,
                    title = "Protect your family with life insurance",
                    body = "With ${hh.childCount} ${if (hh.childCount == 1) "child" else "children"} depending on you, term life insurance is essential. A healthy 30-year-old can get \$500K coverage for ~\$25–\$35/month. It ensures your family is protected if something happens to you.",
                    actionLabel = "Add insurance budget",
                    iconKey = "insurance",
                    colorArgb = 0xFFB39CFF,
                )
            )
        }

        // Renters / home insurance — general recommendation if missing
        if (catNames.none { it.contains("insurance") && (it.contains("renter") || it.contains("home") || it.contains("property")) }) {
            val hasHomeCategory = catNames.any { it.contains("rent") || it.contains("mortgage") || it.contains("housing") }
            if (hasHomeCategory || hh.adultCount > 0) {
                result.add(
                    Recommendation(
                        id = nextId(),
                        type = RecommendationType.MISSING_BILL,
                        title = "Get renters or home insurance",
                        body = "Renters insurance costs as little as \$15/month and protects your belongings against theft, fire, and water damage. Homeowners insurance is essential for property owners. Either way, it's one of the most affordable forms of financial protection you can buy.",
                        actionLabel = "Add insurance budget",
                        iconKey = "insurance",
                        colorArgb = 0xFFF5C451,
                    )
                )
            }
        }

        // 2. Overspending alerts based on guidelines
        if (income > 0) {
            val guidelines = spendingGuidelines(data)
            val overBudget = guidelines.filter { it.actualPct > it.suggestedPct && it.spent > 0 }
                .sortedByDescending { it.actualPct - it.suggestedPct }

            for (row in overBudget.take(2)) {
                val excess = row.spent - row.suggestedAmount
                if (excess > 20) {
                    val tip = when {
                        row.category.name.lowercase().contains("dining") || row.category.name.lowercase().contains("food") ->
                            "Try meal prepping on Sundays and limiting restaurant meals to weekends. Even cutting 2 takeout orders per week could save ~\$${rawAmount(excess * 0.6)}/month."
                        row.category.name.lowercase().contains("shopping") ->
                            "Use the 30-day rule for non-essential purchases over \$100. Unsubscribe from retail marketing emails to reduce temptation."
                        row.category.name.lowercase().contains("fun") || row.category.name.lowercase().contains("entertainment") ->
                            "Look for free local events, library resources, and streaming rotation (keep only 1–2 services at a time)."
                        row.category.name.lowercase().contains("groceries") || row.category.name.lowercase().contains("grocer") ->
                            "Shop with a list, buy store brands, and avoid shopping hungry. Bulk-buy staples when on sale."
                        else ->
                            "Review your subscriptions and recurring charges in this category. Small monthly charges add up quickly."
                    }
                    result.add(
                        Recommendation(
                            id = nextId(),
                            type = RecommendationType.OVERSPENDING,
                            title = "Overspending on ${row.category.name}",
                            body = "You're spending ${row.actualPct.toInt()}% of income on ${row.category.name} (suggested: ${row.suggestedPct.toInt()}%). That's ~\$${rawAmount(excess)} over the guideline. $tip",
                            actionLabel = "Set a budget cap",
                            iconKey = row.category.iconKey,
                            colorArgb = row.category.colorArgb,
                        )
                    )
                }
            }
        }

        // 3. Saving opportunity — if savings rate is low
        if (income > 0) {
            val savingsRate = ((income - spent) / income * 100).coerceIn(0.0, 100.0)
            if (savingsRate < 15 && spent > 0) {
                val gap = income * 0.20 - (income - spent)
                if (gap > 50) {
                    result.add(
                        Recommendation(
                            id = nextId(),
                            type = RecommendationType.SAVING_OPPORTUNITY,
                            title = "Boost your savings rate",
                            body = "You're saving about ${savingsRate.toInt()}% of your income — below the recommended 20%. If you can free up ~\$${rawAmount(gap)}/month, you'd hit a 20% savings rate. Try auditing one spending category at a time to find the extra room.",
                            actionLabel = "Review spending",
                            iconKey = "savings",
                            colorArgb = 0xFFF5C451,
                        )
                    )
                }
            } else if (savingsRate >= 20 && savingsRate < 40) {
                result.add(
                    Recommendation(
                        id = nextId(),
                        type = RecommendationType.GENERAL_TIP,
                        title = "Great savings discipline",
                        body = "You're saving ${savingsRate.toInt()}% of your income — solid work! Consider putting any surplus beyond your emergency fund into a Roth IRA or index fund to let compound growth work for you.",
                        actionLabel = null,
                        iconKey = "star",
                        colorArgb = 0xFF34E0A1,
                    )
                )
            }
        }

        // 4. Debt reduction — prioritize high-interest debt
        val highInterestDebts = data.debts.filter { it.apr > 15 && it.total > it.paid }
        if (highInterestDebts.isNotEmpty()) {
            val worst = highInterestDebts.maxByOrNull { it.apr }
            if (worst != null) {
                val remaining = worst.total - worst.paid
                result.add(
                    Recommendation(
                        id = nextId(),
                        type = RecommendationType.DEBT_REDUCTION,
                        title = "Tackle your ${worst.apr.toInt()}% APR debt",
                        body = "Your \"${worst.name}\" has \$${rawAmount(remaining)} remaining at ${worst.apr}% APR. At this rate, interest alone costs ~\$${rawAmount(remaining * worst.apr / 100 / 12)}/month. Pay this off before investing beyond any 401k match — it's a guaranteed ${worst.apr.toInt()}% return.",
                    actionLabel = "Make a payment",
                    iconKey = "card",
                    colorArgb = 0xFFFF6B6B,
                )
            )
            }
        }

        // 5. Emergency fund check
        if (income > 0) {
            val emergencyFund = data.accounts
                .filter { it.type == AccountType.SAVINGS }
                .sumOf { it.balance }
            val monthlyExpenses = spent.coerceAtLeast(income * 0.5)
            val monthsCovered = if (monthlyExpenses > 0) emergencyFund / monthlyExpenses else 0.0

            if (monthsCovered < 3.0 && emergencyFund < income * 6) {
                val target = (3.0 * monthlyExpenses - emergencyFund).coerceAtLeast(0.0)
                result.add(
                    Recommendation(
                        id = nextId(),
                        type = RecommendationType.EMERGENCY_FUND,
                        title = "Build your emergency fund",
                        body = "Your savings cover about ${String.format("%.1f", monthsCovered)} month${if (monthsCovered < 2) "" else "s"} of expenses. Aim for 3–6 months. You're ~\$${rawAmount(target)} away from a 3-month cushion. Automate a monthly transfer to savings to make it effortless.",
                        actionLabel = "Set savings goal",
                        iconKey = "savings",
                        colorArgb = 0xFFF5C451,
                    )
                )
            }
        }

        // 6. Subscriptions audit recommendation
        val hasSubscriptions = data.categories.any { it.name.lowercase().contains("subscription") }
        if (!hasSubscriptions && data.categories.size >= 4) {
            result.add(
                Recommendation(
                    id = nextId(),
                    type = RecommendationType.GENERAL_TIP,
                    title = "Audit your subscriptions",
                    body = "The average person spends \$219/month on subscriptions they've forgotten about. Check your bank statements for streaming services, apps, and memberships you no longer use. A 10-minute audit could save \$50+/month.",
                    actionLabel = null,
                    iconKey = "subscription",
                    colorArgb = 0xFFB39CFF,
                )
            )
        }

        return result
    }

    /** Format a raw dollar amount without currency symbol for inline use. */
    private fun rawAmount(amount: Double): String {
        val n = Math.abs(amount)
        return when {
            n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000)
            n >= 1_000 -> String.format("%.0f", n)
            else -> String.format("%.0f", n)
        }
    }

    // --- Financial Health Dashboard -------------------------------------------

    /**
     * Computes a comprehensive financial health snapshot covering emergency
     * fund, credit utilization, savings rate, retirement readiness, and
     * debt-to-income ratio. Each metric is scored and rolled into a 0–100
     * composite score.
     */
    fun financialHealth(data: BudgetData = _data.value): FinancialHealth {
        val income = incomeThisMonth(data)
        val spent = spentThisMonth(data)
        val annualIncome = income * 12.0
        val monthlyExpenses = spent.coerceAtLeast(income * 0.3)

        val metrics = mutableListOf<HealthMetric>()
        var totalWeight = 0f
        var weightedSum = 0f

        // 1. Emergency Fund — savings accounts balance vs monthly expenses
        val emergencyFund = data.accounts
            .filter { it.type == AccountType.SAVINGS }
            .sumOf { it.balance }
        val monthsCovered = if (monthlyExpenses > 0) emergencyFund / monthlyExpenses else 0.0
        val emergencyStatus: HealthStatus
        val emergencyScore: Float
        val emergencyValue: String
        when {
            monthsCovered >= 6.0 -> {
                emergencyStatus = HealthStatus.GOOD
                emergencyScore = 1.0f
                emergencyValue = "${String.format("%.1f", monthsCovered)} months"
            }
            monthsCovered >= 3.0 -> {
                emergencyStatus = HealthStatus.ON_TRACK
                emergencyScore = 0.7f
                emergencyValue = "${String.format("%.1f", monthsCovered)} months"
            }
            monthsCovered >= 1.0 -> {
                emergencyStatus = HealthStatus.NEEDS_ATTENTION
                emergencyScore = 0.35f
                emergencyValue = "${String.format("%.1f", monthsCovered)} months"
            }
            else -> {
                emergencyStatus = HealthStatus.CRITICAL
                emergencyScore = 0.1f
                emergencyValue = if (emergencyFund > 0) "< 1 month" else "No savings"
            }
        }
        val emergencySubtitle = "Aim for 3–6 months of expenses"
        metrics.add(
            HealthMetric(
                key = "emergency",
                label = "Emergency fund",
                value = emergencyValue,
                subtitle = emergencySubtitle,
                status = emergencyStatus,
                iconKey = "savings",
                scoreFraction = emergencyScore,
            )
        )
        weightedSum += emergencyScore * 2.5f
        totalWeight += 2.5f

        // 2. Credit Utilization — total credit debt / total credit limit
        val creditAccts = data.accounts.filter { it.type == AccountType.CREDIT }
        val totalCreditDebt = creditAccts.sumOf { it.balance }
        val totalCreditLimit = creditAccts.sumOf { it.creditLimit }
        val creditUtilization = if (totalCreditLimit > 0) totalCreditDebt / totalCreditLimit else 0.0
        val creditStatus: HealthStatus
        val creditScore: Float
        val creditValue: String
        when {
            totalCreditLimit == 0.0 -> {
                creditStatus = HealthStatus.ON_TRACK
                creditScore = 0.8f
                creditValue = "No credit cards"
            }
            creditUtilization < 0.10 -> {
                creditStatus = HealthStatus.GOOD
                creditScore = 1.0f
                creditValue = "${(creditUtilization * 100).toInt()}% used"
            }
            creditUtilization < 0.30 -> {
                creditStatus = HealthStatus.ON_TRACK
                creditScore = 0.75f
                creditValue = "${(creditUtilization * 100).toInt()}% used"
            }
            creditUtilization < 0.50 -> {
                creditStatus = HealthStatus.NEEDS_ATTENTION
                creditScore = 0.4f
                creditValue = "${(creditUtilization * 100).toInt()}% used"
            }
            else -> {
                creditStatus = HealthStatus.CRITICAL
                creditScore = 0.1f
                creditValue = "${(creditUtilization * 100).toInt()}% used"
            }
        }
        val creditSubtitle = if (totalCreditLimit > 0)
            "Keep below 30% of \$${rawAmount(totalCreditLimit)} limit"
        else "Using credit responsibly builds your score"
        metrics.add(
            HealthMetric(
                key = "credit",
                label = "Credit utilization",
                value = creditValue,
                subtitle = creditSubtitle,
                status = creditStatus,
                iconKey = "card",
                scoreFraction = creditScore,
            )
        )
        weightedSum += creditScore * 2.0f
        totalWeight += 2.0f

        // 3. Savings Rate — (income - spent) / income
        val savingsRate = if (income > 0) ((income - spent) / income).coerceIn(0.0, 1.0) else 0.0
        val savingsStatus: HealthStatus
        val savingsScore: Float
        val savingsValue: String
        when {
            savingsRate >= 0.30 -> {
                savingsStatus = HealthStatus.GOOD
                savingsScore = 1.0f
                savingsValue = "${(savingsRate * 100).toInt()}% rate"
            }
            savingsRate >= 0.20 -> {
                savingsStatus = HealthStatus.GOOD
                savingsScore = 0.9f
                savingsValue = "${(savingsRate * 100).toInt()}% rate"
            }
            savingsRate >= 0.10 -> {
                savingsStatus = HealthStatus.ON_TRACK
                savingsScore = 0.65f
                savingsValue = "${(savingsRate * 100).toInt()}% rate"
            }
            savingsRate >= 0.05 -> {
                savingsStatus = HealthStatus.NEEDS_ATTENTION
                savingsScore = 0.35f
                savingsValue = "${(savingsRate * 100).toInt()}% rate"
            }
            else -> {
                savingsStatus = if (income > 0) HealthStatus.CRITICAL else HealthStatus.NEEDS_ATTENTION
                savingsScore = if (income > 0) 0.1f else 0.4f
                savingsValue = if (income > 0) "${(savingsRate * 100).toInt()}% rate" else "No income yet"
            }
        }
        metrics.add(
            HealthMetric(
                key = "savings_rate",
                label = "Savings rate",
                value = savingsValue,
                subtitle = "Experts recommend 20%+",
                status = savingsStatus,
                iconKey = "star",
                scoreFraction = savingsScore,
            )
        )
        weightedSum += savingsScore * 2.0f
        totalWeight += 2.0f

        // 4. Retirement Readiness
        val retirementAccts = data.accounts.filter { it.type == AccountType.RETIREMENT }
        val retirementBalance = retirementAccts.sumOf { it.balance }
        val retirementRatio = if (annualIncome > 0) retirementBalance / annualIncome else 0.0
        val retirementStatus: HealthStatus
        val retirementScore: Float
        val retirementValue: String
        when {
            retirementBalance == 0.0 && income > 0 -> {
                retirementStatus = HealthStatus.NEEDS_ATTENTION
                retirementScore = 0.2f
                retirementValue = "No retirement account"
            }
            retirementBalance == 0.0 -> {
                retirementStatus = HealthStatus.NEEDS_ATTENTION
                retirementScore = 0.3f
                retirementValue = "Not started yet"
            }
            retirementRatio >= 3.0 -> {
                retirementStatus = HealthStatus.GOOD
                retirementScore = 1.0f
                retirementValue = "${Math.round(retirementRatio)}x annual income"
            }
            retirementRatio >= 1.0 -> {
                retirementStatus = HealthStatus.ON_TRACK
                retirementScore = 0.7f
                retirementValue = "${Math.round(retirementRatio)}x annual income"
            }
            retirementRatio >= 0.5 -> {
                retirementStatus = HealthStatus.ON_TRACK
                retirementScore = 0.55f
                retirementValue = "${Math.round(retirementRatio * 10) / 10.0}x annual income"
            }
            else -> {
                retirementStatus = HealthStatus.NEEDS_ATTENTION
                retirementScore = 0.35f
                retirementValue = "\$${rawAmount(retirementBalance)}"
            }
        }
        val retirementSubtitle = when {
            retirementBalance == 0.0 && income > 0 -> "Start a 401k or IRA — even \$50/month helps"
            retirementBalance == 0.0 -> "Open a retirement account when income starts"
            retirementRatio < 1.0 -> "Aim for 1x annual income by age 30"
            retirementRatio < 3.0 -> "Aim for 3x annual income by age 40"
            else -> "You're building a solid retirement"
        }
        metrics.add(
            HealthMetric(
                key = "retirement",
                label = "Retirement",
                value = retirementValue,
                subtitle = retirementSubtitle,
                status = retirementStatus,
                iconKey = "retirement",
                scoreFraction = retirementScore,
            )
        )
        weightedSum += retirementScore * 2.0f
        totalWeight += 2.0f

        // 5. Debt-to-Income Ratio
        val totalDebt = data.debts.sumOf { it.total - it.paid } + totalCreditDebt
        val dti = if (annualIncome > 0) totalDebt / annualIncome else 0.0
        val dtiStatus: HealthStatus
        val dtiScore: Float
        val dtiValue: String
        when {
            totalDebt == 0.0 -> {
                dtiStatus = HealthStatus.GOOD
                dtiScore = 1.0f
                dtiValue = "Debt free"
            }
            dti < 0.10 -> {
                dtiStatus = HealthStatus.GOOD
                dtiScore = 0.95f
                dtiValue = "${(dti * 100).toInt()}% DTI"
            }
            dti < 0.20 -> {
                dtiStatus = HealthStatus.ON_TRACK
                dtiScore = 0.7f
                dtiValue = "${(dti * 100).toInt()}% DTI"
            }
            dti < 0.36 -> {
                dtiStatus = HealthStatus.NEEDS_ATTENTION
                dtiScore = 0.4f
                dtiValue = "${(dti * 100).toInt()}% DTI"
            }
            else -> {
                dtiStatus = HealthStatus.CRITICAL
                dtiScore = 0.1f
                dtiValue = "${(dti * 100).toInt()}% DTI"
            }
        }
        val dtiSubtitle = if (totalDebt > 0)
            "Keep debt below 36% of income"
        else "No debt — excellent position"
        metrics.add(
            HealthMetric(
                key = "dti",
                label = "Debt-to-income",
                value = dtiValue,
                subtitle = dtiSubtitle,
                status = dtiStatus,
                iconKey = "wallet",
                scoreFraction = dtiScore,
            )
        )
        weightedSum += dtiScore * 1.5f
        totalWeight += 1.5f

        // 6. Bill payment proxy — check if key bill categories have consistent spending
        val billCategories = data.categories.filter {
            val n = it.name.lowercase()
            n.contains("bill") || n.contains("rent") || n.contains("mortgage") ||
            n.contains("utilities") || n.contains("phone") || n.contains("insurance")
        }
        val billStatus: HealthStatus
        val billScore: Float
        val billValue: String
        if (billCategories.isEmpty()) {
            billStatus = HealthStatus.NEEDS_ATTENTION
            billScore = 0.3f
            billValue = "No bill categories set"
        } else {
            // Check if recent transactions exist in those categories
            val recentBillTxns = data.transactions.filter { tx ->
                !tx.isIncome && tx.categoryId != null &&
                billCategories.any { it.id == tx.categoryId } &&
                Dates.isThisMonth(tx.timestamp)
            }
            val hasActiveBills = recentBillTxns.isNotEmpty()
            billStatus = if (hasActiveBills) HealthStatus.ON_TRACK else HealthStatus.NEEDS_ATTENTION
            billScore = if (hasActiveBills) 0.8f else 0.4f
            billValue = if (hasActiveBills) "Bills tracked this month" else "No bills recorded recently"
        }
        metrics.add(
            HealthMetric(
                key = "bills",
                label = "Bill payments",
                value = billValue,
                subtitle = "Consistent bill tracking builds payment history",
                status = billStatus,
                iconKey = "pay",
                scoreFraction = billScore,
            )
        )
        weightedSum += billScore * 1.0f
        totalWeight += 1.0f

        // Composite score 0–100
        val compositeScore = if (totalWeight > 0) (weightedSum / totalWeight * 100).toInt().coerceIn(0, 100) else 0
        val overallStatus = when {
            compositeScore >= 80 -> HealthStatus.GOOD
            compositeScore >= 55 -> HealthStatus.ON_TRACK
            compositeScore >= 30 -> HealthStatus.NEEDS_ATTENTION
            else -> HealthStatus.CRITICAL
        }

        return FinancialHealth(
            overallScore = compositeScore,
            status = overallStatus,
            metrics = metrics,
        )
    }
}

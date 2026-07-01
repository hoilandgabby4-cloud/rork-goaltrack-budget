package com.rork.budgetflow.data

import kotlinx.serialization.Serializable

/** The kind of money source a payment account represents. */
@Serializable
enum class AccountType {
    CHECKING,
    SAVINGS,
    CREDIT,
    CASH,
    INCOME,
    RETIREMENT;

    val label: String
        get() = when (this) {
            CHECKING -> "Checking"
            SAVINGS -> "Savings"
            CREDIT -> "Credit Card"
            CASH -> "Cash"
            INCOME -> "Income"
            RETIREMENT -> "401k / IRA"
        }
}

/**
 * A payment source. For CREDIT accounts [balance] is the amount currently owed
 * (a debt that grows when spent against). For every other type [balance] is
 * available money that shrinks when spent.
 */
@Serializable
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val colorArgb: Long,
    val creditLimit: Double = 0.0,
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long,
    val monthlyBudget: Double = 0.0,
    /** Suggested percentage of monthly income to allocate (0 = no suggestion). */
    val suggestedPercentage: Double = 0.0,
)

@Serializable
data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val accountId: String,
    val categoryId: String?,
    val isIncome: Boolean,
    val timestamp: Long,
)

@Serializable
data class SavingsGoal(
    val id: String,
    val name: String,
    val target: Double,
    val saved: Double,
    val colorArgb: Long,
    val iconKey: String,
    val deadline: Long? = null,
)

@Serializable
data class Debt(
    val id: String,
    val name: String,
    val total: Double,
    val paid: Double,
    val apr: Double,
    val colorArgb: Long,
)

/** A vehicle you own, with auto-depreciation or manual current value. */
@Serializable
data class Vehicle(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val purchasePrice: Double,
    val currentValue: Double? = null,
    val colorArgb: Long,
)

/** Pre-computed buying power estimate for a car or home. */
@Serializable
data class BuyingPower(
    val id: String,
    val type: BuyingPowerType,
    /** How much you could afford based on income, savings, and debts. */
    val maxPurchase: Double,
    /** Suggested monthly payment (for home: mortgage; for car: loan). */
    val monthlyPayment: Double,
    val colorArgb: Long,
)

@Serializable
enum class BuyingPowerType {
    CAR,
    HOME;

    val label: String
        get() = when (this) {
            CAR -> "Car"
            HOME -> "Home"
        }
}

/** Household composition used to personalise spending guidelines. */
@Serializable
data class HouseholdProfile(
    val adultCount: Int = 1,
    val childCount: Int = 0,
    val petCount: Int = 0,
    val monthlyIncome: Double = 0.0,
) {
    val totalPeople: Int get() = adultCount + childCount
    val hasDependents: Boolean get() = childCount > 0 || petCount > 0
}

/** The full persisted application state. */
@Serializable
data class BudgetData(
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val goals: List<SavingsGoal> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val buyingPowers: List<BuyingPower> = emptyList(),
    val household: HouseholdProfile = HouseholdProfile(),
    val onboarded: Boolean = false,
)

/** One month of income-vs-spending for the bar chart. */
data class MonthBar(
    val label: String,
    val income: Double,
    val spent: Double,
)

/** A spending guideline row comparing suggested vs actual allocation. */
data class GuidelineRow(
    val category: Category,
    val suggestedPct: Double,
    val actualPct: Double,
    val spent: Double,
    val suggestedAmount: Double,
)

/** Types of personalized recommendations the app can generate. */
enum class RecommendationType {
    OVERSPENDING,
    MISSING_BILL,
    SAVING_OPPORTUNITY,
    DEBT_REDUCTION,
    EMERGENCY_FUND,
    GENERAL_TIP;
}

/** A personalized, actionable recommendation generated from spending patterns and household data. */
data class Recommendation(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val body: String,
    val actionLabel: String?,
    val iconKey: String,
    val colorArgb: Long,
)

/** Severity level for financial health metrics. */
enum class HealthStatus {
    GOOD,
    ON_TRACK,
    NEEDS_ATTENTION,
    CRITICAL;

    val label: String
        get() = when (this) {
            GOOD -> "Looking great"
            ON_TRACK -> "On the right track"
            NEEDS_ATTENTION -> "Needs attention"
            CRITICAL -> "Needs immediate action"
        }
}

/**
 * A single health metric with a value, label, status, and optional sub-detail.
 * Used by the financial health dashboard to show scannable status cards.
 */
data class HealthMetric(
    val key: String,
    val label: String,
    val value: String,
    val subtitle: String,
    val status: HealthStatus,
    val iconKey: String,
    /** Normalized contribution to overall score (0.0–1.0). */
    val scoreFraction: Float,
)

/**
 * Composite financial health snapshot computed from the user's actual data.
 * Each metric is derived from accounts, spending, debts, and household profile.
 */
data class FinancialHealth(
    val overallScore: Int,
    val status: HealthStatus,
    val metrics: List<HealthMetric>,
)

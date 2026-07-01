package com.rork.budgetflow.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.rork.budgetflow.ui.theme.AccentPalette
import kotlinx.serialization.json.Json
import java.util.UUID

/** Persists [BudgetData] to SharedPreferences as JSON. */
class BudgetRepository(context: Context) {

    private val prefs = context.getSharedPreferences("budgetflow", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): BudgetData {
        val raw = prefs.getString(KEY_DATA, null) ?: return seed()
        return runCatching { json.decodeFromString(BudgetData.serializer(), raw) }.getOrElse { seed() }
    }

    fun save(data: BudgetData) {
        prefs.edit().putString(KEY_DATA, json.encodeToString(BudgetData.serializer(), data)).apply()
    }

    private fun seed(): BudgetData {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        val checking = Account(uuid(), "Everyday Checking", AccountType.CHECKING, 2840.0, AccentPalette[1].toArgb().toLong())
        val savings = Account(uuid(), "Emergency Fund", AccountType.SAVINGS, 6200.0, AccentPalette[0].toArgb().toLong())
        val visa = Account(uuid(), "Visa Platinum", AccountType.CREDIT, 640.0, AccentPalette[3].toArgb().toLong(), creditLimit = 5000.0)
        val cash = Account(uuid(), "Cash Wallet", AccountType.CASH, 120.0, AccentPalette[2].toArgb().toLong())
        val retirement = Account(uuid(), "Vanguard 401k", AccountType.RETIREMENT, 42500.0, AccentPalette[7].toArgb().toLong())

        val groceries = Category(uuid(), "Groceries", "cart", AccentPalette[0].toArgb().toLong(), 500.0, 12.0)
        val dining = Category(uuid(), "Dining", "food", AccentPalette[7].toArgb().toLong(), 250.0, 8.0)
        val transport = Category(uuid(), "Transport", "car", AccentPalette[1].toArgb().toLong(), 180.0, 10.0)
        val bills = Category(uuid(), "Bills", "bolt", AccentPalette[4].toArgb().toLong(), 600.0, 30.0)
        val fun_ = Category(uuid(), "Fun", "movie", AccentPalette[5].toArgb().toLong(), 200.0, 5.0)
        val shopping = Category(uuid(), "Shopping", "shopping", AccentPalette[6].toArgb().toLong(), 300.0, 10.0)

        val tx = listOf(
            Transaction(uuid(), "Whole Foods", 64.20, checking.id, groceries.id, false, now - day / 4),
            Transaction(uuid(), "Blue Bottle Coffee", 5.75, cash.id, dining.id, false, now - day / 2),
            Transaction(uuid(), "Uber", 18.40, visa.id, transport.id, false, now - day),
            Transaction(uuid(), "Electric Bill", 92.10, checking.id, bills.id, false, now - day * 2),
            Transaction(uuid(), "Cinema Night", 32.00, visa.id, fun_.id, false, now - day * 2),
            Transaction(uuid(), "Paycheck", 3200.00, checking.id, null, true, now - day * 3),
            Transaction(uuid(), "Nike Store", 119.99, visa.id, shopping.id, false, now - day * 4),
            Transaction(uuid(), "Trader Joe's", 47.85, checking.id, groceries.id, false, now - day * 5),
        )

        val goals = listOf(
            SavingsGoal(uuid(), "Japan Trip", 4000.0, 1450.0, AccentPalette[3].toArgb().toLong(), "flight", now + day * 180),
            SavingsGoal(uuid(), "New MacBook", 2500.0, 900.0, AccentPalette[1].toArgb().toLong(), "star"),
            SavingsGoal(uuid(), "Rainy Day", 10000.0, 6200.0, AccentPalette[0].toArgb().toLong(), "savings"),
        )

        val debts = listOf(
            Debt(uuid(), "Student Loan", 18000.0, 7400.0, 4.5, AccentPalette[4].toArgb().toLong()),
            Debt(uuid(), "Car Loan", 12000.0, 8600.0, 6.2, AccentPalette[1].toArgb().toLong()),
            Debt(uuid(), "Visa Balance", 640.0, 0.0, 19.9, AccentPalette[3].toArgb().toLong()),
        )

        val vehicles = listOf(
            Vehicle(uuid(), "Honda", "Civic", 2020, 22000.0, null, AccentPalette[6].toArgb().toLong()),
            Vehicle(uuid(), "Toyota", "RAV4", 2023, 32000.0, 28500.0, AccentPalette[1].toArgb().toLong()),
        )

        val buyingPowers = listOf(
            BuyingPower(uuid(), BuyingPowerType.CAR, 22000.0, 390.0, AccentPalette[3].toArgb().toLong()),
            BuyingPower(uuid(), BuyingPowerType.HOME, 280000.0, 1680.0, AccentPalette[0].toArgb().toLong()),
        )

        return BudgetData(
            accounts = listOf(checking, savings, visa, cash, retirement),
            categories = listOf(groceries, dining, transport, bills, fun_, shopping),
            transactions = tx,
            goals = goals,
            debts = debts,
            vehicles = vehicles,
            buyingPowers = buyingPowers,
            onboarded = false,
        )
    }

    companion object {
        private const val KEY_DATA = "budget_data_v1"
        fun uuid(): String = UUID.randomUUID().toString()
    }
}

fun Color.toLongArgb(): Long = this.toArgb().toLong()
fun Long.toColor(): Color = Color(this.toInt())

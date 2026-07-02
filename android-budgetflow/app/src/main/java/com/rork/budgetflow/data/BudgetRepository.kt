package com.rork.budgetflow.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.json.Json
import java.util.UUID

/** Persists [BudgetData] to SharedPreferences as JSON. */
class BudgetRepository(context: Context) {

    private val prefs = context.getSharedPreferences("budgetflow", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): BudgetData {
        val raw = prefs.getString(KEY_DATA, null) ?: return emptyData()
        return runCatching { json.decodeFromString(BudgetData.serializer(), raw) }.getOrElse { emptyData() }
    }

    fun save(data: BudgetData) {
        prefs.edit().putString(KEY_DATA, json.encodeToString(BudgetData.serializer(), data)).apply()
    }

    /** A clean, empty state for first-time users — no demo accounts, transactions, or goals. */
    private fun emptyData(): BudgetData = BudgetData(
        accounts = emptyList(),
        categories = emptyList(),
        transactions = emptyList(),
        goals = emptyList(),
        debts = emptyList(),
        vehicles = emptyList(),
        buyingPowers = emptyList(),
        calendarEvents = emptyList(),
        household = HouseholdProfile(),
        onboarded = false,
    )

    companion object {
        private const val KEY_DATA = "budget_data_v2"
        fun uuid(): String = UUID.randomUUID().toString()
    }
}

fun Color.toLongArgb(): Long = this.toArgb().toLong()
fun Long.toColor(): Color = Color(this.toInt())

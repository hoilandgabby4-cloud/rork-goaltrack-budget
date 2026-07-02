package com.rork.budgetflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Remove
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rork.budgetflow.data.Account
import com.rork.budgetflow.data.AccountType
import com.rork.budgetflow.data.BuyingPowerType
import com.rork.budgetflow.data.IconCatalog
import com.rork.budgetflow.data.Money
import com.rork.budgetflow.data.toColor
import com.rork.budgetflow.data.toLongArgb
import java.util.Calendar
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.components.IconChip
import com.rork.budgetflow.ui.components.LabeledField
import com.rork.budgetflow.ui.components.PrimaryButton
import com.rork.budgetflow.ui.components.SelectChip
import com.rork.budgetflow.ui.components.androidClick
import com.rork.budgetflow.ui.theme.AccentPalette
import com.rork.budgetflow.ui.theme.Coral
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.OnMint
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

private fun parseAmount(s: String): Double = s.replace(",", "").toDoubleOrNull() ?: 0.0

@Composable
private fun SheetTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp),
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(vm: BudgetViewModel, onDone: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    var isIncome by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(data.accounts.firstOrNull()?.id) }
    var categoryId by remember { mutableStateOf<String?>(data.categories.firstOrNull()?.id) }
    var addToCalendar by remember { mutableStateOf(false) }
    var billDayOfMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()) }

    val selectedCategory = data.categories.firstOrNull { it.id == categoryId }
    val isBillCategory = selectedCategory?.name?.contains("bill", ignoreCase = true) == true ||
        selectedCategory?.name?.contains("monthly", ignoreCase = true) == true

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle(if (isIncome) "Add income" else "Add expense")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(com.rork.budgetflow.ui.theme.InkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ToggleHalf("Expense", !isIncome, Coral, Modifier.weight(1f)) { isIncome = false }
            ToggleHalf("Income", isIncome, Mint, Modifier.weight(1f)) { isIncome = true }
        }
        Spacer(Modifier.height(20.dp))

        LabeledField(
            label = "Amount",
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Description",
            value = title,
            onValueChange = { title = it },
            placeholder = if (isIncome) "e.g. Paycheck" else "e.g. Groceries",
        )
        Spacer(Modifier.height(16.dp))

        FieldLabel(if (isIncome) "Deposit into" else "Pay with")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            data.accounts.forEach { acc ->
                AccountChip(acc, selected = acc.id == accountId) { accountId = acc.id }
            }
        }

        if (!isIncome) {
            Spacer(Modifier.height(16.dp))
            FieldLabel("Category")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.categories.forEach { cat ->
                    SelectChip(
                        label = cat.name,
                        selected = cat.id == categoryId,
                        accent = cat.colorArgb.toColor(),
                        onClick = { categoryId = cat.id },
                    )
                }
            }

            // Calendar option for bill categories
            if (isBillCategory) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (addToCalendar) Mint else Hairline)
                            .androidClick { addToCalendar = !addToCalendar },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (addToCalendar) Icon(Icons.Rounded.Check, null, tint = OnMint, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Add to calendar as repeating bill", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }

                if (addToCalendar) {
                    Spacer(Modifier.height(12.dp))
                    LabeledField(
                        label = "Day of month due",
                        value = billDayOfMonth,
                        onValueChange = { billDayOfMonth = it.filter { c -> c.isDigit() }.take(2) },
                        placeholder = "1–31",
                        keyboardType = KeyboardType.Number,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        val valid = parseAmount(amount) > 0 && accountId != null
        PrimaryButton(label = if (isIncome) "Add income" else "Add expense", enabled = valid) {
            vm.addTransaction(
                title = title,
                amount = parseAmount(amount),
                accountId = accountId!!,
                categoryId = categoryId,
                isIncome = isIncome,
            )
            if (addToCalendar && isBillCategory && !isIncome) {
                val dayNum = billDayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                val c = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, dayNum.coerceIn(1, 28))
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                vm.addCalendarEvent(
                    title = title.ifBlank { selectedCategory?.name ?: "Bill" },
                    type = com.rork.budgetflow.data.CalendarEventType.BILL,
                    dayOfMonth = dayNum,
                    startTimestamp = c.timeInMillis,
                    endTimestamp = null,
                    amount = parseAmount(amount),
                    categoryId = categoryId,
                    colorArgb = selectedCategory?.colorArgb ?: AccentPalette[4].toLongArgb(),
                    notes = "Auto-added from ${selectedCategory?.name ?: "bill"} transaction",
                )
            }
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ToggleHalf(label: String, selected: Boolean, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) accent else Color.Transparent)
            .androidClick(onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) OnMint else TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AccountChip(account: Account, selected: Boolean, onClick: () -> Unit) {
    val color = account.colorArgb.toColor()
    Column(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .background(if (selected) color.copy(alpha = 0.16f) else com.rork.budgetflow.ui.theme.InkSurface)
            .androidClick(onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(
                account.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onBackground else TextSecondary,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            (if (account.type == AccountType.CREDIT) "Owes " else "") + Money.format(account.balance),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

// --- Account ----------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAccountSheet(vm: BudgetViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.CHECKING) }
    var colorIndex by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle("New account")
        LabeledField("Name", name, { name = it }, placeholder = "e.g. Chase Checking")
        Spacer(Modifier.height(16.dp))
        FieldLabel("Type")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AccountType.entries.forEach { t ->
                SelectChip(t.label, t == type, onClick = { type = t })
            }
        }
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = if (type == AccountType.CREDIT) "Current balance owed" else "Current balance",
            value = balance,
            onValueChange = { balance = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        if (type == AccountType.CREDIT) {
            Spacer(Modifier.height(16.dp))
            LabeledField(
                label = "Credit limit",
                value = limit,
                onValueChange = { limit = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = "0.00",
                keyboardType = KeyboardType.Decimal,
                prefix = "$",
            )
        }
        Spacer(Modifier.height(16.dp))
        FieldLabel("Color")
        ColorRow(colorIndex) { colorIndex = it }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Create account", enabled = name.isNotBlank()) {
            vm.addAccount(
                name = name,
                type = type,
                balance = parseAmount(balance),
                colorArgb = AccentPalette[colorIndex].toLongArgb(),
                creditLimit = parseAmount(limit),
            )
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Category ----------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategorySheet(
    vm: BudgetViewModel,
    onDone: () -> Unit,
    editCategory: com.rork.budgetflow.data.Category? = null,
) {
    val isEdit = editCategory != null
    var name by remember { mutableStateOf(editCategory?.name ?: "") }
    var budget by remember { mutableStateOf(if (editCategory != null && editCategory.monthlyBudget > 0) Money.formatNoComma(editCategory.monthlyBudget) else "") }
    var suggestedPct by remember { mutableStateOf(if (editCategory != null && editCategory.suggestedPercentage > 0) Money.formatNoComma(editCategory.suggestedPercentage) else "") }
    var colorIndex by remember {
        mutableStateOf(
            com.rork.budgetflow.ui.theme.AccentPalette.indexOfFirst {
                it.toLongArgb() == (editCategory?.colorArgb ?: 0L)
            }.coerceAtLeast(0)
        )
    }
    var iconKey by remember { mutableStateOf(editCategory?.iconKey ?: "cart") }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle(if (isEdit) "Edit category" else "New category")
        LabeledField("Name", name, { name = it }, placeholder = "e.g. Groceries")
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Monthly budget (optional)",
            value = budget,
            onValueChange = { budget = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Suggested % of income",
            value = suggestedPct,
            onValueChange = { suggestedPct = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "e.g. 15",
            keyboardType = KeyboardType.Decimal,
            suffix = "%",
        )
        Spacer(Modifier.height(16.dp))
        FieldLabel("Icon")
        IconRow(iconKey, AccentPalette[colorIndex]) { iconKey = it }
        Spacer(Modifier.height(16.dp))
        FieldLabel("Color")
        ColorRow(colorIndex) { colorIndex = it }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(if (isEdit) "Save changes" else "Create category", enabled = name.isNotBlank()) {
            if (isEdit) {
                vm.updateCategory(
                    id = editCategory!!.id,
                    name = name,
                    iconKey = iconKey,
                    colorArgb = AccentPalette[colorIndex].toLongArgb(),
                    monthlyBudget = parseAmount(budget),
                    suggestedPercentage = parseAmount(suggestedPct),
                )
            } else {
                vm.addCategory(
                    name = name,
                    iconKey = iconKey,
                    colorArgb = AccentPalette[colorIndex].toLongArgb(),
                    monthlyBudget = parseAmount(budget),
                    suggestedPercentage = parseAmount(suggestedPct),
                )
            }
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Goal --------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGoalSheet(vm: BudgetViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    var iconKey by remember { mutableStateOf("star") }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle("New savings goal")
        LabeledField("Name", name, { name = it }, placeholder = "e.g. Vacation")
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Target amount",
            value = target,
            onValueChange = { target = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(16.dp))
        FieldLabel("Icon")
        IconRow(iconKey, AccentPalette[colorIndex]) { iconKey = it }
        Spacer(Modifier.height(16.dp))
        FieldLabel("Color")
        ColorRow(colorIndex) { colorIndex = it }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Create goal", enabled = name.isNotBlank() && parseAmount(target) > 0) {
            vm.addGoal(
                name = name,
                target = parseAmount(target),
                colorArgb = AccentPalette[colorIndex].toLongArgb(),
                iconKey = iconKey,
            )
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Debt --------------------------------------------------------------------

@Composable
fun AddDebtSheet(vm: BudgetViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var apr by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(3) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle("New debt")
        LabeledField("Name", name, { name = it }, placeholder = "e.g. Student Loan")
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Total amount",
            value = total,
            onValueChange = { total = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "APR %",
            value = apr,
            onValueChange = { apr = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.0",
            keyboardType = KeyboardType.Decimal,
        )
        Spacer(Modifier.height(16.dp))
        FieldLabel("Color")
        ColorRow(colorIndex) { colorIndex = it }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Add debt", enabled = name.isNotBlank() && parseAmount(total) > 0) {
            vm.addDebt(
                name = name,
                total = parseAmount(total),
                apr = parseAmount(apr),
                colorArgb = AccentPalette[colorIndex].toLongArgb(),
            )
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Move money (goal contribution / debt payment) --------------------------

@Composable
fun MoveMoneySheet(
    vm: BudgetViewModel,
    title: String,
    actionLabel: String,
    onConfirm: (amount: Double, fromAccountId: String?) -> Unit,
    onDone: () -> Unit,
) {
    val data by vm.data.collectAsStateWithLifecycle()
    var amount by remember { mutableStateOf("") }
    var fromId by remember { mutableStateOf<String?>(data.accounts.firstOrNull { it.type != AccountType.CREDIT }?.id) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle(title)
        LabeledField(
            label = "Amount",
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(16.dp))
        FieldLabel("From account")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            data.accounts.forEach { acc ->
                AccountChip(acc, selected = acc.id == fromId) { fromId = acc.id }
            }
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(actionLabel, enabled = parseAmount(amount) > 0) {
            onConfirm(parseAmount(amount), fromId)
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Vehicle ----------------------------------------------------------------

@Composable
fun AddVehicleSheet(vm: BudgetViewModel, onDone: () -> Unit) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var year by remember { mutableStateOf("$currentYear") }
    var purchasePrice by remember { mutableStateOf("") }
    var useCustomValue by remember { mutableStateOf(false) }
    var currentValue by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(6) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle("Add vehicle")
        LabeledField("Make", make, { make = it }, placeholder = "e.g. Toyota")
        Spacer(Modifier.height(16.dp))
        LabeledField("Model", model, { model = it }, placeholder = "e.g. Camry")
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Year",
            value = year,
            onValueChange = { year = it.filter { c -> c.isDigit() }.take(4) },
            placeholder = "$currentYear",
            keyboardType = KeyboardType.Number,
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Purchase price",
            value = purchasePrice,
            onValueChange = { purchasePrice = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (useCustomValue) Mint else Hairline)
                    .androidClick { useCustomValue = !useCustomValue },
                contentAlignment = Alignment.Center,
            ) {
                if (useCustomValue) Icon(Icons.Rounded.Check, null, tint = OnMint, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("Set custom current value", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        }
        if (useCustomValue) {
            Spacer(Modifier.height(12.dp))
            LabeledField(
                label = "Current value",
                value = currentValue,
                onValueChange = { currentValue = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = "0.00",
                keyboardType = KeyboardType.Decimal,
                prefix = "$",
            )
        } else {
            Spacer(Modifier.height(8.dp))
            Text(
                "Value auto-depreciates 15% first year, then 10%/year",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
            )
        }
        Spacer(Modifier.height(16.dp))
        FieldLabel("Color")
        ColorRow(colorIndex) { colorIndex = it }
        Spacer(Modifier.height(24.dp))
        val valid = make.isNotBlank() && model.isNotBlank() && parseAmount(purchasePrice) > 0
        PrimaryButton("Add vehicle", enabled = valid) {
            vm.addVehicle(
                make = make,
                model = model,
                year = year.toIntOrNull() ?: currentYear,
                purchasePrice = parseAmount(purchasePrice),
                currentValue = if (useCustomValue && parseAmount(currentValue) > 0) parseAmount(currentValue) else null,
                colorArgb = AccentPalette[colorIndex].toLongArgb(),
            )
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Buying Power -----------------------------------------------------------

@Composable
fun AddBuyingPowerSheet(vm: BudgetViewModel, onDone: () -> Unit) {
    var type by remember { mutableStateOf(BuyingPowerType.CAR) }
    var maxPurchase by remember { mutableStateOf("") }
    var monthlyPayment by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(3) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle("Buying power")
        FieldLabel("Type")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(com.rork.budgetflow.ui.theme.InkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ToggleHalf("Car", type == BuyingPowerType.CAR, AccentPalette[3], Modifier.weight(1f)) { type = BuyingPowerType.CAR }
            ToggleHalf("Home", type == BuyingPowerType.HOME, AccentPalette[0], Modifier.weight(1f)) { type = BuyingPowerType.HOME }
        }
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Max purchase price",
            value = maxPurchase,
            onValueChange = { maxPurchase = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Estimated monthly payment",
            value = monthlyPayment,
            onValueChange = { monthlyPayment = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(16.dp))
        FieldLabel("Color")
        ColorRow(colorIndex) { colorIndex = it }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Save", enabled = parseAmount(maxPurchase) > 0) {
            vm.addBuyingPower(
                type = type,
                maxPurchase = parseAmount(maxPurchase),
                monthlyPayment = parseAmount(monthlyPayment),
                colorArgb = AccentPalette[colorIndex].toLongArgb(),
            )
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Calendar event ---------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCalendarEventSheet(
    vm: BudgetViewModel,
    onDone: () -> Unit,
    editEvent: com.rork.budgetflow.data.CalendarEvent? = null,
) {
    val data by vm.data.collectAsStateWithLifecycle()
    val isEdit = editEvent != null

    val cal = Calendar.getInstance()
    var title by remember { mutableStateOf(editEvent?.title ?: "") }
    var eventType by remember { mutableStateOf(editEvent?.type ?: com.rork.budgetflow.data.CalendarEventType.BILL) }
    var dayOfMonth by remember { mutableStateOf(if (editEvent != null) editEvent.dayOfMonth.toString() else "1") }
    var amount by remember { mutableStateOf(if (editEvent != null && editEvent.amount > 0) Money.formatNoComma(editEvent.amount) else "") }
    var notes by remember { mutableStateOf(editEvent?.notes ?: "") }
    var categoryId by remember { mutableStateOf(editEvent?.categoryId) }
    var colorIndex by remember {
        mutableStateOf(
            AccentPalette.indexOfFirst {
                it.toLongArgb() == (editEvent?.colorArgb ?: AccentPalette[2].toLongArgb())
            }.coerceAtLeast(0)
        )
    }

    // For vacations: month/year pickers (simplified to current month by default)
    val startMonth = if (editEvent != null) {
        cal.timeInMillis = editEvent.startTimestamp
        cal.get(Calendar.DAY_OF_MONTH)
    } else cal.get(Calendar.DAY_OF_MONTH)
    var startDay by remember { mutableStateOf(startMonth.toString()) }
    var durationDays by remember {
        mutableStateOf(
            if (editEvent != null && editEvent.endTimestamp != null) {
                val diff = editEvent.endTimestamp!! - editEvent.startTimestamp
                (diff / 86400000L).toInt().coerceAtLeast(1).toString()
            } else "3"
        )
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle(if (isEdit) "Edit event" else "New calendar event")

        // Type toggle
        FieldLabel("Type")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(com.rork.budgetflow.ui.theme.InkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ToggleHalf("Bill", eventType == com.rork.budgetflow.data.CalendarEventType.BILL, Mint, Modifier.weight(1f)) {
                eventType = com.rork.budgetflow.data.CalendarEventType.BILL
            }
            ToggleHalf("Vacation", eventType == com.rork.budgetflow.data.CalendarEventType.VACATION, Coral, Modifier.weight(1f)) {
                eventType = com.rork.budgetflow.data.CalendarEventType.VACATION
            }
        }
        Spacer(Modifier.height(16.dp))

        LabeledField("Title", title, { title = it }, placeholder = if (eventType == com.rork.budgetflow.data.CalendarEventType.BILL) "e.g. Rent" else "e.g. Beach Trip")
        Spacer(Modifier.height(16.dp))

        if (eventType == com.rork.budgetflow.data.CalendarEventType.BILL) {
            // Day of month for recurring bills
            LabeledField(
                label = "Day of month",
                value = dayOfMonth,
                onValueChange = { dayOfMonth = it.filter { c -> c.isDigit() }.take(2) },
                placeholder = "1–31",
                keyboardType = KeyboardType.Number,
            )
            Spacer(Modifier.height(16.dp))

            LabeledField(
                label = "Amount",
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = "0.00",
                keyboardType = KeyboardType.Decimal,
                prefix = "$",
            )
            Spacer(Modifier.height(16.dp))

            FieldLabel("Linked category (optional)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.categories.forEach { cat ->
                    SelectChip(
                        label = cat.name,
                        selected = cat.id == categoryId,
                        accent = cat.colorArgb.toColor(),
                        onClick = { categoryId = if (cat.id == categoryId) null else cat.id },
                    )
                }
            }
        } else {
            // Vacation date range
            LabeledField(
                label = "Start day of month",
                value = startDay,
                onValueChange = { startDay = it.filter { c -> c.isDigit() }.take(2) },
                placeholder = "1–31",
                keyboardType = KeyboardType.Number,
            )
            Spacer(Modifier.height(16.dp))

            LabeledField(
                label = "Number of days",
                value = durationDays,
                onValueChange = { durationDays = it.filter { c -> c.isDigit() }.take(3) },
                placeholder = "e.g. 3",
                keyboardType = KeyboardType.Number,
            )
        }

        Spacer(Modifier.height(16.dp))

        LabeledField(
            label = "Notes (optional)",
            value = notes,
            onValueChange = { notes = it },
            placeholder = "e.g. Includes tax"
        )
        Spacer(Modifier.height(16.dp))

        FieldLabel("Color")
        ColorRow(colorIndex) { colorIndex = it }
        Spacer(Modifier.height(24.dp))

        val dayNum = dayOfMonth.toIntOrNull() ?: 1
        val startDayNum = startDay.toIntOrNull() ?: 1
        val valid = title.isNotBlank() && (eventType != com.rork.budgetflow.data.CalendarEventType.BILL || dayNum in 1..31)

        PrimaryButton(if (isEdit) "Save changes" else "Add event", enabled = valid) {
            val now = Calendar.getInstance()
            fun makeTimestamp(day: Int): Long {
                val c = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, day.coerceIn(1, 28))
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                return c.timeInMillis
            }

            val startTs = if (eventType == com.rork.budgetflow.data.CalendarEventType.BILL) {
                makeTimestamp(dayNum)
            } else {
                makeTimestamp(startDayNum)
            }
            val endTs = if (eventType == com.rork.budgetflow.data.CalendarEventType.VACATION) {
                val dur = durationDays.toIntOrNull()?.coerceAtLeast(1) ?: 3
                startTs + (dur - 1).toLong() * 86400000L
            } else null

            if (isEdit) {
                vm.updateCalendarEvent(
                    id = editEvent!!.id,
                    title = title,
                    type = eventType,
                    dayOfMonth = if (eventType == com.rork.budgetflow.data.CalendarEventType.BILL) dayNum else startDayNum,
                    startTimestamp = startTs,
                    endTimestamp = endTs,
                    amount = parseAmount(amount),
                    categoryId = categoryId,
                    colorArgb = AccentPalette[colorIndex].toLongArgb(),
                    notes = notes,
                )
            } else {
                vm.addCalendarEvent(
                    title = title,
                    type = eventType,
                    dayOfMonth = if (eventType == com.rork.budgetflow.data.CalendarEventType.BILL) dayNum else startDayNum,
                    startTimestamp = startTs,
                    endTimestamp = endTs,
                    amount = parseAmount(amount),
                    categoryId = categoryId,
                    colorArgb = AccentPalette[colorIndex].toLongArgb(),
                    notes = notes,
                )
            }
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Household editing -------------------------------------------------------

@Composable
fun EditHouseholdSheet(vm: BudgetViewModel, onDone: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val hh = data.household
    var adultCount by remember { mutableStateOf(hh.adultCount) }
    var childCount by remember { mutableStateOf(hh.childCount) }
    var petCount by remember { mutableStateOf(hh.petCount) }
    var incomeText by remember { mutableStateOf(if (hh.monthlyIncome > 0) hh.monthlyIncome.toLong().toString() else "") }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        SheetTitle("Edit household")

        // Adults counter
        CounterRow(
            icon = Icons.Rounded.People,
            label = "Adults",
            count = adultCount,
            onCountChange = { adultCount = it },
            min = 1,
            max = 8,
        )
        Spacer(Modifier.height(16.dp))

        // Children counter
        CounterRow(
            icon = Icons.Rounded.ChildCare,
            label = "Children",
            count = childCount,
            onCountChange = { childCount = it },
            min = 0,
            max = 10,
        )
        Spacer(Modifier.height(16.dp))

        // Pets counter
        CounterRow(
            icon = Icons.Rounded.Pets,
            label = "Pets",
            count = petCount,
            onCountChange = { petCount = it },
            min = 0,
            max = 15,
        )
        Spacer(Modifier.height(16.dp))

        // Income field
        LabeledField(
            label = "Monthly take-home income",
            value = incomeText,
            onValueChange = { incomeText = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            prefix = "$",
        )
        Spacer(Modifier.height(24.dp))

        PrimaryButton("Save changes") {
            vm.updateHousehold(
                com.rork.budgetflow.data.HouseholdProfile(
                    adultCount = adultCount,
                    childCount = childCount,
                    petCount = petCount,
                    monthlyIncome = incomeText.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: hh.monthlyIncome,
                )
            )
            onDone()
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CounterRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    onCountChange: (Int) -> Unit,
    min: Int,
    max: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(com.rork.budgetflow.ui.theme.InkSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Mint,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "$count ${if (count == 1) label.trimEnd('s') else label}",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
            )
        }
        // Minus button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (count > min) com.rork.budgetflow.ui.theme.InkElevated else com.rork.budgetflow.ui.theme.Hairline.copy(alpha = 0.3f))
                .androidClick { if (count > min) onCountChange(count - 1) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Remove,
                contentDescription = "Decrease",
                tint = if (count > min) Mint else TextTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        // Plus button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (count < max) com.rork.budgetflow.ui.theme.InkElevated else com.rork.budgetflow.ui.theme.Hairline.copy(alpha = 0.3f))
                .androidClick { if (count < max) onCountChange(count + 1) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "Increase",
                tint = if (count < max) Mint else TextTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// --- Pickers -----------------------------------------------------------------

@Composable
private fun ColorRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AccentPalette.forEachIndexed { i, c ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(c)
                    .androidClick { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                if (i == selected) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = OnMint, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun IconRow(selected: String, accent: Color, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconCatalog.entries.forEach { (key, vector) ->
            val isSel = key == selected
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSel) accent.copy(alpha = 0.20f) else com.rork.budgetflow.ui.theme.InkSurface)
                    .androidClick { onSelect(key) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    vector,
                    contentDescription = null,
                    tint = if (isSel) accent else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

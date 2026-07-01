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
import androidx.compose.material.icons.rounded.Check
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

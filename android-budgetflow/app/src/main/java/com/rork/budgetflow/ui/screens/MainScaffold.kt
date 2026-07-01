package com.rork.budgetflow.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rork.budgetflow.data.Category
import com.rork.budgetflow.ui.BudgetViewModel
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.MintDeep
import com.rork.budgetflow.ui.theme.OnMint
import com.rork.budgetflow.ui.theme.TextTertiary

private enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    ACCOUNTS("Wallet", Icons.Rounded.CreditCard),
    TRENDS("Budget", Icons.Rounded.BarChart),
    ACTIVITY("Activity", Icons.Rounded.SwapVert),
    GOALS("Goals", Icons.Rounded.Flag),
    LEARN("Learn", Icons.Rounded.Lightbulb),
}

private sealed interface ActiveSheet {
    data object Transaction : ActiveSheet
    data object Account : ActiveSheet
    data object AddCategory : ActiveSheet
    data class EditCategory(val category: Category) : ActiveSheet
    data object Goal : ActiveSheet
    data object Debt : ActiveSheet
    data class Contribute(val goalId: String) : ActiveSheet
    data class PayDebt(val debtId: String) : ActiveSheet
    data object Vehicle : ActiveSheet
    data object BuyingPower : ActiveSheet
    data object EditHousehold : ActiveSheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val vm: BudgetViewModel = viewModel()
    val data by vm.data.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.HOME) }
    var sheet by remember { mutableStateOf<ActiveSheet?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (!data.onboarded) {
        OnboardingScreen(
            onComplete = { profile -> vm.completeOnboarding(profile) }
        )
        return
    }

    Box(Modifier.fillMaxSize().background(Ink)) {
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                (fadeIn(tween(220)) togetherWith fadeOut(tween(150)))
            },
            label = "tab",
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) { current ->
            when (current) {
                Tab.HOME -> DashboardScreen(
                    vm = vm,
                    onSeeAccounts = { tab = Tab.ACCOUNTS },
                    onSeeActivity = { tab = Tab.ACTIVITY },
                    onAdd = { sheet = ActiveSheet.Transaction },
                    onEditHousehold = { sheet = ActiveSheet.EditHousehold },
                )
                Tab.ACCOUNTS -> AccountsScreen(
                    vm = vm,
                    onAddAccount = { sheet = ActiveSheet.Account },
                    onAddCategory = { sheet = ActiveSheet.AddCategory },
                    onEditCategory = { sheet = ActiveSheet.EditCategory(it) },
                )
                Tab.TRENDS -> TrendsScreen(vm = vm)
                Tab.ACTIVITY -> ActivityScreen(vm = vm)
                Tab.GOALS -> GoalsScreen(
                    vm = vm,
                    onAddGoal = { sheet = ActiveSheet.Goal },
                    onAddDebt = { sheet = ActiveSheet.Debt },
                    onContribute = { sheet = ActiveSheet.Contribute(it) },
                    onPayDebt = { sheet = ActiveSheet.PayDebt(it) },
                    onAddVehicle = { sheet = ActiveSheet.Vehicle },
                    onAddBuyingPower = { sheet = ActiveSheet.BuyingPower },
                )
                Tab.LEARN -> LearnScreen(
                    vm = vm,
                    onNavigateToWallet = { tab = Tab.ACCOUNTS },
                    onNavigateToGoals = { tab = Tab.GOALS },
                )
            }
        }

        BottomBar(
            selected = tab,
            onSelect = { tab = it },
            onAdd = { sheet = ActiveSheet.Transaction },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    val active = sheet
    if (active != null) {
        ModalBottomSheet(
            onDismissRequest = { sheet = null },
            sheetState = sheetState,
            containerColor = InkElevated,
            dragHandle = { SheetHandle() },
        ) {
            val close = { sheet = null }
            when (active) {
                ActiveSheet.Transaction -> AddTransactionSheet(vm, close)
                ActiveSheet.Account -> AddAccountSheet(vm, close)
                ActiveSheet.AddCategory -> AddCategorySheet(vm, close)
                is ActiveSheet.EditCategory -> AddCategorySheet(vm, close, editCategory = active.category)
                ActiveSheet.Goal -> AddGoalSheet(vm, close)
                ActiveSheet.Debt -> AddDebtSheet(vm, close)
                is ActiveSheet.Contribute -> MoveMoneySheet(
                    vm = vm,
                    title = "Add to goal",
                    actionLabel = "Add money",
                    onConfirm = { amount, from -> vm.contributeToGoal(active.goalId, amount, from) },
                    onDone = close,
                )
                is ActiveSheet.PayDebt -> MoveMoneySheet(
                    vm = vm,
                    title = "Make a payment",
                    actionLabel = "Pay debt",
                    onConfirm = { amount, from -> vm.payDebt(active.debtId, amount, from) },
                    onDone = close,
                )
                ActiveSheet.Vehicle -> AddVehicleSheet(vm, close)
                ActiveSheet.BuyingPower -> AddBuyingPowerSheet(vm, close)
                ActiveSheet.EditHousehold -> EditHouseholdSheet(vm, close)
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(Hairline)
        )
    }
}

@Composable
private fun BottomBar(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(InkElevated)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        NavItem(Tab.HOME, selected, onSelect, Modifier.weight(1f))
        NavItem(Tab.ACCOUNTS, selected, onSelect, Modifier.weight(1f))
        NavItem(Tab.TRENDS, selected, onSelect, Modifier.weight(1f))
        AddButton(onAdd)
        NavItem(Tab.ACTIVITY, selected, onSelect, Modifier.weight(1f))
        NavItem(Tab.GOALS, selected, onSelect, Modifier.weight(1f))
        NavItem(Tab.LEARN, selected, onSelect, Modifier.weight(1f))
    }
}

@Composable
private fun NavItem(tab: Tab, selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val isSel = tab == selected
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickableNoRipple { onSelect(tab) }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            tab.icon,
            contentDescription = tab.label,
            tint = if (isSel) Mint else TextTertiary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSel) Mint else TextTertiary,
        )
    }
}

@Composable
private fun AddButton(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(52.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Mint, MintDeep)))
            .clickableNoRipple(onAdd),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Add, contentDescription = "Add", tint = OnMint, modifier = Modifier.size(28.dp))
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        onClick = onClick,
    )

package com.rork.budgetflow.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rork.budgetflow.data.HouseholdProfile
import com.rork.budgetflow.ui.components.PrimaryButton
import com.rork.budgetflow.ui.components.androidClick
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.MintDeep
import com.rork.budgetflow.ui.theme.OnMint
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

private const val STEP_WELCOME = 0
private const val STEP_ADULTS = 1
private const val STEP_CHILDREN = 2
private const val STEP_PETS = 3
private const val STEP_AGE = 4
private const val STEP_INCOME = 5
private const val STEP_DONE = 6

@Composable
fun OnboardingScreen(onComplete: (HouseholdProfile) -> Unit) {
    var step by remember { mutableIntStateOf(STEP_WELCOME) }
    var adultCount by remember { mutableIntStateOf(1) }
    var childCount by remember { mutableIntStateOf(0) }
    var petCount by remember { mutableIntStateOf(0) }
    var ageText by remember { mutableStateOf("") }
    var incomeText by remember { mutableStateOf("") }

    val direction = remember(step) { if (step > 0) 1 else -1 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Ink, Color(0xFF0D1A16), Ink)
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 36.dp),
            ) {
                for (i in 1..6) {
                    val filled = i <= step.coerceIn(1, 6)
                    Box(
                        modifier = Modifier
                            .size(if (filled) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (filled) Mint else InkSurface),
                    )
                }
            }

            // Animated step content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(300)) { it * dir } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(300)) { -it * dir } + fadeOut(tween(150)))
                },
                label = "onboard-step",
                modifier = Modifier.weight(1f),
            ) { current ->
                when (current) {
                    STEP_WELCOME -> WelcomeStep(
                        onNext = { step = STEP_ADULTS }
                    )
                    STEP_ADULTS -> CounterStep(
                        icon = { Icon(Icons.Rounded.ChildCare, null, tint = Mint, modifier = Modifier.size(48.dp)) },
                        title = "Household",
                        subtitle = "Including yourself, how many adults live in your home?",
                        count = adultCount,
                        onCountChange = { adultCount = it },
                        min = 1,
                        max = 8,
                        label = { "$it adult${if (it != 1) "s" else ""}" },
                        onNext = { step = STEP_CHILDREN },
                        onBack = { step = STEP_WELCOME },
                    )
                    STEP_CHILDREN -> CounterStep(
                        icon = { Icon(Icons.Rounded.ChildCare, null, tint = Mint, modifier = Modifier.size(48.dp)) },
                        title = "Children",
                        subtitle = "How many children are in your household?",
                        count = childCount,
                        onCountChange = { childCount = it },
                        min = 0,
                        max = 10,
                        label = { if (it == 0) "None" else "$it ${if (it == 1) "child" else "children"}" },
                        onNext = { step = STEP_PETS },
                        onBack = { step = STEP_ADULTS },
                    )
                    STEP_PETS -> CounterStep(
                        icon = { Icon(Icons.Rounded.Pets, null, tint = Mint, modifier = Modifier.size(48.dp)) },
                        title = "Pets",
                        subtitle = "How many pets live with you? (dogs, cats, etc.)",
                        count = petCount,
                        onCountChange = { petCount = it },
                        min = 0,
                        max = 15,
                        label = { if (it == 0) "None" else "$it pet${if (it != 1) "s" else ""}" },
                        onNext = { step = STEP_AGE },
                        onBack = { step = STEP_CHILDREN },
                    )
                    STEP_AGE -> AgeStep(
                        age = ageText,
                        onAgeChange = { ageText = it },
                        onNext = { step = STEP_INCOME },
                        onBack = { step = STEP_PETS },
                    )
                    STEP_INCOME -> IncomeStep(
                        income = incomeText,
                        onIncomeChange = { incomeText = it },
                        onNext = { step = STEP_DONE },
                        onBack = { step = STEP_AGE },
                    )
                    STEP_DONE -> SummaryStep(
                        adultCount = adultCount,
                        childCount = childCount,
                        petCount = petCount,
                        totalPeople = adultCount + childCount,
                        age = ageText.filter { it.isDigit() }.toIntOrNull() ?: 0,
                        income = incomeText.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0,
                        onFinish = {
                            onComplete(
                                HouseholdProfile(
                                    adultCount = adultCount,
                                    childCount = childCount,
                                    petCount = petCount,
                                    monthlyIncome = incomeText.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                                        ?: 0.0,
                                    userAge = ageText.filter { it.isDigit() }.toIntOrNull() ?: 0,
                                )
                            )
                        },
                        onBack = { step = STEP_INCOME },
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // App icon / logo
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(MintDeep, Mint))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "GPS",
                style = MaterialTheme.typography.headlineLarge,
                color = OnMint,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Welcome to\nFinancial GPS",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Let's personalise your budget. Answer a few quick questions so we can tailor spending guidelines to your household.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(48.dp))
        PrimaryButton("Get started") { onNext() }
    }
}

@Composable
private fun CounterStep(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    count: Int,
    onCountChange: (Int) -> Unit,
    min: Int,
    max: Int,
    label: (Int) -> String,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(0.3f))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(36.dp))

        // Counter controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (count > min) InkElevated else InkSurface)
                    .androidClick { if (count > min) onCountChange(count - 1) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Remove,
                    null,
                    tint = if (count > min) Mint else TextTertiary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${count}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    label(count),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (count < max) InkElevated else InkSurface)
                    .androidClick { if (count < max) onCountChange(count + 1) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Add,
                    null,
                    tint = if (count < max) Mint else TextTertiary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Spacer(Modifier.weight(0.4f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                icon = Icons.Rounded.ArrowBack,
                label = "Back",
                onClick = onBack,
            )
            PrimaryButton("Next") { onNext() }
        }
    }
}

@Composable
private fun IncomeStep(
    income: String,
    onIncomeChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(0.3f))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$",
                style = MaterialTheme.typography.headlineLarge,
                color = Mint,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Monthly income",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "What's your approximate monthly take-home income? This helps us suggest realistic spending targets.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(36.dp))

        OutlinedTextField(
            value = income,
            onValueChange = { onIncomeChange(it.filter { c -> c.isDigit() || c == '.' }) },
            placeholder = {
                Text("0.00", color = TextTertiary)
            },
            prefix = {
                Text("$", color = Mint, style = MaterialTheme.typography.titleLarge)
            },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Mint,
                unfocusedBorderColor = InkSurface,
                cursorColor = Mint,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(0.4f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                icon = Icons.Rounded.ArrowBack,
                label = "Back",
                onClick = onBack,
            )
            PrimaryButton("Next") { onNext() }
        }
    }
}

@Composable
private fun AgeStep(
    age: String,
    onAgeChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(0.3f))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Cake,
                null,
                tint = Mint,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Your age",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "This helps us suggest retirement milestones and timelines tailored to where you are in life.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(36.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { onAgeChange(it.filter { c -> c.isDigit() }.take(3)) },
            placeholder = {
                Text("30", color = TextTertiary)
            },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Mint,
                unfocusedBorderColor = InkSurface,
                cursorColor = Mint,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(0.4f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                icon = Icons.Rounded.ArrowBack,
                label = "Back",
                onClick = onBack,
            )
            PrimaryButton("Next") { onNext() }
        }
    }
}

@Composable
private fun SummaryStep(
    adultCount: Int,
    childCount: Int,
    petCount: Int,
    totalPeople: Int,
    age: Int,
    income: Double,
    onFinish: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(0.2f))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.ChildCare,
                null,
                tint = Mint,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Your household",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "We'll tailor your spending guidelines based on this profile.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(32.dp))

        // Summary cards
        SummaryCard(adultCount.toString(), "Adult${if (adultCount != 1) "s" else ""}", Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        SummaryCard(childCount.toString(), "Child${if (childCount != 1) "ren" else ""}", Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        SummaryCard(petCount.toString(), "Pet${if (petCount != 1) "s" else ""}", Modifier.weight(1f))

        if (age > 0) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(InkElevated)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Your age",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                        )
                        Text(
                            "$age years old",
                            style = MaterialTheme.typography.titleLarge,
                            color = Mint,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        if (income > 0) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(InkElevated)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Monthly income",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                        )
                        Text(
                            com.rork.budgetflow.data.Money.format(income),
                            style = MaterialTheme.typography.titleLarge,
                            color = Mint,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(0.3f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                icon = Icons.Rounded.ArrowBack,
                label = "Back",
                onClick = onBack,
            )
            PrimaryButton("Start tracking") { onFinish() }
        }
    }
}

@Composable
private fun SummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(InkElevated)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
        )
    }
}

@Composable
private fun TextButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .androidClick(onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
    }
}

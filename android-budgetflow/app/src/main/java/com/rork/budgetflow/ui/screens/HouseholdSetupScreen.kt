package com.rork.budgetflow.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rork.budgetflow.ui.components.PrimaryButton
import com.rork.budgetflow.ui.components.androidClick
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.MintDeep
import com.rork.budgetflow.ui.theme.OnMint
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

sealed interface HouseholdAction {
    data object Create : HouseholdAction
    data object Join : HouseholdAction
}

@Composable
fun HouseholdSetupScreen(
    isLoading: Boolean,
    error: String?,
    onCreateHousehold: () -> Unit,
    onJoinHousehold: (code: String) -> Unit,
) {
    var action by remember { mutableStateOf<HouseholdAction?>(null) }
    var inviteCode by remember { mutableStateOf("") }

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
                .padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.10f))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Mint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = null,
                    tint = Mint,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Set up your household",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Create a new shared budget or join your partner's household with an invite code.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.weight(0.15f))

            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(
                        color = Mint,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                    )
                    Text(
                        "Setting up...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            } else {
                when (action) {
                    is HouseholdAction.Join -> {
                        JoinHouseholdForm(
                            code = inviteCode,
                            onCodeChange = { inviteCode = it.take(8).uppercase() },
                            onJoin = { onJoinHousehold(inviteCode.trim()) },
                            onBack = { action = null },
                            error = error,
                        )
                    }
                    else -> {
                        // Choice screen
                        ChoiceCard(
                            icon = { Icon(Icons.Rounded.Home, null, tint = Mint, modifier = Modifier.size(32.dp)) },
                            title = "Create new household",
                            subtitle = "Start fresh and invite your partner later with a shareable code",
                            onClick = { onCreateHousehold() },
                        )

                        Spacer(Modifier.height(12.dp))

                        ChoiceCard(
                            icon = { Icon(Icons.Rounded.PersonAdd, null, tint = Mint, modifier = Modifier.size(32.dp)) },
                            title = "Join existing household",
                            subtitle = "Enter the invite code your partner shared with you",
                            onClick = { action = HouseholdAction.Join },
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.25f))
        }
    }
}

@Composable
private fun ChoiceCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(InkElevated)
            .androidClick(onClick)
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Mint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Spacer(Modifier.padding(start = 16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun JoinHouseholdForm(
    code: String,
    onCodeChange: (String) -> Unit,
    onJoin: () -> Unit,
    onBack: () -> Unit,
    error: String?,
) {
    val clipboard = LocalClipboardManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            "Enter invite code",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Ask your partner to share their household code",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            placeholder = { Text("e.g. A1B2C3D4", color = TextTertiary) },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Mint,
                unfocusedBorderColor = InkElevated,
                cursorColor = Mint,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Paste button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .androidClick {
                    clipboard.getText()?.let { onCodeChange(it.text.take(8).uppercase()) }
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.ContentCopy,
                    null,
                    tint = Mint,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.padding(start = 8.dp))
                Text("Paste from clipboard", style = MaterialTheme.typography.labelMedium, color = Mint)
            }
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF6B6B),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton("Join household", enabled = code.length >= 6) { onJoin() }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .androidClick(onBack)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text("Back", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        }
    }
}

@Composable
fun HouseholdCreatedScreen(code: String) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Ink, Color(0xFF0D1A16), Ink))
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.15f))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(MintDeep, Mint))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.GroupAdd, null, tint = OnMint, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Household created!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Share this code with your significant other so they can join your household and sync finances.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(32.dp))

            // Code display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(InkElevated)
                    .androidClick {
                        clipboard.setText(AnnotatedString(code))
                        copied = true
                    }
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Invite code",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextTertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        code,
                        style = MaterialTheme.typography.displayMedium,
                        color = Mint,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (copied) "Copied!" else "Tap to copy",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (copied) Mint else TextTertiary,
                    )
                }
            }
        }
    }
}

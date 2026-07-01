package com.rork.budgetflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rork.budgetflow.ui.components.androidClick
import com.rork.budgetflow.ui.theme.Ink
import com.rork.budgetflow.ui.theme.InkElevated
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.MintDeep
import com.rork.budgetflow.ui.theme.OnMint
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

@Composable
fun SignInScreen(
    isSigningIn: Boolean,
    error: String?,
    onSignIn: (provider: String) -> Unit,
    onClearError: () -> Unit,
) {
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
            Spacer(Modifier.weight(0.15f))

            // App logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
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

            Spacer(Modifier.height(28.dp))

            Text(
                "Sync your finances\ntogether",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Sign in so you and your significant other can track your shared budget in real time, across any device.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.weight(0.20f))

            // Error message
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x33FF6B6B))
                        .padding(12.dp),
                ) {
                    Text(
                        error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Sign in buttons
            if (isSigningIn) {
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
                        "Signing in...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            } else {
                // Google button
                SignInButton(
                    label = "Continue with Google",
                    icon = {
                        Text(
                            "G",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    bgColor = Color(0xFF4285F4),
                    onClick = { onSignIn("google") },
                )

                Spacer(Modifier.height(14.dp))

                // Apple button
                SignInButton(
                    label = "Continue with Apple",
                    icon = {
                        Icon(
                            Icons.Rounded.Group,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    bgColor = Color(0xFF000000),
                    borderColor = Color(0xFF333333),
                    onClick = { onSignIn("apple") },
                )
            }

            Spacer(Modifier.weight(0.25f))

            Text(
                "Your data is synced securely across devices.\nSignificant others can join your household with an invite code.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun SignInButton(
    label: String,
    icon: @Composable () -> Unit,
    bgColor: Color,
    borderColor: Color? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (borderColor != null) {
                    Modifier.background(
                        bgColor.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp),
                    )
                } else {
                    Modifier.background(bgColor)
                }
            )
            .androidClick(onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp),
        ) {
            icon()
        }
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

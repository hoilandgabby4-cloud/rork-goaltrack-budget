package com.rork.budgetflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.rork.budgetflow.ui.theme.Hairline
import com.rork.budgetflow.ui.theme.InkSurface
import com.rork.budgetflow.ui.theme.Mint
import com.rork.budgetflow.ui.theme.TextSecondary
import com.rork.budgetflow.ui.theme.TextTertiary

/** Labelled rounded text input used inside add sheets. */
@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
    suffix: String? = null,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.size(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(InkSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (prefix != null) {
                Text(
                    prefix,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.width(6.dp))
            }
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextTertiary,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                    cursorBrush = SolidColor(Mint),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (suffix != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    suffix,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}

/** A selectable pill chip used for choosing accounts / categories / types. */
@Composable
fun SelectChip(
    label: String,
    selected: Boolean,
    accent: Color = Mint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) accent.copy(alpha = 0.18f) else InkSurface)
            .border(
                width = 1.dp,
                color = if (selected) accent else Color.Transparent,
                shape = CircleShape,
            )
            .androidClick(onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) accent else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

/** A primary call-to-action button. */
@Composable
fun PrimaryButton(
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(if (enabled) Mint else Hairline)
            .androidClick { if (enabled) onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) com.rork.budgetflow.ui.theme.OnMint else TextTertiary,
            fontWeight = FontWeight.Bold,
        )
    }
}

package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.HaulaufGoldDark

enum class ActionTone { Primary, Secondary, Destructive }
enum class ActionSize(val minHeight: Dp) { Regular(56.dp), Prominent(88.dp), Glove(96.dp) }

/** Primary actions advance the task; secondary actions navigate or offer an alternative.
 * Destructive actions use the error color while keeping the secondary outline.
 */
@Composable
fun AppActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ActionTone = ActionTone.Primary,
    size: ActionSize = ActionSize.Regular,
    content: @Composable RowScope.() -> Unit
) {
    val buttonModifier = modifier.heightIn(min = size.minHeight)
    val shape = RoundedCornerShape(16.dp)
    when (tone) {
        ActionTone.Primary -> Button(
            onClick = onClick, modifier = buttonModifier, enabled = enabled, shape = shape,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), content = content
        )
        ActionTone.Secondary, ActionTone.Destructive -> OutlinedButton(
            onClick = onClick, modifier = buttonModifier, enabled = enabled, shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (tone == ActionTone.Destructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), content = content
        )
    }
}

@Composable
fun AppDestructiveTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(onClick = onClick, modifier = modifier, enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        content = content)
}

/** The large start control is deliberately distinct for use during training with gloves. */
@Composable
fun AppLaunchButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showPlayIcon: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    val colorScheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 88.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp).background(
                if (enabled) Brush.verticalGradient(listOf(colorScheme.primary, HaulaufGoldDark))
                else Brush.verticalGradient(listOf(colorScheme.surfaceVariant, colorScheme.surfaceVariant)),
                shape
            ).padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showPlayIcon) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp),
                        tint = if (enabled) colorScheme.onPrimary else colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                }
                Text(label, style = (if (showPlayIcon) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium)
                    .copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = if (enabled) colorScheme.onPrimary else colorScheme.onSurfaceVariant)
            }
        }
    }
}

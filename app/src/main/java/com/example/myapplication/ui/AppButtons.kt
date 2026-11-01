package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ActionTone { Primary, Secondary, Destructive }
enum class ActionSize(val minHeight: Dp) { Regular(56.dp), Glove(96.dp) }

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
        ActionTone.Secondary -> Button(
            onClick = onClick, modifier = buttonModifier, enabled = enabled, shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), content = content
        )
        ActionTone.Destructive -> OutlinedButton(
            onClick = onClick, modifier = buttonModifier, enabled = enabled, shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
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

/** Start uses the same primary style and regular height as other actions outside training. */
@Composable
fun AppLaunchButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showPlayIcon: Boolean = false
) {
    AppActionButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        if (showPlayIcon) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center)
    }
}

/** A background-free 48 dp icon action for app bars and compact toolbars. */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        content = content
    )
}

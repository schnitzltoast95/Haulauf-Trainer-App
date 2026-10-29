package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.example.myapplication.ui.theme.HaulaufCard

@Composable
fun StepperCard(
    title: String,
    value: Int,
    onChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        BoxWithConstraints(Modifier.padding(12.dp)) {
            if (maxWidth < 340.dp || LocalDensity.current.fontScale > 1.3f) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.bodyMedium)
                    StepperInline(value, onChange, title)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    StepperInline(value, onChange, title, Modifier.width(208.dp))
                }
            }
        }
    }
}

@Composable
fun StepperInline(
    value: Int,
    onChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val de = LocalStrings.current === StringsDe
    var input by remember(value) { mutableStateOf(value.toString()) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallRoundButton(label = if (de) "$label verringern" else "Decrease $label", onClick = { onChange(value - 1) }) {
            Text(text = "−", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        }
        OutlinedTextField(
            value = input,
            onValueChange = { text ->
                val digitsOnly = text.filter { it.isDigit() }
                input = digitsOnly
                digitsOnly.toIntOrNull()?.let(onChange)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { contentDescription = label },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.onSurface
            )
        )
        SmallRoundButton(label = if (de) "$label erhöhen" else "Increase $label", onClick = { onChange(value + 1) }) {
            Text(text = "+", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun SmallRoundButton(
    label: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

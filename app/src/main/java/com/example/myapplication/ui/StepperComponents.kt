package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        color = HaulaufCard
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            StepperInline(value = value, onChange = onChange)
        }
    }
}

@Composable
fun StepperInline(
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallRoundButton(onClick = { onChange(value - 1) }) {
            Text(text = "−", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color.White
        )
        SmallRoundButton(onClick = { onChange(value + 1) }) {
            Text(text = "+", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun SmallRoundButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        color = Color(0xFF2A2A2A)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

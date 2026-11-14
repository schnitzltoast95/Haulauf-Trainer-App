package com.example.myapplication.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Shared slider styling: no decorative dot at either end of the track. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors()
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        colors = colors,
        track = { state ->
            SliderDefaults.Track(
                sliderState = state,
                enabled = enabled,
                colors = colors,
                drawStopIndicator = null,
                drawTick = { offset, color ->
                    // Discrete sliders also draw ticks at the rounded track endpoints.
                    val endInset = size.height / 2f + 0.5f
                    if (offset.x > endInset && offset.x < size.width - endInset) {
                        drawCircle(color = color, radius = 2.dp.toPx(), center = offset)
                    }
                }
            )
        }
    )
}

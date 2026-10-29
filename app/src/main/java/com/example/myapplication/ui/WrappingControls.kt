package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/** Wrap complete controls without shrinking their touch targets or clipping scaled labels. */
@Composable
fun WrappingControls(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gap = 8.dp.roundToPx()
        val children = measurables.map { it.measure(Constraints(maxWidth = constraints.maxWidth)) }
        var x = 0
        var y = 0
        var rowHeight = 0
        val positions = children.map { child ->
            if (x > 0 && x + child.width > constraints.maxWidth) {
                x = 0; y += rowHeight + gap; rowHeight = 0
            }
            val position = x to y
            x += child.width + gap
            rowHeight = maxOf(rowHeight, child.height)
            position
        }
        layout(constraints.maxWidth, constraints.constrainHeight(y + rowHeight)) {
            children.forEachIndexed { index, child ->
                child.placeRelative(positions[index].first, positions[index].second)
            }
        }
    }
}

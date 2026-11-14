package com.example.myapplication.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.MoveCategory

/** Vector insignia: shield, category mark and four earned-level studs. */
@Composable
internal fun BadgeEmblem(level: Int, category: MoveCategory, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val rim = if (level == 0) colors.outlineVariant else colors.primary
    val field = if (level == 0) colors.surfaceVariant else colors.secondaryContainer
    val mark = if (level == 0) colors.onSurfaceVariant else colors.onSecondaryContainer
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val shield = Path().apply {
            moveTo(w * .12f, h * .08f); lineTo(w * .88f, h * .08f)
            lineTo(w * .88f, h * .57f)
            quadraticTo(w * .85f, h * .80f, w * .5f, h * .96f)
            quadraticTo(w * .15f, h * .80f, w * .12f, h * .57f); close()
        }
        drawPath(shield, field)
        drawPath(shield, rim, style = Stroke(2.dp.toPx()))
        val stroke = 2.5.dp.toPx()
        if (category == MoveCategory.HAU) {
            drawLine(mark, Offset(w*.31f,h*.3f),Offset(w*.69f,h*.64f),stroke,StrokeCap.Round)
            drawLine(mark, Offset(w*.69f,h*.3f),Offset(w*.31f,h*.64f),stroke,StrokeCap.Round)
            drawLine(mark, Offset(w*.24f,h*.52f),Offset(w*.4f,h*.66f),stroke,StrokeCap.Round)
            drawLine(mark, Offset(w*.6f,h*.66f),Offset(w*.76f,h*.52f),stroke,StrokeCap.Round)
        } else if (category == MoveCategory.STICH) {
            drawLine(mark, Offset(w*.5f,h*.27f),Offset(w*.5f,h*.68f),stroke,StrokeCap.Round)
            drawLine(mark, Offset(w*.35f,h*.54f),Offset(w*.65f,h*.54f),stroke,StrokeCap.Round)
            drawLine(mark, Offset(w*.5f,h*.27f),Offset(w*.4f,h*.37f),stroke,StrokeCap.Round)
            drawLine(mark, Offset(w*.5f,h*.27f),Offset(w*.6f,h*.37f),stroke,StrokeCap.Round)
        } else {
            val guard = Path().apply {
                moveTo(w*.32f,h*.3f); lineTo(w*.68f,h*.3f); lineTo(w*.65f,h*.53f)
                lineTo(w*.5f,h*.66f); lineTo(w*.35f,h*.53f); close()
            }
            drawPath(guard, mark, style = Stroke(stroke))
        }
        repeat(4) { index ->
            drawCircle(if (index < level) rim else colors.outlineVariant, 2.dp.toPx(),
                Offset(w * (.32f + index*.12f),h*.76f))
        }
    }
}

package io.github.shreyasskdev.tiledeck.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun PieChartIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        drawArc(color = tint, startAngle = 30f, sweepAngle = 290f, useCenter = true)
        drawArc(color = tint.copy(alpha = 0.5f), startAngle = 330f, sweepAngle = 50f, useCenter = true)
    }
}

@Composable
internal fun EditIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        drawRoundRect(color = tint, cornerRadius = CornerRadius(4f, 4f), style = Stroke(width = 3f))
        drawLine(
            color = tint,
            start = Offset(size.width * 0.25f, size.height * 0.75f),
            end = Offset(size.width * 0.75f, size.height * 0.25f),
            strokeWidth = 3f,
        )
    }
}

@Composable
internal fun SettingsIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        drawCircle(color = tint, radius = size.minDimension / 2.2f, style = Stroke(width = 3f))
        drawCircle(color = tint, radius = size.minDimension / 5f)
    }
}

@Composable
internal fun RefreshIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        drawArc(
            color = tint,
            startAngle = 30f,
            sweepAngle = 290f,
            useCenter = false,
            style = Stroke(width = 3f, cap = StrokeCap.Round),
        )
    }
}

@Composable
internal fun ClearIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(14.dp)) {
        drawLine(color = tint, start = Offset(0f, 0f), end = Offset(size.width, size.height), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(size.width, 0f), end = Offset(0f, size.height), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

@Composable
internal fun AutoFixIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(10.dp)) {
        drawCircle(color = tint, radius = size.minDimension / 2f)
    }
}

@Composable
internal fun ChevronIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        drawLine(color = tint, start = Offset(w * 0.3f, h * 0.2f), end = Offset(w * 0.75f, h * 0.5f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.75f, h * 0.5f), end = Offset(w * 0.3f, h * 0.8f), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

@Composable
internal fun BackArrowIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        drawLine(color = tint, start = Offset(w * 0.7f, h * 0.2f), end = Offset(w * 0.3f, h * 0.5f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.3f, h * 0.5f), end = Offset(w * 0.7f, h * 0.8f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.3f, h * 0.5f), end = Offset(w * 0.9f, h * 0.5f), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}
package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PerformanceLineChart(
    times: List<Pair<Long, Double>>, // timestamp to seconds
    modifier: Modifier = Modifier,
    lineColor: Color = PrimaryIndigo,
    fillColor: Color = PrimaryIndigo.copy(alpha = 0.15f)
) {
    if (times.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Complete practice sessions to view your speed chart!",
                color = Slate400,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    val values = times.map { it.second }
    val maxVal = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val minVal = (values.minOrNull() ?: 0.0).coerceAtLeast(0.0)
    val range = (maxVal - minVal).coerceAtLeast(0.5)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padY = 20f
                val chartHeight = h - padY * 2

                // Horizontal Grid lines (3 lines)
                val gridColor = Color(0xFFF1F5F9)
                for (i in 0..3) {
                    val y = padY + chartHeight * (i / 3f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 2f
                    )
                }

                if (values.size == 1) {
                    val cy = h / 2f
                    val cx = w / 2f
                    drawCircle(color = lineColor, radius = 8f, center = Offset(cx, cy))
                    return@Canvas
                }

                val stepX = w / (values.size - 1)
                val points = values.mapIndexed { idx, v ->
                    val normY = (v - minVal) / range
                    val x = idx * stepX
                    val y = padY + chartHeight * (1f - normY.toFloat())
                    Offset(x, y)
                }

                // Fill Path
                val fillPath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                    lineTo(points.last().x, h)
                    lineTo(points.first().x, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, fillColor.copy(alpha = 0.01f)),
                        startY = padY,
                        endY = h
                    )
                )

                // Stroke Path
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )

                // Draw point circles
                points.forEachIndexed { i, p ->
                    drawCircle(color = Color.White, radius = 6f, center = p)
                    drawCircle(color = lineColor, radius = 4f, center = p)
                }
            }
        }

        // Bottom labels (first and last session time)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(
                text = "Earlier: ${sdf.format(Date(times.first().first))}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400
            )
            Text(
                text = "Recent: ${sdf.format(Date(times.last().first))}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400
            )
        }
    }
}

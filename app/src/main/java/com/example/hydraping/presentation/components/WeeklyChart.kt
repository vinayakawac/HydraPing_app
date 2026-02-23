package com.example.hydraping.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydraping.domain.model.DailySummary
import com.example.hydraping.ui.theme.HydraBlue
import com.example.hydraping.ui.theme.HydraBlue10
import com.example.hydraping.ui.theme.HydraBlueLight
import com.example.hydraping.ui.theme.SuccessGreen
import com.example.hydraping.ui.theme.SuccessGreenLight

@Composable
fun WeeklyChart(
    summaries: List<DailySummary>,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) return

    val maxValue = maxOf(summaries.maxOf { it.totalMl }, dailyGoal).toFloat()
    val primaryColor = MaterialTheme.colorScheme.primary
    val goalLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Animate chart drawing on first load
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(summaries) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(900))
    }

    // Today is the last entry
    val todayIndex = summaries.lastIndex

    Column(modifier = modifier.fillMaxWidth()) {
        // Y-axis labels + chart
        Row(modifier = Modifier.fillMaxWidth()) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .height(180.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("100", "80", "60", "40", "20").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = onSurfaceVariant
                    )
                }
            }

            // Chart area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val chartWidth = size.width
                    val chartHeight = size.height
                    val pointCount = summaries.size

                    // Horizontal grid lines
                    for (i in 0..4) {
                        val y = chartHeight * i / 4f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    // Goal line (dashed)
                    if (maxValue > 0) {
                        val goalY = chartHeight - (dailyGoal / maxValue) * chartHeight
                        val dashWidth = 8f
                        val gapWidth = 6f
                        var x = 0f
                        while (x < chartWidth) {
                            drawLine(
                                color = goalLineColor,
                                start = Offset(x, goalY),
                                end = Offset((x + dashWidth).coerceAtMost(chartWidth), goalY),
                                strokeWidth = 1.5f
                            )
                            x += dashWidth + gapWidth
                        }
                    }

                    if (pointCount < 2) return@Canvas

                    // Calculate points
                    val points = summaries.mapIndexed { index, summary ->
                        val xPos = chartWidth * index / (pointCount - 1).toFloat()
                        val yPos = if (maxValue > 0) {
                            chartHeight - (summary.totalMl / maxValue) * chartHeight
                        } else chartHeight
                        Offset(xPos, yPos)
                    }

                    // Build cubic bezier line path
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2f
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }

                    // Build fill path (line path + close to bottom)
                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, chartHeight)
                        lineTo(points.first().x, chartHeight)
                        close()
                    }

                    // Clip to animated progress for reveal effect
                    val revealWidth = chartWidth * animationProgress.value

                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = revealWidth,
                        bottom = chartHeight,
                        clipOp = ClipOp.Intersect
                    ) {
                        // Gradient fill under the line
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    HydraBlueLight.copy(alpha = 0.35f),
                                    HydraBlue10.copy(alpha = 0.05f)
                                ),
                                startY = 0f,
                                endY = chartHeight
                            )
                        )

                        // Draw line
                        drawPath(
                            path = linePath,
                            color = primaryColor,
                            style = Stroke(
                                width = 2.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Data point dots
                        points.forEachIndexed { index, point ->
                            val isToday = index == todayIndex
                            val metGoal = summaries[index].totalMl >= dailyGoal

                            // Outer ring for today
                            if (isToday) {
                                drawCircle(
                                    color = if (metGoal) SuccessGreen.copy(alpha = 0.3f) else primaryColor.copy(alpha = 0.3f),
                                    radius = 10f,
                                    center = point
                                )
                            }

                            drawCircle(
                                color = if (metGoal) SuccessGreen else primaryColor,
                                radius = if (isToday) 6f else 4.5f,
                                center = point
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            summaries.forEachIndexed { index, summary ->
                val isToday = index == todayIndex
                Text(
                    text = summary.dayLabel,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) primaryColor else onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

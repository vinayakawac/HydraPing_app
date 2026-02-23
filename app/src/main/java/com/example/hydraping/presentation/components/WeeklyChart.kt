package com.example.hydraping.presentation.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydraping.domain.model.DailySummary

@Composable
fun WeeklyChart(
    summaries: List<DailySummary>,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) return

    val maxValue = maxOf(summaries.maxOf { it.totalMl }, dailyGoal).toFloat()
    val barColor = MaterialTheme.colorScheme.primary
    val goalLineColor = MaterialTheme.colorScheme.error

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 4.dp)) {
                val barCount = summaries.size
                val spacing = size.width / (barCount * 2f + 1f)
                val barWidth = spacing
                val chartHeight = size.height

                // Goal line
                if (maxValue > 0) {
                    val goalY = chartHeight - (dailyGoal / maxValue) * chartHeight
                    drawLine(
                        color = goalLineColor.copy(alpha = 0.5f),
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 2f
                    )
                }

                // Bars
                summaries.forEachIndexed { index, summary ->
                    val barHeight = if (maxValue > 0) {
                        (summary.totalMl / maxValue) * chartHeight
                    } else 0f
                    val x = spacing * (index * 2 + 1)
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, chartHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            summaries.forEach { summary ->
                Text(
                    text = summary.dayLabel,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

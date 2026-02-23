package com.example.hydraping.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydraping.ui.theme.HydraBlue
import com.example.hydraping.ui.theme.HydraBlueLight
import com.example.hydraping.ui.theme.SuccessGreen
import com.example.hydraping.ui.theme.SuccessGreenLight
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun WaterProgressIndicator(
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (goalMl > 0) (currentMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
    val isGoalMet = progress >= 1f

    // Smooth fill animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 120f),
        label = "progress"
    )

    // Bounce scale on change
    var targetScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(currentMl) {
        if (currentMl > 0) {
            targetScale = 1.06f
            delay(200)
            targetScale = 1f
        }
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "scale"
    )

    // Animated counter
    var displayedPct by remember { mutableIntStateOf(0) }
    val targetPct = (progress * 100).toInt()
    LaunchedEffect(targetPct) {
        val start = displayedPct
        val diff = targetPct - start
        if (diff != 0) {
            val steps = kotlin.math.abs(diff)
            val delayMs = (400L / steps.coerceAtLeast(1)).coerceIn(8L, 40L)
            val direction = if (diff > 0) 1 else -1
            repeat(steps) {
                displayedPct += direction
                delay(delayMs)
            }
        }
    }

    // Wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Glow pulse when goal met
    val glowAlpha by if (isGoalMet) {
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
    } else {
        animateFloatAsState(targetValue = 0f, label = "noGlow")
    }

    val fillGradientTop = if (isGoalMet) SuccessGreenLight else HydraBlueLight
    val fillGradientBottom = if (isGoalMet) SuccessGreen else HydraBlue
    val outlineColor = if (isGoalMet) SuccessGreen else MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .width((180 * scale).dp)
            .height((240 * scale).dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dropPath = createDropPath(size.width, size.height)

            // Glow behind drop when goal met
            if (isGoalMet) {
                drawPath(
                    path = dropPath,
                    color = SuccessGreen.copy(alpha = glowAlpha),
                    style = Stroke(width = 14f)
                )
            }

            // Fill with gradient + wave
            clipPath(dropPath) {
                val fillTop = size.height * (1f - animatedProgress)

                // Gradient fill
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(fillGradientTop, fillGradientBottom),
                        startY = fillTop,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, fillTop),
                    size = Size(size.width, size.height - fillTop)
                )

                // Wave line on the surface
                if (animatedProgress > 0.01f && animatedProgress < 0.99f) {
                    val wavePath = Path().apply {
                        val amplitude = 6f
                        val waveY = fillTop
                        moveTo(0f, waveY)
                        var x = 0f
                        while (x <= size.width) {
                            val y = waveY + sin((x / size.width * 4 * Math.PI + wavePhase).toDouble()).toFloat() * amplitude
                            lineTo(x, y)
                            x += 2f
                        }
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                fillGradientTop.copy(alpha = 0.6f),
                                fillGradientBottom.copy(alpha = 0.3f)
                            ),
                            startY = fillTop,
                            endY = size.height
                        )
                    )
                }
            }

            // Outline stroke
            drawPath(
                path = dropPath,
                color = outlineColor,
                style = Stroke(width = 2.5f)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$displayedPct%",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "${currentMl}ml / ${goalMl}ml",
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.55f)
            )
        }
    }
}

private fun DrawScope.createDropPath(width: Float, height: Float): Path {
    return Path().apply {
        val cx = width / 2f
        val tipY = height * 0.05f
        val bottomY = height * 0.92f
        val radius = width * 0.42f

        moveTo(cx, tipY)

        cubicTo(
            cx - width * 0.02f, height * 0.25f,
            cx - radius * 1.45f, height * 0.45f,
            cx - radius, height * 0.62f
        )
        cubicTo(
            cx - radius * 1.1f, height * 0.78f,
            cx - radius * 0.85f, bottomY,
            cx, bottomY
        )
        cubicTo(
            cx + radius * 0.85f, bottomY,
            cx + radius * 1.1f, height * 0.78f,
            cx + radius, height * 0.62f
        )
        cubicTo(
            cx + radius * 1.45f, height * 0.45f,
            cx + width * 0.02f, height * 0.25f,
            cx, tipY
        )

        close()
    }
}

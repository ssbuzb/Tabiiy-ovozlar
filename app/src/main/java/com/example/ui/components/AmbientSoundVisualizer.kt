package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AmbientSoundVisualizer(
    amplitude: Float,
    accentColor: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_pulse")
    val pulseProg by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_prog"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val centerOffset = center
            val baseRadius = (this.size.minDimension / 2f) * 0.65f

            if (isPlaying) {
                val ampFactor = (0.2f + amplitude * 0.8f) * pulseProg

                // Outer gentle ring
                drawCircle(
                    color = accentColor.copy(alpha = (0.12f * ampFactor).coerceIn(0.04f, 0.28f)),
                    radius = baseRadius * 1.35f * ampFactor,
                    center = centerOffset,
                    style = Stroke(width = 2.5f)
                )

                // Middle gentle ring
                drawCircle(
                    color = accentColor.copy(alpha = (0.22f * ampFactor).coerceIn(0.08f, 0.45f)),
                    radius = baseRadius * 1.12f * ampFactor,
                    center = centerOffset,
                    style = Stroke(width = 3.5f)
                )

                // Inner aura ring
                drawCircle(
                    color = accentColor.copy(alpha = (0.15f * ampFactor).coerceIn(0.05f, 0.35f)),
                    radius = baseRadius * 0.9f * ampFactor,
                    center = centerOffset
                )
            } else {
                // Subtle static halo when paused
                drawCircle(
                    color = accentColor.copy(alpha = 0.08f),
                    radius = baseRadius * 0.95f,
                    center = centerOffset,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

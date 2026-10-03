package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.SoundCatalog
import com.example.data.model.SoundId
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AtmosphericBackground(
    primarySoundId: SoundId,
    isPlaying: Boolean,
    animationsEnabled: Boolean,
    isBatterySaver: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val sound = SoundCatalog.getSound(primarySoundId)

    // Animated color transitions when sound changes
    val animatedPrimary by animateColorAsState(
        targetValue = sound.primaryColor,
        animationSpec = tween(durationMillis = 1200),
        label = "primary_color"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = sound.secondaryColor,
        animationSpec = tween(durationMillis = 1200),
        label = "secondary_color"
    )
    val animatedAccent by animateColorAsState(
        targetValue = sound.accentColor,
        animationSpec = tween(durationMillis = 1200),
        label = "accent_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_anim")
    val timeProg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (primarySoundId == SoundId.HEAVY_RAIN) 2500 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_prog"
    )

    val waveProg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_prog"
    )

    // Particle seeds
    val particles = remember(primarySoundId) {
        val rand = Random(42)
        List(30) {
            ParticleSeed(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                speed = 0.5f + rand.nextFloat() * 1.2f,
                size = 2f + rand.nextFloat() * 4f,
                opacity = 0.2f + rand.nextFloat() * 0.5f
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        animatedPrimary,
                        animatedSecondary,
                        Color(0xFF060D0C)
                    )
                )
            )
    ) {
        if (animationsEnabled && !isBatterySaver) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                when (primarySoundId) {
                    SoundId.GENTLE_RAIN, SoundId.HEAVY_RAIN, SoundId.RAIN_WINDOW -> {
                        // Falling rain streaks
                        val isHeavy = primarySoundId == SoundId.HEAVY_RAIN
                        val rainColor = animatedAccent.copy(alpha = if (isHeavy) 0.35f else 0.22f)
                        val rainLength = if (isHeavy) 45f else 25f
                        val rainWidth = if (isHeavy) 2.2f else 1.4f

                        particles.forEach { p ->
                            val currentY = ((p.y + timeProg * (p.speed * if (isHeavy) 2.2f else 1.3f)) % 1f) * canvasHeight
                            val currentX = (p.x * canvasWidth + sin((timeProg * 2 * PI + p.y * 10).toDouble()).toFloat() * 12f)
                            drawLine(
                                color = rainColor,
                                start = Offset(currentX, currentY),
                                end = Offset(currentX - 5f, currentY + rainLength),
                                strokeWidth = rainWidth
                            )
                        }
                    }

                    SoundId.OCEAN, SoundId.RIVER -> {
                        // Gentle rolling waves
                        val waveColor1 = animatedAccent.copy(alpha = 0.12f)
                        val waveColor2 = animatedAccent.copy(alpha = 0.08f)

                        val path1 = Path()
                        val path2 = Path()

                        val baseHeight1 = canvasHeight * 0.72f
                        val baseHeight2 = canvasHeight * 0.82f

                        path1.moveTo(0f, canvasHeight)
                        path1.lineTo(0f, baseHeight1)
                        path2.moveTo(0f, canvasHeight)
                        path2.lineTo(0f, baseHeight2)

                        val waveSegments = 20
                        val step = canvasWidth / waveSegments
                        for (i in 0..waveSegments) {
                            val x = i * step
                            val y1 = baseHeight1 + sin((x * 0.015f + waveProg).toDouble()).toFloat() * 25f
                            val y2 = baseHeight2 + sin((x * 0.018f - waveProg * 0.8f).toDouble()).toFloat() * 18f
                            path1.lineTo(x, y1)
                            path2.lineTo(x, y2)
                        }
                        path1.lineTo(canvasWidth, canvasHeight)
                        path1.close()
                        path2.lineTo(canvasWidth, canvasHeight)
                        path2.close()

                        drawPath(path1, waveColor1)
                        drawPath(path2, waveColor2)
                    }

                    SoundId.FIREPLACE -> {
                        // Rising warm glowing ember sparks
                        particles.take(24).forEach { p ->
                            val currentY = canvasHeight - (((p.y + timeProg * p.speed) % 1f) * (canvasHeight * 0.65f))
                            val currentX = p.x * canvasWidth + sin((timeProg * 4 * PI + p.y * 5).toDouble()).toFloat() * 20f
                            val fade = (1f - (canvasHeight - currentY) / (canvasHeight * 0.65f)).coerceIn(0f, 1f)
                            drawCircle(
                                color = animatedAccent.copy(alpha = p.opacity * fade),
                                radius = p.size * fade,
                                center = Offset(currentX, currentY)
                            )
                        }
                    }

                    SoundId.NIGHT_NATURE -> {
                        // Twinkling stars and fireflies
                        particles.forEach { p ->
                            val twinkle = (sin((timeProg * 2 * PI * p.speed + p.x * 20).toDouble()).toFloat() * 0.5f + 0.5f)
                            val currentX = p.x * canvasWidth
                            val currentY = p.y * (canvasHeight * 0.75f)
                            drawCircle(
                                color = animatedAccent.copy(alpha = p.opacity * twinkle),
                                radius = p.size * (0.6f + twinkle * 0.5f),
                                center = Offset(currentX, currentY)
                            )
                        }
                    }

                    else -> {
                        // Ambient floating light spores/leaves
                        particles.take(18).forEach { p ->
                            val currentY = ((p.y + timeProg * p.speed * 0.6f) % 1f) * canvasHeight
                            val currentX = p.x * canvasWidth + sin((timeProg * 2 * PI + p.y * 6).toDouble()).toFloat() * 30f
                            drawCircle(
                                color = animatedAccent.copy(alpha = p.opacity * 0.4f),
                                radius = p.size * 1.5f,
                                center = Offset(currentX, currentY)
                            )
                        }
                    }
                }
            }
        }

        content()
    }
}

private data class ParticleSeed(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val opacity: Float
)

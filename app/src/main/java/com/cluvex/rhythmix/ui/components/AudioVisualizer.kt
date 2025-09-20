package com.cluvex.rhythmix.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    barCount: Int = 64
) {
    val density = LocalDensity.current
    
    val infiniteTransition = rememberInfiniteTransition()
    
    val animatedValues = (0 until barCount).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (800 + index * 50),
                    easing = EaseInOutSine
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Canvas(
        modifier = modifier
    ) {
        if (isPlaying) {
            drawAudioBars(
                animatedValues = animatedValues.map { it.value },
                color = color,
                barCount = barCount
            )
        }
    }
}

private fun DrawScope.drawAudioBars(
    animatedValues: List<Float>,
    color: Color,
    barCount: Int
) {
    val barWidth = size.width / barCount
    val maxBarHeight = size.height * 0.8f
    
    for (i in 0 until barCount) {
        val animatedValue = animatedValues[i]
        val normalizedValue = sin(animatedValue * PI).toFloat()
        val barHeight = maxBarHeight * (0.1f + normalizedValue * 0.9f)
        
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.9f),
                color.copy(alpha = 0.3f)
            ),
            startY = size.height - barHeight,
            endY = size.height
        )
        
        drawRect(
            brush = gradient,
            topLeft = androidx.compose.ui.geometry.Offset(
                x = i * barWidth + barWidth * 0.1f,
                y = size.height - barHeight
            ),
            size = androidx.compose.ui.geometry.Size(
                width = barWidth * 0.8f,
                height = barHeight
            )
        )
    }
}

@Composable
fun CircularWaveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    waveCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val waves = (0 until waveCount).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (3000 + index * 500),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(
        modifier = modifier
    ) {
        if (isPlaying) {
            drawCircularWaves(
                waves = waves.map { it.value },
                color = color,
                waveCount = waveCount
            )
        }
    }
}

private fun DrawScope.drawCircularWaves(
    waves: List<Float>,
    color: Color,
    waveCount: Int
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = minOf(size.width, size.height) / 2 * 0.8f
    
    waves.forEachIndexed { index, wave ->
        val radius = maxRadius * (0.3f + 0.7f * (index + 1f) / waveCount)
        val alpha = 1f - (index.toFloat() / waveCount) * 0.7f
        
        val path = Path()
        val points = 60
        
        for (i in 0..points) {
            val angle = (i.toFloat() / points) * 2 * PI
            val waveAmplitude = 20f * sin(wave + angle * 4).toFloat()
            val currentRadius = radius + waveAmplitude
            
            val x = centerX + currentRadius * cos(angle).toFloat()
            val y = centerY + currentRadius * sin(angle).toFloat()
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        
        drawPath(
            path = path,
            color = color.copy(alpha = alpha * 0.6f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun ParticleVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    particleCount: Int = 50
) {
    val particles = remember {
        List(particleCount) { Particle() }
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            particles.forEach { it.reset() }
        }
    }

    Canvas(
        modifier = modifier
    ) {
        if (isPlaying) {
            drawParticles(
                particles = particles,
                time = time,
                color = color
            )
        }
    }
}

private fun DrawScope.drawParticles(
    particles: List<Particle>,
    time: Float,
    color: Color
) {
    particles.forEach { particle ->
        particle.update(time, size.width, size.height)
        
        val alpha = (1f - particle.life).coerceIn(0f, 1f)
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = particle.size,
            center = androidx.compose.ui.geometry.Offset(particle.x, particle.y)
        )
    }
}

private class Particle {
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var life: Float = 0f
    var size: Float = 0f
    
    fun reset() {
        x = Random.nextFloat()
        y = Random.nextFloat()
        vx = (Random.nextFloat() - 0.5f) * 2f
        vy = (Random.nextFloat() - 0.5f) * 2f
        life = 0f
        size = Random.nextFloat() * 8f + 2f
    }
    
    fun update(deltaTime: Float, width: Float, height: Float) {
        life += deltaTime * 0.02f
        
        if (life > 1f) {
            reset()
        }
        
        x = (x + vx * deltaTime * 0.1f) * width
        y = (y + vy * deltaTime * 0.1f) * height
        
        if (x < 0 || x > width || y < 0 || y > height) {
            reset()
        }
    }
}

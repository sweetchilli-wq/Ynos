package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

enum class PspThemeColor(
    val title: String,
    val bgStart: Color,
    val bgEnd: Color,
    val waveColor1: Color,
    val waveColor2: Color,
    val waveColor3: Color,
    val accentColor: Color
) {
    SLATE_GRAY(
        "Cosmic Slate",
        Color(0xFF141619), Color(0xFF242830),
        Color(0x228B9CB4), Color(0x11A0B2C6), Color(0x1873839B),
        Color(0xFF8B9CB4)
    ),
    CLASSIC_BLUE(
        "PSP Blue",
        Color(0xFF001A3F), Color(0xFF003366),
        Color(0x333399FF), Color(0x1A66CCFF), Color(0x260055AA),
        Color(0xFF00BFFF)
    ),
    CHERRY_PINK(
        "Sakura Pink",
        Color(0xFF2F0F1B), Color(0xFF4C1C30),
        Color(0x33FF66A3), Color(0x1AFF99C2), Color(0x269E2A5D),
        Color(0xFFFF3385)
    ),
    FOREST_GREEN(
        "Sage Green",
        Color(0xFF0F2615), Color(0xFF1B3D25),
        Color(0x3344DD66), Color(0x1A66FF88), Color(0x261A5C2D),
        Color(0xFF4EEF74)
    ),
    CRIMSON_RED(
        "Crimson Velvet",
        Color(0xFF2B0909), Color(0xFF481414),
        Color(0x33FF4D4D), Color(0x1AFF8080), Color(0x269C1E1E),
        Color(0xFFFF3333)
    ),
    ROYAL_AMBER(
        "Golden Amber",
        Color(0xFF281C06), Color(0xFF42300B),
        Color(0x33FFB833), Color(0x1AFFE066), Color(0x26946006),
        Color(0xFFFFCC00)
    )
}

@Composable
fun XmbBackground(
    themeColor: PspThemeColor = PspThemeColor.SLATE_GRAY,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bgStartAnim by animateColorAsState(themeColor.bgStart, animationSpec = tween(1000), label = "bgStart")
    val bgEndAnim by animateColorAsState(themeColor.bgEnd, animationSpec = tween(1000), label = "bgEnd")
    
    val infiniteTransition = rememberInfiniteTransition(label = "xmbWave")

    // Phase animations to move waves over time
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (-2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgStartAnim, bgEndAnim)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height * 0.55f // Draw across middle-lower section

            // Define waves parameters: (amplitude, frequency, phase shift, color)
            // Layer 1: Slow, large primary wave
            drawXmbWave(
                width = width,
                centerY = centerY,
                amplitude = height * 0.12f,
                frequency = 0.003f,
                phase = phase1,
                waveColor = themeColor.waveColor1,
                strokeWidth = 3.5f
            )

            // Layer 2: Faster, medium secondary wave
            drawXmbWave(
                width = width,
                centerY = centerY * 0.9f,
                amplitude = height * 0.08f,
                frequency = 0.005f,
                phase = phase2,
                waveColor = themeColor.waveColor2,
                strokeWidth = 2.0f
            )

            // Layer 3: Subtle bottom thick ribbon
            drawXmbWave(
                width = width,
                centerY = centerY * 1.1f,
                amplitude = height * 0.15f,
                frequency = 0.002f,
                phase = phase3,
                waveColor = themeColor.waveColor3,
                strokeWidth = 14f,
                alpha = 0.6f
            )
        }
        
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawXmbWave(
    width: Float,
    centerY: Float,
    amplitude: Float,
    frequency: Float,
    phase: Float,
    waveColor: Color,
    strokeWidth: Float,
    alpha: Float = 1.0f
) {
    val path = Path()
    val step = 10f // draw points every 10 pixels

    var x = 0f
    var y = centerY + amplitude * sin(frequency * x + phase)
    path.moveTo(x, y)

    x = step
    while (x <= width) {
        y = centerY + amplitude * sin(frequency * x + phase)
        path.lineTo(x, y)
        x += step
    }

    drawPath(
        path = path,
        color = waveColor,
        alpha = alpha,
        style = Stroke(width = strokeWidth)
    )
}

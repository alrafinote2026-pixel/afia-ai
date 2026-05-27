package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ListeningState
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SciFiGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonBlue.copy(alpha = 0.3f),
    glowColor: Color = NeonBlue.copy(alpha = 0.15f),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CyberCardBgTranslucent)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .drawBehind {
                // Subtle neon border backing glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width
                    ),
                    alpha = 0.2f
                )
            }
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun HolographicAiOrb(
    state: ListeningState,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 180.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_rotation")

    // Rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing core scale based on listening states
    val targetScale = when (state) {
        ListeningState.LISTENING -> 1.25f
        ListeningState.RECORDING -> 1.45f
        ListeningState.PROCESSING -> 1.10f
        ListeningState.IDLE -> 0.95f
    }
    
    val pulsingScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    // Secondary breath animation
    val breathPct by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = SineIntensityEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    val currentFinalScale = pulsingScale * breathPct

    Box(
        modifier = modifier
            .size(sizeDp)
            .drawBehind {
                // Giant outer planetary ring back glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            when(state) {
                                ListeningState.LISTENING -> NeonPurple.copy(alpha = 0.25f)
                                ListeningState.RECORDING -> NeonPink.copy(alpha = 0.35f)
                                ListeningState.PROCESSING -> NeonGreen.copy(alpha = 0.25f)
                                ListeningState.IDLE -> NeonBlue.copy(alpha = 0.15f)
                            },
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 1.5f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension / 3.3f * currentFinalScale

            // Draw Layer 1: Core glowing energy singularity
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        when(state) {
                            ListeningState.LISTENING -> NeonPurple
                            ListeningState.RECORDING -> NeonPink
                            ListeningState.PROCESSING -> NeonGreen
                            ListeningState.IDLE -> NeonBlue
                        }.copy(alpha = 0.45f),
                        Color.Transparent
                    )
                ),
                radius = baseRadius * 0.75f,
                center = center
            )

            // Draw Layer 2: Rotating Cybernetic Orbital Ring 1
            drawCircle(
                color = NeonBlue.copy(alpha = 0.35f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw Layer 3: Concentric sci-fi tick marks
            val tickCount = 20
            for (i in 0 until tickCount) {
                val angleRad = Math.toRadians((360f / tickCount * i + rotationAngle).toDouble())
                val startDist = baseRadius + 4.dp.toPx()
                val endDist = baseRadius + 12.dp.toPx()
                val p1 = Offset(
                    (center.x + startDist * cos(angleRad)).toFloat(),
                    (center.y + startDist * sin(angleRad)).toFloat()
                )
                val p2 = Offset(
                    (center.x + endDist * cos(angleRad)).toFloat(),
                    (center.y + endDist * sin(angleRad)).toFloat()
                )
                
                val tickColor = when(state) {
                    ListeningState.LISTENING -> NeonPurple
                    ListeningState.RECORDING -> NeonPink
                    ListeningState.PROCESSING -> NeonGreen
                    ListeningState.IDLE -> NeonBlue
                }
                
                drawLine(
                    color = tickColor.copy(alpha = if (i % 2 == 0) 0.8f else 0.4f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (i % 2 == 0) 3.dp.toPx() else 1.5f.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw Layer 4: Orbital Satellite node
            val satDistance = baseRadius * 1.35f
            val satAngleRad = Math.toRadians(rotationAngle.toDouble() * 2.1)
            val satCenter = Offset(
                (center.x + satDistance * cos(satAngleRad)).toFloat(),
                (center.y + satDistance * sin(satAngleRad)).toFloat()
            )
            drawCircle(
                color = NeonPurple,
                radius = 6.dp.toPx(),
                center = satCenter
            )
            // Sat glow ring
            drawCircle(
                color = NeonPurple.copy(alpha = 0.4f),
                radius = 12.dp.toPx(),
                center = satCenter,
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw Layer 5: Concentric Digital Core line
            drawCircle(
                color = NeonPurple.copy(alpha = 0.2f),
                radius = baseRadius * 0.5f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Inner core orb
            drawCircle(
                color = TextPrimary,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // Concentric core text label for futuristic boot feeling
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when(state) {
                    ListeningState.LISTENING -> "LISTENING"
                    ListeningState.RECORDING -> "RECORDING"
                    ListeningState.PROCESSING -> "PROCESSING"
                    ListeningState.IDLE -> "AFIA CORE"
                },
                color = when(state) {
                    ListeningState.LISTENING -> NeonPurple
                    ListeningState.RECORDING -> NeonPink
                    ListeningState.PROCESSING -> NeonGreen
                    ListeningState.IDLE -> NeonBlue
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "ONLINE",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

val SineIntensityEasing = Easing { fraction ->
    fraction // Linear map supporting custom sin loops easily
}

@Composable
fun HolographicDiagnosticDial(
    label: String,
    valueString: String,
    progress: Float,
    glowColor: Color = NeonBlue,
    modifier: Modifier = Modifier
) {
    val animateProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "dial_anim"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2.3f

                // Inactive sector background ring
                drawCircle(
                    color = Color(0xFF1E1A34),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Active glowing ring segment
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(glowColor.copy(alpha = 0.3f), glowColor, glowColor.copy(alpha = 0.1f))
                    ),
                    startAngle = -90f,
                    sweepAngle = animateProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Outer border alignment ring tick marks (simplified)
                drawCircle(
                    color = glowColor.copy(alpha = 0.15f),
                    radius = radius + 6.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            Text(
                text = valueString,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label.uppercase(),
            color = TextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun OscillatingWaveform(
    state: ListeningState,
    modifier: Modifier = Modifier,
    lineCount: Int = 24
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_oscillation")

    val phases = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(1200 + index * 400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase_$index"
        )
    }

    val waveMultiplier = when(state) {
        ListeningState.LISTENING -> 0.7f
        ListeningState.RECORDING -> 1.4f
        ListeningState.PROCESSING -> 0.4f
        ListeningState.IDLE -> 0.15f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        // Draw multiple layered waves
        for (w in 0 until 3) {
            val path = Path()
            path.moveTo(0f, centerY)

            val sliceWidth = width / lineCount
            for (i in 0..lineCount) {
                val x = i * sliceWidth
                val phase = phases[w].value
                val angle = (i.toFloat() / lineCount * 2 * Math.PI * 2.2).toFloat() + phase
                // Envelope shape so waves flatten at the screen edges (left & right)
                val envelope = sin((i.toFloat() / lineCount) * Math.PI).toFloat()

                val yOffset = sin(angle.toDouble()).toFloat() * 15.dp.toPx() * waveMultiplier * envelope
                val y = centerY + yOffset
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            val waveColor = when (w) {
                0 -> NeonBlue
                1 -> NeonPurple
                else -> NeonPink
            }

            drawPath(
                path = path,
                color = waveColor.copy(alpha = if (w == 0) 0.8f else if (w == 1) 0.45f else 0.25f),
                style = Stroke(
                    width = if (w == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

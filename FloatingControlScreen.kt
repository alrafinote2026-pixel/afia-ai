package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.ListeningState
import com.example.ui.theme.*
import com.example.ui.viewmodel.AfiaViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingControlScreen(
    viewModel: AfiaViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val isSynthActive by viewModel.isSynthRunning.collectAsStateWithLifecycle()

    var isHudBubbleEnabled by remember { mutableStateOf(true) }
    var hudOffset by remember { mutableStateOf(Offset(50f, 320f)) }
    var isHudPanelOpen by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Header ---
            Column {
                Text(
                    text = "FLOATING AI COUPLING",
                    color = NeonBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "OVERLAY BUBBLE DEPLOYMENT ENGINE",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            SciFiGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SYSTEM HUD EMULATOR STATUS",
                    color = NeonBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "The overlay controller activates an interactive floating AI bubble HUD inside the application namespace. Drag it left or right, then tap it to deploy the physical quick access action dial without leaving the screen.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DEPLOY FLOATING HUD ORB",
                        color = if (isHudBubbleEnabled) NeonGreen else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Switch(
                        checked = isHudBubbleEnabled,
                        onCheckedChange = {
                            isHudBubbleEnabled = it
                            viewModel.addLog("Overlay HUD configuration: ${if (it) "ENABLED" else "SUSPENDED"}")
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonGreen,
                            uncheckedTrackColor = Color(0xFF161326)
                        )
                    )
                }
            }

            // Real System Permission Info
            SciFiGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonPink.copy(alpha = 0.25f),
                glowColor = NeonPink.copy(alpha = 0.04f)
            ) {
                Text(
                    text = "TUTORIAL // SYSTEM OVERLAYS",
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To draw actual floating widgets on top of outer system applications (e.g. Android Home screen or Chrome), go to Settings & permit 'Draw over other apps'. Tap below to initiate hardware links.",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val reply = viewModel.deviceController.openSettings()
                        viewModel.addLog("Overlay permission redirect issued.")
                        viewModel.speak("Redirecting to system permission arrays.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("AUTHORIZE DEVICE OVERLAYS", color = CyberBlack, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            // Diagnostic indicators
            SciFiGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                borderColor = NeonPurple.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "OVERLAY COORDINATES MONITOR",
                    color = NeonPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CORE VECTOR X: ${hudOffset.x.roundToInt()}\n" +
                            "CORE VECTOR Y: ${hudOffset.y.roundToInt()}\n" +
                            "WAVE FREQUENCY: Mapped 44.1Khz\n" +
                            "ACTIVE OVERLAY PANELS: ${if (isHudPanelOpen) "OPEN (MODAL LINK)" else "CLOSED"}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
            }
        }

        // --- THE ACTUAL FLOATING INTERACTIVE GESTURE BUBBLE HUD ---
        if (isHudBubbleEnabled) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(hudOffset.x.roundToInt(), hudOffset.y.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            hudOffset = Offset(
                                x = (hudOffset.x + dragAmount.x).coerceIn(0f, 750f),
                                y = (hudOffset.y + dragAmount.y).coerceIn(0f, 1300f)
                            )
                        }
                    }
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(NeonBlue.copy(alpha = 1f), NeonPurple.copy(alpha = 0.6f))
                        )
                    )
                    .border(
                        2.dp * pulseScale,
                        Brush.verticalGradient(
                            colors = listOf(Color.White, NeonBlue)
                        ),
                        CircleShape
                    )
                    .drawBehind {
                        drawCircle(
                            color = NeonBlue.copy(alpha = 0.35f),
                            radius = size.minDimension / 1.7f * pulseScale
                        )
                    }
                    .clickable {
                        isHudPanelOpen = !isHudPanelOpen
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Adjust,
                    contentDescription = "Floating Hub Menu",
                    tint = CyberBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // --- EXPANDED CONCENTRIC GLASS ACTION QUICK DIAL PANEL ---
        AnimatedVisibility(
            visible = isHudPanelOpen && isHudBubbleEnabled,
            enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.7f),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.7f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            SciFiGlassCard(
                modifier = Modifier
                    .width(280.dp)
                    .wrapContentHeight(),
                borderColor = NeonBlue,
                glowColor = NeonBlue.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AFIA OVERLAY HUD Core",
                        color = NeonBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = { isHudPanelOpen = false },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = NeonPink)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close HUD", modifier = Modifier.size(16.dp))
                    }
                }
                Divider(color = Color(0xFF19172B), modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick flashlight
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0A19))
                            .clickable {
                                val log = viewModel.deviceController.setFlashlight(true)
                                viewModel.addLog(log)
                                viewModel.speak(log)
                                isHudPanelOpen = false
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.FlashlightOn, contentDescription = "Light", tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("FLASHLIGHT ON", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    // Quick micro input trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0A19))
                            .clickable {
                                isHudPanelOpen = false
                                viewModel.startVoiceListening()
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic", tint = NeonPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("ENGAGE MICROPHONE", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    // Quick synth hum toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0A19))
                            .clickable {
                                viewModel.toggleCyberSynth()
                                isHudPanelOpen = false
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSynthActive) Icons.Default.MusicOff else Icons.Default.MusicNote,
                            contentDescription = "Synth",
                            tint = NeonPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(if (isSynthActive) "SUSPEND SYNTH HUM" else "TRIGGER CYBER SYNTH", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    // System Battery Diagnosis
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0A19))
                            .clickable {
                                val batteryReport = viewModel.deviceController.getBatteryStatus()
                                viewModel.addLog(batteryReport)
                                viewModel.speak(batteryReport)
                                isHudPanelOpen = false
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Power, contentDescription = "Power", tint = NeonBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("POWER CELL TELETEMETRY", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

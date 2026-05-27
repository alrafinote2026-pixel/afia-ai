package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.ListeningState
import com.example.ui.theme.*
import com.example.ui.viewmodel.AfiaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: AfiaViewModel,
    modifier: Modifier = Modifier
) {
    val listeningState by viewModel.listeningState.collectAsStateWithLifecycle()
    val continuousListening by viewModel.continuousListening.collectAsStateWithLifecycle()
    val ttsEnabled by viewModel.textToSpeechEnabled.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val isSynthPlaying by viewModel.isSynthRunning.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val ram = remember { viewModel.deviceController.getRamDiagnostics() }
    val storage = remember { viewModel.deviceController.getStorageDiagnostics() }
    val batteryPct = viewModel.deviceController.getBatteryPercent()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // --- 1. Top Cyber Shield Banner ---
        item {
            SciFiGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "AFIA INTEL OS",
                            color = NeonBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "SECURE GRID LINK // PROTOCOL 2099 V1.28",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (listeningState != ListeningState.IDLE) NeonPink else NeonGreen)
                    )
                }
            }
        }

        // --- 2. Interactive Holographic AI Orb Bridge ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HolographicAiOrb(
                        state = listeningState,
                        sizeDp = 180.dp,
                        modifier = Modifier.clickable {
                            if (listeningState == ListeningState.IDLE) {
                                viewModel.addLog("Voice recognition core queried manually via central orb.")
                                viewModel.startVoiceListening()
                            } else {
                                viewModel.stopVoiceListening()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "TAP CORE ORB TO TRANSMIT COMMANDS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- 3. Circular Hardware Diagnostics Matrix ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HolographicDiagnosticDial(
                    label = "Power",
                    valueString = "$batteryPct%",
                    progress = batteryPct / 100f,
                    glowColor = if (batteryPct > 40) NeonGreen else NeonPink
                )

                val ramPct = ram.usedGB.toFloat() / ram.totalGB.toFloat()
                HolographicDiagnosticDial(
                    label = "RAM Load",
                    valueString = "${ram.usedGB}G/${ram.totalGB}G",
                    progress = ramPct,
                    glowColor = NeonPurple
                )

                val spacePct = storage.usedGB.toFloat() / storage.totalGB.toFloat()
                HolographicDiagnosticDial(
                    label = "Disk load",
                    valueString = "${storage.usedGB}G",
                    progress = spacePct,
                    glowColor = NeonBlue
                )
            }
        }

        // --- 4. Tactical Fast Controls Terminal Capsule Grid ---
        item {
            SciFiGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HARDWARE ACTUATOR REGISTRY",
                    color = NeonBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Flashlight toggle
                    var isFlashlightOn by remember { mutableStateOf(false) }
                    AssistChip(
                        onClick = {
                            isFlashlightOn = !isFlashlightOn
                            val res = viewModel.deviceController.setFlashlight(isFlashlightOn)
                            viewModel.addLog(res)
                            viewModel.speak(res)
                        },
                        label = { Text("PHOTON EMITTER", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = "Flashlight",
                                tint = if (isFlashlightOn) NeonGreen else NeonPink,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = CyberCardBg)
                    )

                    // Cyber beats Synth Toggle
                    AssistChip(
                        onClick = { viewModel.toggleCyberSynth() },
                        label = { Text("CYBER BEATS", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isSynthPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = "Synth",
                                tint = if (isSynthPlaying) NeonGreen else NeonPink,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = CyberCardBg)
                    )

                    // Voice Output toggle
                    AssistChip(
                        onClick = { viewModel.toggleTTS() },
                        label = { Text("VOX VOCALIZER", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "TTS",
                                tint = if (ttsEnabled) NeonBlue else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = CyberCardBg)
                    )

                    // Continuous trigger
                    AssistChip(
                        onClick = { viewModel.toggleContinuousListening() },
                        label = { Text("WAKE WORD (HEY AFIA)", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (continuousListening) Icons.Default.Hearing else Icons.Default.HearingDisabled,
                                contentDescription = "Continuous",
                                tint = if (continuousListening) NeonPurple else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = CyberCardBg)
                    )
                }
            }
        }

        // --- 5. Real-Time OS Process Console Logger ---
        item {
            SciFiGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                borderColor = NeonPurple.copy(alpha = 0.25f),
                glowColor = NeonPurple.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE TELEMETRY INTERRUPT LOGGER",
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "SECUREGID_A_V03",
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            Text(
                                text = log,
                                color = if (log.contains("Anomaly") || log.contains("Error")) NeonPink else if (log.contains("ACTIVE") || log.contains("ONLINE") || log.contains("successful")) NeonGreen else TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

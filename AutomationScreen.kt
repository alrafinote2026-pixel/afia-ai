package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AutomationTask
import com.example.data.CustomShortcut
import com.example.ui.theme.*
import com.example.ui.viewmodel.AfiaViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: AfiaViewModel,
    modifier: Modifier = Modifier
) {
    val automationTasks by viewModel.automationTasks.collectAsStateWithLifecycle()
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()

    // Creation States for Custom Shortcuts
    var showCreator by remember { mutableStateOf(false) }
    var phraseInput by remember { mutableStateOf("") }
    var actionTypeSelected by remember { mutableStateOf("SAY_TEXT") }
    var actionArgInput by remember { mutableStateOf("") }

    val actionTypes = listOf(
        Pair("SAY_TEXT", "Vocal announcement"),
        Pair("FLASHLIGHT", "Flashlight state"),
        Pair("VOLUME_MAX", "Maximize device volume"),
        Pair("SYSTEM_INFO", "Device health readout"),
        Pair("OPEN_APP", "Deploy URL/App Settings")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // --- Tab Heading ---
        item {
            Column {
                Text(
                    text = "AUTOMATION MATRIX REACTOR",
                    color = NeonBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "PRESET SYSTEM TASKS & SPEECH ACTION DECODER",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- SECTION 1: Core System Automations presets ---
        item {
            Text(
                text = "Preset Routines",
                color = NeonBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        items(automationTasks) { task ->
            SciFiGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (task.isEnabled) NeonGreen.copy(alpha = 0.4f) else NeonBlue.copy(alpha = 0.15f),
                glowColor = if (task.isEnabled) NeonGreen.copy(alpha = 0.08f) else NeonBlue.copy(alpha = 0.02f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (task.isEnabled) NeonGreen.copy(alpha = 0.15f) else Color(0xFF13111E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when(task.taskType) {
                                "STUDY_MODE" -> Icons.Default.Book
                                "SLEEP_MODE" -> Icons.Default.Bedtime
                                "BATTERY_SAVER" -> Icons.Default.Power
                                "MORNING" -> Icons.Default.LightMode
                                else -> Icons.Default.Settings
                            },
                            contentDescription = task.name,
                            tint = if (task.isEnabled) NeonGreen else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.name,
                            color = if (task.isEnabled) NeonGreen else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = task.description,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 12.sp
                        )
                    }
                    Switch(
                        checked = task.isEnabled,
                        onCheckedChange = {
                            viewModel.resetAutomationTask(task)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Color(0xFF1E1A34)
                        )
                    )
                }
            }
        }

        // --- SECTION 2: Custom Voice Controls creator ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom User Commands",
                    color = NeonPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Button(
                    onClick = { showCreator = !showCreator },
                    colors = ButtonDefaults.buttonColors(containerColor = if (showCreator) Color(0xFF1D1B2D) else NeonPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (showCreator) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add shortcut",
                        tint = if (showCreator) NeonPink else CyberBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showCreator) "CANCEL" else "CREATE",
                        color = if (showCreator) NeonPink else CyberBlack,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Expanded Creation Panel
        if (showCreator) {
            item {
                SciFiGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonPurple.copy(alpha = 0.4f),
                    glowColor = NeonPurple.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "NEW SPEECH COMMAND TRANSLATION",
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Phrase Textfield
                    Text(
                        text = "SPOKEN KEYWORD / PHRASE",
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = phraseInput,
                        onValueChange = { phraseInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. initiate full drive", fontSize = 11.sp, color = TextMuted) },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = Color(0xFF272144),
                            focusedContainerColor = Color(0xFF070511),
                            unfocusedContainerColor = Color(0xFF070511)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Action classification dropdown (simulated in row for compilation safety)
                    Text(
                        text = "HARDWARE EXECUTION ROUTINE",
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        actionTypes.forEach { (type, label) ->
                            FilterChip(
                                selected = actionTypeSelected == type,
                                onClick = { actionTypeSelected = type },
                                label = { Text(type, fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple,
                                    containerColor = Color(0xFF120E22)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Parameter / Speech response arg
                    Text(
                        text = when(actionTypeSelected) {
                            "SAY_TEXT" -> "WHAT AFIA SAYS IN RESPONSE"
                            "FLASHLIGHT" -> "TOGGLE STATE ('ON' OR 'OFF')"
                            "VOLUME_MAX" -> "No arguments needed"
                            "SYSTEM_INFO" -> "No arguments needed"
                            "OPEN_APP" -> "APP TARGET ('youtube', 'camera' or url)"
                            else -> "ARGUMENT VALUE"
                        },
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (actionTypeSelected != "VOLUME_MAX" && actionTypeSelected != "SYSTEM_INFO") {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = actionArgInput,
                            onValueChange = { actionArgInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter payload value...", fontSize = 11.sp, color = TextMuted) },
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = Color(0xFF272144),
                                focusedContainerColor = Color(0xFF070511),
                                unfocusedContainerColor = Color(0xFF070511)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (phraseInput.isNotBlank()) {
                                viewModel.addShortcut(
                                    phrase = phraseInput.trim(),
                                    actionType = actionTypeSelected,
                                    actionArg = actionArgInput.trim()
                                )
                                // success reset
                                phraseInput = ""
                                actionArgInput = ""
                                showCreator = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SAVE INTERRUPT CODE", color = CyberBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Current Active List
        if (shortcuts.isEmpty()) {
            item {
                SciFiGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NO LOCAL COMMAND INTERRUPTS REGISTERED",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            }
        } else {
            items(shortcuts) { shortcut ->
                SciFiGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonPurple.copy(alpha = 0.2f),
                    glowColor = NeonPurple.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\"${shortcut.phrase}\"",
                                color = NeonPurple,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Action: ${shortcut.actionType} " + (if (shortcut.actionArg.isNotEmpty()) "(Payload: ${shortcut.actionArg})" else ""),
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { viewModel.deleteShortcutItem(shortcut) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = NeonPink)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete shortcut", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

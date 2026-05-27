package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.ListeningState
import com.example.ui.theme.*
import com.example.ui.viewmodel.AfiaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(
    viewModel: AfiaViewModel,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val listeningState by viewModel.listeningState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var textInput by remember { mutableStateOf("") }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- 1. Conversation Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEURAL DIALOGUE STREAM",
                    color = NeonBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "GEMINI-3.5-FLASH COGNITUDE SYSTEM",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (chatHistory.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearChat() },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = NeonPink)
                ) {
                    Icon(imageVector = Icons.Default.ClearAll, contentDescription = "Purge logs")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. Interactive Message Log View ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(CyberCardBgTranslucent)
                .border(1.dp, Color(0xFF19172B), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (chatHistory.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF141224)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic Ready",
                            tint = NeonPurple.copy(alpha = 0.5f),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "COMMUNICATION LINK FLUID",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Speak to initialize cyber conversation subroutines. \"Ask: What load is my battery?\" or \"Launch Youtube.\"",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatHistory) { msg ->
                        val isUser = msg.sender == "user"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = if (isUser) "COMMANDER" else "AFIA AI",
                                color = if (isUser) NeonBlue else NeonPurple,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 2.dp,
                                            bottomEnd = if (isUser) 2.dp else 12.dp
                                        )
                                    )
                                    .background(if (isUser) Color(0xFF121424) else Color(0xFF20132B))
                                    .border(
                                        1.dp,
                                        if (isUser) NeonBlue.copy(alpha = 0.15f) else NeonPurple.copy(alpha = 0.15f),
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 2.dp,
                                            bottomEnd = if (isUser) 2.dp else 12.dp
                                        )
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.content,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = android.text.format.DateFormat.format("HH:mm:ss", msg.timestamp).toString(),
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 3. Animated Acoustic Waveform Visualization ---
        OscillatingWaveform(state = listeningState)

        Spacer(modifier = Modifier.height(8.dp))

        // --- 4. Glowing Transmitter Bottom Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Text Entry
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("TRANSPOSE COMMAND DATA...", fontSize = 11.sp, color = TextMuted) },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color(0xFF1E1C33),
                    focusedContainerColor = Color(0xFF04030A),
                    unfocusedContainerColor = Color(0xFF04030A)
                ),
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.sendMessageToAfia(textInput)
                            textInput = ""
                            keyboardController?.hide()
                        }) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send text", tint = NeonBlue)
                        }
                    }
                }
            )

            // Dynamic microphone buttons (active state toggle)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (listeningState != ListeningState.IDLE) NeonPink else NeonPurple
                    )
                    .clickable {
                        if (listeningState == ListeningState.IDLE) {
                            viewModel.startVoiceListening()
                        } else {
                            viewModel.stopVoiceListening()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (listeningState != ListeningState.IDLE) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Voice prompt",
                    tint = CyberBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

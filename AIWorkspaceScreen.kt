package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WorkspaceNote
import com.example.ui.theme.*
import com.example.ui.viewmodel.AfiaViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AIWorkspaceScreen(
    viewModel: AfiaViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val isAnalyzingOcular by viewModel.isAnalyzingOcular.collectAsStateWithLifecycle()
    val ocularAnalysisResult by viewModel.ocularAnalysisResult.collectAsStateWithLifecycle()

    // Ocular system choices
    var selectedPhotoType by remember { mutableStateOf("MATH") } // "MATH" or "JOURNAL"
    var selectedWorkspaceTab by remember { mutableStateOf("notes") } // "notes", "ocr", "canvas"

    // Note input parameters
    var noteTitleInput by remember { mutableStateOf("") }
    var noteContentInput by remember { mutableStateOf("") }

    // Interactive Node states for Diagram Generator
    var nodesList by remember {
        mutableStateOf(
            listOf(
                WorkspaceNodeItem(id = 1, label = "Afia Core", position = Offset(150f, 150f), color = NeonBlue),
                WorkspaceNodeItem(id = 2, label = "Sensors Core", position = Offset(450f, 220f), color = NeonPurple),
                WorkspaceNodeItem(id = 3, label = "Voice Vocalizer", position = Offset(180f, 380f), color = NeonGreen),
                WorkspaceNodeItem(id = 4, label = "Grid Network", position = Offset(430f, 450f), color = NeonPink)
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // --- Header title ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI QUANTUM LABORATORY",
                        color = NeonBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "ADVANCED OCULAR TELEMETRY & DIAGRAM DECODER",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- Workspace selector navigation ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF19172B), RoundedCornerShape(8.dp))
                    .background(Color(0xFF070511))
            ) {
                val tabs = listOf("notes" to "File Log", "ocr" to "Ocular OCR", "canvas" to "Diagram Maker")
                tabs.forEach { (tabKey, tabLabel) ->
                    val isSelected = selectedWorkspaceTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) NeonPurple.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedWorkspaceTab = tabKey }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel.uppercase(),
                            color = if (isSelected) NeonPurple else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- ACTIVE SELECTED MULTI-WORKSPACE MODE ---
        when (selectedWorkspaceTab) {
            "notes" -> {
                // --- 1. Note Creation Grid ---
                item {
                    SciFiGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonBlue.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "FILE WRITING INTERFACE",
                            color = NeonBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = noteTitleInput,
                            onValueChange = { noteTitleInput = it },
                            placeholder = { Text("File label title...", fontSize = 11.sp, color = TextMuted) },
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonBlue,
                                unfocusedBorderColor = Color(0xFF1E1B31),
                                focusedContainerColor = Color(0xFF030206)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = noteContentInput,
                            onValueChange = { noteContentInput = it },
                            placeholder = { Text("Write note transcript or transcripts summary...", fontSize = 11.sp, color = TextMuted) },
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonBlue,
                                unfocusedBorderColor = Color(0xFF1E1B31),
                                focusedContainerColor = Color(0xFF030206)
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (noteTitleInput.isNotBlank() && noteContentInput.isNotBlank()) {
                                    viewModel.saveNote(noteTitleInput, noteContentInput)
                                    noteTitleInput = ""
                                    noteContentInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("SAVE SECURE TRANSCRIPT", color = CyberBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Note listings
                if (notes.isEmpty()) {
                    item {
                        SciFiGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "WORK SPACE DATABASE TERMINAL IS VACANT",
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
                    items(notes) { note ->
                        SciFiGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = NeonBlue.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        color = NeonBlue,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", note.timestamp).toString(),
                                        color = TextMuted,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = note.content,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteNoteItem(note) },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = NeonPink)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear transcript", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            "ocr" -> {
                // --- 2. Ocular visual feed & OCR analysis ---
                item {
                    SciFiGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonPurple.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "OCULAR PAYLOAD COMPILER",
                            color = NeonPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { selectedPhotoType = "MATH" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPhotoType == "MATH") NeonPurple else Color(0xFF141124)
                                )
                            ) {
                                Text(
                                    text = "MATH HW TEXT",
                                    color = if (selectedPhotoType == "MATH") CyberBlack else TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Button(
                                onClick = { selectedPhotoType = "JOURNAL" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPhotoType == "JOURNAL") NeonPurple else Color(0xFF141124)
                                )
                            ) {
                                Text(
                                    text = "SECURE JOURNAL",
                                    color = if (selectedPhotoType == "JOURNAL") CyberBlack else TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Drawing our Simulated Live Camera Feed with drawn contents!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Draw nice background grid
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val grid = 20.dp.toPx()
                                for (x in 0..size.width.toInt() step grid.toInt()) {
                                    drawLine(Color(0xFF0F0B1E), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
                                }
                                for (y in 0..size.height.toInt() step grid.toInt()) {
                                    drawLine(Color(0xFF0F0B1E), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                                }
                            }
                            
                            // Visual text
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedPhotoType == "MATH") {
                                        "[OCR DETECTED MATH MATRIX]"
                                    } else {
                                        "[OCR DETECTED JOURNAL SYSTEM LOG]"
                                    },
                                    color = NeonPurple,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (selectedPhotoType == "MATH") {
                                        "Solve for x:   2x + 15 = 35"
                                    } else {
                                        "Project Afia Node links active.\nCode decryption key sequence validated."
                                    },
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "[OCULAR MULTICAST CAMERA ACTIVE]",
                                    color = NeonPink,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (!isAnalyzingOcular) {
                                    // Make a REAL Bitmap of the selected text and compile with Gemini!
                                    val bmp = Bitmap.createBitmap(400, 150, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(bmp)
                                    canvas.drawColor(android.graphics.Color.BLACK)
                                    val paint = Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 30f
                                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                                    }
                                    if (selectedPhotoType == "MATH") {
                                        canvas.drawText("Equation: 2x + 15 = 35", 30f, 80f, paint)
                                        viewModel.performOcularAnalysis(
                                            prompt = "This is an image of a math math problem. Please extract the equation, solve it step-by-step, and state the value of x clearly.",
                                            bitmap = bmp
                                        )
                                    } else {
                                        canvas.drawText("Afia Core Project online.", 20f, 60f, paint)
                                        canvas.drawText("Sequence decrypted.", 20f, 100f, paint)
                                        viewModel.performOcularAnalysis(
                                            prompt = "This is a handwritten secret terminal log. Perform complete OCR extraction, and summarize what the message is saying about Project Afia.",
                                            bitmap = bmp
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isAnalyzingOcular) {
                                CircularProgressIndicator(color = CyberBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PARSING WAVEFORMS...", color = CyberBlack, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            } else {
                                Text("INSPECT VISUAL DATA ARRAY", color = CyberBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Results card
                if (ocularAnalysisResult.isNotEmpty()) {
                    item {
                        SciFiGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = NeonGreen.copy(alpha = 0.3f),
                            glowColor = NeonGreen.copy(alpha = 0.05f)
                        ) {
                            Text(
                                text = "TELECTRICAL OCR TELEMETRY RESULTS",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = ocularAnalysisResult,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            "canvas" -> {
                // --- 3. Interactive Diagram Maker ---
                item {
                    SciFiGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonPink.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "HOLOGRAPHIC STRUCTURE BUILDER",
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DRAG SHIFT CORE VECTOR NODES INTERACTIVELY",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Canvas component with Gestures!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF030206))
                                .border(1.dp, NeonPink.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        // Find nearest node within target capture area of 50dp
                                        val clickPos = change.position
                                        nodesList = nodesList.map { node ->
                                            val dist = (node.position - clickPos).getDistance()
                                            if (dist < 120f) {
                                                node.copy(position = node.position + dragAmount)
                                            } else {
                                                node
                                            }
                                        }
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                // Draw laser connections beams
                                for (i in 0 until nodesList.size) {
                                    for (j in (i + 1) until nodesList.size) {
                                        drawLine(
                                            color = NeonPink.copy(alpha = 0.18f),
                                            start = nodesList[i].position,
                                            end = nodesList[j].position,
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                                }

                                // Render glowing node particles
                                for (node in nodesList) {
                                    // Back glow arc
                                    drawCircle(
                                        color = node.color.copy(alpha = 0.25f),
                                        radius = 24.dp.toPx(),
                                        center = node.position
                                    )
                                    // Solid node core
                                    drawCircle(
                                        color = node.color,
                                        radius = 12.dp.toPx(),
                                        center = node.position
                                    )
                                    // White core point
                                    drawCircle(
                                        color = Color.White,
                                        radius = 3.dp.toPx(),
                                        center = node.position
                                    )
                                }
                            }

                            // Render node label overlay positions
                            nodesList.forEach { node ->
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = (node.position.x / 2.75).dp, // scaling coordinates roughly to match display density
                                            y = (node.position.y / 2.75).dp
                                        )
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberCardBgTranslucent)
                                        .border(0.5.dp, node.color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = node.label.uppercase(),
                                        color = TextPrimary,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                // Scramble coordinates to simulate generator
                                nodesList = nodesList.map { node ->
                                    node.copy(
                                        position = Offset(
                                            kotlin.random.Random.nextFloat() * (550f - 100f) + 100f,
                                            kotlin.random.Random.nextFloat() * (450f - 100f) + 100f
                                        )
                                    )
                                }
                                viewModel.addLog("Re-sequenced Holographic node array coordinates.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RE-SEQUENCE STRUCT CODE", color = CyberBlack, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class WorkspaceNodeItem(
    val id: Int,
    val label: String,
    val position: Offset,
    val color: Color
)

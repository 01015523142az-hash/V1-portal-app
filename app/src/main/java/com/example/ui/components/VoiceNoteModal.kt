package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.model.LeadEntity
import com.example.ui.theme.*

@Composable
fun VoiceNoteModal(
    lead: LeadEntity?,
    isRecording: Boolean,
    durationSec: Int,
    isTranscribing: Boolean,
    transcribedText: String,
    waveform: List<Float>,
    onStartRecording: () -> Unit,
    onStopAndTranscribe: () -> Unit,
    onSaveVoiceNote: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            onStartRecording()
        }
    }

    var editableTranscript by remember(transcribedText) { mutableStateOf(transcribedText) }
    var memoTitle by remember { mutableStateOf("Client Voice Memo") }

    // Pulsing animation for mic button while recording
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.22f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("voice_note_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CoralPrimaryDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "Voice Memo & AI Transcription",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (lead != null) {
                                Text(
                                    text = "${lead.sellerName} · ${lead.propertyAddress}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Microphone permission warning if not granted
                if (!hasMicPermission) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralPrimaryDim,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Microphone access is required to capture your voice memos.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // Recording Stage or Results Stage
                if (transcribedText.isBlank() && !isTranscribing) {
                    // Big Recording Visualizer
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(if (isRecording) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                if (isRecording) CoralPrimary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 2.dp,
                                color = if (isRecording) CoralPrimary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (!hasMicPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (isRecording) {
                                        onStopAndTranscribe()
                                    } else {
                                        onStartRecording()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) DangerRed else CoralPrimary)
                                .testTag("record_mic_btn")
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop & Transcribe" else "Record Voice Note",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Timer display
                    val minutes = durationSec / 60
                    val seconds = durationSec % 60
                    val timeString = String.format("%02d:%02d", minutes, seconds)

                    Text(
                        text = if (isRecording) timeString else "Tap Mic to Start Recording",
                        fontSize = if (isRecording) 22.sp else 13.sp,
                        fontWeight = if (isRecording) FontWeight.ExtraBold else FontWeight.Medium,
                        fontFamily = if (isRecording) FontFamily.Monospace else FontFamily.Default,
                        color = if (isRecording) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Live Waveform visualizer bars
                    if (isRecording) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bars = if (waveform.isEmpty()) listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 0.6f, 0.3f) else waveform
                            bars.forEach { amp ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .width(5.dp)
                                        .fillMaxHeight(amp.coerceIn(0.2f, 1f))
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(CoralPrimary)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onStopAndTranscribe,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stop_and_transcribe_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stop & Transcribe with Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isTranscribing) {
                    // Transcribing Progress State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = GoldAccent,
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "✨ Gemini Neural Audio Transcribing...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = "Extracting deal notes, seller terms, and asking price nuances",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    // Transcribed Text Results Ready
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SleekSuccessGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SleekSuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "AI Transcription Completed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekSuccessGreen
                                    )
                                }
                                Text(
                                    text = "${durationSec.coerceAtLeast(6)}s audio",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = memoTitle,
                            onValueChange = { memoTitle = it },
                            label = { Text("Note Title", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editableTranscript,
                            onValueChange = { editableTranscript = it },
                            label = { Text("Transcribed Deal Note", fontSize = 11.sp) },
                            minLines = 4,
                            maxLines = 7,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transcribed_text_input")
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onStartRecording,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Re-record", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    if (editableTranscript.isNotBlank()) {
                                        onSaveVoiceNote(editableTranscript)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("save_voice_note_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save to Lead Feed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

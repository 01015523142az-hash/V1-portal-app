package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAssistantView(
    messages: List<ChatMessage>,
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendVoiceMessage: () -> Unit,
    isPeerTyping: Boolean,
    onStartVoiceCall: () -> Unit,
    onClose: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Close Chat", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CoralPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }

                        Column {
                            Text("Remi AI & Support Team", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Proptech Client Assistant · Online", fontSize = 11.sp, color = SuccessGreen)
                        }
                    }

                    // Voice Call Escalation Button
                    IconButton(
                        onClick = onStartVoiceCall,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CoralPrimaryDim)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Start Voice Call", tint = CoralPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isClient = msg.sender == "client"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isClient) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            horizontalAlignment = if (isClient) Alignment.End else Alignment.Start,
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isClient) 16.dp else 4.dp,
                                    bottomEnd = if (isClient) 4.dp else 16.dp
                                ),
                                color = if (isClient) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (!isClient) {
                                        Text(
                                            text = msg.senderName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent
                                        )
                                        Spacer(Modifier.height(2.dp))
                                    }
                                    if (msg.isAudio) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = if (isClient) Color.White else CoralPrimary)
                                            Text(msg.body, fontSize = 13.sp, color = if (isClient) Color.White else MaterialTheme.colorScheme.onSurface)
                                        }
                                    } else {
                                        Text(
                                            text = msg.body,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = if (isClient) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Text(
                                text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.createdAt)),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp)
                            )
                        }
                    }
                }

                if (isPeerTyping) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = GoldAccent)
                            Text("Remi is typing…", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onSendVoiceMessage) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Message", tint = CoralPrimary)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChanged,
                        placeholder = { Text("Ask Remi or team…", fontSize = 13.sp) },
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text")
                    )

                    IconButton(
                        onClick = onSendMessage,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CoralPrimary)
                            .testTag("chat_send_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

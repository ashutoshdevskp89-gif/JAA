package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.AssistantState
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.components.AnimeCharacterView
import com.example.ui.theme.JaCyanAccent
import com.example.ui.theme.JaDeepBackground
import com.example.ui.theme.JaPinkAccent
import com.example.ui.theme.JaPurpleLight
import com.example.ui.theme.JaPurplePrimary
import com.example.ui.theme.JaSurfaceCard
import com.example.viewmodel.JaUiState

@Composable
fun HomeScreen(
    uiState: JaUiState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSendTextCommand: (String) -> Unit,
    onStartLiveCall: () -> Unit,
    onAvatarTapped: () -> Unit,
    onSpeakText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    // Pulse animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState.isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JaDeepBackground)
    ) {
        // Top Header Banner with Live Call Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "JA AI Assistant",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "2D Anime Voice & Phone Action Partner",
                    color = JaCyanAccent,
                    fontSize = 11.sp
                )
            }

            // Prominent Live Call Button
            ElevatedButton(
                onClick = onStartLiveCall,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = JaPinkAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
                modifier = Modifier.testTag("live_call_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Live Call",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Call",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Hero 2D Anime Girl Character
        AnimeCharacterView(
            state = uiState.assistantState,
            expression = uiState.characterExpression,
            isSpeaking = uiState.isSpeaking,
            audioLevel = uiState.audioLevel,
            height = 240.dp,
            onAvatarTapped = onAvatarTapped,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Action Command Pills
        val quickChips = listOf(
            "🔴 Live Call शुरू करो" to { onStartLiveCall() },
            "🎬 JA, वीडियो एडिट कर दो" to { onSendTextCommand("JA, इस वीडियो को एडिट कर दो") },
            "▶️ YouTube खोलो" to { onSendTextCommand("YouTube खोलो") },
            "💬 WhatsApp मैसेज भेजो" to { onSendTextCommand("WhatsApp पर मैसेज लिखो") },
            "📸 Instagram खोलो" to { onSendTextCommand("Instagram खोलो") },
            "📘 Facebook खोलो" to { onSendTextCommand("Facebook खोलो") },
            "✂️ CapCut खोलो" to { onSendTextCommand("CapCut खोलो") },
            "📷 कैमरा खोलो" to { onSendTextCommand("कैमरा खोलो") }
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickChips) { chip ->
                Surface(
                    onClick = chip.second,
                    color = JaSurfaceCard,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JaPurpleLight.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("quick_chip_${chip.first.take(5)}")
                ) {
                    Text(
                        text = chip.first,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Interactive Conversation & Action History Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.chatMessages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onSpeakAgain = { onSpeakText(msg.text) }
                )
            }
        }

        // Bottom Voice & Text Input Section
        Surface(
            color = Color(0xFF140D2B),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Voice Listening Waveform Indicator
                AnimatedVisibility(visible = uiState.isListening) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎤 JA सुन रही है... बोलिए!",
                            color = JaCyanAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text Input
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                "बोलें या लिखें (उदा: YouTube खोलो)...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JaPurpleLight,
                            unfocusedBorderColor = JaSurfaceCard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = JaCyanAccent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    onSendTextCommand(textInput)
                                    textInput = ""
                                }
                            }
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("command_text_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (textInput.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSendTextCommand(textInput)
                                textInput = ""
                            },
                            modifier = Modifier
                                .background(JaPurplePrimary, CircleShape)
                                .size(46.dp)
                                .testTag("send_command_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // Glowing Voice Mic Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.scale(micScale)
                        ) {
                            IconButton(
                                onClick = {
                                    if (uiState.isListening) onStopListening() else onStartListening()
                                },
                                modifier = Modifier
                                    .shadow(8.dp, CircleShape, spotColor = if (uiState.isListening) JaCyanAccent else JaPinkAccent)
                                    .background(
                                        brush = Brush.linearGradient(
                                            if (uiState.isListening) listOf(JaCyanAccent, Color(0xFF00B0FF))
                                            else listOf(JaPinkAccent, JaPurplePrimary)
                                        ),
                                        shape = CircleShape
                                    )
                                    .size(52.dp)
                                    .testTag("voice_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSpeakAgain: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) JaPurplePrimary else JaSurfaceCard
            ),
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, JaPurpleLight.copy(alpha = 0.3f)) else null,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .testTag("chat_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "आप (You)" else "JA AI Assistant 🌸",
                        color = if (isUser) Color.White.copy(alpha = 0.8f) else JaCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isUser) {
                        IconButton(
                            onClick = onSpeakAgain,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play voice",
                                tint = JaPurpleLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                if (message.actionType != ActionType.NONE && message.actionLabel != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0x3300E5FF), RoundedCornerShape(8.dp))
                            .border(1.dp, JaCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡ Action: ${message.actionLabel}",
                            color = JaCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

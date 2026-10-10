package com.example.ui.screens

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantState
import com.example.ui.components.AnimeCharacterView
import com.example.ui.theme.JaCyanAccent
import com.example.ui.theme.JaDeepBackground
import com.example.ui.theme.JaPinkAccent
import com.example.ui.theme.JaPurpleLight
import com.example.ui.theme.JaSurfaceCard
import com.example.viewmodel.JaUiState

@Composable
fun LiveCallScreen(
    uiState: JaUiState,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    onAvatarTapped: () -> Unit,
    onSendQuickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMinutes = uiState.liveCallDurationSec / 60
    val durationSeconds = uiState.liveCallDurationSec % 60
    val durationFormatted = String.format("%02d:%02d", durationMinutes, durationSeconds)

    // Glowing background aura
    val infiniteTransition = rememberInfiniteTransition(label = "call_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E1038),
                        JaDeepBackground,
                        Color(0xFF090414)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Live Call Header & Timer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF00E676), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "JA Live Call 💖",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = durationFormatted,
                color = JaCyanAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.testTag("call_duration_timer")
            )

            Text(
                text = if (uiState.isMicMuted) "🔇 आपका माइक म्यूट है"
                else if (uiState.isSpeaking) "💖 JA बोल रही है..."
                else "🎧 JA सुन रही है... बात करें",
                color = JaPurpleLight,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 2. Center Anime Character with Live Reactive Expressions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AnimeCharacterView(
                state = uiState.assistantState,
                expression = uiState.characterExpression,
                isSpeaking = uiState.isSpeaking,
                audioLevel = uiState.audioLevel,
                height = 290.dp,
                onAvatarTapped = onAvatarTapped,
                modifier = Modifier.scale(if (uiState.isSpeaking) pulseScale else 1f)
            )
        }

        // 3. Audio Waveform Visualizer Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barCount = 12
            for (i in 0 until barCount) {
                val factor = ((i % 4) + 1) * 0.25f
                val barHeight = if (uiState.isSpeaking || uiState.isListening) {
                    (12 + (uiState.audioLevel * 28f * factor) + (if (uiState.isSpeaking) 14f else 4f)).coerceIn(8f, 38f)
                } else {
                    6f
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(4.dp)
                        .height(barHeight.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                if (uiState.isSpeaking) listOf(JaPinkAccent, JaPurpleLight)
                                else listOf(JaCyanAccent, Color(0xFF00B0FF))
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Live Speech Subtitle Bubble
        val latestMessage = uiState.chatMessages.lastOrNull()?.text ?: "नमस्ते सर! मैं सुन रही हूँ..."
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JaSurfaceCard.copy(alpha = 0.9f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, JaPurpleLight.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("live_call_transcript_bubble")
        ) {
            Text(
                text = latestMessage,
                color = Color.White,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Topic Suggestions during Live Call
        val callPrompts = listOf(
            "▶️ YouTube खोलो",
            "🎬 वीडियो एडिट करो",
            "💬 WhatsApp खोलो",
            "✨ एक प्यारी शायरी सुनाओ",
            "📸 Instagram खोलो"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(callPrompts) { prompt ->
                Surface(
                    onClick = { onSendQuickPrompt(prompt) },
                    color = Color(0x33281A4C),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JaCyanAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = prompt,
                        color = JaCyanAccent,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Call Control Buttons: Mute, Speaker, End Call
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (uiState.isMicMuted) Color(0xFFFF5252) else JaSurfaceCard,
                        CircleShape
                    )
                    .border(1.dp, JaPurpleLight, CircleShape)
                    .testTag("mute_button")
            ) {
                Icon(
                    imageVector = if (uiState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // End Call Button (Big Red Button)
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(68.dp)
                    .shadow(12.dp, CircleShape, spotColor = Color(0xFFFF1744))
                    .background(Color(0xFFFF1744), CircleShape)
                    .testTag("end_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Speaker Button
            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (uiState.isSpeakerOn) JaCyanAccent.copy(alpha = 0.8f) else JaSurfaceCard,
                        CircleShape
                    )
                    .border(1.dp, JaPurpleLight, CircleShape)
                    .testTag("speaker_button")
            ) {
                Icon(
                    imageVector = if (uiState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    contentDescription = "Speaker Control",
                    tint = if (uiState.isSpeakerOn) JaDeepBackground else Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

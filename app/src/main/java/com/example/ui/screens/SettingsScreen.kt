package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VoiceSettings
import com.example.ui.theme.JaCyanAccent
import com.example.ui.theme.JaDeepBackground
import com.example.ui.theme.JaPinkAccent
import com.example.ui.theme.JaPurpleLight
import com.example.ui.theme.JaPurplePrimary
import com.example.ui.theme.JaSurfaceBorder
import com.example.ui.theme.JaSurfaceCard

@Composable
fun SettingsScreen(
    voiceSettings: VoiceSettings,
    onPitchChanged: (Float) -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onTestVoice: () -> Unit,
    onTriggerWink: () -> Unit,
    modifier: Modifier = Modifier
) {
    var wakeWordEnabled by remember { mutableStateOf(voiceSettings.wakeWordEnabled) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JaDeepBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = JaCyanAccent,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "सेटिंग्स (Settings)",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "वॉइस, एनीमे कैरेक्टर और सुरक्षा विकल्प",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 2. Voice Settings Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JaSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, JaSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎙️ प्यारी एनीमे आवाज़ सेटिंग्स (Voice)",
                        color = JaCyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("आवाज़ की पिच (Cute Pitch):", color = Color.White, fontSize = 12.sp)
                        Text(String.format("%.2fx", voiceSettings.pitch), color = JaPinkAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = voiceSettings.pitch,
                        onValueChange = onPitchChanged,
                        valueRange = 0.8f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = JaPinkAccent,
                            activeTrackColor = JaPinkAccent,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("pitch_slider")
                    )

                    // Speech Rate Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("बोलने की गति (Speed):", color = Color.White, fontSize = 12.sp)
                        Text(String.format("%.2fx", voiceSettings.speechRate), color = JaCyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = voiceSettings.speechRate,
                        onValueChange = onSpeechRateChanged,
                        valueRange = 0.7f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = JaCyanAccent,
                            activeTrackColor = JaCyanAccent,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("speed_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ElevatedButton(
                        onClick = onTestVoice,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = JaPurplePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_voice_button")
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("JA की आवाज़ टेस्ट करें 🌸")
                    }
                }
            }
        }

        // 3. Anime Character Settings Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JaSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, JaSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🌸 2D एनीमे कैरेक्टर सेटिंग्स",
                        color = JaPinkAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("JA को “JA” बोलकर एक्टिवेट करें", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("क्विक वॉइस वेक वर्ड ऑप्शन", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = wakeWordEnabled,
                            onCheckedChange = { wakeWordEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = JaPurplePrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ElevatedButton(
                        onClick = onTriggerWink,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF28194C),
                            contentColor = JaCyanAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.WavingHand, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("JA से आँख झपकवाएं (Wink & Smile Test) ✨")
                    }
                }
            }
        }

        // 4. App Automation & Permissions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JaSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📱 सपोर्टेड ऐप्स और ऑटोमेशन स्टेटस",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val supportedApps = listOf(
                        "YouTube" to "वीडियो सर्च व ऐप ओपनिंग सपोर्ट",
                        "WhatsApp" to "मैसेज ड्राफ्ट व कन्फर्मेशन प्रोटेक्शन",
                        "Instagram" to "रील्स व पोस्ट अपलोड वर्कफ़्लो",
                        "Facebook" to "वीडियो शेयरिंग सपोर्ट",
                        "CapCut" to "स्मार्ट वीडियो एडिटिंग गाइड्स व ट्रिम कट्स"
                    )

                    supportedApps.forEach { (name, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(desc, color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // 5. User Privacy & Safety Disclaimer
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130E26)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332352)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = JaCyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "सुरक्षा सूचना: Android और संबंधित ऐप्स की पॉलिसी और परमिशन्स के अनुसार JA सुरक्षित कार्यप्रणाली का पालन करती है। किसी भी संवेदनशील कार्य (मैसेज भेजना, वीडियो शेयर करना) से पहले JA हमेशा आपकी सहमति (Confirmation) लेगी।",
                        color = Color(0xFFA59EC2),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

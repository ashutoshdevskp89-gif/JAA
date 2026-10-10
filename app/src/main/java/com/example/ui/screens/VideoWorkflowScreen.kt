package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TargetPlatform
import com.example.model.VideoProject
import com.example.ui.theme.JaCyanAccent
import com.example.ui.theme.JaDeepBackground
import com.example.ui.theme.JaPinkAccent
import com.example.ui.theme.JaPurpleLight
import com.example.ui.theme.JaPurplePrimary
import com.example.ui.theme.JaSurfaceBorder
import com.example.ui.theme.JaSurfaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoWorkflowScreen(
    videoProject: VideoProject,
    onVideoSelected: (Uri?) -> Unit,
    onPlatformSelected: (TargetPlatform) -> Unit,
    onGenerateMetadata: (String) -> Unit,
    onConfirmUpload: () -> Unit,
    onLaunchCapCut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var topicQuery by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    // Android Zero-Permission Photo/Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onVideoSelected(uri)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JaDeepBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner
        item {
            Column {
                Text(
                    text = "🎥 JA Video & Social Studio",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "YouTube, Instagram, Facebook अपलोड वर्कफ़्लो और CapCut वीडियो एडिटिंग",
                    color = JaPurpleLight,
                    fontSize = 12.sp
                )
            }
        }

        // 2. Video Picker Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JaSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, JaSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                    .testTag("pick_video_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                Brush.linearGradient(listOf(JaPurplePrimary, JaPinkAccent)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Select Video",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (videoProject.uriString != null) "✅ वीडियो चुना गया है" else "📁 वीडियो चुनें (गैलरी)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = videoProject.fileName ?: "YouTube / Instagram / Facebook के लिए वीडियो सेलेक्ट करें",
                            color = if (videoProject.uriString != null) JaCyanAccent else Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    ElevatedButton(
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = JaPurplePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (videoProject.uriString != null) "बदलें" else "चुनें", fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. Platform Selector Tabs
        item {
            Text(
                text = "प्लेटफ़ॉर्म चुनें:",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            val platforms = TargetPlatform.values()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                platforms.forEach { platform ->
                    val isSelected = videoProject.targetPlatform == platform
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPlatformSelected(platform) },
                        label = { Text(platform.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JaPurplePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = JaSurfaceCard,
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) JaCyanAccent else JaSurfaceBorder
                        ),
                        modifier = Modifier.testTag("platform_tab_${platform.name}")
                    )
                }
            }
        }

        // 4. Topic Generator Input & Button
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JaSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "✨ AI टाइटल, हैशटैग्स और एडिटिंग जनरेटर",
                        color = JaCyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = topicQuery,
                        onValueChange = { topicQuery = it },
                        placeholder = { Text("वीडियो का विषय (उदा: Funny Gym Moments, Travel Vlog)...", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = JaCyanAccent,
                            unfocusedBorderColor = JaSurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topic_generator_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ElevatedButton(
                        onClick = { onGenerateMetadata(topicQuery.ifBlank { "Viral Trending Video" }) },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = JaCyanAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_details_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("JA से डिटेल्स तैयार करवाएं", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. CapCut Video Editing Guide ("JA, इस वीडियो को एडिट कर दो")
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1138)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, JaPinkAccent.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✂️ “JA, इस वीडियो को एडिट कर दो” (CapCut)",
                            color = JaPinkAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(JaPinkAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("9:16 Shorts/Reels", color = JaPinkAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• ट्रिम और कट्स: शुरुआती 1.5 सेकंड का हुक रखें, ख़ामोशी वाले हिस्से काटें\n" +
                               "• म्यूज़िक आइडिया: ${videoProject.musicSuggestion.ifBlank { "Trending Instagram Reels Bass Drop" }}\n" +
                               "• ट्रांज़िशन: ${videoProject.transitionsSuggestion.ifBlank { "Smooth Zoom & Flash" }}\n" +
                               "• सबटाइटल्स: CapCut Auto-Caption ऑन करें, बोल्ड फ़ॉन्ट चुनें\n" +
                               "• थंबनेल: ${videoProject.thumbnailSuggestion.ifBlank { "हाई-कंट्रास्ट टेक्स्ट और एक्सप्रेशन" }}",
                        color = Color(0xFFE2DCF7),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onLaunchCapCut,
                        colors = ButtonDefaults.buttonColors(containerColor = JaPinkAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_capcut_button")
                    ) {
                        Icon(imageVector = Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CapCut एडिटर खोलें", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Generated Metadata Details: Title, Description, Hashtags
        if (videoProject.title.isNotBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = JaSurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📌 तैयार टाइटल:", color = JaCyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(videoProject.title))
                                    copiedNotice = "टाइटल कॉपी हुआ!"
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(text = videoProject.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📝 तैयार डिस्क्रिप्शन:", color = JaCyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(videoProject.description))
                                    copiedNotice = "डिस्क्रिप्शन कॉपी हुआ!"
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(text = videoProject.description, color = Color(0xFFDDD7F0), fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hashtags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏷️ ट्रेंडिंग हैशटैग्स:", color = JaCyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(videoProject.hashtags.joinToString(" ")))
                                    copiedNotice = "हैशटैग्स कॉपी हुए!"
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(videoProject.hashtags) { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x3300E5FF), RoundedCornerShape(12.dp))
                                        .border(1.dp, JaCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = tag, color = JaCyanAccent, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Final Preview & Safe Confirmation Upload Button
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130E26)),
                border = androidx.compose.foundation.BorderStroke(1.dp, JaPurpleLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔍 अपलोड से पहले फाइनल प्रीव्यू",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "टारगेट: ${videoProject.targetPlatform.displayName} | वीडियो: ${videoProject.fileName ?: "चयनित नहीं"}",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ElevatedButton(
                        onClick = onConfirmUpload,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (videoProject.uriString != null) JaPurplePrimary else Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("upload_workflow_button")
                    ) {
                        Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${videoProject.targetPlatform.displayName} पर अपलोड शुरू करें",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "🔒 सुरक्षा गारंटी: आपकी पुष्टि के बिना कोई फ़ाइल अपलोड नहीं की जाती।",
                        color = Color(0xFF9E94BA),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

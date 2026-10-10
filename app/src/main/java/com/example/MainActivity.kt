package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ConfirmationDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveCallScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideoWorkflowScreen
import com.example.ui.theme.JaCyanAccent
import com.example.ui.theme.JaDeepBackground
import com.example.ui.theme.JaPinkAccent
import com.example.ui.theme.JaPurpleLight
import com.example.ui.theme.JaSurfaceCard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JaViewModel
import com.example.viewmodel.NavTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: JaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Audio recording permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            viewModel.speak("आवाज़ सुनने के लिए कृपया माइक्रोफ़ोन अनुमति दें।")
        }
    }

    val requestMicAndListen = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Back handling
    BackHandler(enabled = uiState.activeNavTab != NavTab.HOME) {
        if (uiState.isLiveCallActive) {
            viewModel.endLiveCall()
        } else {
            viewModel.setNavTab(NavTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JaDeepBackground,
        bottomBar = {
            if (!uiState.isLiveCallActive) {
                NavigationBar(
                    containerColor = JaSurfaceCard,
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = uiState.activeNavTab == NavTab.HOME,
                        onClick = { viewModel.setNavTab(NavTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("JA") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JaCyanAccent,
                            selectedTextColor = JaCyanAccent,
                            indicatorColor = Color(0x3300E5FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    NavigationBarItem(
                        selected = uiState.activeNavTab == NavTab.LIVE_CALL,
                        onClick = { viewModel.startLiveCall() },
                        icon = { Icon(Icons.Default.Call, contentDescription = "Live Call") },
                        label = { Text("Live Call") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JaPinkAccent,
                            selectedTextColor = JaPinkAccent,
                            indicatorColor = Color(0x33FF4081),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_tab_live_call")
                    )

                    NavigationBarItem(
                        selected = uiState.activeNavTab == NavTab.VIDEO_STUDIO,
                        onClick = { viewModel.setNavTab(NavTab.VIDEO_STUDIO) },
                        icon = { Icon(Icons.Default.Movie, contentDescription = "Studio") },
                        label = { Text("Video Studio") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JaPurpleLight,
                            selectedTextColor = JaPurpleLight,
                            indicatorColor = Color(0x33B388FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_tab_video_studio")
                    )

                    NavigationBarItem(
                        selected = uiState.activeNavTab == NavTab.HISTORY,
                        onClick = { viewModel.setNavTab(NavTab.HISTORY) },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JaCyanAccent,
                            selectedTextColor = JaCyanAccent,
                            indicatorColor = Color(0x3300E5FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_tab_history")
                    )

                    NavigationBarItem(
                        selected = uiState.activeNavTab == NavTab.SETTINGS,
                        onClick = { viewModel.setNavTab(NavTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JaPurpleLight,
                            selectedTextColor = JaPurpleLight,
                            indicatorColor = Color(0x33B388FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (uiState.activeNavTab) {
            NavTab.HOME -> {
                HomeScreen(
                    uiState = uiState,
                    onStartListening = requestMicAndListen,
                    onStopListening = { viewModel.stopListening() },
                    onSendTextCommand = { viewModel.handleUserVoiceInput(it) },
                    onStartLiveCall = { viewModel.startLiveCall() },
                    onAvatarTapped = { viewModel.triggerAvatarWink() },
                    onSpeakText = { viewModel.speak(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            NavTab.LIVE_CALL -> {
                LiveCallScreen(
                    uiState = uiState,
                    onToggleMute = { viewModel.toggleMute() },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onEndCall = { viewModel.endLiveCall() },
                    onAvatarTapped = { viewModel.triggerAvatarWink() },
                    onSendQuickPrompt = { viewModel.handleUserVoiceInput(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            NavTab.VIDEO_STUDIO -> {
                VideoWorkflowScreen(
                    videoProject = uiState.currentVideoProject,
                    onVideoSelected = { viewModel.onVideoSelected(it) },
                    onPlatformSelected = { viewModel.updateVideoTargetPlatform(it) },
                    onGenerateMetadata = { viewModel.generateVideoMetadata(it) },
                    onConfirmUpload = { viewModel.confirmAndLaunchVideoUpload() },
                    onLaunchCapCut = { viewModel.launchCapCutForEditing() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            NavTab.HISTORY -> {
                HistoryScreen(
                    historyItems = uiState.commandHistory,
                    onReplayVoice = { viewModel.speak(it) },
                    onRerunCommand = { viewModel.handleUserVoiceInput(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            NavTab.SETTINGS -> {
                SettingsScreen(
                    voiceSettings = uiState.voiceSettings,
                    onPitchChanged = { viewModel.updateVoicePitch(it) },
                    onSpeechRateChanged = { viewModel.updateSpeechRate(it) },
                    onLanguageChanged = { viewModel.updateLanguage(it) },
                    onTestVoice = { viewModel.speak("नमस्ते! मैं आपकी 2D AI साथी JA हूँ~ मेरी आवाज़ आपको कैसी लगी?") },
                    onTriggerWink = { viewModel.triggerAvatarWink() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Safety & Permission Confirmation Dialog
        uiState.pendingConfirmation?.let { conf ->
            ConfirmationDialog(
                confirmation = conf,
                onConfirm = conf.onConfirm,
                onDismiss = {
                    conf.onDismiss()
                    viewModel.dismissConfirmation()
                }
            )
        }
    }
}

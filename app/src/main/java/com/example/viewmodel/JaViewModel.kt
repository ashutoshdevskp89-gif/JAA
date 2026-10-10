package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ActionType
import com.example.model.AssistantState
import com.example.model.CharacterExpression
import com.example.model.ChatMessage
import com.example.model.CommandHistoryItem
import com.example.model.MessageSender
import com.example.model.PendingConfirmation
import com.example.model.TargetPlatform
import com.example.model.VideoProject
import com.example.model.VoiceSettings
import com.example.service.AppActionDispatcher
import com.example.service.JaAiEngine
import com.example.service.SpeechRecognizerManager
import com.example.service.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class JaUiState(
    val assistantState: AssistantState = AssistantState.IDLE,
    val characterExpression: CharacterExpression = CharacterExpression.SMILING,
    val isSpeaking: Boolean = false,
    val isListening: Boolean = false,
    val audioLevel: Float = 0f,
    val chatMessages: List<ChatMessage> = emptyList(),
    val commandHistory: List<CommandHistoryItem> = emptyList(),
    val currentVideoProject: VideoProject = VideoProject(),
    val pendingConfirmation: PendingConfirmation? = null,
    val voiceSettings: VoiceSettings = VoiceSettings(),
    val isLiveCallActive: Boolean = false,
    val liveCallDurationSec: Int = 0,
    val isMicMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val speechInputText: String = "",
    val activeNavTab: NavTab = NavTab.HOME
)

enum class NavTab {
    HOME,
    LIVE_CALL,
    VIDEO_STUDIO,
    HISTORY,
    SETTINGS
}

class JaViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(JaUiState())
    val uiState: StateFlow<JaUiState> = _uiState.asStateFlow()

    private val dispatcher = AppActionDispatcher(application)
    private val aiEngine = JaAiEngine()

    private var ttsManager: TtsManager? = null
    private var speechManager: SpeechRecognizerManager? = null
    private var callTimerJob: Job? = null

    init {
        initTts()
        initSpeechRecognizer()
        // Initial greeting
        addMessage(
            ChatMessage(
                sender = MessageSender.JA,
                text = "नमस्ते! मैं JA हूँ, आपकी 2D एनीमे AI साथी~ मैं YouTube, WhatsApp, Instagram खोलने, वीडियो एडिट करने और लाइव बात करने के लिए तैयार हूँ!",
                actionType = ActionType.NONE
            )
        )
    }

    private fun initTts() {
        ttsManager = TtsManager(
            context = getApplication(),
            onSpeechStarted = {
                _uiState.update {
                    it.copy(
                        isSpeaking = true,
                        assistantState = AssistantState.SPEAKING,
                        characterExpression = CharacterExpression.SPEAKING
                    )
                }
            },
            onSpeechFinished = {
                _uiState.update {
                    it.copy(
                        isSpeaking = false,
                        assistantState = AssistantState.COMPLETED,
                        characterExpression = CharacterExpression.SMILING
                    )
                }
                // If in live call mode and mic not muted, automatically listen again!
                if (_uiState.value.isLiveCallActive && !_uiState.value.isMicMuted) {
                    viewModelScope.launch {
                        delay(600)
                        startListening()
                    }
                }
            },
            onSpeechError = {
                _uiState.update {
                    it.copy(
                        isSpeaking = false,
                        assistantState = AssistantState.ERROR,
                        characterExpression = CharacterExpression.APOLOGETIC
                    )
                }
            }
        )
    }

    private fun initSpeechRecognizer() {
        speechManager = SpeechRecognizerManager(
            context = getApplication(),
            onReady = {
                _uiState.update {
                    it.copy(
                        isListening = true,
                        assistantState = AssistantState.LISTENING,
                        characterExpression = CharacterExpression.LISTENING
                    )
                }
            },
            onRmsChanged = { level ->
                _uiState.update { it.copy(audioLevel = level) }
            },
            onResultReceived = { recognizedText ->
                _uiState.update {
                    it.copy(
                        isListening = false,
                        speechInputText = recognizedText
                    )
                }
                handleUserVoiceInput(recognizedText)
            },
            onError = { errorMsg ->
                _uiState.update {
                    it.copy(
                        isListening = false,
                        assistantState = AssistantState.IDLE,
                        characterExpression = CharacterExpression.SMILING
                    )
                }
                // If in live call, try listening again after short delay
                if (_uiState.value.isLiveCallActive && !_uiState.value.isMicMuted) {
                    viewModelScope.launch {
                        delay(1200)
                        if (_uiState.value.isLiveCallActive && !_uiState.value.isSpeaking) {
                            startListening()
                        }
                    }
                }
            }
        )
    }

    fun startListening() {
        ttsManager?.stop()
        _uiState.update {
            it.copy(
                isSpeaking = false,
                assistantState = AssistantState.LISTENING,
                characterExpression = CharacterExpression.LISTENING
            )
        }
        speechManager?.startListening(_uiState.value.voiceSettings.language)
    }

    fun stopListening() {
        speechManager?.stopListening()
        _uiState.update {
            it.copy(
                isListening = false,
                assistantState = AssistantState.IDLE
            )
        }
    }

    fun handleUserVoiceInput(input: String) {
        if (input.isBlank()) return

        addMessage(ChatMessage(sender = MessageSender.USER, text = input))

        _uiState.update {
            it.copy(
                assistantState = AssistantState.PROCESSING,
                characterExpression = CharacterExpression.THINKING
            )
        }

        viewModelScope.launch {
            val parsed = aiEngine.processUserCommand(input)

            when (parsed.intentType) {
                JaAiEngine.IntentType.OPEN_APP -> {
                    executeOpenApp(parsed.targetApp ?: "", parsed.naturalReply)
                }

                JaAiEngine.IntentType.WHATSAPP_MESSAGE -> {
                    val draftMessage = parsed.payload ?: "नमस्ते!"
                    speakAndShowResponse(
                        text = parsed.naturalReply,
                        actionType = ActionType.WHATSAPP_DRAFT,
                        actionLabel = "WhatsApp मैसेज भेजें",
                        actionPayload = draftMessage
                    )
                    // Request user confirmation for safety
                    _uiState.update {
                        it.copy(
                            pendingConfirmation = PendingConfirmation(
                                title = "WhatsApp मैसेज भेजने की पुष्टि",
                                message = "क्या आप यह मैसेज WhatsApp में खोलना चाहते हैं?\n\n\"$draftMessage\"",
                                confirmButtonText = "हाँ, WhatsApp खोलें",
                                onConfirm = {
                                    _uiState.update { s -> s.copy(pendingConfirmation = null) }
                                    val res = dispatcher.sendWhatsAppDraft(draftMessage)
                                    speak(res.feedbackMessage)
                                    recordHistory(input, res.feedbackMessage, "WhatsApp Message", res.isSuccess)
                                },
                                onDismiss = {
                                    _uiState.update { s -> s.copy(pendingConfirmation = null) }
                                    speak("ठीक है, मैसेज रद्द कर दिया गया है।")
                                }
                            )
                        )
                    }
                }

                JaAiEngine.IntentType.VIDEO_EDIT_REQUEST -> {
                    speakAndShowResponse(
                        text = parsed.naturalReply,
                        actionType = ActionType.CAPCUT_EDIT,
                        actionLabel = "वीडियो स्टूडियो खोलें"
                    )
                    _uiState.update { it.copy(activeNavTab = NavTab.VIDEO_STUDIO) }
                    recordHistory(input, parsed.naturalReply, "CapCut Video Edit Request", true)
                }

                JaAiEngine.IntentType.VIDEO_METADATA_REQUEST -> {
                    speakAndShowResponse(
                        text = parsed.naturalReply,
                        actionType = ActionType.VIDEO_WORKFLOW,
                        actionLabel = "वीडियो अपलोड स्टूडियो"
                    )
                    _uiState.update { it.copy(activeNavTab = NavTab.VIDEO_STUDIO) }
                    recordHistory(input, parsed.naturalReply, "Video Upload Workflow", true)
                }

                JaAiEngine.IntentType.LIVE_CALL -> {
                    startLiveCall()
                    speak(parsed.naturalReply)
                }

                JaAiEngine.IntentType.GENERAL_CONVERSATION -> {
                    speakAndShowResponse(text = parsed.naturalReply)
                    recordHistory(input, parsed.naturalReply, "AI Chat", true)
                }
            }
        }
    }

    private fun executeOpenApp(appKey: String, replyVoice: String) {
        val result = when (appKey.lowercase()) {
            "youtube" -> dispatcher.openYouTube()
            "whatsapp" -> dispatcher.openWhatsApp()
            "instagram" -> dispatcher.openInstagram()
            "facebook" -> dispatcher.openFacebook()
            "capcut" -> dispatcher.openCapCut()
            "camera" -> dispatcher.openCamera()
            "settings" -> dispatcher.openSettings()
            else -> AppActionDispatcher.ActionResult(false, "ऐप नहीं मिला", appKey)
        }

        val speech = if (result.isSuccess) replyVoice else result.feedbackMessage
        speakAndShowResponse(
            text = speech,
            actionType = ActionType.APP_LAUNCH,
            actionLabel = result.openedTarget
        )
        recordHistory("Open $appKey", speech, result.openedTarget, result.isSuccess)
    }

    fun speakAndShowResponse(
        text: String,
        actionType: ActionType = ActionType.NONE,
        actionLabel: String? = null,
        actionPayload: String? = null
    ) {
        addMessage(
            ChatMessage(
                sender = MessageSender.JA,
                text = text,
                actionType = actionType,
                actionLabel = actionLabel,
                actionPayload = actionPayload
            )
        )
        speak(text)
    }

    fun speak(text: String) {
        ttsManager?.speak(text)
    }

    fun stopSpeaking() {
        ttsManager?.stop()
        _uiState.update { it.copy(isSpeaking = false) }
    }

    // Live Call Controls
    fun startLiveCall() {
        _uiState.update {
            it.copy(
                isLiveCallActive = true,
                activeNavTab = NavTab.LIVE_CALL,
                liveCallDurationSec = 0,
                characterExpression = CharacterExpression.HAPPY
            )
        }
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_uiState.value.isLiveCallActive) {
                delay(1000)
                _uiState.update { it.copy(liveCallDurationSec = it.liveCallDurationSec + 1) }
            }
        }
        val callGreeting = "नमस्ते! मैं JA लाइव कॉल पर हूँ। आप मुझसे कुछ भी पूछ या कह सकते हैं~"
        speak(callGreeting)
    }

    fun endLiveCall() {
        callTimerJob?.cancel()
        ttsManager?.stop()
        speechManager?.stopListening()
        _uiState.update {
            it.copy(
                isLiveCallActive = false,
                activeNavTab = NavTab.HOME,
                isSpeaking = false,
                isListening = false,
                characterExpression = CharacterExpression.SMILING,
                assistantState = AssistantState.IDLE
            )
        }
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isMicMuted
        _uiState.update { it.copy(isMicMuted = newMuted) }
        if (newMuted) {
            speechManager?.stopListening()
        } else if (_uiState.value.isLiveCallActive && !_uiState.value.isSpeaking) {
            startListening()
        }
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    // Video Workflow
    fun onVideoSelected(uri: Uri?) {
        if (uri == null) return
        val current = _uiState.value.currentVideoProject
        _uiState.update {
            it.copy(
                currentVideoProject = current.copy(
                    uriString = uri.toString(),
                    fileName = uri.lastPathSegment ?: "Selected_Video.mp4"
                )
            )
        }
        generateVideoMetadata("Trending Video")
    }

    fun generateVideoMetadata(topic: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    assistantState = AssistantState.PROCESSING,
                    characterExpression = CharacterExpression.THINKING
                )
            }
            speak("JA आपके वीडियो के लिए टाइटल, टैग्स और एडिटिंग नोट्स तैयार कर रही है...")
            val project = aiEngine.generateVideoDetails(topic, _uiState.value.currentVideoProject.targetPlatform)
            val updated = project.copy(
                uriString = _uiState.value.currentVideoProject.uriString,
                fileName = _uiState.value.currentVideoProject.fileName
            )
            _uiState.update {
                it.copy(
                    currentVideoProject = updated,
                    assistantState = AssistantState.COMPLETED,
                    characterExpression = CharacterExpression.HAPPY
                )
            }
            speak("वीडियो का टाइटल और हैशटैग्स तैयार हैं, सर!")
        }
    }

    fun updateVideoTargetPlatform(platform: TargetPlatform) {
        val current = _uiState.value.currentVideoProject
        _uiState.update {
            it.copy(currentVideoProject = current.copy(targetPlatform = platform))
        }
        generateVideoMetadata(current.title.ifBlank { "Viral Clip" })
    }

    fun confirmAndLaunchVideoUpload() {
        val project = _uiState.value.currentVideoProject
        val uriStr = project.uriString
        if (uriStr == null) {
            speak("कृपया पहले गैलरी से कोई वीडियो चुनें!")
            return
        }

        _uiState.update {
            it.copy(
                pendingConfirmation = PendingConfirmation(
                    title = "${project.targetPlatform.displayName} अपलोड की पुष्टि",
                    message = "क्या आप यह वीडियो ${project.targetPlatform.displayName} पर शेयर करना चाहते हैं?\n\nटाइटल: ${project.title}",
                    confirmButtonText = "हाँ, शेयर करें",
                    onConfirm = {
                        _uiState.update { s -> s.copy(pendingConfirmation = null) }
                        val caption = "${project.title}\n\n${project.description}\n\n${project.hashtags.joinToString(" ")}"
                        val res = dispatcher.shareVideoToPlatform(Uri.parse(uriStr), project.targetPlatform, caption)
                        speak(res.feedbackMessage)
                        recordHistory("Upload to ${project.targetPlatform.displayName}", res.feedbackMessage, project.title, res.isSuccess)
                    },
                    onDismiss = {
                        _uiState.update { s -> s.copy(pendingConfirmation = null) }
                    }
                )
            )
        }
    }

    fun launchCapCutForEditing() {
        _uiState.update {
            it.copy(
                pendingConfirmation = PendingConfirmation(
                    title = "CapCut में वीडियो एडिट करें",
                    message = "JA के एडिटिंग नोट्स:\n${_uiState.value.currentVideoProject.capcutEditingNotes}\n\nक्या आप CapCut खोलना चाहते हैं?",
                    confirmButtonText = "हाँ, CapCut खोलें",
                    onConfirm = {
                        _uiState.update { s -> s.copy(pendingConfirmation = null) }
                        val res = dispatcher.openCapCut()
                        speak(res.feedbackMessage)
                        recordHistory("CapCut Edit", res.feedbackMessage, "CapCut Video Editor", res.isSuccess)
                    },
                    onDismiss = {
                        _uiState.update { s -> s.copy(pendingConfirmation = null) }
                    }
                )
            )
        }
    }

    fun setNavTab(tab: NavTab) {
        _uiState.update { it.copy(activeNavTab = tab) }
    }

    fun updateVoicePitch(pitch: Float) {
        val current = _uiState.value.voiceSettings
        _uiState.update { it.copy(voiceSettings = current.copy(pitch = pitch)) }
        ttsManager?.setPitch(pitch)
    }

    fun updateSpeechRate(rate: Float) {
        val current = _uiState.value.voiceSettings
        _uiState.update { it.copy(voiceSettings = current.copy(speechRate = rate)) }
        ttsManager?.setRate(rate)
    }

    fun updateLanguage(langCode: String) {
        val current = _uiState.value.voiceSettings
        _uiState.update { it.copy(voiceSettings = current.copy(language = langCode)) }
        val locale = if (langCode.startsWith("hi")) Locale("hi", "IN") else Locale("en", "IN")
        ttsManager?.configureVoice(locale)
    }

    fun triggerAvatarWink() {
        _uiState.update { it.copy(characterExpression = CharacterExpression.WINKING) }
        speak("हाँजी! JA हमेशा आपके साथ है~ ✨")
        viewModelScope.launch {
            delay(1500)
            _uiState.update { it.copy(characterExpression = CharacterExpression.SMILING) }
        }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(pendingConfirmation = null) }
    }

    private fun addMessage(msg: ChatMessage) {
        _uiState.update { it.copy(chatMessages = it.chatMessages + msg) }
    }

    private fun recordHistory(cmd: String, resp: String, summary: String, success: Boolean) {
        val item = CommandHistoryItem(
            commandText = cmd,
            responseText = resp,
            actionSummary = summary,
            isSuccess = success
        )
        _uiState.update { it.copy(commandHistory = listOf(item) + it.commandHistory) }
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        ttsManager?.shutdown()
        speechManager?.destroy()
    }
}

package com.example.model

enum class AssistantState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR,
    COMPLETED
}

enum class CharacterExpression {
    SMILING,
    SPEAKING,
    THINKING,
    WINKING,
    LISTENING,
    HAPPY,
    APOLOGETIC
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: ActionType = ActionType.NONE,
    val actionLabel: String? = null,
    val actionPayload: String? = null
)

enum class MessageSender {
    USER,
    JA
}

enum class ActionType {
    NONE,
    APP_LAUNCH,
    WHATSAPP_DRAFT,
    VIDEO_WORKFLOW,
    CAPCUT_EDIT,
    LIVE_CALL
}

data class CommandHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val commandText: String,
    val responseText: String,
    val actionSummary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

data class VideoProject(
    val uriString: String? = null,
    val fileName: String? = null,
    val title: String = "",
    val description: String = "",
    val hashtags: List<String> = emptyList(),
    val captions: String = "",
    val targetPlatform: TargetPlatform = TargetPlatform.YOUTUBE,
    val isShortOrReel: Boolean = true,
    val musicSuggestion: String = "",
    val transitionsSuggestion: String = "",
    val thumbnailSuggestion: String = "",
    val capcutEditingNotes: String = ""
)

enum class TargetPlatform(val displayName: String, val iconRes: String, val packageName: String) {
    YOUTUBE("YouTube", "youtube", "com.google.android.youtube"),
    INSTAGRAM("Instagram", "instagram", "com.instagram.android"),
    FACEBOOK("Facebook", "facebook", "com.facebook.katana"),
    CAPCUT("CapCut", "capcut", "com.lemon.lvoverseas")
}

data class PendingConfirmation(
    val title: String,
    val message: String,
    val confirmButtonText: String,
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit = {}
)

data class VoiceSettings(
    val pitch: Float = 1.35f, // Cute anime voice pitch (> 1.0f)
    val speechRate: Float = 1.02f,
    val language: String = "hi-IN", // Hindi/Hinglish
    val wakeWordEnabled: Boolean = true,
    val autoListenAfterResponse: Boolean = true,
    val characterStyle: String = "Cyber Anime JA"
)

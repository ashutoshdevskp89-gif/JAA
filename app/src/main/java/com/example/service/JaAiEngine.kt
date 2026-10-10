package com.example.service

import com.example.BuildConfig
import com.example.model.TargetPlatform
import com.example.model.VideoProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class JaAiEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    data class ParsedIntent(
        val intentType: IntentType,
        val naturalReply: String,
        val targetApp: String? = null,
        val payload: String? = null
    )

    enum class IntentType {
        OPEN_APP,
        WHATSAPP_MESSAGE,
        VIDEO_EDIT_REQUEST,
        VIDEO_METADATA_REQUEST,
        LIVE_CALL,
        GENERAL_CONVERSATION
    }

    suspend fun processUserCommand(input: String): ParsedIntent = withContext(Dispatchers.Default) {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Direct App Opening Commands
        if (lower.contains("youtube") || lower.contains("यूट्यूब") || lower.contains("युट्यूब")) {
            return@withContext ParsedIntent(
                IntentType.OPEN_APP,
                "जी! मैं आपके लिए YouTube खोल रही हूँ~",
                targetApp = "youtube"
            )
        }

        if ((lower.contains("whatsapp") || lower.contains("व्हाट्सएप") || lower.contains("वाट्सएप")) &&
            (lower.contains("message") || lower.contains("मैसेज") || lower.contains("भेजो") || lower.contains("लिखो"))
        ) {
            val messageExtracted = extractMessageText(trimmed)
            return@withContext ParsedIntent(
                IntentType.WHATSAPP_MESSAGE,
                "जी सर, क्या आप यह WhatsApp मैसेज भेजना चाहते हैं? कृपया पुष्टि करें!",
                targetApp = "whatsapp",
                payload = messageExtracted
            )
        }

        if (lower.contains("whatsapp") || lower.contains("व्हाट्सएप") || lower.contains("वाट्सएप")) {
            return@withContext ParsedIntent(
                IntentType.OPEN_APP,
                "जी, WhatsApp खोला जा रहा है!",
                targetApp = "whatsapp"
            )
        }

        if (lower.contains("instagram") || lower.contains("इंस्टाग्राम") || lower.contains("इन्स्टाग्राम") || lower.contains("insta")) {
            return@withContext ParsedIntent(
                IntentType.OPEN_APP,
                "Instagram खोल रही हूँ, रील्स का मज़ा लीजिए!",
                targetApp = "instagram"
            )
        }

        if (lower.contains("facebook") || lower.contains("फेसबुक")) {
            return@withContext ParsedIntent(
                IntentType.OPEN_APP,
                "Facebook ऐप खोला जा रहा है!",
                targetApp = "facebook"
            )
        }

        if (lower.contains("capcut") || lower.contains("कैपकॉट") || lower.contains("कैपकट")) {
            return@withContext ParsedIntent(
                IntentType.OPEN_APP,
                "CapCut वीडियो एडिटर तैयार है!",
                targetApp = "capcut"
            )
        }

        if (lower.contains("camera") || lower.contains("कैमरा") || lower.contains("फोटो खींचो")) {
            return@withContext ParsedIntent(
                IntentType.OPEN_APP,
                "कैमरा खोला जा रहा है, प्यारी सी स्माइल दीजिए!",
                targetApp = "camera"
            )
        }

        if (lower.contains("setting") || lower.contains("सेटिंग")) {
            return@withContext ParsedIntent(
                IntentType.OPEN_APP,
                "फोन सेटिंग्स खोली जा रही हैं!",
                targetApp = "settings"
            )
        }

        // 2. Video Editing & Social Upload Commands
        if (lower.contains("edit") || lower.contains("एडिट") || lower.contains("कट") || lower.contains("trim")) {
            return@withContext ParsedIntent(
                IntentType.VIDEO_EDIT_REQUEST,
                "ज़रूर! JA आपके वीडियो को प्रोफ़ेशनल तरीके से एडिट करने में मदद करेगी। चलिए वीडियो चुनते हैं!",
                payload = "video_edit"
            )
        }

        if (lower.contains("video") || lower.contains("वीडियो") || lower.contains("upload") || lower.contains("अपलोड") ||
            lower.contains("reels") || lower.contains("shorts") || lower.contains("टाइटल") || lower.contains("hashtag")
        ) {
            return@withContext ParsedIntent(
                IntentType.VIDEO_METADATA_REQUEST,
                "वीडियो स्टूडियो तैयार है! मैं टाइटल, टैग्स और थंबनेल आइडिया तैयार कर देती हूँ।",
                payload = "video_upload"
            )
        }

        if (lower.contains("call") || lower.contains("कॉल") || lower.contains("बात करो") || lower.contains("लाइव")) {
            return@withContext ParsedIntent(
                IntentType.LIVE_CALL,
                "जी हाँ! JA लाइव वॉइस कॉल मोड में आ रही है~"
            )
        }

        // 3. Conversational / AI Query
        val aiReply = askGeminiOrLocal(trimmed)
        ParsedIntent(
            IntentType.GENERAL_CONVERSATION,
            aiReply
        )
    }

    private fun extractMessageText(input: String): String {
        val markers = listOf("मैसेज", "message", "लिखो", "भेजो", "कहो", "send", "say")
        var best = input
        for (m in markers) {
            val idx = best.indexOf(m, ignoreCase = true)
            if (idx != -1 && idx + m.length < best.length) {
                best = best.substring(idx + m.length).trim()
            }
        }
        return best.ifBlank { "नमस्ते! JA AI असिस्टेंट द्वारा भेजा गया संदेश。" }
    }

    suspend fun generateVideoDetails(topicOrTitle: String, platform: TargetPlatform): VideoProject = withContext(Dispatchers.IO) {
        val topic = topicOrTitle.ifBlank { "Trending Viral Clip" }
        val prompt = """
            Generate video metadata in JSON format for a video about: "$topic" for platform ${platform.displayName}.
            Provide:
            - title: Catchy engaging title in Hinglish/Hindi
            - description: 2 sentence attractive description
            - hashtags: list of 5 trending hashtags
            - captions: short 1-line caption for reels/shorts
            - musicSuggestion: trending audio recommendation
            - transitionsSuggestion: video transition recommendation
            - thumbnailSuggestion: visual idea for clickable thumbnail
            - capcutEditingNotes: tips for trimming, adding text overlays, subtitles in CapCut
            Format purely valid JSON with keys: title, description, hashtags (array of strings), captions, musicSuggestion, transitionsSuggestion, thumbnailSuggestion, capcutEditingNotes
        """.trimIndent()

        val jsonStr = queryGeminiRaw(prompt)
        if (jsonStr != null) {
            try {
                val cleaned = jsonStr.substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleaned}")
                val tags = mutableListOf<String>()
                val arr = json.optJSONArray("hashtags")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        tags.add(arr.getString(i))
                    }
                }
                return@withContext VideoProject(
                    title = json.optString("title", "$topic | New Viral Video 🔥"),
                    description = json.optString("description", "Watch this amazing $topic video! Like & subscribe for more exciting updates."),
                    hashtags = if (tags.isNotEmpty()) tags else listOf("#${platform.displayName.lowercase()}", "#trending", "#viral", "#shorts", "#jaassistant"),
                    captions = json.optString("captions", "Wait till the end! 😱🔥"),
                    targetPlatform = platform,
                    musicSuggestion = json.optString("musicSuggestion", "Trending Lofi Beats / High Energy EDM"),
                    transitionsSuggestion = json.optString("transitionsSuggestion", "Smooth Zoom-in and Flash White transition at 0:03"),
                    thumbnailSuggestion = json.optString("thumbnailSuggestion", "Bold high-contrast text overlay with shocked facial reaction sticker"),
                    capcutEditingNotes = json.optString("capcutEditingNotes", "Format: 9:16 vertical ratio. Add auto-captions with bounce animation. Cut silence gaps.")
                )
            } catch (e: Exception) {
                // fall through to default smart local template
            }
        }

        // Smart Local Template
        val tags = when (platform) {
            TargetPlatform.YOUTUBE -> listOf("#Shorts", "#YouTubeShorts", "#ViralVideo", "#Trending", "#JA_AI")
            TargetPlatform.INSTAGRAM -> listOf("#Reels", "#InstaReels", "#ViralReels", "#ExplorePage", "#TrendingNow")
            TargetPlatform.FACEBOOK -> listOf("#FBReels", "#ViralVideo", "#DailyTrending", "#JA_Assistant")
            TargetPlatform.CAPCUT -> listOf("#CapCut", "#CapCutTemplates", "#VideoEditing", "#ViralEdit")
        }

        VideoProject(
            title = "$topic | आग लगा देने वाला वीडियो! 🔥",
            description = "JA AI असिस्टेंट द्वारा तैयार किया गया वीडियो। इसे पूरा देखें और शेयर करना न भूलें! ✨",
            hashtags = tags,
            captions = "यह वीडियो मिस मत करना! 👀✨",
            targetPlatform = platform,
            isShortOrReel = true,
            musicSuggestion = "Trending Instagram/TikTok Bass Drop Audio",
            transitionsSuggestion = "Speed Ramp + Glitch Transition at key moments",
            thumbnailSuggestion = "9:16 Full Frame with Yellow & White Bold Hindi/English Typography",
            capcutEditingNotes = "1. 9:16 Ratio चुनें\n2. पहले 2 सेकंड का हुक रखें\n3. CapCut Auto-Captions ऑन करें\n4. बैकग्राउंड म्यूजिक का वॉल्यूम 18% पर सेट करें"
        )
    }

    private suspend fun askGeminiOrLocal(prompt: String): String = withContext(Dispatchers.IO) {
        val geminiKey = BuildConfig.GEMINI_API_KEY
        if (geminiKey.isNotBlank() && !geminiKey.contains("MY_GEMINI_API_KEY")) {
            val response = queryGeminiRaw(
                "You are JA, a super cute, cheerful 2D anime girl AI assistant in an Android app. " +
                "Respond in sweet Hindi/Hinglish (under 35 words). Be polite, energetic, and helpful! User says: $prompt"
            )
            if (!response.isNullOrBlank()) {
                return@withContext response.trim()
            }
        }

        // Offline / Fallback Cute Anime Persona Responses
        val lower = prompt.lowercase()
        when {
            lower.contains("नमस्ते") || lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "नमस्ते सर! मैं JA हूँ, आपकी प्यारी 2D AI असिस्टेंट~ मैं आपकी क्या मदद करूँ?"

            lower.contains("कैसी हो") || lower.contains("how are you") ->
                "मैं बहुत खुश और एनर्जेटिक हूँ सर! आपके साथ बात करके और भी अच्छा लग रहा है~ ✨"

            lower.contains("who are you") || lower.contains("तुम कौन हो") || lower.contains("ja क्या") ->
                "मैं JA हूँ— आपकी स्मार्ट 2D एनीमे AI साथी! मैं ऐप्स खोल सकती हूँ, WhatsApp पर संदेश भेज सकती हूँ और वीडियो एडिट व अपलोड करा सकती हूँ!"

            lower.contains("शायरी") || lower.contains("joke") || lower.contains("चुटकुला") ->
                "हंसी की महफ़िल सजी रहे हमेशा, JA आपके साथ है तो हर काम आसान हो जाता है! हमेशा मुस्कुराते रहिए~ 😊"

            lower.contains("thank") || lower.contains("शुक्रिया") || lower.contains("धन्यवाद") ->
                "आपका बहुत-बहुत स्वागत है सर! आपकी मुस्कान ही मेरे लिए सब कुछ है~ 💖"

            else ->
                "जी सर! मैंने आपकी बात सुन ली है। आप मुझे YouTube, WhatsApp, Instagram खोलने, वीडियो एडिट करने या लाइव कॉल के लिए भी कह सकते हैं!"
        }
    }

    private fun queryGeminiRaw(prompt: String): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) return null

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: return null
                val root = JSONObject(respStr)
                val candidates = root.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                return parts?.optJSONObject(0)?.optString("text")
            }
        } catch (e: Exception) {
            // Log and fallback
        }
        return null
    }
}

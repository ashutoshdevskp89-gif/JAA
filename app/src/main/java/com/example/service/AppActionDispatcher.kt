package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import com.example.model.TargetPlatform
import java.net.URLEncoder

class AppActionDispatcher(private val context: Context) {

    data class ActionResult(
        val isSuccess: Boolean,
        val feedbackMessage: String,
        val openedTarget: String
    )

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openYouTube(): ActionResult {
        val ytPackage = "com.google.android.youtube"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(ytPackage)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult(true, "YouTube खोल दिया गया है, सर!", "YouTube App")
        } else {
            // Fallback to web
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ActionResult(true, "YouTube ऐप नहीं मिला, ब्राउज़र में खोल दिया गया है!", "YouTube Web")
        }
    }

    fun openWhatsApp(): ActionResult {
        val waPackage = "com.whatsapp"
        val waBusiness = "com.whatsapp.w4b"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(waPackage)
            ?: context.packageManager.getLaunchIntentForPackage(waBusiness)

        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult(true, "WhatsApp खोल दिया गया है!", "WhatsApp App")
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ActionResult(true, "WhatsApp ऐप नहीं मिला, वेब पेज खोल दिया गया है!", "WhatsApp Web")
        }
    }

    fun openInstagram(): ActionResult {
        val igPackage = "com.instagram.android"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(igPackage)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult(true, "Instagram खोल दिया गया है!", "Instagram App")
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ActionResult(true, "Instagram ऐप नहीं मिला, ब्राउज़र में खोल दिया गया है!", "Instagram Web")
        }
    }

    fun openFacebook(): ActionResult {
        val fbPackage = "com.facebook.katana"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(fbPackage)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult(true, "Facebook खोल दिया गया है!", "Facebook App")
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ActionResult(true, "Facebook ऐप नहीं मिला, ब्राउज़र में खोल दिया गया है!", "Facebook Web")
        }
    }

    fun openCapCut(): ActionResult {
        val capcutPackage = "com.lemon.lvoverseas"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(capcutPackage)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult(true, "CapCut वीडियो एडिटर खोल दिया गया है!", "CapCut App")
        } else {
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$capcutPackage")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(playStoreIntent)
                ActionResult(true, "CapCut इनस्टॉल करने के लिए प्ले स्टोर पेज खोल दिया गया है!", "Play Store (CapCut)")
            } catch (e: Exception) {
                ActionResult(false, "CapCut ऐप आपके डिवाइस में नहीं मिला।", "CapCut")
            }
        }
    }

    fun openCamera(): ActionResult {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionResult(true, "कैमरा खोल दिया गया है!", "Camera")
        } catch (e: Exception) {
            ActionResult(false, "कैमरा खोलने में समस्या हुई: ${e.message}", "Camera")
        }
    }

    fun openSettings(): ActionResult {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionResult(true, "फोन सेटिंग्स खोल दी गई हैं!", "Settings")
        } catch (e: Exception) {
            ActionResult(false, "सेटिंग्स नहीं खुलीं", "Settings")
        }
    }

    fun sendWhatsAppDraft(message: String, phoneNumber: String? = null): ActionResult {
        val encodedMsg = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }
        val url = if (!phoneNumber.isNullOrBlank()) {
            val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
        } else {
            "https://api.whatsapp.com/send?text=$encodedMsg"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            ActionResult(true, "WhatsApp में मैसेज तैयार कर दिया गया है!", "WhatsApp Draft")
        } catch (e: Exception) {
            // Fallback generic send
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(Intent.createChooser(sendIntent, "मैसेज भेजें").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                ActionResult(true, "मैसेज शेयरिंग स्क्रीन खोल दी गई है!", "Share Message")
            } catch (err: Exception) {
                ActionResult(false, "WhatsApp मैसेज खोलने में त्रुटि हुई", "WhatsApp")
            }
        }
    }

    fun shareVideoToPlatform(videoUri: Uri, platform: TargetPlatform, caption: String): ActionResult {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, videoUri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val targetPackage = platform.packageName
        if (isAppInstalled(targetPackage)) {
            shareIntent.setPackage(targetPackage)
        }

        return try {
            val chooser = Intent.createChooser(shareIntent, "${platform.displayName} पर वीडियो अपलोड करें").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            ActionResult(true, "${platform.displayName} अपलोड वर्कफ़्लो शुरू हो गया है!", platform.displayName)
        } catch (e: Exception) {
            Log.e("AppActionDispatcher", "Share video failed", e)
            ActionResult(false, "वीडियो शेयर करने में समस्या हुई: ${e.message}", platform.displayName)
        }
    }
}

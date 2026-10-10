package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.JaAiEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JA", appName)
  }

  @Test
  fun `test ja ai engine parses youtube command`() = runBlocking {
    val engine = JaAiEngine()
    val intent = engine.processUserCommand("YouTube खोलो")
    assertEquals(JaAiEngine.IntentType.OPEN_APP, intent.intentType)
    assertEquals("youtube", intent.targetApp)
  }

  @Test
  fun `test ja ai engine parses whatsapp message draft command`() = runBlocking {
    val engine = JaAiEngine()
    val intent = engine.processUserCommand("WhatsApp पर मैसेज लिखो कल मिलते हैं")
    assertEquals(JaAiEngine.IntentType.WHATSAPP_MESSAGE, intent.intentType)
    assertEquals("whatsapp", intent.targetApp)
    assertNotNull(intent.payload)
  }

  @Test
  fun `test ja ai engine parses capcut edit command`() = runBlocking {
    val engine = JaAiEngine()
    val intent = engine.processUserCommand("JA, इस वीडियो को एडिट कर दो")
    assertEquals(JaAiEngine.IntentType.VIDEO_EDIT_REQUEST, intent.intentType)
  }
}

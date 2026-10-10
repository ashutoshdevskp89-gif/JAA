package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AssistantState
import com.example.model.CharacterExpression
import com.example.ui.theme.JaCyanAccent
import com.example.ui.theme.JaPinkAccent
import com.example.ui.theme.JaPurpleLight
import com.example.ui.theme.JaPurplePrimary
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AnimeCharacterView(
    state: AssistantState,
    expression: CharacterExpression,
    isSpeaking: Boolean,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier,
    height: Dp = 280.dp,
    onAvatarTapped: () -> Unit = {}
) {
    // 1. Natural Eye Blinking Coroutine
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            // Blink every 2.5 to 5 seconds
            val delayMs = Random.nextLong(2500, 5000)
            delay(delayMs)
            isBlinking = true
            delay(130)
            isBlinking = false
        }
    }

    // 2. Lip-Sync Mouth Opening State
    val mouthAnim = remember { Animatable(0f) }
    LaunchedEffect(isSpeaking) {
        if (isSpeaking) {
            while (isSpeaking) {
                // Rapid phoneme movement simulation
                mouthAnim.animateTo(
                    targetValue = Random.nextFloat() * 0.9f + 0.1f,
                    animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing)
                )
                mouthAnim.animateTo(
                    targetValue = 0.05f,
                    animationSpec = tween(durationMillis = 90, easing = LinearEasing)
                )
            }
        } else {
            mouthAnim.snapTo(0f)
        }
    }

    // 3. Gentle Breathing / Floating Animation
    val infiniteTransition = rememberInfiniteTransition(label = "anime_character_breathing")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("anime_character_container")
            .clickable { onAvatarTapped() },
        contentAlignment = Alignment.Center
    ) {
        // Outer Cyber Hologram Glow Ring
        Box(
            modifier = Modifier
                .size(height - 16.dp)
                .scale(1f + (audioLevel * 0.12f))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            when (state) {
                                AssistantState.LISTENING -> JaCyanAccent.copy(alpha = 0.35f * glowAlpha)
                                AssistantState.SPEAKING -> JaPinkAccent.copy(alpha = 0.4f * glowAlpha)
                                AssistantState.PROCESSING -> JaPurpleLight.copy(alpha = 0.35f * glowAlpha)
                                else -> JaPurplePrimary.copy(alpha = 0.25f * glowAlpha)
                            },
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Character Frame with Rounded Anime Glow Border
        Box(
            modifier = Modifier
                .size(height - 24.dp)
                .offset(y = floatOffset.dp)
                .shadow(elevation = 16.dp, shape = CircleShape, spotColor = JaCyanAccent)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            JaPurpleLight,
                            JaCyanAccent,
                            JaPinkAccent,
                            JaPurpleLight
                        )
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(Color(0xFF140D2B))
        ) {
            // High-Res 2D Anime Girl Character Image
            Image(
                painter = painterResource(id = R.drawable.ja_character_idle_1791600437783),
                contentDescription = "JA Anime AI Assistant",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("ja_avatar_image")
            )

            // Animated Canvas Overlay for Live Blinking, Lip-Sync Mouth, and Expressions!
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("ja_expression_overlay")
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Calibrated Face Coordinate Anchors based on the portrait composition:
                // Head center is around (w * 0.49, h * 0.30)
                // Left Eye ~ (w * 0.41, h * 0.27), Right Eye ~ (w * 0.56, h * 0.27)
                // Mouth ~ (w * 0.49, h * 0.36)
                // Cheeks ~ (w * 0.39, h * 0.31), (w * 0.58, h * 0.31)

                val leftEyeX = canvasW * 0.415f
                val rightEyeX = canvasW * 0.565f
                val eyeY = canvasH * 0.285f
                val eyeWidth = canvasW * 0.075f

                val mouthX = canvasW * 0.49f
                val mouthY = canvasH * 0.362f

                // A. Eye Blinking / Expression Overlay
                if (isBlinking || expression == CharacterExpression.WINKING) {
                    // Right eye winks or both blink shut
                    val blinkColor = Color(0xFF283648)
                    // Draw closed eye cute curve for Left Eye
                    if (isBlinking) {
                        val eyePathLeft = Path().apply {
                            moveTo(leftEyeX - eyeWidth * 0.5f, eyeY)
                            quadraticTo(leftEyeX, eyeY + 4f, leftEyeX + eyeWidth * 0.5f, eyeY - 2f)
                        }
                        drawPath(eyePathLeft, blinkColor, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
                    }

                    // Draw closed eye cute curve for Right Eye (always when winking or blinking)
                    val eyePathRight = Path().apply {
                        moveTo(rightEyeX - eyeWidth * 0.5f, eyeY - 2f)
                        quadraticTo(rightEyeX, eyeY + 4f, rightEyeX + eyeWidth * 0.5f, eyeY)
                    }
                    drawPath(eyePathRight, blinkColor, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
                }

                // B. Animated Lip-Sync Mouth Movement
                val mouthOpenAmount = mouthAnim.value
                if (mouthOpenAmount > 0.08f) {
                    val mouthW = canvasW * (0.045f + mouthOpenAmount * 0.04f)
                    val mouthH = canvasH * (0.015f + mouthOpenAmount * 0.045f)

                    // Mouth inner cavity
                    val mouthPath = Path().apply {
                        moveTo(mouthX - mouthW * 0.5f, mouthY - mouthH * 0.2f)
                        quadraticTo(mouthX, mouthY + mouthH * 1.2f, mouthX + mouthW * 0.5f, mouthY - mouthH * 0.2f)
                        quadraticTo(mouthX, mouthY - mouthH * 0.3f, mouthX - mouthW * 0.5f, mouthY - mouthH * 0.2f)
                    }
                    drawPath(mouthPath, Color(0xFFC2185B), style = Fill)

                    // Cute little white teeth highlight
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(mouthX - mouthW * 0.3f, mouthY - mouthH * 0.25f),
                        size = androidx.compose.ui.geometry.Size(mouthW * 0.6f, mouthH * 0.45f)
                    )

                    // Mouth outline
                    drawPath(mouthPath, Color(0xFF531127), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                }

                // C. Cute Anime Cheek Blush (Pulsing when speaking or happy)
                val blushAlpha = if (isSpeaking || expression == CharacterExpression.HAPPY) 0.45f else 0.2f
                val blushRadius = canvasW * 0.045f
                drawCircle(
                    color = JaPinkAccent.copy(alpha = blushAlpha),
                    center = Offset(canvasW * 0.38f, canvasH * 0.315f),
                    radius = blushRadius
                )
                drawCircle(
                    color = JaPinkAccent.copy(alpha = blushAlpha),
                    center = Offset(canvasW * 0.60f, canvasH * 0.315f),
                    radius = blushRadius
                )

                // D. Cute Anime Sparkles when excited / winking
                if (expression == CharacterExpression.WINKING || state == AssistantState.COMPLETED) {
                    val starCenter = Offset(canvasW * 0.68f, canvasH * 0.25f)
                    drawCircle(Color(0xFFFFEE58), radius = 6f, center = starCenter)
                    drawLine(
                        color = Color(0xFFFFEE58),
                        start = Offset(starCenter.x - 12f, starCenter.y),
                        end = Offset(starCenter.x + 12f, starCenter.y),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFFFEE58),
                        start = Offset(starCenter.x, starCenter.y - 12f),
                        end = Offset(starCenter.x, starCenter.y + 12f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Status Badge Pill
        val badgeText = when (state) {
            AssistantState.LISTENING -> "🎧 सुन रही हूँ..."
            AssistantState.PROCESSING -> "✨ सोच रही हूँ..."
            AssistantState.SPEAKING -> "💖 बोल रही हूँ~"
            AssistantState.COMPLETED -> "✅ काम हो गया!"
            AssistantState.ERROR -> "⚠️ समस्या हुई"
            AssistantState.IDLE -> "🌸 JA AI Active"
        }
        val badgeColor = when (state) {
            AssistantState.LISTENING -> JaCyanAccent
            AssistantState.PROCESSING -> JaPurpleLight
            AssistantState.SPEAKING -> JaPinkAccent
            AssistantState.COMPLETED -> Color(0xFF00E676)
            AssistantState.ERROR -> Color(0xFFFF5252)
            AssistantState.IDLE -> JaPurpleLight
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 12.dp)
                .background(Color(0xEE160F30), RoundedCornerShape(20.dp))
                .border(1.5.dp, badgeColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = badgeText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

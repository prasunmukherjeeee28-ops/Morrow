package com.morrow.ai

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlin.random.Random

// ---------- Colors ----------

private val MorrowBackground = Color(0xFF080A13)
private val MorrowSurface = Color(0xFF111421)
private val MorrowSurfaceRaised = Color(0xFF191D30)
private val MorrowSidebar = Color(0xFF0C0F1B)
private val MorrowAccentDefault = Color(0xFFB7A0FF)
private val MorrowVioletDeep = Color(0xFF55468F)
private val MorrowCoreLight = Color(0xFFF0E9FF)
private val MorrowText = Color(0xFFF5F3FF)
private val MorrowMuted = Color(0xFF9EA4C4)
private val MorrowBorder = Color(0xFF292D43)
private val MorrowSelected = Color(0xFF211E38)
private val MorrowChipFill = Color(0xFF17192A)
private val MorrowChipBorder = Color(0xFF343550)
private val MorrowCodeBg = Color(0xFF0C1020)
private val MorrowDanger = Color(0xFFE18484)

private val MorrowAccentChoices = listOf(
    Color(0xFFB7A0FF), // Soft violet
    Color(0xFFE1A4D6), // Muted rose
    Color(0xFFE8BE8A), // Warm amber
    Color(0xFF97D9C0)  // Mint
)
private val MorrowAccentNames = listOf("Violet", "Rose", "Amber", "Emerald")

private val LocalMorrowAccent = compositionLocalOf { MorrowAccentDefault }

// The Gemini API key stays in Cloudflare Worker secrets, never inside this app.
private const val MORROW_CHAT_ENDPOINT =
    "https://morrow-backend.prasunmukherjeeee28.workers.dev/chat"

private val TwoPi = 2f * PI.toFloat()

// ---------- Limits ----------

private const val MAX_ATTACHMENTS = 5
private const val MAX_IMAGE_SIDE = 1024
private const val MAX_TEXT_BYTES = 200_000
private const val MAX_PDF_BYTES = 20 * 1024 * 1024
private val PDFBoxLock = Any()
@Volatile private var pdfBoxInitialised = false

// ---------- Storage keys ----------

private const val WELCOME_ID = "welcome"
private const val PREFS_CHATS = "morrow_chats"
private const val PREFS_SETTINGS = "morrow_settings"
private const val KEY_CHATS_JSON = "chats_json"
private const val KEY_LANDSCAPE = "landscape_on_v2"
private const val KEY_ACCENT = "accent_index"

// ---------- Prompt marquee tuning ----------

private const val MarqueeSpeedDpPerSecond = 40f
private val MarqueeChipGap = 10.dp
private val MarqueeRowHeight = 46.dp
private val MarqueeEdgeFade = Brush.horizontalGradient(
    0f to Color.Transparent,
    0.12f to Color.Black,
    0.88f to Color.Black,
    1f to Color.Transparent
)

private val MarqueeRowOnePrompts = listOf(
    "Teach me Python from the beginning",
    "Explain a hard idea simply",
    "Quiz me on world history",
    "Draft a message to a friend",
    "Brainstorm names for my app"
)

private val MarqueeRowTwoPrompts = listOf(
    "Give me three creative project ideas",
    "Help me plan a project step by step",
    "Summarize this topic in five points",
    "Write a short story about the sea",
    "Compare two options for me"
)

private val MarqueeRowThreePrompts = listOf(
    "Help me build a daily study routine",
    "Turn my notes into a checklist",
    "Explain how the internet works",
    "Suggest a weekend itinerary",
    "Help me prepare for an interview"
)

// ---------- Data ----------

enum class MorrowSender { USER, ASSISTANT }

/**
 * payload: base64 image or text file content, used only when sending. Not saved to disk.
 * thumbnail: small preview for the chat. Not saved to disk.
 */
data class MorrowAttachment(
    val name: String,
    val mime: String,
    val isImage: Boolean,
    val payload: String? = null,
    val thumbnail: ImageBitmap? = null
)

data class MorrowChatMessage(
    val id: String,
    val text: String,
    val sender: MorrowSender,
    val attachments: List<MorrowAttachment> = emptyList(),
    val feedback: Int = 0 // 1 = thumbs up, -1 = thumbs down, 0 = none
)

data class MorrowConversationSession(
    val id: String,
    val title: String,
    val pinned: Boolean = false,
    val temporary: Boolean = false
)

private data class SavedState(
    val conversations: List<MorrowConversationSession>,
    val messages: Map<String, List<MorrowChatMessage>>,
    val activeId: String
)

private fun defaultState() = SavedState(
    conversations = listOf(MorrowConversationSession(WELCOME_ID, "New conversation")),
    messages = mapOf(WELCOME_ID to emptyList()),
    activeId = WELCOME_ID
)

private fun loadChats(context: Context): SavedState {
    val raw = context.getSharedPreferences(PREFS_CHATS, Context.MODE_PRIVATE)
        .getString(KEY_CHATS_JSON, null) ?: return defaultState()
    return runCatching {
        val root = JSONObject(raw)
        val chats = root.getJSONArray("chats")
        val conversations = mutableListOf<MorrowConversationSession>()
        val messages = mutableMapOf<String, List<MorrowChatMessage>>()
        for (i in 0 until chats.length()) {
            val chat = chats.getJSONObject(i)
            if (chat.optBoolean("temporary", false)) continue
            val id = chat.getString("id")
            conversations += MorrowConversationSession(
                id = id,
                title = chat.getString("title"),
                pinned = chat.optBoolean("pinned", false)
            )
            val array = chat.getJSONArray("messages")
            messages[id] = (0 until array.length()).map { j ->
                val m = array.getJSONObject(j)
                val attachmentArray = m.optJSONArray("attachments")
                val attachments: List<MorrowAttachment> = if (attachmentArray == null) {
                    emptyList()
                } else {
                    (0 until attachmentArray.length()).map { k ->
                        val a = attachmentArray.getJSONObject(k)
                        MorrowAttachment(
                            name = a.getString("name"),
                            mime = a.getString("mime"),
                            isImage = a.optBoolean("isImage", false)
                        )
                    }
                }
                MorrowChatMessage(
                    id = m.getString("id"),
                    text = m.getString("text"),
                    sender = MorrowSender.valueOf(m.getString("sender")),
                    attachments = attachments,
                    feedback = m.optInt("feedback", 0)
                )
            }
        }
        if (conversations.isEmpty()) return@runCatching defaultState()
        val active = root.optString("active", conversations.first().id)
        SavedState(
            conversations = conversations,
            messages = messages,
            activeId = if (active in messages) active else conversations.first().id
        )
    }.getOrElse { defaultState() }
}

private fun saveChats(context: Context, state: SavedState) {
    // Temporary chats are intentionally excluded from on-device persistence.
    val persistentConversations = state.conversations.filterNot { it.temporary }
    val conversationsToSave = persistentConversations.ifEmpty {
        listOf(MorrowConversationSession(WELCOME_ID, "New conversation"))
    }
    val chats = JSONArray()
    conversationsToSave.forEach { conversation ->
        val messages = JSONArray()
        state.messages[conversation.id].orEmpty().forEach { m ->
            val attachments = JSONArray()
            m.attachments.forEach { a ->
                attachments.put(
                    JSONObject()
                        .put("name", a.name)
                        .put("mime", a.mime)
                        .put("isImage", a.isImage)
                )
            }
            messages.put(
                JSONObject()
                    .put("id", m.id)
                    .put("text", m.text)
                    .put("sender", m.sender.name)
                    .put("feedback", m.feedback)
                    .put("attachments", attachments)
            )
        }
        chats.put(
            JSONObject()
                .put("id", conversation.id)
                .put("title", conversation.title)
                .put("pinned", conversation.pinned)
                .put("temporary", false)
                .put("messages", messages)
        )
    }
    val savedActiveId = if (conversationsToSave.any { it.id == state.activeId }) {
        state.activeId
    } else {
        conversationsToSave.first().id
    }
    val root = JSONObject()
        .put("active", savedActiveId)
        .put("chats", chats)
    context.getSharedPreferences(PREFS_CHATS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_CHATS_JSON, root.toString())
        .apply()
}

// ---------- Time of day ----------

private data class SkyPalette(
    val skyTop: Color,
    val skyMid: Color,
    val skyBottom: Color,
    val hillFar: Color,
    val hillMid: Color,
    val hillNear: Color,
    val celestial: Color,
    val glow: Color,
    val celestialIsSun: Boolean,
    val celestialX: Float,
    val celestialY: Float,
    val starAlpha: Float
)

private enum class MorrowPhase(
    val label: String,
    val greeting: String,
    val headline: String,
    val palette: SkyPalette
) {
    MIDNIGHT(
        "Midnight", "Good night", "The city is quiet. What's on your mind?",
        SkyPalette(
            Color(0xFF05061A), Color(0xFF0B0B2E), Color(0xFF12103A),
            Color(0xFF1A1245), Color(0xFF120C35), Color(0xFF07051A),
            Color(0xFFDCD3FF), Color(0xFF7C5CFF), false, 0.78f, 0.18f, 1f
        )
    ),
    DAWN(
        "Dawn", "Early morning", "A new day is breaking. Where should we begin?",
        SkyPalette(
            Color(0xFF2B1B63), Color(0xFF7B3A8C), Color(0xFFE98A7A),
            Color(0xFF6B3370), Color(0xFF4A2459), Color(0xFF2B1538),
            Color(0xFFFFD3A6), Color(0xFFFF9F7A), true, 0.25f, 0.62f, 0.15f
        )
    ),
    MORNING(
        "Morning", "Good morning", "Good morning. What should we build today?",
        SkyPalette(
            Color(0xFF3B2F9E), Color(0xFF7A6BE0), Color(0xFFD9C4F5),
            Color(0xFF6A5BC4), Color(0xFF4B3CA0), Color(0xFF2E2378),
            Color(0xFFFFF1D6), Color(0xFFFFD08A), true, 0.72f, 0.3f, 0f
        )
    ),
    NOON(
        "Midday", "Hello", "Midday check-in. What can I help with?",
        SkyPalette(
            Color(0xFF4A3AB8), Color(0xFF8F7BEA), Color(0xFFD7CCFA),
            Color(0xFF7C6BD0), Color(0xFF5A4AB0), Color(0xFF3A2C86),
            Color(0xFFFFFFFF), Color(0xFFFFE6A8), true, 0.5f, 0.16f, 0f
        )
    ),
    AFTERNOON(
        "Afternoon", "Good afternoon", "Good afternoon. Ready to dig in?",
        SkyPalette(
            Color(0xFF3A2A86), Color(0xFF6E4BAA), Color(0xFFD899C0),
            Color(0xFF7A4BA0), Color(0xFF55368A), Color(0xFF352060),
            Color(0xFFFFE2B0), Color(0xFFFFA98A), true, 0.32f, 0.26f, 0f
        )
    ),
    SUNSET(
        "Sunset", "Good evening", "The day is winding down. What are we working on?",
        SkyPalette(
            Color(0xFF241257), Color(0xFF6A2C7E), Color(0xFFF27D6A),
            Color(0xFF5A2466), Color(0xFF3E1A52), Color(0xFF220D33),
            Color(0xFFFFB27A), Color(0xFFFF7A59), true, 0.22f, 0.64f, 0.2f
        )
    ),
    DUSK(
        "Dusk", "Good evening", "Evening is here. What would you like to explore?",
        SkyPalette(
            Color(0xFF1A0F4A), Color(0xFF3A1D6E), Color(0xFF8A4AA8),
            Color(0xFF3A1D66), Color(0xFF2A1450), Color(0xFF160A34),
            Color(0xFFE6DCFF), Color(0xFF9C7CFF), false, 0.8f, 0.22f, 0.6f
        )
    ),
    NIGHT(
        "Night", "Good evening", "Good evening. Let's think something through.",
        SkyPalette(
            Color(0xFF080A1E), Color(0xFF140E3A), Color(0xFF1E1355),
            Color(0xFF1F1650), Color(0xFF140E3C), Color(0xFF080620),
            Color(0xFFE4DBFF), Color(0xFF7C5CFF), false, 0.8f, 0.2f, 1f
        )
    )
}

private data class MorrowClock(val phase: MorrowPhase, val timeText: String)

private fun phaseForHour(hour: Int): MorrowPhase = when (hour) {
    in 0..3 -> MorrowPhase.MIDNIGHT
    in 4..6 -> MorrowPhase.DAWN
    in 7..10 -> MorrowPhase.MORNING
    in 11..13 -> MorrowPhase.NOON
    in 14..16 -> MorrowPhase.AFTERNOON
    in 17..18 -> MorrowPhase.SUNSET
    in 19..20 -> MorrowPhase.DUSK
    else -> MorrowPhase.NIGHT
}

private fun currentClock(): MorrowClock {
    val now = Calendar.getInstance()
    val phase = phaseForHour(now.get(Calendar.HOUR_OF_DAY))
    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(now.time)
    return MorrowClock(phase, time)
}

/** Glides every palette value between phases, or snaps when animation is off. */
@Composable
private fun animatedPalette(target: SkyPalette, animate: Boolean): SkyPalette {
    val colorSpec: AnimationSpec<Color> = if (animate) tween(2000, easing = FastOutSlowInEasing) else snap()
    val floatSpec: AnimationSpec<Float> = if (animate) tween(2000, easing = FastOutSlowInEasing) else snap()

    val skyTop by animateColorAsState(target.skyTop, colorSpec, label = "skyTop")
    val skyMid by animateColorAsState(target.skyMid, colorSpec, label = "skyMid")
    val skyBottom by animateColorAsState(target.skyBottom, colorSpec, label = "skyBottom")
    val hillFar by animateColorAsState(target.hillFar, colorSpec, label = "hillFar")
    val hillMid by animateColorAsState(target.hillMid, colorSpec, label = "hillMid")
    val hillNear by animateColorAsState(target.hillNear, colorSpec, label = "hillNear")
    val celestial by animateColorAsState(target.celestial, colorSpec, label = "celestial")
    val glow by animateColorAsState(target.glow, colorSpec, label = "glow")
    val celestialX by animateFloatAsState(target.celestialX, floatSpec, label = "celestialX")
    val celestialY by animateFloatAsState(target.celestialY, floatSpec, label = "celestialY")
    val starAlpha by animateFloatAsState(target.starAlpha, floatSpec, label = "starAlpha")

    return SkyPalette(
        skyTop = skyTop,
        skyMid = skyMid,
        skyBottom = skyBottom,
        hillFar = hillFar,
        hillMid = hillMid,
        hillNear = hillNear,
        celestial = celestial,
        glow = glow,
        celestialIsSun = target.celestialIsSun,
        celestialX = celestialX,
        celestialY = celestialY,
        starAlpha = starAlpha
    )
}

// ---------- Activity ----------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = MorrowAccentDefault,
                    background = MorrowBackground,
                    surface = MorrowSurface,
                    onBackground = MorrowText,
                    onSurface = MorrowText
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MorrowBackground
                ) {
                    MorrowMainScreen()
                }
            }
        }
    }
}

// ---------- Main screen ----------

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MorrowMainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val settingsPrefs = remember { context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE) }

    var landscapeOn by remember { mutableStateOf(settingsPrefs.getBoolean(KEY_LANDSCAPE, false)) }
    var accentIndex by remember { mutableIntStateOf(settingsPrefs.getInt(KEY_ACCENT, 0)) }
    val accent = MorrowAccentChoices[accentIndex.coerceIn(0, MorrowAccentChoices.lastIndex)]

    var showSettings by remember { mutableStateOf(false) }
    var showExplore by remember { mutableStateOf(false) }
    var showLibrary by remember { mutableStateOf(false) }
    var showProDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var chatActionsId by remember { mutableStateOf<String?>(null) }
    var renameChatId by remember { mutableStateOf<String?>(null) }
    var renameChatValue by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }

    // Chat state
    val saved = remember { loadChats(context) }
    val conversations = remember {
        mutableStateListOf<MorrowConversationSession>().also { it.addAll(saved.conversations) }
    }
    val messagesByConversation = remember {
        mutableStateMapOf<String, List<MorrowChatMessage>>().also { it.putAll(saved.messages) }
    }
    var activeId by remember { mutableStateOf(saved.activeId) }
    var loadingConversationIds by remember { mutableStateOf(setOf<String>()) }
    val generationJobs = remember { mutableMapOf<String, Job>() }

    // Composer attachments
    var attachmentsOpen by remember { mutableStateOf(false) }
    var pendingAttachments by remember { mutableStateOf(listOf<MorrowAttachment>()) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Reply typing and speech
    var typingMessageId by remember { mutableStateOf<String?>(null) }
    var typingSkip by remember { mutableStateOf(false) }
    var speakingId by remember { mutableStateOf<String?>(null) }

    val clock by produceState(initialValue = currentClock()) {
        while (true) {
            value = currentClock()
            delay(30_000L)
        }
    }
    val palette = animatedPalette(clock.phase.palette, animate = landscapeOn)

    val currentMessages = messagesByConversation[activeId].orEmpty()
    val activeConversation = conversations.firstOrNull { it.id == activeId }
    val isLoading = activeId in loadingConversationIds
    val isTyping = typingMessageId != null
    val listState = rememberLazyListState()
    val voice = rememberVoiceInput { spoken ->
        input = if (input.isBlank()) spoken else "$input $spoken"
    }
    val speaker = rememberSpeaker { utteranceId ->
        if (speakingId == utteranceId) speakingId = null
    }

    // Persist every change to conversations, messages, or the active chat.
    LaunchedEffect(conversations.toList(), messagesByConversation.toMap(), activeId) {
        val snapshot = SavedState(conversations.toList(), messagesByConversation.toMap(), activeId)
        withContext(Dispatchers.IO) { saveChats(context, snapshot) }
    }

    LaunchedEffect(activeId, currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.lastIndex)
        }
    }

    fun createNewChat(temporary: Boolean = false) {
        val id = System.currentTimeMillis().toString()
        conversations.add(
            0,
            MorrowConversationSession(
                id = id,
                title = if (temporary) "Temporary chat" else "New conversation",
                temporary = temporary
            )
        )
        messagesByConversation[id] = emptyList()
        activeId = id
        input = ""
        pendingAttachments = emptyList()
        attachmentsOpen = false
        typingMessageId = null
        typingSkip = false
    }

    fun addAttachments(uris: List<Uri>) {
        if (uris.isEmpty()) return
        scope.launch {
            val prepared = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri -> runCatching { prepareAttachment(context, uri) }.getOrNull() }
            }
            pendingAttachments = (pendingAttachments + prepared).take(MAX_ATTACHMENTS)
            attachmentsOpen = false
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_ATTACHMENTS)
    ) { uris -> addAttachments(uris) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> addAttachments(uris) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { captured ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (captured && uri != null) addAttachments(listOf(uri))
    }

    fun openCamera() {
        attachmentsOpen = false
        val uri = runCatching { newCameraUri(context) }.getOrNull() ?: return
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    fun sendPrompt(prompt: String, attachments: List<MorrowAttachment>) {
        val targetChatId = activeId
        if ((prompt.isBlank() && attachments.isEmpty()) || targetChatId in loadingConversationIds) return

        messagesByConversation[targetChatId] = messagesByConversation[targetChatId].orEmpty() +
            MorrowChatMessage(
                id = "user_${System.currentTimeMillis()}",
                text = prompt,
                sender = MorrowSender.USER,
                attachments = attachments
            )

        val conversationIndex = conversations.indexOfFirst { it.id == targetChatId }
        if (conversationIndex >= 0 && conversations[conversationIndex].title == "New conversation") {
            val source = prompt.ifBlank { attachments.firstOrNull()?.name ?: "Attachment" }
            conversations[conversationIndex] = conversations[conversationIndex].copy(
                title = source.take(34).let { if (source.length > 34) "$it…" else it }
            )
        }

        loadingConversationIds = loadingConversationIds + targetChatId
        val job = scope.launch {
            try {
                val reply = fetchMorrowReply(prompt, attachments)
                // The chat may have been cleared while the request was running.
                if (messagesByConversation.containsKey(targetChatId)) {
                    val replyId = "assistant_${System.currentTimeMillis()}"
                    messagesByConversation[targetChatId] = messagesByConversation[targetChatId].orEmpty() +
                        MorrowChatMessage(id = replyId, text = reply, sender = MorrowSender.ASSISTANT)
                    if (targetChatId == activeId) {
                        typingSkip = false
                        typingMessageId = replyId
                    }
                }
            } finally {
                generationJobs.remove(targetChatId)
                loadingConversationIds = loadingConversationIds - targetChatId
            }
        }
        generationJobs[targetChatId] = job
    }

    fun sendInput() {
        if (activeId in loadingConversationIds || typingMessageId != null) return
        val prompt = input.trim()
        val attachments = pendingAttachments
        if (prompt.isBlank() && attachments.isEmpty()) return
        input = ""
        pendingAttachments = emptyList()
        attachmentsOpen = false
        sendPrompt(prompt, attachments)
    }

    /** Removes the last user message and everything after it, and returns that message. */
    fun takeLastUserMessage(): MorrowChatMessage? {
        val chatId = activeId
        if (chatId in loadingConversationIds) return null
        val messages = messagesByConversation[chatId].orEmpty()
        val lastUser = messages.indexOfLast { it.sender == MorrowSender.USER }
        if (lastUser < 0) return null
        messagesByConversation[chatId] = messages.subList(0, lastUser).toList()
        return messages[lastUser]
    }

    fun regenerateLast() {
        typingMessageId = null
        typingSkip = false
        takeLastUserMessage()?.let { sendPrompt(it.text, it.attachments) }
    }

    fun editMessage(message: MorrowChatMessage) {
        val chatId = activeId
        if (chatId in loadingConversationIds || message.sender != MorrowSender.USER) return
        val messages = messagesByConversation[chatId].orEmpty()
        val index = messages.indexOfFirst { it.id == message.id }
        if (index < 0) return
        messagesByConversation[chatId] = messages.take(index)
        input = message.text
        pendingAttachments = message.attachments
        typingMessageId = null
        typingSkip = false
    }

    /**
     * Stop button. While the reply is typing out, it shows the full reply at once.
     * While the request is still running, it cancels the request and puts your message back in the composer.
     */
    fun stopGeneration() {
        if (typingMessageId != null) {
            typingSkip = true
            return
        }
        val chatId = activeId
        generationJobs.remove(chatId)?.cancel()
        loadingConversationIds = loadingConversationIds - chatId
        takeLastUserMessage()?.let {
            input = it.text
            pendingAttachments = it.attachments
        }
    }

    fun onTypingDone() {
        typingMessageId = null
        typingSkip = false
    }

    fun setFeedback(message: MorrowChatMessage, value: Int) {
        val chatId = activeId
        messagesByConversation[chatId] = messagesByConversation[chatId].orEmpty().map { m ->
            if (m.id == message.id) m.copy(feedback = if (m.feedback == value) 0 else value) else m
        }
    }

    fun copyText(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Morrow reply", text))
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share reply"))
    }

    fun toggleSpeak(message: MorrowChatMessage) {
        val engine = speaker
        if (engine == null) {
            Toast.makeText(context, "Text to speech isn't ready yet", Toast.LENGTH_SHORT).show()
            return
        }
        if (speakingId == message.id) {
            engine.stop()
            speakingId = null
            return
        }
        engine.stop()
        engine.speak(plainText(message.text), message.id)
        speakingId = message.id
    }

    fun clearAllChats() {
        generationJobs.values.forEach { it.cancel() }
        generationJobs.clear()
        conversations.clear()
        messagesByConversation.clear()
        conversations.add(MorrowConversationSession(WELCOME_ID, "New conversation"))
        messagesByConversation[WELCOME_ID] = emptyList()
        activeId = WELCOME_ID
        loadingConversationIds = emptySet()
        input = ""
        pendingAttachments = emptyList()
        typingMessageId = null
        typingSkip = false
    }

    CompositionLocalProvider(LocalMorrowAccent provides accent) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimColor = Color.Black.copy(alpha = 0.68f),
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MorrowSidebar,
                    drawerContentColor = MorrowText,
                    modifier = Modifier.width(300.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BrandMark(Modifier.size(44.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Morrow", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MorrowText)
                                Text(
                                    "Better questions. Brighter tomorrows.",
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MorrowMuted
                                )
                            }
                            IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                CloseIcon(tint = MorrowMuted)
                            }
                        }

                        Spacer(Modifier.height(22.dp))
                        SidebarAction(
                            icon = { ChatIcon(tint = if (!showExplore && !showLibrary && !showSettings) accent else MorrowMuted) },
                            label = "New Chat",
                            selected = !showExplore && !showLibrary && !showSettings
                        ) {
                            showExplore = false
                            showLibrary = false
                            showSettings = false
                            scope.launch { drawerState.close() }
                        }
                        Spacer(Modifier.height(6.dp))
                        SidebarAction(
                            icon = { Icon(Icons.Outlined.Explore, contentDescription = null, tint = if (showExplore) accent else MorrowMuted, modifier = Modifier.size(22.dp)) },
                            label = "Explore",
                            selected = showExplore
                        ) {
                            showExplore = true
                            showLibrary = false
                            showSettings = false
                            scope.launch { drawerState.close() }
                        }
                        Spacer(Modifier.height(6.dp))
                        SidebarAction(
                            icon = { Icon(Icons.Outlined.History, contentDescription = null, tint = if (showLibrary) accent else MorrowMuted, modifier = Modifier.size(22.dp)) },
                            label = "Chat History",
                            selected = showLibrary
                        ) {
                            showLibrary = true
                            showExplore = false
                            showSettings = false
                            scope.launch { drawerState.close() }
                        }
                        Spacer(Modifier.height(6.dp))
                        SidebarAction(
                            icon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = if (showSettings) accent else MorrowMuted, modifier = Modifier.size(22.dp)) },
                            label = "Settings",
                            selected = showSettings
                        ) {
                            showSettings = true
                            showExplore = false
                            showLibrary = false
                            scope.launch { drawerState.close() }
                        }
                        Spacer(Modifier.height(4.dp))
                        SidebarAction(
                            icon = { Icon(Icons.Outlined.Timer, contentDescription = null, tint = MorrowMuted, modifier = Modifier.size(22.dp)) },
                            label = "Temporary chat"
                        ) {
                            createNewChat(temporary = true)
                            showSettings = false
                            showExplore = false
                            showLibrary = false
                            scope.launch { drawerState.close() }
                        }

                        HorizontalDivider(color = MorrowBorder, modifier = Modifier.padding(top = 16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 14.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "RECENT CHATS",
                                color = MorrowMuted,
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    searchVisible = !searchVisible
                                    searchText = ""
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                SearchIcon(modifier = Modifier.size(18.dp), tint = if (searchVisible) accent else MorrowMuted)
                            }
                        }
                        AnimatedVisibility(searchVisible) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                singleLine = true,
                                placeholder = { Text("Search titles and messages", color = MorrowMuted, fontSize = 12.sp) },
                                shape = RoundedCornerShape(13.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = MorrowBorder,
                                    focusedTextColor = MorrowText,
                                    unfocusedTextColor = MorrowText,
                                    cursorColor = accent
                                )
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            val filteredConversations = conversations
                                .filterNot { it.temporary }
                                .filter { chat ->
                                    searchText.isBlank() ||
                                        chat.title.contains(searchText, ignoreCase = true) ||
                                        messagesByConversation[chat.id].orEmpty().any { message ->
                                            message.text.contains(searchText, ignoreCase = true)
                                        }
                                }
                                .sortedByDescending { it.pinned }

                            items(filteredConversations, key = { it.id }) { chat ->
                                val selected = chat.id == activeId
                                val chatMessages = messagesByConversation[chat.id].orEmpty()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(
                                            brush = if (selected) {
                                                Brush.linearGradient(listOf(MorrowSelected, MorrowSelected))
                                            } else {
                                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                            },
                                            shape = RoundedCornerShape(13.dp)
                                        )
                                        .border(
                                            width = if (selected) 1.dp else 0.dp,
                                            color = if (selected) accent.copy(alpha = 0.32f) else Color.Transparent,
                                            shape = RoundedCornerShape(13.dp)
                                        )
                                        .combinedClickable(
                                            onClick = {
                                                activeId = chat.id
                                                input = ""
                                                pendingAttachments = emptyList()
                                                attachmentsOpen = false
                                                typingMessageId = null
                                                typingSkip = false
                                                showSettings = false
                                                showExplore = false
                                                showLibrary = false
                                                scope.launch { drawerState.close() }
                                            },
                                            onLongClick = { chatActionsId = chat.id }
                                        )
                                        .padding(horizontal = 11.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ChatIcon(
                                        modifier = Modifier.size(20.dp),
                                        tint = if (selected) accent else MorrowMuted
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            chat.title,
                                            color = if (selected) MorrowText else MorrowMuted,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            if (chat.pinned) "Pinned · " + relativeChatTime(chat.id, chatMessages)
                                            else relativeChatTime(chat.id, chatMessages),
                                            color = MorrowMuted.copy(alpha = 0.82f),
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                    if (chat.pinned) {
                                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Pinned chat", tint = accent, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }

                        SidebarProCard(accent = accent) {
                            showProDialog = true
                            scope.launch { drawerState.close() }
                        }
                    }
                }
            }
        ) {
            if (showSettings) {
                SettingsScreen(
                    landscapeOn = landscapeOn,
                    onLandscapeChange = {
                        landscapeOn = it
                        settingsPrefs.edit().putBoolean(KEY_LANDSCAPE, it).apply()
                    },
                    accentIndex = accentIndex,
                    onAccentChange = {
                        accentIndex = it
                        settingsPrefs.edit().putInt(KEY_ACCENT, it).apply()
                    },
                    onClearChats = { showClearDialog = true },
                    onBack = { showSettings = false }
                )
            } else if (showExplore) {
                ExploreScreen(
                    onBack = { showExplore = false },
                    onPromptClick = { prompt ->
                        if (currentMessages.isNotEmpty()) createNewChat()
                        input = prompt
                        showExplore = false
                        showLibrary = false
                        showSettings = false
                    }
                )
            } else if (showLibrary) {
                LibraryScreen(
                    chats = conversations.filter { !it.temporary }.sortedByDescending { it.pinned },
                    onBack = { showLibrary = false },
                    onOpenChat = { chatId ->
                        activeId = chatId
                        input = ""
                        pendingAttachments = emptyList()
                        typingMessageId = null
                        typingSkip = false
                        showLibrary = false
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MorrowBackground)
                        .imePadding()
                ) {
                    TopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNewChatClick = { createNewChat() },
                        temporary = activeConversation?.temporary == true
                    )

                    Crossfade(
                        targetState = currentMessages.isEmpty(),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        label = "chatArea"
                    ) { isEmpty ->
                        if (isEmpty) {
                            WelcomeHero(
                                clock = clock,
                                palette = palette,
                                animate = landscapeOn,
                                recentChats = conversations.filter { !it.temporary && messagesByConversation[it.id].orEmpty().isNotEmpty() }.take(3),
                                onOpenChat = { chatId ->
                                    activeId = chatId
                                    input = ""
                                    pendingAttachments = emptyList()
                                    attachmentsOpen = false
                                    typingMessageId = null
                                    typingSkip = false
                                },
                                onPromptClick = { input = it }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(MorrowBackground)) {
                                MessageList(
                                    messages = currentMessages,
                                    isLoading = isLoading,
                                    listState = listState,
                                    loadingKey = "loading_$activeId",
                                    typingMessageId = typingMessageId,
                                    typingSkip = typingSkip,
                                    speakingId = speakingId,
                                    onCopy = { copyText(it.text) },
                                    onShare = { shareText(it.text) },
                                    onSpeak = { toggleSpeak(it) },
                                    onFeedback = { message, value -> setFeedback(message, value) },
                                    onRegenerate = { regenerateLast() },
                                    onEdit = { message -> editMessage(message) },
                                    onTypingDone = { onTypingDone() }
                                )
                            }
                        }
                    }

                    MorrowComposer(
                        inputText = input,
                        isLoading = isLoading,
                        isTyping = isTyping,
                        attachments = pendingAttachments,
                        attachmentsOpen = attachmentsOpen,
                        voice = voice,
                        onTextChanged = { input = it },
                        onToggleAttachments = { attachmentsOpen = !attachmentsOpen },
                        onPickPhotos = {
                            attachmentsOpen = false
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onPickFiles = {
                            attachmentsOpen = false
                            filePicker.launch(arrayOf("*/*"))
                        },
                        onCamera = { openCamera() },
                        onRemoveAttachment = { index ->
                            pendingAttachments = pendingAttachments.filterIndexed { i, _ -> i != index }
                        },
                        onSend = { sendInput() },
                        onStop = { stopGeneration() }
                    )
                    Text(
                        "Morrow can make mistakes. Check important information.",
                        color = MorrowMuted.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
                    )
                    MainBottomBar(
                        homeSelected = currentMessages.isEmpty(), historySelected = false, toolsSelected = false, settingsSelected = false,
                        onHome = { if (currentMessages.isNotEmpty()) createNewChat() },
                        onHistory = { showLibrary = true; showExplore = false; showSettings = false },
                        onTools = { showExplore = true; showLibrary = false; showSettings = false },
                        onSettings = { showSettings = true; showExplore = false; showLibrary = false }
                    )
                }
            }
        }

        val chatForActions = conversations.firstOrNull { it.id == chatActionsId }
        if (chatForActions != null) {
            AlertDialog(
                onDismissRequest = { chatActionsId = null },
                containerColor = MorrowSurface,
                title = { Text(chatForActions.title, color = MorrowText) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextButton(
                            onClick = {
                                val index = conversations.indexOfFirst { it.id == chatForActions.id }
                                if (index >= 0) conversations[index] = conversations[index].copy(pinned = !chatForActions.pinned)
                                chatActionsId = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (chatForActions.pinned) "Unpin chat" else "Pin chat", color = MorrowText)
                        }
                        TextButton(
                            onClick = {
                                renameChatId = chatForActions.id
                                renameChatValue = chatForActions.title
                                chatActionsId = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rename chat", color = MorrowText)
                        }
                        TextButton(
                            onClick = {
                                conversations.removeAll { it.id == chatForActions.id }
                                messagesByConversation.remove(chatForActions.id)
                                chatActionsId = null
                                if (activeId == chatForActions.id) {
                                    val nextChat = conversations.firstOrNull()
                                    if (nextChat != null) activeId = nextChat.id else createNewChat()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete chat", color = MorrowDanger)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { chatActionsId = null }) { Text("Done", color = LocalMorrowAccent.current) }
                }
            )
        }

        if (renameChatId != null) {
            AlertDialog(
                onDismissRequest = { renameChatId = null },
                containerColor = MorrowSurface,
                title = { Text("Rename chat", color = MorrowText) },
                text = {
                    OutlinedTextField(
                        value = renameChatValue,
                        onValueChange = { renameChatValue = it },
                        singleLine = true,
                        label = { Text("Chat name", color = MorrowMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LocalMorrowAccent.current,
                            unfocusedBorderColor = MorrowBorder,
                            focusedTextColor = MorrowText,
                            unfocusedTextColor = MorrowText,
                            cursorColor = LocalMorrowAccent.current
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val cleanTitle = renameChatValue.trim()
                        val index = conversations.indexOfFirst { it.id == renameChatId }
                        if (cleanTitle.isNotBlank() && index >= 0) {
                            conversations[index] = conversations[index].copy(title = cleanTitle.take(80))
                        }
                        renameChatId = null
                    }) { Text("Save", color = LocalMorrowAccent.current) }
                },
                dismissButton = {
                    TextButton(onClick = { renameChatId = null }) { Text("Cancel", color = MorrowMuted) }
                }
            )
        }

        if (showProDialog) {
            AlertDialog(
                onDismissRequest = { showProDialog = false },
                containerColor = MorrowSurface,
                title = { Text("Morrow Pro", color = MorrowText) },
                text = { Text("Coming soon. The current version of Morrow remains free to use.", color = MorrowMuted) },
                confirmButton = {
                    TextButton(onClick = { showProDialog = false }) { Text("Got it", color = LocalMorrowAccent.current) }
                }
            )
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                containerColor = MorrowSurface,
                title = { Text("Clear all chats?", color = MorrowText) },
                text = {
                    Text(
                        "This deletes every conversation on this device. It can't be undone.",
                        color = MorrowMuted
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        clearAllChats()
                        showClearDialog = false
                        showSettings = false
                    }) {
                        Text("Delete", color = MorrowDanger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel", color = MorrowMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    temporary: Boolean = false
) {
    val accent = LocalMorrowAccent.current
    Row(Modifier.fillMaxWidth().statusBarsPadding().height(58.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMenuClick) { MenuIcon(tint = MorrowText) }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BrandMark(Modifier.size(25.dp))
            Column {
                Text(if (temporary) "Temporary chat" else "Morrow", color = MorrowText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp)
                if (!temporary) Text("AI COMPANION", color = MorrowMuted, fontSize = 8.sp, letterSpacing = 1.2.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onNewChatClick, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(MorrowSurface)) { ComposeIcon(tint = accent) }
    }
}

@Composable
private fun MainBottomBar(
    homeSelected: Boolean, historySelected: Boolean, toolsSelected: Boolean, settingsSelected: Boolean,
    onHome: () -> Unit, onHistory: () -> Unit, onTools: () -> Unit, onSettings: () -> Unit
) {
    HorizontalDivider(color = MorrowBorder.copy(alpha = 0.7f), thickness = 1.dp)
    Row(Modifier.fillMaxWidth().background(Color(0xFF0D0D14)).navigationBarsPadding().padding(horizontal = 8.dp, vertical = 7.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        BottomNavItem("Home", Icons.Outlined.Home, homeSelected, onHome, Modifier.weight(1f))
        BottomNavItem("Chats", Icons.Outlined.ChatBubbleOutline, historySelected, onHistory, Modifier.weight(1f))
        BottomNavItem("Tools", Icons.Outlined.AutoAwesome, toolsSelected, onTools, Modifier.weight(1f))
        BottomNavItem("Settings", Icons.Outlined.Settings, settingsSelected, onSettings, Modifier.weight(1f))
    }
}

@Composable
private fun BottomNavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = LocalMorrowAccent.current
    Column(modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, contentDescription = label, tint = if (selected) accent else MorrowMuted, modifier = Modifier.size(21.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = if (selected) accent else MorrowMuted, fontSize = 10.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun ExploreScreen(
    onBack: () -> Unit,
    onPromptClick: (String) -> Unit
) {
    val accent = LocalMorrowAccent.current
    val prompts = MarqueeRowOnePrompts + MarqueeRowTwoPrompts + MarqueeRowThreePrompts
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MorrowBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { BackIcon(tint = accent) }
            Text("Explore", color = MorrowText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            "A starting point for whatever you're thinking about.",
            color = MorrowMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            itemsIndexed(prompts) { _, prompt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            Brush.linearGradient(listOf(MorrowSurfaceRaised, MorrowSurface)),
                            RoundedCornerShape(15.dp)
                        )
                        .border(1.dp, MorrowBorder, RoundedCornerShape(15.dp))
                        .clickable { onPromptClick(prompt) }
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(prompt, color = MorrowText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = MorrowMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    chats: List<MorrowConversationSession>,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    val accent = LocalMorrowAccent.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MorrowBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { BackIcon(tint = accent) }
            Column {
                Text("Chat history", color = MorrowText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Pick up where you left off", color = MorrowMuted, fontSize = 11.sp)
            }
        }
        if (chats.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = accent, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(14.dp))
                Text("Your conversations appear here", color = MorrowText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Start a conversation and it will be saved here on your device. Long-press a chat in the sidebar for more actions.",
                    color = MorrowMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(chats, key = { it.id }) { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(MorrowSurface)
                            .border(1.dp, MorrowBorder, RoundedCornerShape(15.dp))
                            .clickable { onOpenChat(chat.id) }
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(chat.title, color = MorrowText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MorrowMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeHero(
    clock: MorrowClock,
    palette: SkyPalette,
    animate: Boolean,
    recentChats: List<MorrowConversationSession>,
    onOpenChat: (String) -> Unit,
    onPromptClick: (String) -> Unit
) {
    val accent = LocalMorrowAccent.current
    Box(Modifier.fillMaxSize().background(MorrowBackground)) {
        if (animate) LandscapeBackdrop(palette = palette, animate = true, modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.09f })
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 23.dp)) {
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(0xFFBFA4FF), Color(0xFF7962C9), Color(0xFF33284E)))), contentAlignment = Alignment.Center) { BrandMark(Modifier.size(40.dp)) }
                Column {
                    Text("Morrow", color = MorrowText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.55).sp)
                    Text("YOUR SPACE TO THINK", color = MorrowMuted, fontSize = 9.sp, letterSpacing = 1.45.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF171522)).border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("FREE", color = accent, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(clock.phase.greeting, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(7.dp))
            Text("What would you like\nto explore today?", color = MorrowText, fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.65).sp)
            Spacer(Modifier.height(8.dp))
            Text("Think through a problem, learn something new, or turn an idea into a plan.", color = MorrowMuted, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(23.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("QUICK START", color = MorrowMuted, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("4 ways to begin", color = MorrowMuted.copy(alpha = 0.75f), fontSize = 10.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WelcomePromptCard("Study & learn", "Explain it simply", "Explain a difficult topic in simple steps with an example", onPromptClick, Modifier.weight(1f))
                WelcomePromptCard("Coding help", "Build and debug", "Help me with coding step by step. Ask what I'm trying to build first.", onPromptClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WelcomePromptCard("Write something", "Find the right words", "Help me write something clear and natural. Ask what it's for if needed.", onPromptClick, Modifier.weight(1f))
                WelcomePromptCard("Plan a project", "Turn ideas into steps", "Help me turn my idea into a practical step-by-step plan", onPromptClick, Modifier.weight(1f))
            }
            if (recentChats.isNotEmpty()) {
                Spacer(Modifier.height(23.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("RECENT CHATS", color = MorrowMuted, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("See all below", color = accent, fontSize = 10.sp)
                }
                Spacer(Modifier.height(8.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color(0xFF12121B)).border(1.dp, MorrowBorder, RoundedCornerShape(17.dp))) {
                    recentChats.forEachIndexed { index, chat ->
                        Row(Modifier.fillMaxWidth().clickable { onOpenChat(chat.id) }.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(32.dp).clip(RoundedCornerShape(11.dp)).background(Brush.linearGradient(listOf(Color(0xFF33264E), Color(0xFF1D1A2B)))), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(chat.title, color = MorrowText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                                Text(relativeChatTime(chat.id, emptyList()), color = MorrowMuted, fontSize = 10.sp)
                            }
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MorrowMuted, modifier = Modifier.size(17.dp))
                        }
                        if (index != recentChats.lastIndex) HorizontalDivider(color = MorrowBorder.copy(alpha = 0.7f), modifier = Modifier.padding(start = 54.dp))
                    }
                }
            } else {
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(0xFF1C1830), Color(0xFF13131C)))).border(1.dp, accent.copy(alpha = 0.19f), RoundedCornerShape(16.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Your next idea starts here", color = MorrowText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Ask Morrow anything using the box below.", color = MorrowMuted, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}


private data class OrbParticle(
    val angle: Float,
    val orbitScale: Float,
    val sizeScale: Float,
    val speed: Float,
    val phase: Float
)

@Composable
private fun MorrowSignatureOrb(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "morrowOrb")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbRotation"
    )
    val floatMotion by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbFloat"
    )
    val density = LocalDensity.current
    val bobPx = with(density) { floatMotion * 4.dp.toPx() }
    val particles = remember {
        List(20) { index ->
            OrbParticle(
                angle = (index * 137.5f) % 360f,
                orbitScale = 1.18f + ((index * 7) % 9) / 18f,
                sizeScale = 0.7f + ((index * 3) % 6) / 10f,
                speed = if (index % 2 == 0) 0.75f else -0.52f,
                phase = ((index * 13) % 17) / 17f
            )
        }
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f + bobPx
        val radius = minOf(size.width * 0.245f, size.height * 0.405f)
        if (radius <= 0f) return@Canvas

        // Soft ambient halo: one broad gradient, no image assets or blur layers.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x263F344B),
                    Color(0x12352D40),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = radius * 1.85f
            ),
            radius = radius * 1.85f,
            center = Offset(centerX, centerY)
        )

        // Fine orbit tracks create depth without a heavy 3D renderer.
        val orbitWidth = radius * 3.05f
        val orbitHeight = radius * 1.10f
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0x55B8A8D0),
                    Color(0x168E829F),
                    Color.Transparent,
                    Color(0x40D9D0E5),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY)
            ),
            startAngle = rotation * 360f + 12f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(centerX - orbitWidth / 2f, centerY - orbitHeight / 2f),
            size = Size(orbitWidth, orbitHeight),
            style = Stroke(width = 1.1.dp.toPx())
        )
        drawArc(
            color = Color(0x268F829F),
            startAngle = 195f - rotation * 210f,
            sweepAngle = 145f,
            useCenter = false,
            topLeft = Offset(centerX - orbitWidth * 0.44f, centerY - orbitHeight * 0.78f),
            size = Size(orbitWidth * 0.88f, orbitHeight * 1.56f),
            style = Stroke(width = 0.8.dp.toPx())
        )

        // Draw the sphere with radial shading and a restrained pearl-lilac core.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE9E2F1),
                    Color(0xFFB9A9D0),
                    Color(0xFF756781),
                    Color(0xFF29252F)
                ),
                center = Offset(centerX - radius * 0.34f, centerY - radius * 0.42f),
                radius = radius * 1.62f
            ),
            radius = radius,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xC8FFFFFF),
                    Color(0x55E7DDF4),
                    Color.Transparent
                ),
                center = Offset(centerX - radius * 0.37f, centerY - radius * 0.47f),
                radius = radius * 0.88f
            ),
            radius = radius * 0.96f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color(0x668F7CA8),
            radius = radius * 0.94f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )

        // Tiny orbiting particles fade in and out; their positions are deterministic,
        // so there is no per-frame allocation or random-number generation.
        particles.forEach { particle ->
            val angle = (
                particle.angle + rotation * 360f * particle.speed + particle.phase * 360f
            ) * (Math.PI.toFloat() / 180f)
            val orbitX = radius * particle.orbitScale * 1.52f
            val orbitY = radius * particle.orbitScale * 0.56f
            val px = centerX + cos(angle.toDouble()).toFloat() * orbitX
            val py = centerY + sin(angle.toDouble()).toFloat() * orbitY
            val fade = (0.5f + 0.5f * sin((angle + particle.phase * 6.28f).toDouble()).toFloat())
            val alpha = 0.10f + fade * 0.62f
            val particleRadius = (1.1f + particle.sizeScale * 1.25f).dp.toPx()
            drawCircle(
                color = if (particle.phase > 0.48f) {
                    Color(0xFFE5D9F2).copy(alpha = alpha)
                } else {
                    Color(0xFFB8A8D0).copy(alpha = alpha)
                },
                radius = particleRadius,
                center = Offset(px, py)
            )
        }

        // A small moving glint makes the orb feel alive without flashing.
        val glintAngle = (rotation * 360f - 135f) * (Math.PI.toFloat() / 180f)
        val glintX = centerX + cos(glintAngle.toDouble()).toFloat() * radius * 0.57f
        val glintY = centerY + sin(glintAngle.toDouble()).toFloat() * radius * 0.57f
        drawCircle(
            color = Color.White.copy(alpha = 0.58f),
            radius = 1.5.dp.toPx(),
            center = Offset(glintX, glintY)
        )
    }
}

@Composable
private fun WelcomePromptCard(
    title: String,
    detail: String,
    prompt: String,
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(17.dp)
    Column(
        modifier = modifier
            .height(82.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF1C1A29), Color(0xFF14141D))))
            .border(1.dp, Color(0xFF39324B), shape)
            .clickable { onPromptClick(prompt) }
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            color = MorrowText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(5.dp))
        Text(
            detail,
            color = MorrowMuted,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------- Landscape ----------

private data class Star(
    val x: Float,
    val y: Float,
    val radiusDp: Float,
    val brightness: Float,
    val phase: Float
)

private val LandscapeStars: List<Star> by lazy {
    val random = Random(7)
    List(64) {
        Star(
            x = random.nextFloat(),
            y = random.nextFloat() * 0.6f,
            radiusDp = 0.6f + random.nextFloat() * 1.2f,
            brightness = 0.4f + random.nextFloat() * 0.6f,
            phase = random.nextFloat()
        )
    }
}

/** Static when animation is off: same colors, no drift or twinkle. */
@Composable
private fun LandscapeBackdrop(palette: SkyPalette, animate: Boolean, modifier: Modifier = Modifier) {
    if (animate) {
        AnimatedLandscape(palette = palette, modifier = modifier)
    } else {
        Canvas(modifier = modifier) { drawLandscape(palette, drift = 0f, twinkle = 0f) }
    }
}

@Composable
private fun AnimatedLandscape(palette: SkyPalette, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "landscape")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 90_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )
    Canvas(modifier = modifier) { drawLandscape(palette, drift, twinkle) }
}

private fun DrawScope.drawLandscape(palette: SkyPalette, drift: Float, twinkle: Float) {
    val w = size.width
    val h = size.height

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(palette.skyTop, palette.skyMid, palette.skyBottom),
            startY = 0f,
            endY = h
        )
    )

    if (palette.starAlpha > 0.01f) {
        LandscapeStars.forEach { star ->
            val wave = 0.5f + 0.5f * sin(TwoPi * (twinkle + star.phase))
            drawCircle(
                color = Color.White.copy(alpha = (0.25f + 0.75f * wave) * star.brightness * palette.starAlpha),
                radius = star.radiusDp.dp.toPx(),
                center = Offset(w * star.x, h * star.y)
            )
        }
    }

    val celestialCenter = Offset(w * palette.celestialX, h * palette.celestialY)
    val celestialRadius = minOf(w, h) * (if (palette.celestialIsSun) 0.07f else 0.055f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.glow.copy(alpha = 0.45f), palette.glow.copy(alpha = 0f)),
            center = celestialCenter,
            radius = celestialRadius * 4f
        ),
        radius = celestialRadius * 4f,
        center = celestialCenter
    )
    drawCircle(color = palette.celestial, radius = celestialRadius, center = celestialCenter)

    drawRidge(drift, speed = 1, k1 = 2, k2 = 5, baseY = 0.6f, amplitude = 0.16f, color = palette.hillFar, alpha = 0.75f)
    drawRidge(drift, speed = 2, k1 = 3, k2 = 7, baseY = 0.72f, amplitude = 0.13f, color = palette.hillMid, alpha = 1f)
    drawRidge(drift, speed = 3, k1 = 2, k2 = 4, baseY = 0.86f, amplitude = 0.1f, color = palette.hillNear, alpha = 1f)
}

/**
 * One layer of hills. Each term is an integer number of waves across the width,
 * and the shift is an integer multiple of a full wave at drift = 1, so the
 * layer wraps without a visible jump.
 */
private fun DrawScope.drawRidge(
    drift: Float,
    speed: Int,
    k1: Int,
    k2: Int,
    baseY: Float,
    amplitude: Float,
    color: Color,
    alpha: Float
) {
    val w = size.width
    val h = size.height
    val path = Path()
    path.moveTo(0f, h)
    val steps = 160
    for (i in 0..steps) {
        val t = i.toFloat() / steps
        val shift = t + speed * drift
        val s = 0.65f * sin(TwoPi * k1 * shift) + 0.35f * sin(TwoPi * k2 * shift + 1.3f)
        val y = h * baseY - h * amplitude * (s + 1f) / 2f
        path.lineTo(t * w, y)
    }
    path.lineTo(w, h)
    path.close()
    drawPath(path = path, color = color.copy(alpha = color.alpha * alpha))
}

// ---------- Prompt marquee ----------

@Composable
private fun PromptMarqueeRow(
    prompts: List<String>,
    reverse: Boolean,
    onPromptClick: (String) -> Unit
) {
    val density = LocalDensity.current
    val speedPx = with(density) { MarqueeSpeedDpPerSecond.dp.toPx() }

    var setWidthPx by remember { mutableFloatStateOf(0f) }
    var phasePx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(setWidthPx, reverse) {
        if (setWidthPx <= 0f) return@LaunchedEffect
        var lastFrameNanos = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos != 0L) {
                    val seconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
                    val direction = if (reverse) -1f else 1f
                    phasePx = wrapPhase(phasePx + speedPx * seconds * direction, setWidthPx)
                }
                lastFrameNanos = frameNanos
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MarqueeRowHeight)
            .clipToBounds()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(brush = MarqueeEdgeFade, blendMode = BlendMode.DstIn)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(align = Alignment.Start, unbounded = true)
                .graphicsLayer { translationX = -phasePx }
        ) {
            PromptChipSet(
                prompts = prompts,
                onPromptClick = onPromptClick,
                modifier = Modifier.onSizeChanged { setWidthPx = it.width.toFloat() }
            )
            PromptChipSet(prompts = prompts, onPromptClick = onPromptClick, modifier = Modifier)
        }
    }
}

private fun wrapPhase(value: Float, period: Float): Float {
    val wrapped = value % period
    return if (wrapped < 0f) wrapped + period else wrapped
}

@Composable
private fun PromptChipSet(
    prompts: List<String>,
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        prompts.forEach { prompt ->
            PromptChip(prompt = prompt, onClick = { onPromptClick(prompt) })
            // Trailing gap is part of the set width so the loop period is exact.
            Spacer(Modifier.width(MarqueeChipGap))
        }
    }
}

@Composable
private fun PromptChip(prompt: String, onClick: () -> Unit) {
    val accent = LocalMorrowAccent.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MorrowChipFill)
            .border(1.dp, MorrowChipBorder, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(8.dp))
            Text(
                prompt,
                color = MorrowText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------- Chat messages ----------

@Composable
private fun MessageList(
    messages: List<MorrowChatMessage>,
    isLoading: Boolean,
    listState: LazyListState,
    loadingKey: String,
    typingMessageId: String?,
    typingSkip: Boolean,
    speakingId: String?,
    onCopy: (MorrowChatMessage) -> Unit,
    onShare: (MorrowChatMessage) -> Unit,
    onSpeak: (MorrowChatMessage) -> Unit,
    onFeedback: (MorrowChatMessage, Int) -> Unit,
    onRegenerate: () -> Unit,
    onEdit: (MorrowChatMessage) -> Unit,
    onTypingDone: () -> Unit
) {
    val lastIndex = messages.lastIndex

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(messages, key = { _, m -> m.id }) { index, chatMessage ->
            MorrowMessageRow(
                message = chatMessage,
                canRegenerate = !isLoading && index == lastIndex && chatMessage.sender == MorrowSender.ASSISTANT,
                isTyping = chatMessage.id == typingMessageId,
                typingSkip = typingSkip,
                isSpeaking = chatMessage.id == speakingId,
                onCopy = { onCopy(chatMessage) },
                onShare = { onShare(chatMessage) },
                onSpeak = { onSpeak(chatMessage) },
                onFeedback = { onFeedback(chatMessage, it) },
                onRegenerate = onRegenerate,
                onEdit = { onEdit(chatMessage) },
                onTypingDone = onTypingDone
            )
        }
        if (isLoading) {
            item(key = loadingKey) {
                ThinkingIndicator()
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MorrowMessageRow(
    message: MorrowChatMessage,
    canRegenerate: Boolean,
    isTyping: Boolean,
    typingSkip: Boolean,
    isSpeaking: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSpeak: () -> Unit,
    onFeedback: (Int) -> Unit,
    onRegenerate: () -> Unit,
    onEdit: (MorrowChatMessage) -> Unit,
    onTypingDone: () -> Unit
) {
    val accent = LocalMorrowAccent.current
    var showUserMessageMenu by remember(message.id) { mutableStateOf(false) }
    var selectingText by remember(message.id) { mutableStateOf(false) }
    val sentAt = remember(message.id) {
        message.id.substringAfterLast("_").toLongOrNull()?.let { timestamp ->
            SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(timestamp))
        } ?: "Time unavailable"
    }

    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(durationMillis = 380, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * 14f
            }
    ) {
        if (message.sender == MorrowSender.USER) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                if (message.attachments.isNotEmpty()) {
                    Row(
                        modifier = Modifier.widthIn(max = 300.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        message.attachments.forEach { attachment ->
                            AttachmentChip(attachment = attachment, onRemove = null)
                            Spacer(Modifier.width(6.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
                if (message.text.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF302640), Color(0xFF201E2A))), RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFF514367), RoundedCornerShape(20.dp))
                            .combinedClickable(onClick = {}, onLongClick = { showUserMessageMenu = true })
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (selectingText) SelectionContainer { Text(message.text, color = Color.White, fontSize = 15.sp, lineHeight = 23.sp) }
                        else Text(message.text, color = Color.White, fontSize = 15.sp, lineHeight = 23.sp)
                    }
                }
                if (showUserMessageMenu) {
                    AlertDialog(
                        onDismissRequest = { showUserMessageMenu = false },
                        title = { Text("Your message", color = MorrowText) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Sent $sentAt", color = MorrowMuted, fontSize = 12.sp)
                                Spacer(Modifier.height(6.dp))
                                TextButton(onClick = { onCopy(); showUserMessageMenu = false }, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { CopyIcon(Modifier.size(18.dp), MorrowText); Spacer(Modifier.width(12.dp)); Text("Copy message", color = MorrowText) }
                                }
                                TextButton(onClick = { selectingText = true; showUserMessageMenu = false }, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { ComposeIcon(Modifier.size(18.dp), MorrowText); Spacer(Modifier.width(12.dp)); Text("Select text", color = MorrowText) }
                                }
                                TextButton(onClick = { showUserMessageMenu = false; onEdit(message) }, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { ComposeIcon(Modifier.size(18.dp), MorrowText); Spacer(Modifier.width(12.dp)); Text("Edit message", color = MorrowText) }
                                }
                                TextButton(onClick = { onShare(); showUserMessageMenu = false }, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { ShareIcon(Modifier.size(18.dp), MorrowText); Spacer(Modifier.width(12.dp)); Text("Share prompt", color = MorrowText) }
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { showUserMessageMenu = false }) { Text("Done", color = LocalMorrowAccent.current) } },
                        containerColor = MorrowSurface
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandMark(Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Morrow",
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (isTyping) TypewriterMarkdown(text = message.text, skipRequested = typingSkip, onFinished = onTypingDone)
                else SelectionContainer { MorrowMarkdown(text = message.text) }
                if (!isTyping) {
                    MessageActionRow(
                        message = message,
                        isSpeaking = isSpeaking,
                        canRegenerate = canRegenerate,
                        onCopy = onCopy,
                        onShare = onShare,
                        onSpeak = onSpeak,
                        onFeedback = onFeedback,
                        onRegenerate = onRegenerate
                    )
                }
            }
        }
    }
}

/** Copy, share, read aloud, thumbs up/down, and regenerate, shown under each reply. */
@Composable
private fun MessageActionRow(
    message: MorrowChatMessage,
    isSpeaking: Boolean,
    canRegenerate: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSpeak: () -> Unit,
    onFeedback: (Int) -> Unit,
    onRegenerate: () -> Unit
) {
    val accent = LocalMorrowAccent.current
    Row(
        modifier = Modifier.padding(top = 6.dp, start = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionIcon(onClick = onCopy) {
            CopyIcon(modifier = Modifier.size(18.dp), tint = MorrowMuted)
        }
        ActionIcon(onClick = onShare) {
            ShareIcon(modifier = Modifier.size(18.dp), tint = MorrowMuted)
        }
        ActionIcon(onClick = onSpeak) {
            SpeakIcon(
                modifier = Modifier.size(18.dp),
                tint = if (isSpeaking) accent else MorrowMuted
            )
        }
        ActionIcon(onClick = { onFeedback(1) }) {
            ThumbIcon(
                down = false,
                modifier = Modifier.size(18.dp),
                tint = if (message.feedback == 1) accent else MorrowMuted
            )
        }
        ActionIcon(onClick = { onFeedback(-1) }) {
            ThumbIcon(
                down = true,
                modifier = Modifier.size(18.dp),
                tint = if (message.feedback == -1) accent else MorrowMuted
            )
        }
        if (canRegenerate) {
            ActionIcon(onClick = onRegenerate) {
                RefreshIcon(modifier = Modifier.size(18.dp), tint = MorrowMuted)
            }
        }
    }
}

@Composable
private fun ActionIcon(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun MessageAction(label: String, onClick: () -> Unit) {
    val accent = LocalMorrowAccent.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .border(1.dp, MorrowBorder, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** Reveals a reply character by character. Skip shows the full text at once. */
@Composable
private fun TypewriterMarkdown(
    text: String,
    skipRequested: Boolean,
    onFinished: () -> Unit
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    var shown by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text, skipRequested) {
        if (skipRequested) shown = text.length
        val step = when {
            text.length > 1200 -> 6
            text.length > 400 -> 3
            else -> 2
        }
        while (shown < text.length) {
            delay(14L)
            shown = minOf(text.length, shown + step)
        }
        currentOnFinished()
    }

    MorrowMarkdown(text = text.take(shown))
}

@Composable
private fun ThinkingIndicator() {
    val accent = LocalMorrowAccent.current
    val transition = rememberInfiniteTransition(label = "thinking")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinkingPhase"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        BrandMark(Modifier.size(24.dp))
        Spacer(Modifier.width(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { index ->
                val p = (phase + index * 0.2f) % 1f
                val wave = 0.5f - 0.5f * cos(TwoPi * p)
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .graphicsLayer {
                            alpha = 0.3f + 0.7f * wave
                            scaleX = 0.8f + 0.4f * wave
                            scaleY = 0.8f + 0.4f * wave
                        }
                        .clip(CircleShape)
                        .background(accent)
                )
            }
        }
        Spacer(Modifier.width(9.dp))
        Text("Thinking…", color = MorrowMuted, fontSize = 13.sp)
    }
}

// ---------- Attachments ----------

@Composable
private fun AttachmentChip(attachment: MorrowAttachment, onRemove: (() -> Unit)?) {
    val accent = LocalMorrowAccent.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MorrowSurface)
            .border(1.dp, MorrowBorder, RoundedCornerShape(14.dp))
            .padding(
                start = 6.dp,
                end = if (onRemove != null) 4.dp else 10.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val thumb = attachment.thumbnail
        if (thumb != null) {
            Image(
                bitmap = thumb,
                contentDescription = attachment.name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MorrowChipFill),
                contentAlignment = Alignment.Center
            ) {
                FileIcon(modifier = Modifier.size(18.dp), tint = accent)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            attachment.name,
            color = MorrowText,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 150.dp)
        )
        if (onRemove != null) {
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                CloseIcon(modifier = Modifier.size(12.dp), tint = MorrowMuted)
            }
        }
    }
}

@Composable
private fun AttachmentPanel(
    onPhotos: () -> Unit,
    onFiles: () -> Unit,
    onCamera: () -> Unit
) {
    val accent = LocalMorrowAccent.current
    Column(Modifier.fillMaxWidth().padding(bottom = 9.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF11111A)).border(1.dp, MorrowBorder, RoundedCornerShape(20.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add to your message", color = MorrowText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("Photos · Camera · Files", color = MorrowMuted, fontSize = 10.sp)
        }
        Spacer(Modifier.height(11.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            AttachOption(label = "Photos", onClick = onPhotos, modifier = Modifier.weight(1f)) { PhotoIcon(modifier = Modifier.size(22.dp), tint = accent) }
            AttachOption(label = "Camera", onClick = onCamera, modifier = Modifier.weight(1f)) { CameraIcon(modifier = Modifier.size(22.dp), tint = accent) }
            AttachOption(label = "Files", onClick = onFiles, modifier = Modifier.weight(1f)) { FileIcon(modifier = Modifier.size(22.dp), tint = accent) }
        }
    }
}

@Composable
private fun AttachOption(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(0xFF211B31), Color(0xFF171720))))
            .border(1.dp, Color(0xFF3A304D), RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF302543)), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.height(7.dp))
        Text(label, color = MorrowText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ---------- Voice input ----------

private class VoiceInputController(
    val available: Boolean,
    val listening: Boolean,
    val toggle: () -> Unit
)

@Composable
private fun rememberVoiceInput(onResult: (String) -> Unit): VoiceInputController {
    val context = LocalContext.current
    val available = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    var listening by remember { mutableStateOf(false) }
    val currentOnResult by rememberUpdatedState(onResult)
    val recognizer = remember(available) {
        if (available) SpeechRecognizer.createSpeechRecognizer(context) else null
    }

    DisposableEffect(recognizer) {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { listening = false }
            override fun onError(error: Int) { listening = false }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onResults(results: Bundle?) {
                listening = false
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { currentOnResult(it) }
            }
        })
        onDispose { recognizer?.destroy() }
    }

    fun start() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        recognizer?.startListening(intent)
        listening = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) start()
    }

    fun toggle() {
        if (recognizer == null) return
        if (listening) {
            recognizer.stopListening()
            listening = false
            return
        }
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            start()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    return VoiceInputController(available = available, listening = listening, toggle = { toggle() })
}

// ---------- Text to speech ----------

private class MorrowSpeaker(private val engine: TextToSpeech) {
    fun speak(text: String, utteranceId: String) {
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        engine.stop()
    }
}

@Composable
private fun rememberSpeaker(onFinished: (String?) -> Unit): MorrowSpeaker? {
    val context = LocalContext.current
    val currentOnFinished by rememberUpdatedState(onFinished)
    val handler = remember { Handler(Looper.getMainLooper()) }
    val speakerState = remember { mutableStateOf<MorrowSpeaker?>(null) }

    DisposableEffect(context) {
        val engineRef = arrayOfNulls<TextToSpeech>(1)
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engineRef[0]?.setLanguage(Locale.getDefault())
            }
        }
        engineRef[0] = engine
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                handler.post { currentOnFinished(utteranceId) }
            }
            override fun onError(utteranceId: String?) {
                handler.post { currentOnFinished(utteranceId) }
            }
        })
        speakerState.value = MorrowSpeaker(engine)
        onDispose {
            speakerState.value = null
            engine.stop()
            engine.shutdown()
        }
    }

    return speakerState.value
}

/** Strips markdown symbols so the voice doesn't read them out. */
private fun plainText(text: String): String =
    text.replace(Regex("[*_`#>|~\\[\\]]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

// ---------- Attachment preparation ----------

private fun newCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun queryDisplayName(context: Context, uri: Uri): String? =
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

private fun scaleToFit(bitmap: Bitmap, maxSide: Int): Bitmap {
    val largest = maxOf(bitmap.width, bitmap.height)
    if (largest <= maxSide) return bitmap
    val ratio = maxSide.toFloat() / largest
    return Bitmap.createScaledBitmap(
        bitmap,
        (bitmap.width * ratio).toInt().coerceAtLeast(1),
        (bitmap.height * ratio).toInt().coerceAtLeast(1),
        true
    )
}

/**
 * Extracts selectable PDF text locally before passing it through the existing /chat request.
 * PDFs are limited to 20 MB and extracted text is capped to MAX_TEXT_BYTES.
 */
private fun extractPdfText(context: Context, uri: Uri): String {
    val temporaryFile = File.createTempFile("morrow_pdf_", ".pdf", context.cacheDir)
    try {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open PDF")
        input.use { stream ->
            temporaryFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_PDF_BYTES) throw IOException("PDF exceeds 20 MB")
                    output.write(buffer, 0, count)
                }
            }
        }
        if (!pdfBoxInitialised) {
            synchronized(PDFBoxLock) {
                if (!pdfBoxInitialised) {
                    PDFBoxResourceLoader.init(context.applicationContext)
                    pdfBoxInitialised = true
                }
            }
        }
        return PDDocument.load(temporaryFile).use { document ->
            PDFTextStripper().getText(document).take(MAX_TEXT_BYTES).ifBlank {
                "[No selectable text was found. This PDF may be scanned or image-only.]"
            }
        }
    } finally {
        temporaryFile.delete()
    }
}

/**
 * Reads a picked file. Images are shrunk and sent as JPEG base64.
 * Text files are read (up to a limit). PDFs are converted to text locally.
 * Other files only send their name.
 */
private fun prepareAttachment(context: Context, uri: Uri): MorrowAttachment? {
    val resolver = context.contentResolver
    val mime = resolver.getType(uri) ?: "application/octet-stream"
    val name = queryDisplayName(context, uri) ?: "attachment"

    if (mime == "application/pdf" || name.endsWith(".pdf", ignoreCase = true)) {
        val pdfText = extractPdfText(context, uri)
        return MorrowAttachment(
            name = name,
            mime = "application/pdf",
            isImage = false,
            payload = "Extracted PDF text (up to $MAX_TEXT_BYTES characters):\n$pdfText"
        )
    }

    if (mime.startsWith("image/")) {
        val original = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
        val scaled = scaleToFit(original, MAX_IMAGE_SIDE)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
        val thumb = scaleToFit(scaled, 160).asImageBitmap()
        return MorrowAttachment(
            name = name,
            mime = "image/jpeg",
            isImage = true,
            payload = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP),
            thumbnail = thumb
        )
    }

    val text = if (mime.startsWith("text/") || mime == "application/json") {
        resolver.openInputStream(uri)?.use { stream ->
            val bytes = stream.readBytes()
            String(bytes, 0, minOf(bytes.size, MAX_TEXT_BYTES), Charsets.UTF_8)
        }
    } else {
        null
    }
    return MorrowAttachment(name = name, mime = mime, isImage = false, payload = text)
}

// ---------- Composer ----------

@Composable
private fun MorrowComposer(
    inputText: String,
    isLoading: Boolean,
    isTyping: Boolean,
    attachments: List<MorrowAttachment>,
    attachmentsOpen: Boolean,
    voice: VoiceInputController,
    onTextChanged: (String) -> Unit,
    onToggleAttachments: () -> Unit,
    onPickPhotos: () -> Unit,
    onPickFiles: () -> Unit,
    onCamera: () -> Unit,
    onRemoveAttachment: (Int) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    val accent = LocalMorrowAccent.current
    var focused by remember { mutableStateOf(false) }
    val busy = isLoading || isTyping
    val canSend = (inputText.isNotBlank() || attachments.isNotEmpty()) && !busy

    val borderColor by animateColorAsState(
        targetValue = if (focused) accent.copy(alpha = 0.48f) else MorrowBorder,
        animationSpec = tween(durationMillis = 250),
        label = "composerBorder"
    )
    val sendTint by animateColorAsState(
        targetValue = if (busy || canSend) accent else Color(0xFF2B292E),
        animationSpec = tween(durationMillis = 220),
        label = "sendTint"
    )
    val plusRotation by animateFloatAsState(
        targetValue = if (attachmentsOpen) 45f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "plusRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (attachments.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                attachments.forEachIndexed { index, attachment ->
                    AttachmentChip(
                        attachment = attachment,
                        onRemove = { onRemoveAttachment(index) }
                    )
                }
            }
        }

        AnimatedVisibility(visible = attachmentsOpen) {
            AttachmentPanel(
                onPhotos = onPickPhotos,
                onFiles = onPickFiles,
                onCamera = onCamera
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MorrowSurface)
                .border(1.dp, borderColor, RoundedCornerShape(28.dp))
                .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleAttachments,
                modifier = Modifier.size(40.dp)
            ) {
                PlusIcon(
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = plusRotation },
                    tint = accent
                )
            }

            BasicTextField(
                value = inputText,
                onValueChange = onTextChanged,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .onFocusChanged { focused = it.isFocused },
                textStyle = TextStyle(color = MorrowText, fontSize = 15.sp, lineHeight = 21.sp),
                cursorBrush = SolidColor(accent),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (inputText.isEmpty()) {
                            Text(
                                when {
                                    voice.listening -> "Listening…"
                                    isLoading -> "Thinking…"
                                    isTyping -> "Morrow is writing…"
                                    else -> "Message Morrow…"
                                },
                                color = MorrowMuted,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (voice.available) {
                IconButton(
                    onClick = voice.toggle,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (voice.listening) MorrowDanger.copy(alpha = 0.18f) else Color.Transparent)
                ) {
                    MicIcon(
                        tint = if (voice.listening) MorrowDanger else MorrowMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = if (busy) onStop else onSend,
                enabled = busy || canSend,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(sendTint)
            ) {
                if (busy) {
                    StopSquareIcon(
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    ArrowUpIcon(
                        tint = if (canSend) Color(0xFF29232F) else MorrowMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ---------- Settings ----------

@Composable
private fun SettingsScreen(
    landscapeOn: Boolean,
    onLandscapeChange: (Boolean) -> Unit,
    accentIndex: Int,
    onAccentChange: (Int) -> Unit,
    onClearChats: () -> Unit,
    onBack: () -> Unit
) {
    val accent = LocalMorrowAccent.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MorrowBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                BackIcon(tint = MorrowText)
            }
            Column {
                Text("Settings", color = MorrowText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Make Morrow feel like yours", color = MorrowMuted, fontSize = 11.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF211A35), Color(0xFF13131D))))
                    .border(1.dp, Color(0xFF413456), RoundedCornerShape(20.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF302443)), contentAlignment = Alignment.Center) { BrandMark(Modifier.size(39.dp)) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Morrow AI", color = MorrowText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Personalize your workspace", color = MorrowMuted, fontSize = 11.sp)
                }
            }
            SettingsSection(title = "Appearance") {
                Text("Accent color", color = MorrowText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    MorrowAccentChoices.forEachIndexed { index, color ->
                        val selected = index == accentIndex
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = if (selected) MorrowText else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onAccentChange(index) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    MorrowAccentNames[accentIndex.coerceIn(0, MorrowAccentNames.lastIndex)],
                    color = MorrowMuted,
                    fontSize = 12.sp
                )
            }

            SettingsSection(title = "Animation") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Animated welcome background", color = MorrowText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Adds a subtle time-of-day scene to the welcome screen. Turn it off for a clean background.",
                            color = MorrowMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = landscapeOn,
                        onCheckedChange = onLandscapeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accent,
                            uncheckedThumbColor = MorrowMuted,
                            uncheckedTrackColor = MorrowChipFill,
                            uncheckedBorderColor = MorrowBorder
                        )
                    )
                }
            }

            SettingsSection(title = "Chats") {
                Text(
                    "Clear all chats",
                    color = MorrowDanger,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClearChats)
                        .padding(vertical = 4.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Deletes every conversation saved on this device.",
                    color = MorrowMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) {
        Text(
            title.uppercase(),
            color = MorrowMuted,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF191821), Color(0xFF14141B))))
                .border(1.dp, MorrowBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            content = content
        )
    }
}

// ---------- Markdown renderer ----------

private sealed interface MdBlock
private data class MdHeading(val level: Int, val text: String) : MdBlock
private data class MdParagraph(val text: String) : MdBlock
private data class MdCode(val language: String, val code: String) : MdBlock
private data class MdQuote(val text: String) : MdBlock
private data class MdListItem(val ordered: Boolean, val number: Int, val depth: Int, val text: String)
private data class MdList(val items: List<MdListItem>) : MdBlock
private data class MdTable(val header: List<String>, val rows: List<List<String>>) : MdBlock
private object MdRule : MdBlock

private val UnorderedItemRegex = Regex("^(\\s*)[-*+]\\s+(.*)$")
private val OrderedItemRegex = Regex("^(\\s*)(\\d+)[.)]\\s+(.*)$")
private val HtmlBreakRegex = Regex("(?i)<br\\s*/?>")
private val HtmlTagRegex = Regex("</?[a-zA-Z][a-zA-Z0-9]*(\\s[^<>]*)?/?>")

@Composable
private fun MorrowMarkdown(text: String, modifier: Modifier = Modifier) {
    val blocks = remember(text) {
        runCatching { parseMarkdownBlocks(text) }.getOrElse { listOf(MdParagraph(text)) }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MdHeading -> MdHeadingBlock(block)
                is MdParagraph -> InlineText(
                    text = block.text,
                    fontSize = 15.5.sp,
                    lineHeight = 26.sp
                )
                is MdCode -> MdCodeBlock(block.language, block.code)
                is MdQuote -> MdQuoteBlock(block.text)
                is MdList -> MdListBlock(block.items)
                is MdTable -> MdTableBlock(block.header, block.rows)
                MdRule -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                        .height(1.dp)
                        .background(MorrowBorder)
                )
            }
        }
    }
}

@Composable
private fun MdHeadingBlock(block: MdHeading) {
    val accent = LocalMorrowAccent.current
    InlineText(
        text = block.text,
        fontSize = when (block.level) {
            1 -> 22.sp
            2 -> 19.sp
            3 -> 17.sp
            else -> 15.sp
        },
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        color = if (block.level <= 2) MorrowText else accent
    )
}

@Composable
private fun MdCodeBlock(language: String, code: String) {
    val accent = LocalMorrowAccent.current
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MorrowCodeBg)
            .border(1.dp, MorrowBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MorrowSurface)
                .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = language.ifBlank { "CODE" }.uppercase(Locale.ROOT),
                color = accent,
                fontSize = 10.sp,
                letterSpacing = 1.3.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Morrow code", code))
                    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(36.dp)
            ) {
                CopyIcon(modifier = Modifier.size(16.dp), tint = MorrowMuted)
            }
        }
        HorizontalDivider(color = MorrowBorder.copy(alpha = 0.75f))
        Text(
            text = code,
            color = MorrowText,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            softWrap = false,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun MdQuoteBlock(text: String) {
    val accent = LocalMorrowAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(Modifier.width(12.dp))
        InlineText(
            text = text,
            modifier = Modifier.weight(1f),
            color = MorrowText.copy(alpha = 0.9f),
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun MdListBlock(items: List<MdListItem>) {
    val accent = LocalMorrowAccent.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (item.depth * 16).dp)
            ) {
                Text(
                    if (item.ordered) "${item.number}." else "•",
                    color = accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(22.dp)
                )
                InlineText(
                    text = item.text,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    lineHeight = 23.sp
                )
            }
        }
    }
}

@Composable
private fun MdTableBlock(header: List<String>, rows: List<List<String>>) {
    val columns = rows.fold(header.size) { acc, row -> maxOf(acc, row.size) }.coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MorrowBorder, RoundedCornerShape(12.dp))
    ) {
        MdTableRow(
            cells = List(columns) { header.getOrElse(it) { "" } },
            background = MorrowSurfaceRaised,
            bold = true
        )
        rows.forEachIndexed { index, row ->
            HorizontalDivider(color = MorrowBorder)
            MdTableRow(
                cells = List(columns) { row.getOrElse(it) { "" } },
                background = if (index % 2 == 0) MorrowSurface else MorrowBackground,
                bold = false
            )
        }
    }
}

@Composable
private fun MdTableRow(cells: List<String>, background: Color, bold: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        cells.forEach { cell ->
            InlineText(
                text = cell,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = if (bold) FontWeight.Bold else null
            )
        }
    }
}

/** Text with inline markdown styling. Unknown syntax is shown literally. */
@Composable
private fun InlineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MorrowText,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null
) {
    val accent = LocalMorrowAccent.current
    val annotated = remember(text, accent) {
        runCatching { buildInlineAnnotated(text, accent) }.getOrElse { AnnotatedString(text) }
    }
    Text(
        text = annotated,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontWeight = fontWeight
    )
}

private fun parseMarkdownBlocks(raw: String): List<MdBlock> {
    val lines = normalizeHtml(raw).split('\n').map { it.replace("\t", "    ") }
    val blocks = mutableListOf<MdBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        when {
            trimmed.isEmpty() -> i++

            isFenceStart(trimmed) -> {
                val fence = if (trimmed.startsWith("```")) "```" else "~~~"
                val language = trimmed.removePrefix(fence).trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith(fence)) {
                    codeLines += lines[i]
                    i++
                }
                i++ // skip the closing fence, or run past the end if unclosed
                blocks += MdCode(language, codeLines.joinToString("\n"))
            }

            headingLevel(trimmed) > 0 -> {
                val level = headingLevel(trimmed)
                blocks += MdHeading(level, trimmed.substring(level).trim().trimEnd('#').trim())
                i++
            }

            isRule(trimmed) -> {
                blocks += MdRule
                i++
            }

            isTableStart(lines, i) -> {
                val header = splitTableRow(lines[i])
                i += 2
                val rows = mutableListOf<List<String>>()
                while (i < lines.size && lines[i].trim().isNotEmpty() && lines[i].contains('|')) {
                    rows += splitTableRow(lines[i])
                    i++
                }
                blocks += MdTable(header, rows)
            }

            trimmed.startsWith(">") -> {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    quoteLines += lines[i].trim().removePrefix(">").trimStart()
                    i++
                }
                blocks += MdQuote(quoteLines.joinToString("\n"))
            }

            UnorderedItemRegex.matchEntire(line) != null || OrderedItemRegex.matchEntire(line) != null -> {
                val items = mutableListOf<MdListItem>()
                while (i < lines.size) {
                    val current = lines[i]
                    val unordered = UnorderedItemRegex.matchEntire(current)
                    val ordered = OrderedItemRegex.matchEntire(current)
                    when {
                        unordered != null -> {
                            items += MdListItem(
                                ordered = false,
                                number = 0,
                                depth = indentDepth(unordered.groupValues[1]),
                                text = unordered.groupValues[2]
                            )
                            i++
                        }
                        ordered != null -> {
                            items += MdListItem(
                                ordered = true,
                                number = ordered.groupValues[2].toIntOrNull() ?: 1,
                                depth = indentDepth(ordered.groupValues[1]),
                                text = ordered.groupValues[3]
                            )
                            i++
                        }
                        else -> break
                    }
                }
                blocks += MdList(items)
            }

            else -> {
                val paragraph = mutableListOf(trimmed)
                i++
                while (i < lines.size && lines[i].trim().isNotEmpty() && !startsBlock(lines, i)) {
                    paragraph += lines[i].trim()
                    i++
                }
                blocks += MdParagraph(paragraph.joinToString("\n"))
            }
        }
    }
    return blocks
}

private fun normalizeHtml(raw: String): String = raw
    .replace("\r\n", "\n")
    .replace('\r', '\n')
    .replace(HtmlBreakRegex, "\n")
    .replace(HtmlTagRegex, "")
    .replace("&nbsp;", " ")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&amp;", "&")

private fun headingLevel(trimmed: String): Int {
    val level = trimmed.takeWhile { it == '#' }.length
    return if (level in 1..6 && trimmed.length > level && trimmed[level] == ' ') level else 0
}

private fun isRule(trimmed: String): Boolean {
    val compact = trimmed.filter { !it.isWhitespace() }
    return compact.length >= 3 &&
        (compact.all { it == '-' } || compact.all { it == '*' } || compact.all { it == '_' })
}

private fun isFenceStart(trimmed: String): Boolean =
    trimmed.startsWith("```") || trimmed.startsWith("~~~")

private fun isTableSeparator(line: String): Boolean {
    val t = line.trim()
    return t.contains('|') && t.contains('-') &&
        t.all { it == '|' || it == '-' || it == ':' || it == ' ' }
}

private fun isTableStart(lines: List<String>, index: Int): Boolean =
    index + 1 < lines.size && lines[index].contains('|') && isTableSeparator(lines[index + 1])

private fun startsBlock(lines: List<String>, index: Int): Boolean {
    val line = lines[index]
    val trimmed = line.trim()
    return headingLevel(trimmed) > 0 ||
        isFenceStart(trimmed) ||
        trimmed.startsWith(">") ||
        isRule(trimmed) ||
        UnorderedItemRegex.matchEntire(line) != null ||
        OrderedItemRegex.matchEntire(line) != null ||
        isTableStart(lines, index)
}

private fun indentDepth(indent: String): Int = (indent.length / 2).coerceAtMost(4)

private fun splitTableRow(line: String): List<String> =
    line.trim()
        .removePrefix("|")
        .removeSuffix("|")
        .split('|')
        .map { it.trim() }

private fun buildInlineAnnotated(text: String, accent: Color): AnnotatedString = buildAnnotatedString {
    appendInline(text, 0, accent)
}

private fun AnnotatedString.Builder.appendInline(text: String, depth: Int, accent: Color) {
    var i = 0
    val n = text.length
    while (i < n) {
        val c = text[i]
        when {
            c == '\\' && i + 1 < n -> {
                append(text[i + 1])
                i += 2
            }

            c == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > i) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = MorrowCodeBg,
                            color = accent
                        )
                    ) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(c)
                    i++
                }
            }

            text.startsWith("**", i) || text.startsWith("__", i) -> {
                val delimiter = text.substring(i, i + 2)
                val end = text.indexOf(delimiter, i + 2)
                if (end > i + 2 && depth < 4) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendInline(text.substring(i + 2, end), depth + 1, accent)
                    }
                    i = end + 2
                } else {
                    append(delimiter)
                    i += 2
                }
            }

            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end > i + 2 && depth < 4) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        appendInline(text.substring(i + 2, end), depth + 1, accent)
                    }
                    i = end + 2
                } else {
                    append("~~")
                    i += 2
                }
            }

            c == '*' || (c == '_' && (i == 0 || !text[i - 1].isLetterOrDigit())) -> {
                val end = text.indexOf(c, i + 1)
                if (end > i + 1 && depth < 4) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        appendInline(text.substring(i + 1, end), depth + 1, accent)
                    }
                    i = end + 1
                } else {
                    append(c)
                    i++
                }
            }

            c == '[' -> {
                val close = text.indexOf("](", i + 1)
                val paren = if (close > 0) text.indexOf(')', close + 2) else -1
                if (close > i && paren > close) {
                    withStyle(SpanStyle(color = accent, textDecoration = TextDecoration.Underline)) {
                        appendInline(text.substring(i + 1, close), depth + 1, accent)
                    }
                    i = paren + 1
                } else {
                    append(c)
                    i++
                }
            }

            else -> {
                append(c)
                i++
            }
        }
    }
}

// ---------- Shared pieces ----------

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.morrow_logo),
        contentDescription = "Morrow logo",
        modifier = modifier.clip(RoundedCornerShape(14.dp))
    )
}

@Composable
private fun SidebarAction(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val accent = LocalMorrowAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = if (selected) {
                    Brush.linearGradient(listOf(MorrowSelected, MorrowSelected))
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                },
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) accent.copy(alpha = 0.30f) else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            icon()
        }
        Spacer(Modifier.width(13.dp))
        Text(
            label,
            color = if (selected) MorrowText else MorrowMuted,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun SidebarProCard(accent: Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(listOf(MorrowSurfaceRaised, MorrowSurface, MorrowSurface)),
                shape
            )
            .border(1.dp, MorrowBorder, shape)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark(Modifier.size(34.dp))
            Spacer(Modifier.width(9.dp))
            Text("Morrow Pro", color = MorrowText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MorrowSelected)
                    .border(1.dp, MorrowBorder, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("COMING SOON", color = MorrowMuted, fontSize = 9.sp, letterSpacing = 0.45.sp)
            }
        }
        Spacer(Modifier.height(7.dp))
        Text("More models. More tools. For you.", color = MorrowMuted, fontSize = 11.sp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Explore Pro", color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
    }
}

private fun relativeChatTime(chatId: String, messages: List<MorrowChatMessage>): String {
    val timestamp = messages.lastOrNull()?.id?.substringAfterLast("_")?.toLongOrNull()
        ?: chatId.toLongOrNull()
        ?: return "Saved chat"
    val elapsed = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L)
    return when {
        elapsed < 60_000L -> "Just now"
        elapsed < 3_600_000L -> "${elapsed / 60_000L}m ago"
        elapsed < 86_400_000L -> "${elapsed / 3_600_000L}h ago"
        elapsed < 604_800_000L -> "${elapsed / 86_400_000L}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

// ---------- Icons (drawn with Canvas, no font glyphs) ----------

private val IconDefaultSize = 22.dp

@Composable
private fun MorrowIcon(
    modifier: Modifier = Modifier.size(IconDefaultSize),
    tint: Color = MorrowText,
    draw: DrawScope.(Color, Stroke) -> Unit
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = size.minDimension * 0.085f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        this.draw(tint, stroke)
    }
}

private fun DrawScope.fracLine(color: Color, stroke: Stroke, x1: Float, y1: Float, x2: Float, y2: Float) {
    drawLine(
        color = color,
        start = Offset(size.width * x1, size.height * y1),
        end = Offset(size.width * x2, size.height * y2),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.fracPolyline(color: Color, stroke: Stroke, vararg points: Pair<Float, Float>) {
    val path = Path()
    points.forEachIndexed { index, (x, y) ->
        val px = size.width * x
        val py = size.height * y
        if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    drawPath(path = path, color = color, style = stroke)
}

@Composable
private fun MenuIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Menu, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun BackIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun CloseIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Close, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun ComposeIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun PlusIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun SearchIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Search, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun SliderIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Tune, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun ChatIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun MicIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.MicNone, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun ArrowUpIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.ArrowUpward, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun StopSquareIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Stop, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun CopyIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun ShareIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Share, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun SpeakIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.VolumeUp, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun ThumbIcon(down: Boolean, modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = if (down) Icons.Outlined.ThumbDownOffAlt else Icons.Outlined.ThumbUpOffAlt, contentDescription = if (down) "Dislike" else "Like", modifier = modifier, tint = tint)
}

@Composable
private fun RefreshIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun PhotoIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.Image, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun FileIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.InsertDriveFile, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun CameraIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = null, modifier = modifier, tint = tint)
}

private suspend fun fetchMorrowReply(
    message: String,
    attachments: List<MorrowAttachment>
): String = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        connection = (URL(MORROW_CHAT_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        val body = JSONObject().put("message", message)
        if (attachments.isNotEmpty()) {
            val array = JSONArray()
            attachments.forEach { a ->
                val item = JSONObject()
                    .put("name", a.name)
                    .put("mime", a.mime)
                    .put("kind", if (a.isImage) "image" else "file")
                a.payload?.let { item.put("data", it) }
                array.put(item)
            }
            body.put("attachments", array)
        }

        connection.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val statusCode = connection.responseCode
        val responseBody = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: ""

        val json = try {
            JSONObject(responseBody)
        } catch (_: Exception) {
            JSONObject()
        }

        if (statusCode !in 200..299) {
            val detail = json.optString("error").ifBlank { "HTTP $statusCode" }
            throw IOException(detail)
        }

        json.optString("reply").takeIf { it.isNotBlank() }
            ?: throw IOException("The server returned an empty reply.")
    } catch (error: CancellationException) {
        // Stop button pressed: let the coroutine cancel instead of showing an error message.
        throw error
    } catch (error: Exception) {
        "I couldn't connect to Morrow right now. ${error.message ?: "Please try again."}"
    } finally {
        connection?.disconnect()
    }
}

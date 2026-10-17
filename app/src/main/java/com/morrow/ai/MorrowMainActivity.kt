package com.morrow.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val MorrowBackground = Color(0xFF080A16)
private val MorrowSurface = Color(0xFF111629)
private val MorrowSidebar = Color(0xFF0B1020)
private val MorrowAccent = Color(0xFFA78BFA)
private val MorrowAccentStrong = Color(0xFF7C5CFF)
private val MorrowVioletDeep = Color(0xFF4B2BD6)
private val MorrowCoreLight = Color(0xFFE4DBFF)
private val MorrowText = Color(0xFFF1F5FF)
private val MorrowMuted = Color(0xFF9AA8C7)
private val MorrowBorder = Color(0xFF26304B)
private val MorrowSelected = Color(0xFF1E1A33)
private val MorrowChipFill = Color(0xFF1A1534)
private val MorrowChipBorder = Color(0xFF3B3170)

// The Gemini API key stays in Cloudflare Worker secrets, never inside this app.
private const val MORROW_CHAT_ENDPOINT =
    "https://morrow-backend.prasunmukherjeeee28.workers.dev/chat"

private const val TwoPi = 2f * PI.toFloat()

// Prompt marquee tuning.
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

enum class MorrowSender { USER, ASSISTANT }

data class MorrowChatMessage(
    val id: String,
    val text: String,
    val sender: MorrowSender
)

data class MorrowConversationSession(
    val id: String,
    val title: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = MorrowAccent,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MorrowMainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }

    val conversations = remember {
        mutableStateListOf(MorrowConversationSession("welcome", "New conversation"))
    }
    val messagesByConversation = remember {
        mutableStateMapOf<String, List<MorrowChatMessage>>("welcome" to emptyList())
    }
    var activeId by remember { mutableStateOf("welcome") }
    var loadingConversationIds by remember { mutableStateOf(setOf<String>()) }
    val currentMessages = messagesByConversation[activeId].orEmpty()
    val isLoading = activeId in loadingConversationIds
    val listState = rememberLazyListState()

    LaunchedEffect(activeId, currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.lastIndex)
        }
    }

    fun createNewChat() {
        val id = System.currentTimeMillis().toString()
        conversations.add(0, MorrowConversationSession(id, "New conversation"))
        messagesByConversation[id] = emptyList()
        activeId = id
        input = ""
    }

    fun sendMessage() {
        val prompt = input.trim()
        if (prompt.isBlank() || activeId in loadingConversationIds) return

        val targetChatId = activeId
        val oldMessages = messagesByConversation[targetChatId].orEmpty()
        messagesByConversation[targetChatId] = oldMessages + MorrowChatMessage(
            id = "user_${System.currentTimeMillis()}",
            text = prompt,
            sender = MorrowSender.USER
        )

        val conversationIndex = conversations.indexOfFirst { it.id == targetChatId }
        if (conversationIndex >= 0 && conversations[conversationIndex].title == "New conversation") {
            conversations[conversationIndex] = conversations[conversationIndex].copy(
                title = prompt.take(34).let { if (prompt.length > 34) "$it…" else it }
            )
        }

        input = ""
        loadingConversationIds = loadingConversationIds + targetChatId
        scope.launch {
            val reply = fetchMorrowReply(prompt)
            messagesByConversation[targetChatId] = messagesByConversation[targetChatId].orEmpty() +
                MorrowChatMessage(
                    id = "assistant_${System.currentTimeMillis()}",
                    text = reply,
                    sender = MorrowSender.ASSISTANT
                )
            loadingConversationIds = loadingConversationIds - targetChatId
        }
    }

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
                        BrandMark(Modifier.size(38.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("morrow", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MorrowText)
                            Text("YOUR SPACE TO THINK", fontSize = 9.sp, letterSpacing = 1.2.sp, color = MorrowMuted)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            CloseIcon(tint = MorrowMuted)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    SidebarAction(
                        icon = { PlusIcon(tint = MorrowAccent) },
                        label = "New chat"
                    ) {
                        createNewChat()
                        scope.launch { drawerState.close() }
                    }
                    Spacer(Modifier.height(7.dp))
                    SidebarAction(
                        icon = { SearchIcon(tint = MorrowAccent) },
                        label = "Search chats"
                    ) {
                        searchVisible = !searchVisible
                        searchText = ""
                    }
                    AnimatedVisibility(searchVisible) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            singleLine = true,
                            placeholder = { Text("Search chat titles", color = MorrowMuted, fontSize = 13.sp) },
                            shape = RoundedCornerShape(13.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MorrowAccent,
                                unfocusedBorderColor = MorrowBorder,
                                focusedTextColor = MorrowText,
                                unfocusedTextColor = MorrowText,
                                cursorColor = MorrowAccent
                            )
                        )
                    }
                    Text(
                        "RECENT CHATS",
                        color = MorrowMuted,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 25.dp, bottom = 10.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        val filteredConversations = conversations.filter {
                            it.title.contains(searchText, ignoreCase = true)
                        }
                        items(filteredConversations, key = { it.id }) { chat ->
                            val selected = chat.id == activeId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(if (selected) MorrowSelected else Color.Transparent)
                                    .clickable {
                                        activeId = chat.id
                                        input = ""
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 11.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChatIcon(
                                    modifier = Modifier.size(18.dp),
                                    tint = if (selected) MorrowAccent else MorrowMuted
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    chat.title,
                                    color = if (selected) MorrowText else MorrowMuted,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MorrowBorder)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(5.dp)) {
                        BrandMark(Modifier.size(30.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Morrow AI", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MorrowText)
                            Text("Connected through Worker", fontSize = 10.sp, color = MorrowMuted)
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MorrowBackground)
                .imePadding()
        ) {
            TopBar(
                onMenuClick = { scope.launch { drawerState.open() } },
                onNewChatClick = { createNewChat() }
            )

            Crossfade(
                targetState = currentMessages.isEmpty(),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "chatArea"
            ) { isEmpty ->
                if (isEmpty) {
                    WelcomeHero(onPromptClick = { input = it })
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LandscapeBackdrop(
                            modifier = Modifier.fillMaxSize(),
                            intensity = 0.35f
                        )
                        MessageList(
                            messages = currentMessages,
                            isLoading = isLoading,
                            listState = listState,
                            loadingKey = "loading_$activeId"
                        )
                    }
                }
            }

            MorrowComposer(
                inputText = input,
                isLoading = isLoading,
                onTextChanged = { input = it },
                onSend = { sendMessage() }
            )
            Text(
                "Morrow can make mistakes. Check important information.",
                color = MorrowMuted.copy(alpha = 0.7f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            )
        }
    }
}

@Composable
private fun TopBar(onMenuClick: () -> Unit, onNewChatClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            MenuIcon(tint = MorrowText)
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Morrow",
            color = MorrowText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onNewChatClick) {
            ComposeIcon(tint = MorrowText)
        }
    }
}

@Composable
private fun WelcomeHero(onPromptClick: (String) -> Unit) {
    val greeting = remember { morrowGreeting() }

    Box(modifier = Modifier.fillMaxSize()) {
        LandscapeBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                greeting,
                color = MorrowAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "How can I help you today?",
                color = MorrowText,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PromptMarqueeRow(
                prompts = MarqueeRowOnePrompts,
                reverse = false,
                onPromptClick = onPromptClick
            )
            PromptMarqueeRow(
                prompts = MarqueeRowTwoPrompts,
                reverse = true,
                onPromptClick = onPromptClick
            )
            PromptMarqueeRow(
                prompts = MarqueeRowThreePrompts,
                reverse = false,
                onPromptClick = onPromptClick
            )
        }
    }
}

/**
 * Night landscape: gradient sky, twinkling stars, a moon, and three layers of
 * hills that drift at different speeds. Every motion loops seamlessly.
 */
@Composable
private fun LandscapeBackdrop(modifier: Modifier = Modifier, intensity: Float = 1f) {
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
    val stars = remember {
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

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Sky wash from violet at the top into the navy background.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF241553).copy(alpha = 0.9f * intensity), Color.Transparent),
                startY = 0f,
                endY = h * 0.7f
            )
        )

        // Stars.
        stars.forEach { star ->
            val wave = 0.5f + 0.5f * sin(TwoPi * (twinkle + star.phase))
            drawCircle(
                color = Color.White.copy(alpha = (0.25f + 0.75f * wave) * star.brightness * intensity),
                radius = star.radiusDp.dp.toPx(),
                center = Offset(w * star.x, h * star.y)
            )
        }

        // Moon with a soft glow.
        val moonCenter = Offset(w * 0.8f, h * 0.2f)
        val moonRadius = minOf(w, h) * 0.06f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MorrowAccent.copy(alpha = 0.35f * intensity), Color.Transparent),
                center = moonCenter,
                radius = moonRadius * 4f
            ),
            radius = moonRadius * 4f,
            center = moonCenter
        )
        drawCircle(
            color = MorrowCoreLight.copy(alpha = 0.92f * intensity),
            radius = moonRadius,
            center = moonCenter
        )

        // Hills, far to near.
        drawRidge(
            drift = drift,
            speed = 1,
            k1 = 2,
            k2 = 5,
            baseY = 0.6f,
            amplitude = 0.16f,
            color = Color(0xFF2E2170),
            alpha = 0.75f * intensity
        )
        drawRidge(
            drift = drift,
            speed = 2,
            k1 = 3,
            k2 = 7,
            baseY = 0.72f,
            amplitude = 0.13f,
            color = Color(0xFF1C1550),
            alpha = intensity
        )
        drawRidge(
            drift = drift,
            speed = 3,
            k1 = 2,
            k2 = 4,
            baseY = 0.86f,
            amplitude = 0.1f,
            color = Color(0xFF0D0B26),
            alpha = intensity
        )
    }
}

private data class Star(
    val x: Float,
    val y: Float,
    val radiusDp: Float,
    val brightness: Float,
    val phase: Float
)

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
                    val step = speedPx * seconds * direction
                    phasePx = wrapPhase(phasePx + step, setWidthPx)
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
            PromptChipSet(
                prompts = prompts,
                onPromptClick = onPromptClick,
                modifier = Modifier
            )
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
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        prompts.forEach { prompt ->
            PromptChip(prompt = prompt, onClick = { onPromptClick(prompt) })
            // Trailing gap is part of the set width so the loop period is exact.
            Spacer(Modifier.width(MarqueeChipGap))
        }
    }
}

@Composable
private fun PromptChip(prompt: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MorrowChipFill)
            .border(1.dp, MorrowChipBorder, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MorrowAccent)
            )
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

@Composable
private fun MessageList(
    messages: List<MorrowChatMessage>,
    isLoading: Boolean,
    listState: LazyListState,
    loadingKey: String
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(messages, key = { it.id }) { chatMessage ->
            MorrowMessageRow(chatMessage)
        }
        if (isLoading) {
            item(key = loadingKey) {
                ThinkingIndicator()
            }
        }
    }
}

@Composable
private fun MorrowMessageRow(message: MorrowChatMessage) {
    // Each new message fades and rises into place.
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MorrowAccentStrong, MorrowVioletDeep)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        message.text,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
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
                        color = MorrowAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MorrowSurface.copy(alpha = 0.62f))
                        .border(1.dp, MorrowBorder.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        message.text,
                        color = MorrowText,
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicator() {
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
                        .background(MorrowAccent)
                )
            }
        }
        Spacer(Modifier.width(9.dp))
        Text("Thinking…", color = MorrowMuted, fontSize = 13.sp)
    }
}

@Composable
private fun MorrowComposer(
    inputText: String,
    isLoading: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val canSend = inputText.isNotBlank() && !isLoading

    val borderColor by animateColorAsState(
        targetValue = if (focused) MorrowAccent.copy(alpha = 0.85f) else MorrowBorder,
        animationSpec = tween(durationMillis = 250),
        label = "composerBorder"
    )
    val sendTint by animateColorAsState(
        targetValue = if (canSend) MorrowAccent else Color(0xFF2A2440),
        animationSpec = tween(durationMillis = 220),
        label = "sendTint"
    )
    val sendScale by animateFloatAsState(
        targetValue = if (canSend) 1f else 0.9f,
        animationSpec = tween(durationMillis = 200),
        label = "sendScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MorrowSurface)
            .border(1.dp, borderColor, RoundedCornerShape(28.dp))
            .padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = inputText,
            onValueChange = onTextChanged,
            enabled = !isLoading,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
                .onFocusChanged { focused = it.isFocused },
            textStyle = TextStyle(color = MorrowText, fontSize = 15.sp, lineHeight = 21.sp),
            cursorBrush = SolidColor(MorrowAccent),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (inputText.isEmpty()) {
                        Text(
                            if (isLoading) "Thinking…" else "Message Morrow…",
                            color = MorrowMuted,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    scaleX = sendScale
                    scaleY = sendScale
                }
                .clip(CircleShape)
                .background(sendTint)
        ) {
            ArrowUpIcon(
                tint = if (canSend) Color.White else MorrowMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            icon()
        }
        Spacer(Modifier.width(11.dp))
        Text(label, color = MorrowText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
    MorrowIcon(modifier = modifier, tint = tint) { color, stroke ->
        fracLine(color, stroke, 0.18f, 0.32f, 0.82f, 0.32f)
        fracLine(color, stroke, 0.18f, 0.5f, 0.82f, 0.5f)
        fracLine(color, stroke, 0.18f, 0.68f, 0.82f, 0.68f)
    }
}

@Composable
private fun CloseIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    MorrowIcon(modifier = modifier, tint = tint) { color, stroke ->
        fracLine(color, stroke, 0.25f, 0.25f, 0.75f, 0.75f)
        fracLine(color, stroke, 0.75f, 0.25f, 0.25f, 0.75f)
    }
}

@Composable
private fun ComposeIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    MorrowIcon(modifier = modifier, tint = tint) { color, stroke ->
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.14f, size.height * 0.2f),
            size = Size(size.width * 0.7f, size.height * 0.66f),
            cornerRadius = CornerRadius(size.minDimension * 0.16f),
            style = stroke
        )
        fracLine(color, stroke, 0.38f, 0.62f, 0.7f, 0.3f)
    }
}

@Composable
private fun PlusIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    MorrowIcon(modifier = modifier, tint = tint) { color, stroke ->
        fracLine(color, stroke, 0.5f, 0.18f, 0.5f, 0.82f)
        fracLine(color, stroke, 0.18f, 0.5f, 0.82f, 0.5f)
    }
}

@Composable
private fun SearchIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    MorrowIcon(modifier = modifier, tint = tint) { color, stroke ->
        drawCircle(
            color = color,
            radius = size.minDimension * 0.27f,
            center = Offset(size.width * 0.44f, size.height * 0.44f),
            style = stroke
        )
        fracLine(color, stroke, 0.64f, 0.64f, 0.84f, 0.84f)
    }
}

@Composable
private fun ChatIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    MorrowIcon(modifier = modifier, tint = tint) { color, stroke ->
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.14f, size.height * 0.2f),
            size = Size(size.width * 0.72f, size.height * 0.5f),
            cornerRadius = CornerRadius(size.minDimension * 0.16f),
            style = stroke
        )
        fracPolyline(color, stroke, 0.36f to 0.7f, 0.3f to 0.86f, 0.52f to 0.7f)
    }
}

@Composable
private fun ArrowUpIcon(modifier: Modifier = Modifier.size(IconDefaultSize), tint: Color = MorrowText) {
    MorrowIcon(modifier = modifier, tint = tint) { color, stroke ->
        fracLine(color, stroke, 0.5f, 0.84f, 0.5f, 0.18f)
        fracPolyline(color, stroke, 0.26f to 0.43f, 0.5f to 0.18f, 0.74f to 0.43f)
    }
}

// ---------- Helpers ----------

private fun morrowGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }
}

private suspend fun fetchMorrowReply(message: String): String = withContext(Dispatchers.IO) {
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

        val requestBody = JSONObject().put("message", message).toString()
        connection.outputStream.use { output ->
            output.write(requestBody.toByteArray(Charsets.UTF_8))
        }

        val statusCode = connection.responseCode
        val responseBody = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            .orEmpty()

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
    } catch (error: Exception) {
        "I couldn't connect to Morrow right now. ${error.message ?: "Please try again."}"
    } finally {
        connection?.disconnect()
    }
}

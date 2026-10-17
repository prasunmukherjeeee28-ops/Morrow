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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import kotlin.math.cos

private val MorrowBackground = Color(0xFF080A16)
private val MorrowSurface = Color(0xFF111629)
private val MorrowSidebar = Color(0xFF0B1020)
private val MorrowAccent = Color(0xFFA78BFA)
private val MorrowGlowSoft = Color(0xFF7C5CFF)
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
                            Text("×", fontSize = 28.sp, color = MorrowMuted)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    SidebarAction("＋", "New chat") {
                        createNewChat()
                        scope.launch { drawerState.close() }
                    }
                    Spacer(Modifier.height(7.dp))
                    SidebarAction("⌕", "Search chats") {
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
                                Text("◦", color = if (selected) MorrowAccent else MorrowMuted, fontSize = 20.sp)
                                Spacer(Modifier.width(9.dp))
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Soft violet glow fading down into the navy background.
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF211447), MorrowBackground),
                            startY = 0f,
                            endY = size.height * 0.55f
                        )
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                        MessageList(
                            messages = currentMessages,
                            isLoading = isLoading,
                            listState = listState,
                            loadingKey = "loading_$activeId"
                        )
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
            Text("☰", fontSize = 21.sp, color = MorrowText)
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
            Text("✎", fontSize = 21.sp, color = MorrowText)
        }
    }
}

@Composable
private fun WelcomeHero(onPromptClick: (String) -> Unit) {
    val greeting = remember { morrowGreeting() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PulseOrb(modifier = Modifier.size(170.dp))
        Spacer(Modifier.height(26.dp))
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
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
   )
        Spacer(Modifier.height(30.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun MessageList(
    messages: List<MorrowChatMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    loadingKey: String
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(23.dp)
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

/** Glowing orb with expanding rings, a breathing core and a slowly rotating sparkle. */
@Composable
private fun PulseOrb(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "orb")
    val ringPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringPhase"
    )
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Canvas(modifier = modifier) {
        val center = this.center
        val base = size.minDimension / 2f

        // Three rings expand outward and fade, staggered across the cycle.
        repeat(3) { index ->
            val p = (ringPhase + index / 3f) % 1f
            val radius = base * (0.35f + 0.65f * p)
            drawCircle(
                color = MorrowAccent.copy(alpha = (1f - p) * 0.45f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Ambient glow that breathes in and out.
        val glowRadius = base * (0.62f + 0.08f * breath)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MorrowGlowSoft.copy(alpha = 0.55f), MorrowGlowSoft.copy(alpha = 0f)),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )

        // Solid core.
        val coreRadius = base * 0.34f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MorrowCoreLight, MorrowVioletDeep),
                center = center,
                radius = coreRadius
            ),
            radius = coreRadius,
            center = center
        )

        // Rotating sparkle in the middle.
        rotate(degrees = spin, pivot = center) {
            drawPath(
                path = sparklePath(center.x, center.y, coreRadius * 0.6f),
                color = Color.White
            )
        }
    }
}

/** Four-point star built from concave quadratic curves. */
private fun sparklePath(cx: Float, cy: Float, r: Float): Path {
    return Path().apply {
        moveTo(cx, cy - r)
        quadraticBezierTo(cx, cy, cx + r, cy)
        quadraticBezierTo(cx, cy, cx, cy + r)
        quadraticBezierTo(cx, cy, cx - r, cy)
        quadraticBezierTo(cx, cy, cx, cy - r)
        close()
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
private fun SidebarAction(symbol: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(symbol, color = MorrowAccent, fontSize = 20.sp)
        Spacer(Modifier.width(11.dp))
        Text(label, color = MorrowText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * Horizontally scrolling prompt chips that loop seamlessly.
 * The set is drawn twice back to back and the phase wraps at exactly one set
 * width, so the frame is identical at the wrap point.
 * reverse = false scrolls left, reverse = true scrolls right.
 */
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
                // Fade both edges so chips dissolve instead of cutting off.
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
                Surface(
                    color = MorrowSelected,
                    shape = RoundedCornerShape(19.dp),
                    modifier = Modifier.widthIn(max = 310.dp)
                ) {
                    Text(
                        message.text,
                        color = MorrowText,
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp)
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandMark(Modifier.size(25.dp))
                    Spacer(Modifier.width(9.dp))
                    Text("Morrow", color = MorrowText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Text(
                    message.text,
                    color = MorrowText,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(start = 34.dp, top = 9.dp)
                )
            }
        }
    }
}

/** Three pulsing dots next to the logo while the backend is answering. */
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
        BrandMark(Modifier.size(25.dp))
        Spacer(Modifier.width(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { index ->
                val p = (phase + index * 0.2f) % 1f
                val wave = 0.5f - 0.5f * cos(2f * Math.PI.toFloat() * p)
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
        Text("Thinking through the stars…", color = MorrowMuted, fontSize = 13.sp)
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
            Text(
                "↑",
                color = if (canSend) Color.White else MorrowMuted,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

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

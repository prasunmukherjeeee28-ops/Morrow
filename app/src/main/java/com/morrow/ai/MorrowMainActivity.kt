package com.morrow.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private val MorrowBackground = Color(0xFF080A16)
private val MorrowSurface = Color(0xFF10172B)
private val MorrowSidebar = Color(0xFF0B1020)
private val MorrowAccent = Color(0xFF38BDF8)
private val MorrowText = Color(0xFFF4F7FF)
private val MorrowMuted = Color(0xFF9BAAC8)
private val MorrowBorder = Color(0xFF263653)
private val MorrowSelected = Color(0xFF172B46)

// The Gemini API key stays in Cloudflare Worker secrets, never inside this app.
private const val MORROW_CHAT_ENDPOINT =
    "https://morrow-backend.prasunmukherjeeee28.workers.dev/chat"

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
                colorScheme = androidx.compose.material3.darkColorScheme(
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
    val drawerState = androidx.compose.material3.rememberDrawerState(
        initialValue = androidx.compose.material3.DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

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
                            Text("YOUR SPACE TO EXPLORE", fontSize = 9.sp, letterSpacing = 1.2.sp, color = MorrowMuted)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MorrowBackground)
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(58.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Text("☰", fontSize = 22.sp, color = MorrowText)
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = Color(0xFF25145A),
                    border = BorderStroke(1.dp, Color(0xFF7652E8).copy(alpha = 0.72f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrandMark(Modifier.size(23.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Morrow", color = MorrowText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Text("✦", color = Color(0xFFE0A5FF), fontSize = 15.sp)
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { createNewChat() },
                    modifier = Modifier
                        .size(43.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF171438))
                ) {
                    Text("＋", color = Color(0xFFDCC8FF), fontSize = 24.sp)
                }
            }

            if (currentMessages.isEmpty()) {
                var activeSuggestion by remember { mutableStateOf(0) }
                val suggestions = listOf(
                    Triple("Explore an idea", "Turn your thoughts into something amazing.", "Give me three creative project ideas"),
                    Triple("Learn something new", "From school topics to the mysteries of space.", "Teach me Python from the beginning"),
                    Triple("Make a plan", "Small steps. Clear goals. Real progress.", "Help me plan a project step by step")
                )
                LaunchedEffect(activeId) {
                    while (true) {
                        delay(5200)
                        activeSuggestion = (activeSuggestion + 1) % suggestions.size
                    }
                }
                val motion = rememberInfiniteTransition(label = "morrow-space-motion")
                val floatY by motion.animateFloat(
                    initialValue = -4f,
                    targetValue = 4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "mascot-float"
                )
                val glowScale by motion.animateFloat(
                    initialValue = 0.97f,
                    targetValue = 1.04f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "mascot-glow"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0B0820), Color(0xFF0B0B21), Color(0xFF050812))
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 5.dp)
                            .size(290.dp)
                            .graphicsLayer {
                                scaleX = glowScale
                                scaleY = glowScale
                                translationY = floatY * density
                                alpha = 0.85f
                            }
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF6328D9).copy(alpha = 0.32f),
                                        Color(0xFF31146F).copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .graphicsLayer {
                                    translationY = floatY * density
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .size(108.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFF45209C).copy(alpha = 0.44f), Color(0xFF211449).copy(alpha = 0.32f), Color.Transparent)
                                        ),
                                        CircleShape
                                    )
                            )
                            BrandMark(Modifier.size(76.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Hi, I’m Morrow.",
                            color = MorrowText,
                            fontSize = 27.sp,
                            lineHeight = 33.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "How can I help you today?",
                            color = Color(0xFFB9AEDB),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 7.dp, bottom = 22.dp)
                        )

                        AnimatedContent(
                            targetState = activeSuggestion,
                            transitionSpec = {
                                (slideInHorizontally(
                                    animationSpec = tween(620, easing = FastOutSlowInEasing),
                                    initialOffsetX = { width -> width }
                                ) + fadeIn(animationSpec = tween(500))) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = tween(540, easing = FastOutSlowInEasing),
                                        targetOffsetX = { width -> -width / 4 }
                                    ) + fadeOut(animationSpec = tween(400)))
                            },
                            label = "morrow-featured-prompt"
                        ) { index ->
                            val item = suggestions[index]
                            val accent = listOf(Color(0xFFB06CFF), Color(0xFF45BFFF), Color(0xFF34D7E7))[index]
                            Surface(
                                onClick = { input = item.third },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                color = Color(0xD910102C),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.68f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(15.dp))
                                            .background(accent.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(listOf("✧", "⌘", "◷")[index], color = accent, fontSize = 27.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.first, color = MorrowText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(4.dp))
                                        Text(item.second, color = Color(0xFFB3B7D8), fontSize = 12.sp, lineHeight = 17.sp)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text("›", color = accent, fontSize = 27.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            suggestions.indices.forEach { index ->
                                val width by animateFloatAsState(
                                    targetValue = if (index == activeSuggestion) 22f else 6f,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                                    label = "featured-dot"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(width.dp)
                                        .height(5.dp)
                                        .clip(CircleShape)
                                        .background(if (index == activeSuggestion) Color(0xFFB06CFF) else Color(0xFF35304B))
                                        .clickable { activeSuggestion = index }
                                )
                            }
                        }
                        Spacer(Modifier.height(23.dp))

                        PromptChipRow(
                            PromptChipData("▧", "Image prompt", Color(0xFF7E7CFF), "Help me write a detailed AI image prompt for: "),
                            PromptChipData("≋", "Summarize text", Color(0xFFB06CFF), "Summarize this text clearly: ")
                        ) { input = it }
                        Spacer(Modifier.height(9.dp))
                        PromptChipRow(
                            PromptChipData("◷", "Make a plan", Color(0xFF38CFF0), "Help me make a realistic step-by-step plan for: "),
                            PromptChipData("✎", "Help me write", Color(0xFFB06CFF), "Help me write: "),
                            PromptChipData("✧", "Brainstorm", Color(0xFF8B82FF), "Brainstorm ideas for: ")
                        ) { input = it }
                        Spacer(Modifier.height(9.dp))
                        PromptChipRow(
                            PromptChipData("⌘", "Explain a topic", Color(0xFF6FB9FF), "Explain this topic simply: "),
                            PromptChipData("▤", "Help with code", Color(0xFF3CCFC6), "Help me understand or write code for: ")
                        ) { input = it }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(23.dp)
                ) {
                    items(currentMessages, key = { it.id }) { chatMessage ->
                        MorrowMessageRow(chatMessage)
                    }
                    if (isLoading) {
                        item(key = "loading_" + activeId) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BrandMark(Modifier.size(25.dp))
                                Spacer(Modifier.width(9.dp))
                                Text("Morrow is thinking…", color = MorrowMuted, fontSize = 13.sp)
                            }
                        }
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
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 4.dp)
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

private val BrushForBrand = Color(0xFF38BDF8)

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

@Composable
private fun SuggestionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(MorrowSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MorrowText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = MorrowMuted, fontSize = 11.sp)
        }
        Text("›", color = MorrowAccent, fontSize = 26.sp)
    }
}

private data class PromptChipData(
    val symbol: String,
    val title: String,
    val accent: Color,
    val prompt: String
)

@Composable
private fun PromptChipRow(vararg chips: PromptChipData, onPick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chips.forEach { chip ->
            Surface(
                onClick = { onPick(chip.prompt) },
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF0E1023),
                border = BorderStroke(1.dp, Color(0xFF292943)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(27.dp).clip(CircleShape).background(chip.accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(chip.symbol, color = chip.accent, fontSize = 15.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(chip.title, color = Color(0xFFE0DFF0), fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun MorrowMessageRow(message: MorrowChatMessage) {
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
                formatAssistantText(message.text),
                color = MorrowText,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(start = 34.dp, top = 9.dp)
            )
        }
    }
}

private fun formatAssistantText(raw: String): String {
    return raw
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</(p|div|h[1-6])\\s*>"), "\n")
        .replace(Regex("(?i)<(p|div|h[1-6])[^>]*>"), "")
        .replace(Regex("(?m)^\\s*#{1,6}\\s*"), "")
        .replace(Regex("(?m)^\\s*[-*_]{3,}\\s*$"), "")
        .lines()
        .map { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                val cells = trimmed.trim('|').split('|').map { it.trim() }
                if (cells.all { it.isEmpty() || it.matches(Regex(":?-{2,}:?")) }) {
                    ""
                } else {
                    cells.filter { it.isNotBlank() }.joinToString("  •  ")
                }
            } else {
                line.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
                    .replace(Regex("(?<!\\*)\\*(.*?)\\*(?!\\*)"), "$1")
            }
        }
        .joinToString("\n")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}

@Composable
private fun MorrowComposer(
    inputText: String,
    isLoading: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF101128))
            .then(
                Modifier
                    .background(Color.Transparent, RoundedCornerShape(28.dp))
            )
            .border(BorderStroke(1.dp, Color(0xFF464073).copy(alpha = 0.75f)), RoundedCornerShape(28.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
            Text("＋", color = MorrowMuted, fontSize = 21.sp)
        }
        BasicTextField(
            value = inputText,
            onValueChange = onTextChanged,
            enabled = !isLoading,
            modifier = Modifier.weight(1f).padding(horizontal = 7.dp),
            textStyle = TextStyle(color = MorrowText, fontSize = 15.sp, lineHeight = 21.sp),
            cursorBrush = SolidColor(MorrowAccent),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (inputText.isEmpty()) {
                        Text(
                            if (isLoading) "Thinking…" else "Ask Morrow anything…",
                            color = MorrowMuted,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
        IconButton(
            onClick = onSend,
            enabled = inputText.isNotBlank() && !isLoading,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (inputText.isNotBlank() && !isLoading) Color(0xFF8B35E9) else Color(0xFF28213E))
        ) {
            Text(
                "➤",
                color = if (inputText.isNotBlank() && !isLoading) Color.White else MorrowMuted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
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

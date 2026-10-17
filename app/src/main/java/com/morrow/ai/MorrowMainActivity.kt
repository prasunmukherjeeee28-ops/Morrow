package com.morrow.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private val MorrowBackground = Color(0xFF100D18)
private val MorrowSurface = Color(0xFF191421)
private val MorrowSidebar = Color(0xFF17111F)
private val MorrowAccent = Color(0xFFA855F7)
private val MorrowText = Color(0xFFF5EEFF)
private val MorrowMuted = Color(0xFFAA9CB9)
private val MorrowBorder = Color(0xFF34273F)
private val MorrowSelected = Color(0xFF302044)

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
                    .height(54.dp)
                    .padding(horizontal = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Text("☰", fontSize = 21.sp, color = MorrowText)
                }
                BrandMark(Modifier.size(27.dp))
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("Morrow", color = MorrowText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("AI assistant", color = MorrowMuted, fontSize = 10.sp)
                }
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(onClick = { showOverflow = true }) {
                        Text("⋮", fontSize = 25.sp, color = MorrowText)
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New chat") },
                            onClick = { createNewChat(); showOverflow = false }
                        )
                        DropdownMenuItem(
                            text = { Text("About Morrow") },
                            onClick = { showOverflow = false }
                        )
                    }
                }
                IconButton(onClick = { createNewChat() }) {
                    Text("✎", fontSize = 21.sp, color = MorrowText)
                }
            }

            if (currentMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    BrandMark(Modifier.size(54.dp))
                    Spacer(Modifier.height(22.dp))
                    Text(
                        "What can I help with?",
                        color = MorrowText,
                        fontSize = 29.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(9.dp))
                    Text(
                        "Ask anything. Let's figure it out together.",
                        color = MorrowMuted,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(25.dp))
                    SuggestionCard("Learn something new", "Understand a topic step by step") {
                        input = "Teach me Python from the beginning"
                    }
                    Spacer(Modifier.height(9.dp))
                    SuggestionCard("Explore an idea", "Turn curiosity into a project") {
                        input = "Give me three creative project ideas"
                    }
                    Spacer(Modifier.height(9.dp))
                    SuggestionCard("Make a plan", "Organize your next project") {
                        input = "Help me plan a project step by step"
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
                        item(key = "loading_${activeId}") {
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
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            )
        }
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BrushForBrand),
        contentAlignment = Alignment.Center
    ) {
        Text("m", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
    }
}

private val BrushForBrand = Color(0xFF8B5CF6)

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
            .padding(horizontal = 14.dp, vertical = 13.dp),
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
                message.text,
                color = MorrowText,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(start = 34.dp, top = 9.dp)
            )
        }
    }
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
            .clip(RoundedCornerShape(25.dp))
            .background(MorrowSurface)
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
                            if (isLoading) "Thinking…" else "Message Morrow…",
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
                .background(if (inputText.isNotBlank() && !isLoading) MorrowAccent else Color(0xFF33283F))
        ) {
            Text(
                "↑",
                color = if (inputText.isNotBlank() && !isLoading) Color.White else MorrowMuted,
                fontSize = 21.sp,
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

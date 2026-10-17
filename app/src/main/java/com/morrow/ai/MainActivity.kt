package com.morrow.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ==========================================
// MORROW DESIGN SYSTEM & COLOR PALETTE
// ==========================================
private val MorrowBackground = Color(0xFF100D18)
private val MorrowSurface = Color(0xFF1A1526)
private val MorrowSidebarBg = Color(0xFF191423)
private val MorrowAccent = Color(0xFFA855F7)
private val MorrowTextPrimary = Color(0xFFF5EEFF)
private val MorrowTextSecondary = Color(0xFFB8AEC9)
private val MorrowBorder = Color(0xFF3B2E52)

// Set your Gemini API key here
private const val GEMINI_API_KEY = "AQ.Ab8RN6LQGY_fktEYzu4HrPlDctQ1DAwqnjSgE_kDEKXgLuh8fw"

private val MorrowColorScheme = darkColorScheme(
    background = MorrowBackground,
    surface = MorrowSurface,
    onBackground = MorrowTextPrimary,
    onSurface = MorrowTextPrimary,
    primary = MorrowAccent,
    secondary = MorrowTextSecondary
)

private enum class MessageSender { USER, ASSISTANT }

private data class ChatMessage(
    val id: String,
    val text: String,
    val sender: MessageSender
)

private data class ConversationSession(
    val id: String,
    val title: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = MorrowColorScheme) {
                Surface(Modifier.fillMaxSize(), color = MorrowBackground) {
                    MorrowMainScreen()
                }
            }
        }
    }
}

@Composable
fun MorrowMainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedModel by remember { mutableStateOf("gemini-2.5-flash") }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val conversations = remember {
        mutableStateListOf(ConversationSession("welcome", "Welcome to Morrow"))
    }
    val messagesByConversation = remember {
        mutableStateMapOf<String, List<ChatMessage>>(
            "welcome" to listOf(
                ChatMessage(
                    "welcome-message", 
                    "Welcome to Morrow.\n\nConnected to Gemini 2.5 Flash API. Ask me anything!", 
                    MessageSender.ASSISTANT
                )
            )
        )
    }
    var activeId by remember { mutableStateOf("welcome") }
    val messages = messagesByConversation[activeId].orEmpty()
    val listState = rememberLazyListState()

    LaunchedEffect(activeId, messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    fun newChat() {
        val id = System.currentTimeMillis().toString()
        conversations.add(0, ConversationSession(id, "New conversation"))
        messagesByConversation[id] = emptyList()
        activeId = id
        input = ""
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MorrowSidebarBg,
                drawerContentColor = MorrowTextPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        BrandMark(Modifier.size(34.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Morrow", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                            Text("YOUR SPACE TO THINK", fontSize = 10.sp, color = MorrowTextSecondary, letterSpacing = 1.2.sp)
                        }
                    }
                    
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MorrowSurface)
                            .clickable { newChat(); scope.launch { drawerState.close() } }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(IconsCustom.Plus, null, tint = MorrowAccent, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("New chat", fontWeight = FontWeight.Medium)
                    }

                    Text(
                        "RECENT", 
                        color = MorrowTextSecondary, 
                        fontSize = 11.sp, 
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = 28.dp, bottom = 10.dp, start = 4.dp)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                        items(conversations, key = { it.id }) { chat ->
                            val isActive = chat.id == activeId
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isActive) Color(0xFF30243F) else Color.Transparent)
                                    .clickable {
                                        activeId = chat.id
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    IconsCustom.Message, 
                                    null, 
                                    tint = if (isActive) MorrowAccent else MorrowTextSecondary, 
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    chat.title, 
                                    color = if (isActive) MorrowTextPrimary else MorrowTextSecondary,
                                    maxLines = 1, 
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    ) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF352545)), 
                            contentAlignment = Alignment.Center
                        ) {
                            Text("M", color = MorrowTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Morrow", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Gemini API Integrated", fontSize = 11.sp, color = MorrowTextSecondary)
                        }
                    }
                }
            }
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MorrowBackground)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(IconsCustom.Menu, "Open chat history", tint = MorrowTextPrimary)
                }

                var modelMenu by remember { mutableStateOf(false) }
                Box {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { modelMenu = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(MorrowAccent))
                        Spacer(Modifier.width(8.dp))
                        Text(selectedModel, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(5.dp))
                        Icon(IconsCustom.ChevronDown, null, tint = MorrowTextSecondary, modifier = Modifier.size(15.dp))
                    }
                    DropdownMenu(expanded = modelMenu, onDismissRequest = { modelMenu = false }) {
                        listOf("gemini-2.5-flash", "gemini-2.5-pro").forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) }, 
                                onClick = { selectedModel = model; modelMenu = false }
                            )
                        }
                    }
                }

                IconButton(onClick = { newChat() }) {
                    Icon(IconsCustom.Edit, "New chat", tint = MorrowTextPrimary)
                }
            }

            if (messages.isEmpty()) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(28.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BrandMark(Modifier.size(58.dp))
                        Spacer(Modifier.height(22.dp))
                        Text("What’s on your mind?", fontSize = 25.sp, fontWeight = FontWeight.Medium, color = MorrowTextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("Powered by Gemini API.", fontSize = 14.sp, color = MorrowTextSecondary)
                        Spacer(Modifier.height(26.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip("Explain quantum computing") { input = "Explain quantum computing in simple terms" }
                            SuggestionChip("Write a code snippet") { input = "Write a simple Kotlin function" }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MorrowMessageRow(message)
                    }
                }
            }

            MorrowComposer(
                inputText = input,
                isLoading = isLoading,
                onTextChanged = { input = it },
                onSend = {
                    val userText = input.trim()
                    if (userText.isNotEmpty() && !isLoading) {
                        val currentList = messagesByConversation[activeId].orEmpty().toMutableList()
                        currentList.add(ChatMessage("u_${System.currentTimeMillis()}", userText, MessageSender.USER))
                        messagesByConversation[activeId] = currentList
                        
                        val index = conversations.indexOfFirst { it.id == activeId }
                        if (index >= 0 && conversations[index].title == "New conversation") {
                            conversations[index] = conversations[index].copy(title = userText.take(32))
                        }
                        
                        input = ""
                        isLoading = true

                        scope.launch {
                            val botReply = fetchGeminiResponse(selectedModel, userText)
                            val updatedList = messagesByConversation[activeId].orEmpty().toMutableList()
                            updatedList.add(ChatMessage("a_${System.currentTimeMillis()}", botReply, MessageSender.ASSISTANT))
                            messagesByConversation[activeId] = updatedList
                            isLoading = false
                        }
                    }
                }
            )

            Text(
                "Connected to Gemini API using current AQ key.",
                color = MorrowTextSecondary.copy(alpha = 0.7f), 
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// GEMINI API CALL (Supports AQ. and AIza keys)
// ==========================================
private suspend fun fetchGeminiResponse(modelName: String, promptText: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-goog-api-key", GEMINI_API_KEY)
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val jsonBody = JSONObject().apply {
                val textPart = JSONObject().put("text", promptText)
                val partsArray = JSONArray().put(textPart)
                val contentObj = JSONObject().put("parts", partsArray)
                put("contents", JSONArray().put(contentObj))
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            val responseStream = if (statusCode == 200) connection.inputStream else connection.errorStream
            val responseText = responseStream.bufferedReader().use { it.readText() }

            if (statusCode == 200) {
                val root = JSONObject(responseText)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No response generated.")
                    }
                }
                "Empty response received."
            } else {
                "Error $statusCode: $responseText"
            }
        } catch (e: Exception) {
            "Failed to send request: ${e.localizedMessage}"
        }
    }
}

// ==========================================
// COMPOSABLE HELPERS
// ==========================================

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(13.dp)).background(MorrowAccent), contentAlignment = Alignment.Center) {
        Text("m", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        color = MorrowSurface, 
        border = BorderStroke(1.dp, MorrowBorder), 
        shape = RoundedCornerShape(13.dp)
    ) {
        Text(
            label, 
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), 
            fontSize = 12.sp, 
            color = MorrowTextSecondary
        )
    }
}

@Composable
private fun MorrowMessageRow(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    if (isUser) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(color = Color(0xFF30243F), shape = RoundedCornerShape(20.dp)) {
                Text(
                    message.text, 
                    color = MorrowTextPrimary, 
                    fontSize = 15.sp, 
                    lineHeight = 23.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    } else {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(Modifier.size(25.dp))
                Spacer(Modifier.width(9.dp))
                Text("Morrow", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MorrowTextPrimary)
            }
            Text(
                message.text, 
                color = MorrowTextPrimary, 
                fontSize = 15.sp, 
                lineHeight = 24.sp,
                modifier = Modifier.padding(start = 34.dp, top = 10.dp)
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
    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(bottom = navPadding)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MorrowSurface)
                .padding(1.dp)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                Icon(IconsCustom.Plus, "Attachments", tint = MorrowTextSecondary, modifier = Modifier.size(19.dp))
            }
            BasicTextField(
                value = inputText, 
                onValueChange = onTextChanged,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = TextStyle(color = MorrowTextPrimary, fontSize = 15.sp, lineHeight = 21.sp),
                cursorBrush = SolidColor(MorrowAccent),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (inputText.isEmpty()) Text(if (isLoading) "Thinking..." else "Message Morrow...", color = MorrowTextSecondary, fontSize = 15.sp)
                        inner()
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
                Icon(
                    IconsCustom.Send, 
                    "Send message", 
                    tint = if (inputText.isNotBlank() && !isLoading) Color.White else MorrowTextSecondary, 
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // ==========================================
// INLINE VECTOR ICONS
// ==========================================
private object IconsCustom {
    val Menu: ImageVector = iconVector("menu") {
        moveTo(3f, 6f); lineTo(21f, 6f)
        moveTo(3f, 12f); lineTo(21f, 12f)
        moveTo(3f, 18f); lineTo(21f, 18f)
    }
    val Plus: ImageVector = iconVector("plus") {
        moveTo(12f, 5f); lineTo(12f, 19f)
        moveTo(5f, 12f); lineTo(19f, 12f)
    }
    val Edit: ImageVector = iconVector("edit") {
        moveTo(12f, 20f); lineTo(20f, 20f)
        moveTo(16.5f, 3.5f); lineTo(20.5f, 7.5f); lineTo(9f, 19f); lineTo(4f, 20f); lineTo(5f, 15f); close()
    }
    val ChevronDown: ImageVector = iconVector("chevron") {
        moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f)
    }
    val Send: ImageVector = iconVector("send") {
        moveTo(22f, 2f); lineTo(11f, 13f)
        moveTo(22f, 2f); lineTo(15f, 22f); lineTo(11f, 13f); lineTo(2f, 9f); close()
    }
    val Message: ImageVector = iconVector("message") {
        moveTo(21f, 11.5f); curveTo(21f, 16f, 17f, 19f, 12f, 19f)
        curveTo(10f, 19f, 8f, 18.5f, 6f, 17f); lineTo(3f, 19f); lineTo(4f, 15f)
        curveTo(2f, 11f, 4f, 5f, 10f, 4f); curveTo(16f, 2f, 21f, 6f, 21f, 11.5f); close()
    }
}

private fun iconVector(name: String, draw: PathBuilder.() -> Unit): ImageVector {
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            draw(this)
        }
    }.build()
}
    }
}

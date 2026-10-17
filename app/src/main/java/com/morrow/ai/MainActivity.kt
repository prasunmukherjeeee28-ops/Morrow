package com.morrow.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF100D18)
private val Panel = Color(0xFF191421)
private val Sidebar = Color(0xFF17111F)
private val Purple = Color(0xFFA855F7)
private val Lavender = Color(0xFFF5EEFF)
private val Muted = Color(0xFFAA9CB9)
private val Border = Color(0xFF34273F)

data class ChatMessage(val text: String, val isUser: Boolean)
data class ChatSession(
    val id: Int,
    val title: String,
    val messages: List<ChatMessage> = emptyList()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MorrowScreen() }
    }
}

@Composable
fun MorrowScreen() {
    var sessions by remember { mutableStateOf(listOf(ChatSession(1, "New chat"))) }
    var activeId by remember { mutableIntStateOf(1) }
    var nextId by remember { mutableIntStateOf(2) }
    var message by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var showSidebar by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showModels by remember { mutableStateOf(false) }
    var model by remember { mutableStateOf("Morrow Core") }
    var notice by remember { mutableStateOf(false) }

    val active = sessions.firstOrNull { it.id == activeId } ?: sessions.first()

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = Panel,
            primary = Purple,
            onBackground = Lavender,
            onSurface = Lavender
        )
    ) {
        Row(modifier = Modifier.fillMaxSize().background(Bg).imePadding()) {
            if (showSidebar) {
                Column(
                    modifier = Modifier.fillMaxHeight().width(270.dp).background(Sidebar)
                        .statusBarsPadding().navigationBarsPadding().padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark()
                        Spacer(Modifier.width(10.dp))
                        Text("morrow", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Lavender)
                        Spacer(Modifier.weight(1f))
                        Text("×", fontSize = 25.sp, color = Muted,
                            modifier = Modifier.clickable { showSidebar = false }.padding(6.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    SidebarAction("＋", "New chat") {
                        val id = nextId++
                        sessions = listOf(ChatSession(id, "New chat")) + sessions
                        activeId = id
                        message = ""
                        notice = false
                        showSidebar = false
                    }
                    Spacer(Modifier.height(8.dp))
                    SidebarAction("⌕", "Search chats") {
                        showSearch = !showSearch
                        search = ""
                    }
                    if (showSearch) {
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = search, onValueChange = { search = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search chats", color = Muted) },
                            singleLine = true, shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Panel, unfocusedContainerColor = Panel,
                                focusedIndicatorColor = Purple, unfocusedIndicatorColor = Border,
                                focusedTextColor = Lavender, unfocusedTextColor = Lavender,
                                cursorColor = Purple
                            )
                        )
                    }
                    Spacer(Modifier.height(26.dp))
                    Text("YOUR CHATS", color = Muted, fontSize = 10.sp,
                        letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        items(sessions.filter { it.title.contains(search, ignoreCase = true) }, key = { it.id }) { chat ->
                            val selected = chat.id == activeId
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp))
                                    .background(if (selected) Color(0xFF302044) else Color.Transparent)
                                    .clickable {
                                        activeId = chat.id
                                        message = ""
                                        notice = false
                                        showSidebar = false
                                    }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("◦", color = Purple, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(chat.title, color = if (selected) Lavender else Muted,
                                    fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    HorizontalDivider(color = Border)
                    Spacer(Modifier.height(12.dp))
                    SidebarAction("⚙", "About Morrow") { notice = !notice }
                    Text("PREVIEW · CHAT HISTORY IS TEMPORARY", color = Muted, fontSize = 9.sp,
                        letterSpacing = 0.7.sp, modifier = Modifier.padding(8.dp))
                }
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight().background(Bg)) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderAction("☰") { showSidebar = !showSidebar }
                    Box {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { showModels = true }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(model, color = Lavender, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(5.dp))
                            Text("⌄", color = Muted)
                        }
                        DropdownMenu(expanded = showModels, onDismissRequest = { showModels = false },
                            modifier = Modifier.background(Panel)) {
                            listOf("Morrow Core", "Morrow Mini").forEach { name ->
                                DropdownMenuItem(text = { Text(name, color = Lavender) }, onClick = {
                                    model = name
                                    showModels = false
                                })
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    HeaderAction("＋") {
                        val id = nextId++
                        sessions = listOf(ChatSession(id, "New chat")) + sessions
                        activeId = id
                        message = ""
                        notice = false
                    }
                }

                if (active.messages.isEmpty()) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        BrandMark()
                        Spacer(Modifier.height(24.dp))
                        Text("What can I help with?", color = Lavender, fontSize = 29.sp,
                            lineHeight = 36.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Text("Ask anything. Let's figure it out together.", color = Muted,
                            fontSize = 14.sp, lineHeight = 22.sp)
                        Spacer(Modifier.height(26.dp))
                        Suggestion("Learn something new", "Understand a topic step by step") {
                            message = "Teach me Python from the beginning"
                        }
                        Spacer(Modifier.height(9.dp))
                        Suggestion("Explore an idea", "Turn curiosity into a project") {
                            message = "Give me three creative project ideas"
                        }
                        Spacer(Modifier.height(9.dp))
                        Suggestion("Make a plan", "Organize your next big idea") {
                            message = "Help me plan a project step by step"
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        items(active.messages) { item ->
                            if (item.isUser) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Text(item.text, modifier = Modifier.widthIn(max = 300.dp)
                                        .clip(RoundedCornerShape(20.dp)).background(Color(0xFF302044)).padding(14.dp),
                                        color = Lavender, fontSize = 15.sp, lineHeight = 23.sp)
                                }
                            } else {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        BrandMark()
                                        Spacer(Modifier.width(9.dp))
                                        Text("Morrow", color = Lavender, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(item.text, color = Lavender, fontSize = 14.sp, lineHeight = 22.sp)
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().background(Bg).padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(Panel)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = message, onValueChange = { message = it },
                            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                            textStyle = TextStyle(color = Lavender, fontSize = 15.sp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Purple),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (message.isEmpty()) Text("Message Morrow...", color = Muted, fontSize = 14.sp)
                                    innerTextField()
                                }
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.size(42.dp).clip(CircleShape)
                                .background(if (message.isNotBlank()) Purple else Color(0xFF382B45))
                                .clickable {
                                    val question = message.trim()
                                    if (question.isNotEmpty()) {
                                        val updatedMessages = active.messages + ChatMessage(question, true) +
                                            ChatMessage("The Morrow interface is working, but its AI backend isn't connected yet. Your message has not been sent to an AI model.", false)
                                        val title = if (active.messages.isEmpty()) question.take(28) else active.title
                                        sessions = sessions.map {
                                            if (it.id == activeId) it.copy(title = title, messages = updatedMessages) else it
                                        }
                                        message = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("↑", color = Lavender, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    Text("MORROW PREVIEW · AI NOT CONNECTED", modifier = Modifier.fillMaxWidth(),
                        color = Muted, fontSize = 9.sp, letterSpacing = 1.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    if (notice) {
                        Text("Morrow is a prototype. Model selection is visual only.", color = Muted,
                            fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandMark() {
    Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Purple),
        contentAlignment = Alignment.Center) {
        Text("m", color = Bg, fontSize = 27.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SidebarAction(icon: String, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, color = Purple, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(title, color = Lavender, fontSize = 13.sp)
    }
}

@Composable
private fun HeaderAction(label: String, onClick: () -> Unit) {
    Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center) {
        Text(label, color = Lavender, fontSize = 22.sp)
    }
}

@Composable
private fun Suggestion(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Panel)
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFF302044)),
            contentAlignment = Alignment.Center) {
            Text("✦", color = Purple, fontSize = 19.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Lavender, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(description, color = Muted, fontSize = 11.sp)
        }
        Text("›", color = Purple, fontSize = 25.sp)
    }
}

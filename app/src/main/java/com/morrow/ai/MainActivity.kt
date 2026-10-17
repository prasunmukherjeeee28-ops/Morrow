
package com.morrow.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Bg = Color(0xFF100D18)
private val SurfaceDark = Color(0xFF191421)
private val SidebarColor = Color(0xFF17111F)
private val Purple = Color(0xFFA855F7)
private val Lavender = Color(0xFFF5EEFF)
private val Muted = Color(0xFFAA9CB9)
private val BorderColor = Color(0xFF34273F)

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorrowScreen() {
    var sessions by remember {
        mutableStateOf(listOf(ChatSession(1, "New chat")))
    }
    var activeId by remember { mutableIntStateOf(1) }
    var nextId by remember { mutableIntStateOf(2) }
    var message by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var model by remember { mutableStateOf("Morrow Core") }
    var modelMenu by remember { mutableStateOf(false) }
    var moreMenu by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val active = sessions.firstOrNull { it.id == activeId }
        ?: sessions.first()

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = SurfaceDark,
            primary = Purple,
            onBackground = Lavender,
            onSurface = Lavender
        )
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContainerColor = SidebarColor,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = SidebarColor,
                    drawerContentColor = Lavender,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 310.dp)
                        .width(300.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BrandMark()
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "morrow",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }
                            ) {
                                Text("×", fontSize = 27.sp, color = Muted)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        SidebarAction("＋", "New chat") {
                            val id = nextId++
                            sessions = listOf(
                                ChatSession(id, "New chat")
                            ) + sessions
                            activeId = id
                            message = ""
                            scope.launch { drawerState.close() }
                        }

                        Spacer(Modifier.height(8.dp))

                        SidebarAction("⌕", "Search chats") {
                            searching = !searching
                            search = ""
                        }

                        AnimatedVisibility(searching) {
                            OutlinedTextField(
                                value = search,
                                onValueChange = { search = it },
                                placeholder = {
                                    Text("Search history", color = Muted)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Purple,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = Lavender,
                                    unfocusedTextColor = Lavender,
                                    cursorColor = Purple
                                )
                            )
                        }

                        Spacer(Modifier.height(28.dp))

                        Text(
                            "YOUR CHATS",
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp,
                            color = Muted,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(10.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(5.dp)
                        ) {
                            val filtered = sessions.filter {
                                it.title.contains(
                                    search,
                                    ignoreCase = true
                                )
                            }

                            items(filtered, key = { it.id }) { chat ->
                                val selected = chat.id == activeId

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (selected)
                                                Color(0xFF302044)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            activeId = chat.id
                                            message = ""
                                            scope.launch {
                                                drawerState.close()
                                            }
                                        }
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 13.dp
                                        ),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Text(
                                        "◦",
                                        color = if (selected)
                                            Purple else Muted
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        chat.title,
                                        fontSize = 13.sp,
                                        color = if (selected)
                                            Lavender else Muted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = BorderColor)

                        Spacer(Modifier.height(12.dp))

                        SidebarAction("⚙", "Settings") {
                            scope.launch { drawerState.close() }
                        }

                        Text(
                            "MORROW PREVIEW",
                            fontSize = 9.sp,
                            letterSpacing = 1.5.sp,
                            color = Muted,
                            modifier = Modifier.padding(
                                start = 12.dp,
                                top = 12.dp
                            )
                        )
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg)
                    .imePadding()
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch { drawerState.open() }
                        }
                    ) {
                        Text("☰", fontSize = 21.sp, color = Lavender)
                    }

                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { modelMenu = true }
                                .padding(
                                    horizontal = 9.dp,
                                    vertical = 10.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                model,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("⌄", color = Muted)
                        }

                        DropdownMenu(
                            expanded = modelMenu,
                            onDismissRequest = { modelMenu = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            listOf("Morrow Core", "Morrow Mini").forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        model = it
                                        modelMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            val id = nextId++
                            sessions = listOf(
                                ChatSession(id, "New chat")
                            ) + sessions
                            activeId = id
                            message = ""
                        }
                    ) {
                        Text("✎", fontSize = 22.sp, color = Lavender)
                    }

                    Box {
                        IconButton(
                            onClick = { moreMenu = true }
                        ) {
                            Text("⋮", fontSize = 25.sp, color = Lavender)
                        }
                        DropdownMenu(
                            expanded = moreMenu,
                            onDismissRequest = { moreMenu = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("New chat") },
                                onClick = {
                                    val id = nextId++
                                    sessions = listOf(
                                        ChatSession(id, "New chat")
                                    ) + sessions
                                    activeId = id
                                    moreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About Morrow") },
                                onClick = { moreMenu = false }
                            )
                        }
                    }
                }

                // Conversation
                if (active.messages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF302044)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✦", fontSize = 28.sp, color = Purple)
                        }

                        Spacer(Modifier.height(22.dp))

                        Text(
                            "What can I help with?",
                            fontSize = 29.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Lavender
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "Ask anything. Let's figure it out together.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Muted
                        )

                        Spacer(Modifier.height(26.dp))

                        Suggestion(
                            "Learn something new",
                            "Understand a topic step by step"
                        ) {
                            message = "Teach me Python from the beginning"
                        }

                        Spacer(Modifier.height(9.dp))

                        Suggestion(
                            "Explore an idea",
                            "Turn curiosity into a project"
                        ) {
                            message = "Give me three creative project ideas"
                        }

                        Spacer(Modifier.height(9.dp))

                        Suggestion(
                            "Make a plan",
                            "Organize your next big idea"
                        ) {
                            message = "Help me plan a project step by step"
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical = 22.dp
                        ),
                        verticalArrangement =
                            Arrangement.spacedBy(25.dp)
                    ) {
                        items(active.messages) { item ->
                            if (item.isUser) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.End
                                ) {
                                    Text(
                                        item.text,
                                        modifier = Modifier
                                            .widthIn(max = 300.dp)
                                            .clip(
                                                RoundedCornerShape(20.dp)
                                            )
                                            .background(
                                                Color(0xFF302044)
                                            )
                                            .padding(14.dp),
                                        color = Lavender,
                                        fontSize = 15.sp,
                                        lineHeight = 23.sp
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {
                                        BrandMark()
                                        Spacer(Modifier.width(9.dp))
                                        Text(
                                            "Morrow",
                                            fontWeight = FontWeight.SemiBold,
                                            color = Lavender
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    Text(
                                        item.text,
                                        color = Lavender,
                                      )
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                fontSize = 12.sp,
                color = Muted
            )
        }
        Text("›", color = Purple, fontSize = 26.sp)
    }
                        }
                    

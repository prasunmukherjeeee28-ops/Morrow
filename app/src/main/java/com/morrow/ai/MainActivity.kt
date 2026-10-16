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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0B0B10)
private val Panel = Color(0xFF14131D)
private val Purple = Color(0xFFB69CFF)
private val TextMain = Color(0xFFF4F0FF)
private val Muted = Color(0xFF9994AD)

data class ChatMessage(val text: String, val isUser: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MorrowScreen()
        }
    }
}

@Composable
fun MorrowScreen() {
    var message by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var conversation by remember { mutableStateOf(listOf<ChatMessage>()) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background,
            surface = Panel,
            primary = Purple,
            onBackground = TextMain,
            onSurface = TextMain
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Panel)
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Purple, Color(0xFF7952D9))
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "m",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Background
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "morrow",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        "THINK BEYOND TODAY",
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                        color = Muted
                    )
                }

                Box {
                    Text(
                        "⋮",
                        fontSize = 27.sp,
                        color = TextMain,
                        modifier = Modifier
                            .clickable { showMenu = true }
                            .padding(horizontal = 8.dp)
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Panel)
                    ) {
                        DropdownMenuItem(
                            text = { Text("New conversation") },
                            onClick = {
                                conversation = emptyList()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About Morrow") },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }

            if (conversation.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "✦  YOUR SPACE TO THINK",
                        color = Purple,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        "Good ideas start\nwith a question.",
                        fontSize = 35.sp,
                        lineHeight = 43.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Explore ideas, learn something new, " +
                            "solve problems and turn your thoughts " +
                            "into something meaningful.",
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = Muted
                    )

                    Spacer(Modifier.height(28.dp))

                    SuggestionCard(
                        "⌘",
                        "Learn something new",
                        "Understand a topic step by step"
                    ) {
                        message = "Teach me Python from the beginning"
                    }

                    Spacer(Modifier.height(10.dp))

                    SuggestionCard(
                        "✳",
                        "Explore an idea",
                        "Turn curiosity into a project"
                    ) {
                        message = "Give me three creative project ideas"
                    }

                    Spacer(Modifier.height(10.dp))

                    SuggestionCard(
                        "◈",
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
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(conversation) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (item.isUser)
                                Arrangement.End else Arrangement.Start
                        ) {
                            Text(
                                text = item.text,
                                modifier = Modifier
                                    .widthIn(max = 310.dp)
                                    .background(
                                        if (item.isUser) Color(0xFF29213C)
                                        else Panel,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp),
                                color = TextMain,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Panel,
                            RoundedCornerShape(18.dp)
                        )
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Ask Morrow anything...",
                                color = Muted,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Purple
                        ),
                        maxLines = 5
                    )

                    Spacer(Modifier.width(6.dp))

                    Button(
                        onClick = {
                            val question = message.trim()
                            if (question.isNotEmpty()) {
                                conversation = conversation + ChatMessage(
                                    question, true
                                ) + ChatMessage(
                                    "Morrow's interface is working! " +
                                        "The AI backend isn't connected yet, " +
                                        "so real AI replies will be enabled " +
                                        "in a later step.",
                                    false
                                )
                                message = ""
                            }
                        },
                        enabled = message.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple,
                            contentColor = Background
                        )
                    ) {
                        Text("↑", fontSize = 24.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "MORROW PREVIEW  ·  AI NOT CONNECTED",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = Muted
                )
            }
        }
    }
}

@Composable
fun SuggestionCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp, color = Purple)

        Spacer(Modifier.width(14.dp))

        Column {
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
            Spacer(Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, color = Muted)
        }
    }
}

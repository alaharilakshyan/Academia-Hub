package com.example.academia.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.academia.ui.theme.VividViolet
import com.example.academia.ui.theme.VividCoral

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    
    val languages = listOf("English", "Spanish", "French", "Arabic", "Hindi", "German")
    
    val listState = rememberLazyListState()

    // Auto-scroll logic
    LaunchedEffect(messages.size, isLoading) {
        val targetSize = if (isLoading) messages.size else messages.size - 1
        if (targetSize >= 0) {
            listState.animateScrollToItem(targetSize)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academia AI Assistant", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.clearChat()
                        onBack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        TextButton(onClick = { languageMenuExpanded = true }) {
                            Text(currentLanguage, color = VividViolet, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            languages.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language, color = VividViolet) },
                                    onClick = {
                                        viewModel.changeLanguage(language)
                                        languageMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat history list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Quick Replies (Only show when at the very beginning)
            if (messages.size == 1) {
                val predefinedQuestions = listOf(
                    "How to scan a certificate?", 
                    "Is my data secure?", 
                    "What is OCR?", 
                    "I got a FAKE result", 
                    "Contact Support"
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predefinedQuestions) { question ->
                        AssistChip(
                            onClick = { viewModel.sendMessage(question) },
                            label = { Text(question) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }

            // Input Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask a question...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        containerColor = VividCoral,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_inf")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing_alpha"
    )
    Row(
        verticalAlignment = Alignment.Bottom, 
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            Icons.Default.SmartToy, 
            contentDescription = "Bot Typing", 
            modifier = Modifier.size(28.dp).padding(end = 6.dp), 
            tint = VividViolet.copy(alpha = alpha)
        )
        Text(
            text = "AI is typing...", 
            fontSize = 14.sp, 
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) {
        VividCoral.copy(alpha = 0.9f)
    } else if (message.isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isUser || message.isError) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Icon(
                Icons.Default.SmartToy, 
                contentDescription = "Bot", 
                modifier = Modifier.size(28.dp).padding(end = 6.dp), 
                tint = VividViolet
            )
        }
        
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(bgColor)
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 16.sp
            )
        }

        if (isUser) {
            Icon(
                Icons.Default.Person, 
                contentDescription = "User", 
                modifier = Modifier.size(28.dp).padding(start = 6.dp), 
                tint = VividCoral
            )
        }
    }
}

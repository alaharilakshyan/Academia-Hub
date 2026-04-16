package com.example.academia.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false,
    val isTyping: Boolean = false
)

class ChatViewModel : ViewModel() {
    
    private val _currentLanguage = MutableStateFlow("English")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var autoClearJob: Job? = null

    // Replace with your actual Gemini API Key from Google AI Studio
    private val apiKey = "AIzaSyCK7GYLMRjnGY5hczjfWwd0L-aqx6afrB4"
    
    private lateinit var generativeModel: GenerativeModel
    private lateinit var chat: com.google.ai.client.generativeai.Chat

    init {
        setupGenerativeModel("English")
    }

    private fun setupGenerativeModel(language: String) {
        val systemInstructionText = """
            [SYSTEM: YOU ARE THE ACADEMIA AI ASSISTANT]
            Academia is a digital credential and certificate verification platform. 
            Your primary goal is to guide students, employers, and institutions through verifying credentials securely and accurately.
            CRITICAL RULES:
            - You must answer questions accurately and in a highly precise, concise manner.
            - Do not invent features; we verify academic records via QR scans, Manual ID entry, and File Upload.
            - You MUST communicate entirely in the $language language.
        """.trimIndent()
        
        generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey,
            systemInstruction = content { text(systemInstructionText) }
        )
        chat = generativeModel.startChat()
        _messages.value = listOf(ChatMessage(text = getDefaultGreeting(language), isUser = false))
    }

    private fun getDefaultGreeting(language: String): String {
        return when (language) {
            "Spanish" -> "¡Hola! Soy tu Asistente de Academia \uD83E\uDD16. ¿Cómo puedo ayudarte hoy?"
            "French" -> "Bonjour! Je suis votre Assistant Academia \uD83E\uDD16. Comment puis-je vous aider?"
            "Arabic" -> "مرحباً! أنا مساعد أكاديميا الخاص بك \uD83E\uDD16. كيف يمكنني مساعدتك اليوم؟"
            "Hindi" -> "नमस्ते! मैं आपका एकेडेमिया असिस्टेंट हूँ \uD83E\uDD16। मैं आज आपकी कैसे मदद कर सकता हूँ?"
            "German" -> "Hallo! Ich bin dein Academia-Assistent \uD83E\uDD16. Wie kann ich dir heute helfen?"
            else -> "Hello! I'm your Academia Assistant \uD83E\uDD16. How can I help you today?"
        }
    }

    fun changeLanguage(language: String) {
        if (_currentLanguage.value != language) {
            _currentLanguage.value = language
            setupGenerativeModel(language)
        }
    }

    fun resetTimeout() {
        autoClearJob?.cancel()
        autoClearJob = viewModelScope.launch {
            delay(30000) // 30 seconds
            clearChat()
        }
    }

    fun clearChat() {
        setupGenerativeModel(_currentLanguage.value)
        autoClearJob?.cancel()
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // Cancel timeout while processing
        autoClearJob?.cancel()

        _messages.value = _messages.value + ChatMessage(text = userText, isUser = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val botMessageId = UUID.randomUUID().toString()
                _messages.value = _messages.value + ChatMessage(id = botMessageId, text = "", isUser = false, isTyping = true)
                _isLoading.value = false 
                
                // Call standalone Gemini API
                val response = chat.sendMessage(userText)
                val responseText = response.text ?: "I'm having trouble thinking right now."
                
                var currentText = ""
                val words = responseText.split(" ")
                for (i in words.indices) {
                    currentText += words[i] + " "
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMessageId) msg.copy(text = currentText) else msg
                    }
                    delay(20) 
                }
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == botMessageId) msg.copy(text = currentText.trimEnd(), isTyping = false) else msg
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _messages.value = _messages.value + ChatMessage(text = "Network Error: ${e.message}", isUser = false, isError = true)
            } finally {
                // Start tracking inactivity
                resetTimeout()
            }
        }
    }
}

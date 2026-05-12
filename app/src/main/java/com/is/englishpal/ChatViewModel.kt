package com.`is`.englishpal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.`is`.englishpal.room.AppDatabase
import com.`is`.englishpal.room.ChatMessageDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val dao: ChatMessageDao = AppDatabase.getInstance(application).chatMessageDao()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = dao.getAllMessages()
            if (saved.isNotEmpty()) {
                _messages.value = saved.map { ChatMessage.fromEntity(it) }
            } else {
                sendWelcome()
            }
        }
    }

    private suspend fun sendWelcome() {
        val welcome = ChatMessage(
            content = "Hello! I'm your English practice partner. What would you like to talk about today?",
            isUser = false
        )
        _messages.value = listOf(welcome)
        dao.insert(welcome.toEntity())
    }

    fun sendMessage(text: String) {
        val userMessage = ChatMessage(content = text, isUser = true)
        _messages.value = _messages.value + userMessage
        viewModelScope.launch { dao.insert(userMessage.toEntity()) }

        val aiMessage = ChatMessage(content = "", isUser = false, isStreaming = true)
        _messages.value = _messages.value + aiMessage
        _isLoading.value = false

        viewModelScope.launch {
            try {
                val json = JSONObject().apply {
                    put("model", "deepseek-chat")
                    put("stream", true)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "你是一个英语陪练助手。请用英语回复用户，然后用中文解释一遍。")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", text)
                        })
                    })
                }

                val request = Request.Builder()
                    .url("https://api.deepseek.com/chat/completions")
                    .addHeader("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(
                        json.toString()
                            .toRequestBody("application/json; charset=utf-8".toMediaType())
                    )
                    .build()

                val sb = StringBuilder()
                val factory = EventSources.createFactory(client)

                suspendCancellableCoroutine<Unit> { continuation ->
                    val eventSource = factory.newEventSource(request, object : EventSourceListener() {

                        override fun onEvent(
                            eventSource: EventSource, id: String?, type: String?, data: String
                        ) {
                            if (data == "[DONE]") return

                            val chunk = JSONObject(data)
                            val choices = chunk.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val delta = choices.getJSONObject(0).optJSONObject("delta")
                                if (delta != null) {
                                    val content = delta.optString("content", "")
                                    sb.append(content)
                                    val list = messages.value.toMutableList()
                                    list[list.size - 1] = ChatMessage(
                                        id = aiMessage.id,
                                        content = sb.toString(),
                                        isUser = false,
                                        isStreaming = true
                                    )
                                    _messages.value = list
                                }
                            }
                        }

                        override fun onFailure(
                            eventSource: EventSource, t: Throwable?, response: Response?
                        ) {
                            val list = _messages.value.toMutableList()
                            list[list.size - 1] = ChatMessage(
                                id = aiMessage.id,
                                content = "请求失败：${t?.message ?: "未知错误"}",
                                isUser = false,
                                isStreaming = false
                            )
                            _messages.value = list
                            continuation.resume(Unit)
                        }

                        override fun onClosed(eventSource: EventSource) {
                            val list = _messages.value.toMutableList()
                            val last = list.lastOrNull()
                            if (last != null && last.isStreaming) {
                                list[list.size - 1] = last.copy(isStreaming = false)
                                _messages.value = list
                            }
                            continuation.resume(Unit)
                        }
                    })

                    continuation.invokeOnCancellation {
                        eventSource.cancel()
                    }
                }

                val lastMsg = _messages.value.lastOrNull()
                if (lastMsg != null && !lastMsg.isUser && !lastMsg.isStreaming && lastMsg.content.isNotEmpty()) {
                    dao.insert(lastMsg.toEntity())
                }

            } catch (e: Exception) {
                _messages.value =
                    _messages.value + ChatMessage(content = "错误：${e.message}", isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            dao.deleteAll()
            _messages.value = emptyList()
            sendWelcome()
        }
    }
}

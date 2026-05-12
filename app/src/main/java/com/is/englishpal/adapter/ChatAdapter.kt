package com.`is`.englishpal.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.`is`.englishpal.ChatMessage
import com.`is`.englishpal.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages: List<ChatMessage> = emptyList()

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_AI = 2
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = messages[position].id

    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_ai, parent, false)
            AiViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.bind(message)
        } else if (holder is AiViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    //时间戳
    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    //长按复制
    private fun setupLongPress(textView: TextView, content: String) {
        textView.setOnLongClickListener {
            val clipboard = textView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Chat Message", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(textView.context, "已复制", Toast.LENGTH_SHORT).show()
            true
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.content
            tvTimestamp.text = formatTime(message.timestamp)
            tvTimestamp.visibility = if (message.isStreaming) View.GONE else View.VISIBLE
            setupLongPress(tvMessage, message.content)
        }
    }

    inner class AiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(message: ChatMessage) {
            tvMessage.text = if (message.isStreaming && message.content.isEmpty()) {
                "typing..."
            } else {
                message.content
            }
            tvTimestamp.text = formatTime(message.timestamp)
            tvTimestamp.visibility = if (message.isStreaming) View.GONE else View.VISIBLE
            setupLongPress(tvMessage, message.content)
        }
    }
}

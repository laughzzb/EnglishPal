package com.`is`.englishpal

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels // 扩展函数 by viewModels()，自动创建 ViewModel
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope // 协程作用域，跟随 Activity 生命周期
import androidx.lifecycle.repeatOnLifecycle // 在指定生命周期状态下重复执行协程
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.`is`.englishpal.adapter.ChatAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // by viewModels() = 委托模式，Activity 销毁时 ViewModel 自动清理，不会内存泄漏
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvChat = findViewById<RecyclerView>(R.id.rvChat)
        val etInput = findViewById<EditText>(R.id.etInput)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val btnClear = findViewById<Button>(R.id.btnClear)

        adapter = ChatAdapter()
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(this).also {
            it.stackFromEnd = true
        }

        // lifecycleScope = Activity 自带的协程作用域，Activity 销毁时自动取消
        // repeatOnLifecycle(STARTED) = 只在 Activity 可见时收集数据，不可见时停止收集，省电省性能
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // collect = 持续监听 StateFlow，每次数据变化都会收到新列表
                viewModel.messages.collect { messages ->
                    adapter.submitList(messages) // 把新数据交给适配器刷新界面
                    if (messages.isNotEmpty()) {
                        rvChat.smoothScrollToPosition(messages.size - 1) // 自动滚到底
                    }
                }
            }
        }

        etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                btnSend.performClick()
                true
            } else {
                false
            }
        }

        btnClear.setOnClickListener { viewModel.clearMessages() }

        btnSend.setOnClickListener {
            val input = etInput.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            etInput.text.clear()
            viewModel.sendMessage(input) // ViewModel 负责网络请求，Activity 只管调用
        }
    }
}

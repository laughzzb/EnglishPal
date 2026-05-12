# EnglishPal

Android AI English practice app powered by DeepSeek API with SSE streaming output.

## Features

- AI English conversation partner with bilingual responses (English + Chinese explanation)
- SSE streaming output (character-by-character display)
- Chat history persistence (Room database)
- Message timestamps & long-press copy
- Web version available for iPhone access via hotspot

## Tech Stack

- **Kotlin** — Main language
- **OkHttp SSE** — Streaming API calls
- **Coroutines + StateFlow** — Async & reactive UI
- **RecyclerView** — Chat message list
- **ViewModel** — UI state management
- **Room** — Local database for chat history
- **DeepSeek API** — AI model backend

## Setup

1. Open in Android Studio
2. Add your DeepSeek API key to `local.properties`:
   ```
   DEEPSEEK_API_KEY=your_key_here
   ```
3. Build and run on device (minSdk 26)

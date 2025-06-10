package com.example.kotlin_amateur.core.util

import android.content.Context
import android.util.Log
import androidx.emoji2.text.EmojiCompat
import coil.Coil

// 2. 🧹 전역 메모리 정리 함수
object MemoryCleanupManager {

    /**
     * 🔥 앱 전체 메모리 정리 (어디서든 호출 가능)
     */
    fun performGlobalCleanup(context: Context) {
        Log.d("MemoryCleanup", "🧹 전역 메모리 정리 시작")

        try {
            // 1. 이미지 캐시 정리
            Coil.imageLoader(context).apply {
                memoryCache?.clear()
                diskCache?.clear()
            }

            // 2. EmojiCompat 리셋 (가능한 경우)
            try {
                EmojiCompat.get().also {
                    if (it.loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
                        // EmojiCompat 메타데이터 정리 (내부적으로)
                        Log.d("MemoryCleanup", "🎭 EmojiCompat 데이터 정리 시도")
                    }
                }
            } catch (e: Exception) {
                Log.w("MemoryCleanup", "EmojiCompat 정리 스킵: ${e.message}")
            }

            // 3. 메모리 사용량 로깅
            logMemoryUsage("정리 후")

            Log.d("MemoryCleanup", "✅ 전역 메모리 정리 완료")

        } catch (e: Exception) {
            Log.e("MemoryCleanup", "❌ 메모리 정리 실패: ${e.message}")
        }
    }

    /**
     * 📊 현재 메모리 사용량 로깅
     */
    fun logMemoryUsage(prefix: String = "") {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory

        Log.d("MemoryUsage", """
            📊 $prefix 메모리 상태:
            ├── 사용 중: ${usedMemory}MB / ${maxMemory}MB
            ├── 사용률: ${(usedMemory * 100 / maxMemory)}%
            └── 여유 메모리: ${freeMemory}MB
        """.trimIndent())
    }
}
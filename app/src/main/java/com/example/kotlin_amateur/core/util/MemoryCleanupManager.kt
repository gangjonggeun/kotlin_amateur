package com.example.kotlin_amateur.core.util

import android.content.Context
import android.util.Log
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
package com.example.kotlin_amateur.utils

import android.util.Log

/**
 * 🔥 메모리 모니터링 유틸리티
 * 메모리 사용량을 실시간으로 추적하고 임계값 초과 시 경고
 */
object MemoryMonitor {
    
    private const val WARNING_THRESHOLD_MB = 200L // 200MB 초과 시 경고
    private const val CRITICAL_THRESHOLD_MB = 400L // 400MB 초과 시 위험
    
    /**
     * 🔍 메모리 사용량 로깅
     */
    fun logMemoryUsage(tag: String) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
            val freeMemInMB = runtime.freeMemory() / 1048576L
            val usagePercent = (usedMemInMB * 100 / maxHeapSizeInMB)
            
            val level = when {
                usedMemInMB > CRITICAL_THRESHOLD_MB -> "🚨 CRITICAL"
                usedMemInMB > WARNING_THRESHOLD_MB -> "⚠️ WARNING"
                else -> "✅ NORMAL"
            }
            
            Log.d("MemoryMonitor", "🔍 [$tag] $level")
            Log.d("MemoryMonitor", "   - 사용: ${usedMemInMB}MB")
            Log.d("MemoryMonitor", "   - 최대: ${maxHeapSizeInMB}MB")
            Log.d("MemoryMonitor", "   - 여유: ${freeMemInMB}MB")
            Log.d("MemoryMonitor", "   - 사용률: ${usagePercent}%")
            
            // 임계값 초과 시 추가 경고
            if (usedMemInMB > CRITICAL_THRESHOLD_MB) {
                Log.e("MemoryMonitor", "🚨 위험! 메모리 사용량이 ${CRITICAL_THRESHOLD_MB}MB를 초과했습니다!")
                forceGC()
            } else if (usedMemInMB > WARNING_THRESHOLD_MB) {
                Log.w("MemoryMonitor", "⚠️ 경고! 메모리 사용량이 ${WARNING_THRESHOLD_MB}MB를 초과했습니다!")
            }
            
        } catch (e: Exception) {
            Log.e("MemoryMonitor", "메모리 사용량 측정 실패", e)
        }
    }
    
    /**
     * 🧹 강제 가비지 컬렉션
     */
    fun forceGC() {
        try {
            System.gc()
            System.runFinalization()
            Log.d("MemoryMonitor", "🧹 강제 가비지 컬렉션 실행")
        } catch (e: Exception) {
            Log.e("MemoryMonitor", "가비지 컬렉션 실행 실패", e)
        }
    }
    
    /**
     * 🔍 메모리 누수 감지
     */
    fun checkMemoryLeak(tag: String, expectedMaxMB: Long = 150L) {
        val runtime = Runtime.getRuntime()
        val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
        
        if (usedMemInMB > expectedMaxMB) {
            Log.w("MemoryMonitor", "🔍 [$tag] 메모리 누수 의심!")
            Log.w("MemoryMonitor", "   - 현재: ${usedMemInMB}MB")
            Log.w("MemoryMonitor", "   - 예상: ${expectedMaxMB}MB")
            Log.w("MemoryMonitor", "   - 초과: ${usedMemInMB - expectedMaxMB}MB")
        }
    }
    
    /**
     * 📊 메모리 사용량 반환
     */
    fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        return MemoryInfo(
            usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L,
            freeMB = runtime.freeMemory() / 1048576L,
            maxMB = runtime.maxMemory() / 1048576L
        )
    }
}

/**
 * 📊 메모리 정보 데이터 클래스
 */
data class MemoryInfo(
    val usedMB: Long,
    val freeMB: Long,
    val maxMB: Long
) {
    val usagePercent: Long get() = (usedMB * 100 / maxMB)
    val isWarning: Boolean get() = usedMB > 200L
    val isCritical: Boolean get() = usedMB > 400L
}

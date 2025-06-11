package com.example.kotlin_amateur.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 🔥 메모리 최적화된 네트워크 인터셉터
 * 메모리 누수 방지 및 네트워크 요청 최적화
 */
class MemoryOptimizedInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        
        // 🔥 메모리 효율적인 헤더 추가
        request = request.newBuilder()
            .header("Connection", "close") // 연결 재사용 방지로 메모리 절약
            .header("Cache-Control", "no-cache, no-store") // 캐시 방지
            .build()

        var response: Response? = null
        
        try {
            response = chain.proceed(request)
            
            // 🔥 응답 크기 체크 (대용량 응답 방지)
            val contentLength = response.header("Content-Length")?.toLongOrNull() ?: 0
            val maxSizeBytes = 10 * 1024 * 1024 // 10MB 제한
            
            if (contentLength > maxSizeBytes) {
                response.close()
                throw IOException("응답 크기가 너무 큽니다: ${contentLength}바이트")
            }
            
            return response
            
        } catch (e: Exception) {
            response?.close() // 🔥 예외 발생 시 응답 스트림 정리
            throw e
        }
    }
}

/**
 * 🔥 메모리 사용량 모니터링 인터셉터
 */
class MemoryMonitoringInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // 요청 전 메모리 상태
        logMemoryBefore(request.url.toString())
        
        val startTime = System.currentTimeMillis()
        
        try {
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()
            
            // 요청 후 메모리 상태
            logMemoryAfter(request.url.toString(), endTime - startTime, response.code)
            
            return response
            
        } catch (e: Exception) {
            logMemoryError(request.url.toString(), e)
            throw e
        }
    }
    
    private fun logMemoryBefore(url: String) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            android.util.Log.d("NetworkMemory", "🚀 요청 시작 [$url] 메모리: ${usedMB}MB")
        } catch (e: Exception) {
            // 로깅 실패는 무시
        }
    }
    
    private fun logMemoryAfter(url: String, duration: Long, responseCode: Int) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            android.util.Log.d("NetworkMemory", "✅ 요청 완료 [$url] 메모리: ${usedMB}MB, 시간: ${duration}ms, 코드: $responseCode")
        } catch (e: Exception) {
            // 로깅 실패는 무시
        }
    }
    
    private fun logMemoryError(url: String, error: Exception) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            android.util.Log.e("NetworkMemory", "❌ 요청 실패 [$url] 메모리: ${usedMB}MB, 오류: ${error.message}")
        } catch (e: Exception) {
            // 로깅 실패는 무시
        }
    }
}

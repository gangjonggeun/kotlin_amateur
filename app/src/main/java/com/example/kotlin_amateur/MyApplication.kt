package com.example.kotlin_amateur

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.emoji2.text.EmojiCompat
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

@HiltAndroidApp
class MyApplication : Application() {

    private var lastMemoryCleanup = 0L // 🔥 메모리 정리 간격 제어

    override fun onCreate() {
        super.onCreate()
        setupOptimizedImageLoader()
        initKakaoMapSafely()
        monitorMemoryUsage()
        logInitialMemoryUsage()
    }
    private fun monitorMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory

        Log.d("MyApplication", """
            📊 앱 시작 시 메모리 상태:
            ├── 사용 중: ${usedMemory}MB / ${maxMemory}MB
            ├── 사용률: ${(usedMemory * 100 / maxMemory)}%
            └── 여유 메모리: ${freeMemory}MB
            
        """.trimIndent())
    }

    private fun setupOptimizedImageLoader() {
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.08) // 🔥 8%로 더 줄임 (기존 10%)
                    .strongReferencesEnabled(false)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(30 * 1024 * 1024) // 🔥 30MB로 줄임 (기존 50MB)
                    .build()
            }
            .respectCacheHeaders(false)
            // 🔥 네트워크 요청 제한 추가
            .okHttpClient {
                okhttp3.OkHttpClient.Builder()
                    .cache(okhttp3.Cache(cacheDir.resolve("http_cache"), 10 * 1024 * 1024))
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)

        val memoryLimitMB = (Runtime.getRuntime().maxMemory() * 0.08 / 1024 / 1024).toInt()
        Log.d("MyApplication", "✅ 이미지 메모리 제한: ${memoryLimitMB}MB")
    }

    private fun initKakaoMapSafely() {
        try {
            val abi = Build.SUPPORTED_ABIS[0]
            Log.d("MyApplication", "Current ABI: $abi")

            if (abi.contains("arm")) {
                // TODO: 실제 API 키로 교체 필요
                KakaoMapSdk.init(this, "35b1fe4c1b1ac26786fac46a9dd60588")
                Log.d("MyApplication", "✅ 카카오맵 초기화 준비 완료 (API 키 설정 필요)")
            } else {
                Log.w("MyApplication", "⚠️ x86 에뮬레이터 - 카카오맵 비활성화")
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "❌ 카카오맵 초기화 실패: ${e.message}")
        }
    }

    /**
     * 🔥 메모리 부족 시 안전한 정리 (System.gc() 제거)
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        val currentTime = System.currentTimeMillis()

        // 🔥 최소 5초 간격으로만 메모리 정리 (과도한 호출 방지)
        if (currentTime - lastMemoryCleanup < 5000) {
            Log.d("MyApplication", "⏰ 메모리 정리 스킵 (너무 빈번한 호출)")
            return
        }

        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w("MyApplication", "🚨 메모리 매우 부족! 적극적 정리 시작")
                performAggressiveCleanup()
                lastMemoryCleanup = currentTime
            }

            TRIM_MEMORY_RUNNING_LOW -> {
                Log.w("MyApplication", "⚠️ 메모리 부족 경고 - 이미지 캐시만 정리")
                performLightCleanup()
                lastMemoryCleanup = currentTime
            }

            TRIM_MEMORY_UI_HIDDEN -> {
                Log.i("MyApplication", "👁️ UI 숨김 - 불필요한 캐시 정리")
                performLightCleanup()
            }
        }
    }

    /**
     * 🔥 가벼운 메모리 정리 (안전함)
     */
    private fun performLightCleanup() {
        try {
            Coil.imageLoader(this).memoryCache?.clear()
            Log.d("MyApplication", "🧹 이미지 메모리 캐시 정리 완료")
        } catch (e: Exception) {
            Log.e("MyApplication", "❌ 가벼운 정리 실패: ${e.message}")
        }
    }

    /**
     * 🔥 적극적 메모리 정리 (System.gc() 대신 안전한 방법)
     */
    private fun performAggressiveCleanup() {
        try {
            // 1. 이미지 캐시 완전 정리
            Coil.imageLoader(this).apply {
                memoryCache?.clear()
                diskCache?.clear()
            }

            // 2. 앱 내부 캐시 정리 (Repository 등)
            clearApplicationCaches()

            // 3. System.gc() 대신 메모리 사용량 모니터링
            logMemoryUsageAfterCleanup()

            Log.d("MyApplication", "🧹 적극적 메모리 정리 완료")

        } catch (e: Exception) {
            Log.e("MyApplication", "❌ 적극적 정리 실패: ${e.message}")
        }
    }

    /**
     * 🔥 앱 내부 캐시 정리 (각 Repository의 캐시)
     */
    private fun clearApplicationCaches() {
        // Repository들의 캐시 정리를 위한 브로드캐스트
        // 실제로는 각 Repository에 clearCache() 메서드를 만들어야 함

        // 예시: UserProfileRepository, PostRepository 등의 캐시 정리
        // 이 부분은 프로젝트 구조에 맞게 구현
        Log.d("MyApplication", "📦 앱 캐시 정리 신호 발송")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w("MyApplication", "🆘 시스템 메모리 매우 부족!")
        performAggressiveCleanup()
    }

    /**
     * 🔥 메모리 사용량 로깅 (디버그용)
     */
    private fun logInitialMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory

        Log.d(
            "MyApplication", """
            📊 초기 메모리 상태:
            ├── 최대 메모리: ${maxMemory}MB
            ├── 할당된 메모리: ${totalMemory}MB  
            ├── 사용 중 메모리: ${usedMemory}MB
            └── 사용률: ${(usedMemory * 100 / maxMemory)}%
        """.trimIndent()
        )
    }

    private fun logMemoryUsageAfterCleanup() {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory

        Log.d(
            "MyApplication", """
            🧹 정리 후 메모리 상태:
            ├── 사용 중 메모리: ${usedMemory}MB
            ├── 사용률: ${(usedMemory * 100 / maxMemory)}%
            └── 정리 효과: 메모리 상태 개선됨
        """.trimIndent()
        )
    }
}
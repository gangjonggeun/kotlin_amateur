package com.example.kotlin_amateur

import android.app.Application
import android.os.Build
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

@HiltAndroidApp
class MyApplication : Application() {

    private var lastMemoryCleanup = 0L

    override fun onCreate() {
        super.onCreate()
        setupOptimizedImageLoader()
        initKakaoMapSafely()
        logInitialMemoryUsage()
    }

    private fun setupOptimizedImageLoader() {
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15) // 15% 사용 (안전한 수준)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)
        Log.d("MyApplication", "✅ 이미지 로더 초기화 완료")
    }

    private fun initKakaoMapSafely() {
        try {
            val abi = Build.SUPPORTED_ABIS[0]
            Log.d("MyApplication", "Current ABI: $abi")

            if (abi.contains("arm")) {
                KakaoMapSdk.init(this, "35b1fe4c1b1ac26786fac46a9dd60588")
                Log.d("MyApplication", "✅ 카카오맵 초기화 완료")
            } else {
                Log.w("MyApplication", "⚠️ x86 에뮬레이터 - 카카오맵 비활성화")
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "❌ 카카오맵 초기화 실패: ${e.message}")
        }
    }

    /**
     * 🔥 안전한 메모리 정리 (위험한 기능 비활성화)
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        val currentTime = System.currentTimeMillis()

        // 🔥 10초 간격으로만 허용 (더 안전하게)
        if (currentTime - lastMemoryCleanup < 10000) {
            Log.d("MyApplication", "⏰ 메모리 정리 스킵 (쿨다운 중)")
            return
        }

        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w("MyApplication", "🚨 메모리 부족 - 안전한 정리만 수행")
                performSafeCleanup()
                lastMemoryCleanup = currentTime
            }

            TRIM_MEMORY_RUNNING_LOW -> {
                Log.w("MyApplication", "⚠️ 메모리 경고 - 가벼운 정리")
                performLightCleanup()
                lastMemoryCleanup = currentTime
            }

            TRIM_MEMORY_UI_HIDDEN -> {
                Log.i("MyApplication", "👁️ UI 숨김 - 캐시 정리")
                performLightCleanup()
            }
        }
    }

    /**
     * 🔥 가벼운 메모리 정리 (매우 안전)
     */
    private fun performLightCleanup() {
        try {
            // 이미지 메모리 캐시만 정리 (안전함)
            Coil.imageLoader(this).memoryCache?.clear()
            Log.d("MyApplication", "🧹 이미지 메모리 캐시 정리")
        } catch (e: Exception) {
            Log.e("MyApplication", "정리 실패: ${e.message}")
        }
    }

    /**
     * 🔥 안전한 메모리 정리 (위험한 기능 제거)
     */
    private fun performSafeCleanup() {
        try {
            // 1. 이미지 캐시만 정리 (안전)
            Coil.imageLoader(this).memoryCache?.clear()
            

            
            Log.d("MyApplication", "🧹 안전한 메모리 정리 완료")
            
            // 메모리 상태만 로깅
            logMemoryUsage()

        } catch (e: Exception) {
            Log.e("MyApplication", "안전한 정리 실패: ${e.message}")
        }
    }

    /**
     * 🔥 위험한 onLowMemory 비활성화
     */
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w("MyApplication", "🆘 시스템 메모리 부족 - 안전 모드")
        
        // 위험한 적극적 정리 대신 가벼운 정리만
        performLightCleanup()
    }

    /**
     * 🔥 안전한 메모리 사용량 로깅
     */
    private fun logInitialMemoryUsage() {
        try {
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory() / 1024 / 1024
            val totalMemory = runtime.totalMemory() / 1024 / 1024
            val freeMemory = runtime.freeMemory() / 1024 / 1024
            val usedMemory = totalMemory - freeMemory

            Log.d("MyApplication", "📊 앱 시작 - 메모리: ${usedMemory}MB/${maxMemory}MB")
        } catch (e: Exception) {
            Log.e("MyApplication", "메모리 로깅 실패: ${e.message}")
        }
    }

    private fun logMemoryUsage() {
        try {
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory() / 1024 / 1024
            val totalMemory = runtime.totalMemory() / 1024 / 1024
            val freeMemory = runtime.freeMemory() / 1024 / 1024
            val usedMemory = totalMemory - freeMemory

            Log.d("MyApplication", "📊 현재 메모리: ${usedMemory}MB/${maxMemory}MB")
        } catch (e: Exception) {
            Log.e("MyApplication", "메모리 측정 실패: ${e.message}")
        }
    }
}

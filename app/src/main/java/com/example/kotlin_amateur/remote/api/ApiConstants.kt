package com.example.kotlin_amateur.remote.api

import android.os.Build
import android.util.Log

object ApiConstants {
    // 🔧 에뮬레이터용 URL
    const val BASE_URL = "http://10.0.2.2:8080"
    const val SPRING_URL = "http://192.168.219.103:8080"
    // 🔧 디버깅용 로깅
    fun logNetworkInfo() {
        Log.d("ApiConstants", "🌐 사용 중인 Base URL: $BASE_URL")
        Log.d("ApiConstants", "📱 기기 타입: ${if (isEmulator()) "에뮬레이터" else "실제 기기"}")
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
    }
}
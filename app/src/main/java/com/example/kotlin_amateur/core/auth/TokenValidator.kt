package com.example.kotlin_amateur.core.auth

import android.util.Base64
import org.json.JSONObject

object TokenValidator {

    fun isAccessTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            val exp = json.getLong("exp")
            val currentTime = System.currentTimeMillis() / 1000
            exp <= currentTime
        } catch (e: Exception) {
            true
        }
    }
}
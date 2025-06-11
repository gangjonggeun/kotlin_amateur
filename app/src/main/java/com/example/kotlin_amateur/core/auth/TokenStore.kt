package com.example.kotlin_amateur.core.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 🔐 메모리 최적화된 토큰 저장소
 * DataStore를 사용하여 안전하고 메모리 효율적으로 토큰 관리
 */
object TokenStore {

    private val Context.dataStore by preferencesDataStore(name = "auth_tokens")

    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

    /**
     * 🔐 토큰 저장 (메모리 안전)
     */
    suspend fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
                preferences[REFRESH_TOKEN_KEY] = refreshToken
            }
        } catch (e: Exception) {
            throw Exception("토큰 저장 실패: ${e.message}", e)
        }
    }

    /**
     * 🔐 액세스 토큰 가져오기
     */
    suspend fun getAccessToken(context: Context): String? {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[ACCESS_TOKEN_KEY]
            }.first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 🔐 리프레시 토큰 가져오기
     */
    suspend fun getRefreshToken(context: Context): String? {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[REFRESH_TOKEN_KEY]
            }.first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 🔐 두 토큰 모두 가져오기
     */
    suspend fun getTokens(context: Context): Pair<String?, String?> {
        return try {
            context.dataStore.data.map { preferences ->
                Pair(
                    preferences[ACCESS_TOKEN_KEY],
                    preferences[REFRESH_TOKEN_KEY]
                )
            }.first()
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    /**
     * 🧹 토큰 삭제 (로그아웃 시)
     */
    suspend fun clearTokens(context: Context) {
        try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
        } catch (e: Exception) {
            throw Exception("토큰 삭제 실패: ${e.message}", e)
        }
    }

    /**
     * 🔍 토큰 존재 여부 확인
     */
    suspend fun hasValidTokens(context: Context): Boolean {
        val (accessToken, refreshToken) = getTokens(context)
        return !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
    }
}

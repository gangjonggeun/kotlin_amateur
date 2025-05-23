package com.example.kotlin_amateur.core.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object TokenStore {

    private val ACCESS = stringPreferencesKey("access_token")
    private val REFRESH = stringPreferencesKey("refresh_token")

    private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

    suspend fun saveTokens(context: Context, access: String, refresh: String) {
        context.dataStore.edit {
            it[ACCESS] = CryptoManager.encrypt(access)
            it[REFRESH] = CryptoManager.encrypt(refresh)
        }
    }

    suspend fun loadTokens(context: Context): Pair<String?, String?> {
        val prefs = context.dataStore.data.first()
        val access = prefs[ACCESS]?.let { CryptoManager.decrypt(it) }
        val refresh = prefs[REFRESH]?.let { CryptoManager.decrypt(it) }
        return access to refresh
    }
    suspend fun getAccessToken(context: Context): String? {
        val prefs = context.dataStore.data.first()
        return prefs[ACCESS]?.let { CryptoManager.decrypt(it) }
    }
    suspend fun clear(context: Context) {
        context.dataStore.edit {
            it.remove(ACCESS)
            it.remove(REFRESH)
        }
    }
}

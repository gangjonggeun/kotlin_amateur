package com.example.kotlin_amateur.core.interceptor


import android.content.Context
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.core.auth.TokenValidator
import com.example.kotlin_amateur.remote.RetrofitHelper
import com.example.kotlin_amateur.remote.api.ReissueApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        runBlocking {
            val (accessToken, refreshToken) = TokenStore.loadTokens(context)
            var token = accessToken

            if (token.isNullOrBlank()) return@runBlocking

            if (TokenValidator.isAccessTokenExpired(token)) {
                val retrofit = RetrofitHelper.retrofitInstance
                val apiService = retrofit.create(ReissueApi::class.java)

                val refresh = refreshToken ?: return@runBlocking

                val response = apiService.reissue("Bearer $refresh").execute()
                if (response.isSuccessful && response.body() != null) {
                    token = response.body()!!.accessToken
                    TokenStore.saveTokens(context, token, refresh)
                }
            }

            if (!token.isNullOrBlank()) {
                request = request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            }
        }

        return chain.proceed(request)
    }
}

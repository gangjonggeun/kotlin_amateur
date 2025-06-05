package com.example.kotlin_amateur.repository

import android.util.Log
import com.example.kotlin_amateur.remote.api.BackendApiService
import com.example.kotlin_amateur.remote.response.ProfileResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: BackendApiService
) {
    suspend fun setupProfile(
        accessToken: String,
        nickname: String,
        imagePart: MultipartBody.Part? // 선택된 이미지 없으면 null
    ): ProfileResponse {
        val nicknamePart = nickname.toRequestBody("text/plain".toMediaTypeOrNull())
        return api.setupProfile("Bearer $accessToken", imagePart, nicknamePart)
    }

    // ✅ 사용자 정보 조회 API (새로 추가)
    suspend fun getMyProfile(accessToken: String): ProfileResponse {
        Log.d("profile TokenTest", "Bearer $accessToken")

        return api.getMyProfile("Bearer $accessToken").body()
            ?: throw IllegalStateException("profile load fail")
    }

}
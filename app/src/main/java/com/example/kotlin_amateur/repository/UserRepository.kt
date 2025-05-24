package com.example.kotlin_amateur.repository

import com.example.kotlin_amateur.remote.api.BackendApiService
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
    ) {
        val nicknamePart = nickname.toRequestBody("text/plain".toMediaTypeOrNull())
        api.setupProfile("Bearer $accessToken", imagePart, nicknamePart)
    }
}
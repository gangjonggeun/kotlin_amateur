package com.example.kotlin_amateur.repository


import android.content.Context
import android.net.Uri
import com.example.kotlin_amateur.remote.api.PostApiService
import com.example.kotlin_amateur.remote.request.PostRequest
import com.example.kotlin_amateur.remote.response.PostResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
class FloatingAddRepository @Inject constructor(
    private val apiService: PostApiService
) {
    suspend fun uploadPost(accessToken: String, request: PostRequest): Response<PostResponse> {
        return apiService.uploadPost("Bearer $accessToken", request)
    }
    suspend fun uploadImages(accessToken: String, parts: List<MultipartBody.Part>): List<String> {
        val response = apiService.uploadImages("Bearer $accessToken", parts)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("이미지 업로드 실패: ${response.code()}")
        }
    }

}

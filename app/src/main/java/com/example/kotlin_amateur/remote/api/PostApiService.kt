package com.example.kotlin_amateur.remote.api

import com.example.kotlin_amateur.remote.request.PostRequest
import com.example.kotlin_amateur.remote.response.PostResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PostApiService {
    @Multipart
    @POST("posts/uploadImages")
    suspend fun uploadImages(@Header("Authorization") accessToken: String, @Part images: List<MultipartBody.Part>): Response<List<String>> // URL 리스트 반환
    @POST("posts/write")
    suspend fun uploadPost(@Header("Authorization") accessToken: String, @Body request: PostRequest): Response<PostResponse>
}
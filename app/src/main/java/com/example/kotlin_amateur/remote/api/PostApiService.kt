package com.example.kotlin_amateur.remote.api

import com.example.kotlin_amateur.remote.request.PostRequest
import com.example.kotlin_amateur.remote.response.ImageUploadResponse
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.remote.response.PostResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PostApiService {
    // 좋아요 추가
    @POST("/api/posts/{postId}/like")
    suspend fun likePost(
        @Header("Authorization") accessToken: String,
        @Path("postId") postId: String
    ): Response<Unit>

    // 좋아요 취소
    @DELETE("/api/posts/{postId}/unlike")
    suspend fun unlikePost(
        @Header("Authorization") accessToken: String,
        @Path("postId") postId: String
    ): Response<Unit>
    @GET("api/posts/list")
    suspend fun getPostsForApp(@Header("Authorization") accessToken: String): Response<List<PostListResponse>>
    @Multipart
    @POST("api/posts/uploadImages")
    suspend fun uploadImages(@Header("Authorization") accessToken: String, @Part images: List<MultipartBody.Part>): Response<ImageUploadResponse> // URL 리스트 반환
    @POST("api/posts/write")
    suspend fun uploadPost(@Header("Authorization") accessToken: String, @Body request: PostRequest): Response<PostResponse>
}
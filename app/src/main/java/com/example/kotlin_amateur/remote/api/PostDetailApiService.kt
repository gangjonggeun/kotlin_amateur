package com.example.kotlin_amateur.remote.api

import com.example.kotlin_amateur.remote.request.CreateCommentRequest
import com.example.kotlin_amateur.remote.request.CreateReplyRequest
import com.example.kotlin_amateur.remote.response.ApiResponse
import com.example.kotlin_amateur.remote.response.CommentResponse
import com.example.kotlin_amateur.remote.response.PostDetailResponse
import com.example.kotlin_amateur.remote.response.ReplyResponse
import retrofit2.Response
import retrofit2.http.*
// API Service (기존 API 그대로 사용)
interface PostDetailApiService {
    @GET("/api/posts/{postId}")
    suspend fun getPostDetail(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<PostDetailResponse>>

    @GET("/api/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<CommentResponse>>>

    @POST("/api/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: String,
        @Body request: CreateCommentRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<CommentResponse>>

    @POST("/api/comments/{commentId}/replies")
    suspend fun createReply(
        @Path("commentId") commentId: String,
        @Body request: CreateReplyRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<ReplyResponse>>

    // 🔥 기존 PostLikeController API 그대로 사용
    @POST("/api/posts/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @DELETE("/api/posts/{postId}/unlike")
    suspend fun unlikePost(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("/api/posts/{postId}/like/status")
    suspend fun getLikeStatus(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>
}

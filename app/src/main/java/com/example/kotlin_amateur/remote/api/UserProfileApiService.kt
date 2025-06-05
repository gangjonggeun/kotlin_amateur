package com.example.kotlin_amateur.remote.api
import com.example.kotlin_amateur.remote.response.ApiResponse
import com.example.kotlin_amateur.remote.response.UserProfileResponse
import com.example.kotlin_amateur.remote.response.UserPostResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * UserProfileApiService - Response DTO 사용
 */
interface UserProfileApiService {

    /**
     * 사용자 프로필 정보 조회
     */
    @GET("/api/users/{userId}/profile")
    suspend fun getUserProfile(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<UserProfileResponse>> // ✅ Response DTO 사용

    /**
     * 사용자의 게시글 목록 조회
     */
    @GET("/api/users/{userId}/posts")
    suspend fun getUserPosts(
        @Path("userId") userId: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 10,
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<List<UserPostResponse>>> // ✅ Response DTO 사용

//    /**
//     * 팔로우 상태 조회
//     */
//    @GET("/api/users/{userId}/follow/status")
//    suspend fun getFollowStatus(
//        @Path("userId") userId: String,
//        @Header("Authorization") authorization: String
//    ): Response<Map<String, Any>>
//
//    /**
//     * 사용자 팔로우
//     */
//    @POST("/api/users/{userId}/follow")
//    suspend fun followUser(
//        @Path("userId") userId: String,
//        @Header("Authorization") authorization: String
//    ): Response<Map<String, Any>>
//
//    /**
//     * 사용자 언팔로우
//     */
//    @DELETE("/api/users/{userId}/follow")
//    suspend fun unfollowUser(
//        @Path("userId") userId: String,
//        @Header("Authorization") authorization: String
//    ): Response<Map<String, Any>>
}
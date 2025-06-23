package com.example.kotlin_amateur.remote.api


import com.example.kotlin_amateur.remote.response.ApiResponse
import com.example.kotlin_amateur.remote.response.PostListResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 🎯 프로필 관련 게시글 API 서비스
 * - 내 게시글, 좋아요한 글, 최근 본 글 전용 API
 * - PostApiService와 분리하여 유지보수성 향상
 * - 메모리 최적화: Result 패턴 사용으로 Exception 방지
 */
interface ProfilePostApiService {
    
    /**
     * 📝 내 게시글 목록 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기 (기본 20)
     * @param sort 정렬 기준 (timeStamp,desc | updatedAt,desc)
     */
    @GET("/api/posts/my")
    suspend fun getMyPosts(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "timeStamp,desc"
    ): Response<ApiResponse<List<PostListResponse>>>
    
    /**
     * ❤️ 좋아요한 게시글 목록 조회
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준 (좋아요 누른 시간 순)
     */
    @GET("/api/posts/liked")
    suspend fun getLikedPosts(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"//"likedAt,desc"
    ): Response<ApiResponse<List<PostListResponse>>>
    
    /**
     * 👀 최근 본 게시글 목록 조회
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준 (조회 시간 순)
     */
    @GET("/api/posts/recent-viewed")
    suspend fun getRecentViewedPosts(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "viewedAt,desc"
    ): Response<ApiResponse<List<PostListResponse>>>

    /**
     * 🗑️ 최근 본 글 기록 삭제
     * @param postId 기록 삭제할 게시글 ID
     */
    @DELETE("/api/posts/{postId}/recent-view")
    suspend fun removeRecentViewedPost(
        @Header("Authorization") authToken: String,
        @Path("postId") postId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * 🗑️ 최근 본 글 전체 기록 삭제
     */
    @DELETE("/api/posts/recent-viewed/all")
    suspend fun clearAllRecentViewedPosts(
        @Header("Authorization") authToken: String
    ): Response<ApiResponse<Unit>>
}

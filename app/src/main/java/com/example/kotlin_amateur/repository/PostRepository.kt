package com.example.kotlin_amateur.repository

import com.example.kotlin_amateur.remote.api.BackendApiService
import com.example.kotlin_amateur.remote.api.PostApiService
import com.example.kotlin_amateur.remote.response.PostListResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiService: PostApiService
) {

    suspend fun getPostsList(accessToken: String): Response<List<PostListResponse>> {
        return apiService.getPostsForApp("Bearer $accessToken")
    }
    // 좋아요 추가
    suspend fun likePost(accessToken: String, postId: String): Response<Unit> {
        return apiService.likePost("Bearer $accessToken", postId)
    }

    // 좋아요 취소
    suspend fun unlikePost(accessToken: String, postId: String): Response<Unit> {
        return apiService.unlikePost("Bearer $accessToken", postId)
    }
    // 필요시 추가적인 게시글 관련 메서드들
    // suspend fun getPostDetail(accessToken: String, postId: Long): Response<PostDetailResponse>
    // suspend fun deletePost(accessToken: String, postId: Long): Response<Unit>
}
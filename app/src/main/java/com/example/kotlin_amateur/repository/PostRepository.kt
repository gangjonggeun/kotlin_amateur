package com.example.kotlin_amateur.repository

import android.content.Context
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.exception.safeApiCall
import com.example.kotlin_amateur.remote.api.BackendApiService
import com.example.kotlin_amateur.remote.api.PostApiService
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.state.ApiResult
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiService: PostApiService
) {


    // 🔄 페이징 목록 조회
    suspend fun getPostList(context: Context, page: Int, size: Int): ApiResult<List<PostListResponse>> {
        return safeApiCall {
            val token = TokenStore.getAccessToken(context) // ✅ Object 직접 호출
                ?: throw Exception("로그인이 필요합니다")

            val response = apiService.getPostList(page, size, "Bearer $token")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data ?: emptyList()
                    } else {
                        throw Exception(apiResponse.message ?: "게시글 목록 조회 실패")
                    }
                } ?: throw Exception("응답 데이터가 없습니다")
            } else {
                throw Exception("네트워크 오류: ${response.code()}")
            }
        }
    }

    // 🔍 검색
    suspend fun searchPosts(context: Context, query: String, page: Int, size: Int): ApiResult<List<PostListResponse>> {
        return safeApiCall {
            val token = TokenStore.getAccessToken(context) // ✅ Object 직접 호출
                ?: throw Exception("로그인이 필요합니다")

            val response = apiService.searchPosts(query, page, size, "Bearer $token")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data ?: emptyList()
                    } else {
                        throw Exception(apiResponse.message ?: "검색 실패")
                    }
                } ?: throw Exception("응답 데이터가 없습니다")
            } else {
                throw Exception("네트워크 오류: ${response.code()}")
            }
        }
    }

//    // 🔄 기존 방식 (호환성 유지)
//    suspend fun getPostsList(accessToken: String): Response<List<PostListResponse>> {
//        return apiService.getPostsForApp("Bearer $accessToken")
//    }
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
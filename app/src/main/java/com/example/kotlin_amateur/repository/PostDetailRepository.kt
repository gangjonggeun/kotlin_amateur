package com.example.kotlin_amateur.repository

import android.content.Context
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.remote.api.PostDetailApiService
import com.example.kotlin_amateur.remote.request.CreateCommentRequest
import com.example.kotlin_amateur.remote.request.CreateReplyRequest
import com.example.kotlin_amateur.remote.response.CommentResponse
import com.example.kotlin_amateur.remote.response.PostDetailResponse
import com.example.kotlin_amateur.remote.response.ReplyResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Repository
@Singleton
class PostDetailRepository @Inject constructor(
    private val apiService: PostDetailApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun getPostDetail(postId: String): Result<PostDetailResponse> {
        return try {
            val accessToken = TokenStore.getAccessToken(context)
                ?: return Result.failure(Exception("로그인이 필요합니다"))

            val response = apiService.getPostDetail(postId, "Bearer $accessToken")

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "포스트를 불러올 수 없습니다"))
                }
            } else {
                Result.failure(Exception("서버 오류가 발생했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(postId: String): Result<List<CommentResponse>> {
        return try {
            val accessToken = TokenStore.getAccessToken(context)
                ?: return Result.failure(Exception("로그인이 필요합니다"))

            val response = apiService.getComments(postId, "Bearer $accessToken")

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "댓글을 불러올 수 없습니다"))
                }
            } else {
                Result.failure(Exception("서버 오류가 발생했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createComment(postId: String, content: String): Result<CommentResponse> {
        return try {
            val accessToken = TokenStore.getAccessToken(context)
                ?: return Result.failure(Exception("로그인이 필요합니다"))

            val response = apiService.createComment(
                postId,
                CreateCommentRequest(content),
                "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "댓글을 작성할 수 없습니다"))
                }
            } else {
                Result.failure(Exception("서버 오류가 발생했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createReply(commentId: String, content: String): Result<ReplyResponse> {
        return try {
            val accessToken = TokenStore.getAccessToken(context)
                ?: return Result.failure(Exception("로그인이 필요합니다"))

            val response = apiService.createReply(
                commentId,
                CreateReplyRequest(content, commentId),
                "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "답글을 작성할 수 없습니다"))
                }
            } else {
                Result.failure(Exception("서버 오류가 발생했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(postId: String): Result<Unit> {
        return try {
            val accessToken = TokenStore.getAccessToken(context)
                ?: return Result.failure(Exception("로그인이 필요합니다"))

            // 1. 현재 좋아요 상태 확인
            val statusResponse = apiService.getLikeStatus(postId.toString(), "Bearer $accessToken")

            if (statusResponse.isSuccessful && statusResponse.body() != null) {
                val statusBody = statusResponse.body()!!
                val isCurrentlyLiked = statusBody["isLiked"] as? Boolean ?: false

                // 2. 좋아요 상태에 따라 토글
                val toggleResponse = if (isCurrentlyLiked) {
                    // 이미 좋아요 상태 → 좋아요 취소
                    apiService.unlikePost(postId.toString(), "Bearer $accessToken")
                } else {
                    // 좋아요 안한 상태 → 좋아요 추가
                    apiService.likePost(postId.toString(), "Bearer $accessToken")
                }

                if (toggleResponse.isSuccessful && toggleResponse.body() != null) {
                    val toggleBody = toggleResponse.body()!!
                    val success = toggleBody["success"] as? Boolean ?: false

                    if (success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(toggleBody["message"] as? String ?: "좋아요 처리에 실패했습니다"))
                    }
                } else {
                    Result.failure(Exception("좋아요 처리 중 서버 오류가 발생했습니다"))
                }
            } else {
                Result.failure(Exception("좋아요 상태를 확인할 수 없습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
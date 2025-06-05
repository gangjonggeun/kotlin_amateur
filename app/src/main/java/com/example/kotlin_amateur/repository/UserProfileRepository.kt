package com.example.kotlin_amateur.repository

import android.content.Context
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.model.PostSummary
import com.example.kotlin_amateur.model.UserProfile
import com.example.kotlin_amateur.remote.api.UserProfileApiService
import com.example.kotlin_amateur.remote.response.toDomainModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserProfileRepository - 사용자 프로필 관련 데이터 저장소
 *
 * 🎯 현재 구현 범위:
 * - 사용자 프로필 조회
 * - 사용자 게시글 목록 조회
 *
 * 🚫 나중에 추가할 기능들:
 * - 팔로우/언팔로우
 * - 팔로우 상태 관리
 */
@Singleton
class UserProfileRepository @Inject constructor(
    private val apiService: UserProfileApiService,
    @ApplicationContext private val context: Context
) {

    /**
     * 사용자 프로필 정보 조회
     * @param userId 조회할 사용자 ID
     * @return Result<UserProfile>
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val accessToken = TokenStore.getAccessToken(context)
                ?: return Result.failure(Exception("로그인이 필요합니다"))

            val response = apiService.getUserProfile(userId, "Bearer $accessToken")

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    // ✅ Response DTO를 Domain Model로 변환
                    Result.success(apiResponse.data.toDomainModel())
                } else {
                    Result.failure(Exception(apiResponse.message ?: "프로필을 불러올 수 없습니다"))
                }
            } else {
                Result.failure(Exception("서버 오류가 발생했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자의 게시글 목록 조회
     * @param userId 사용자 ID
     * @param offset 오프셋 (페이징용)
     * @param limit 가져올 개수
     * @return Result<List<PostSummary>>
     */
    suspend fun getUserPosts(
        userId: String,
        offset: Int = 0,
        limit: Int = 10
    ): Result<List<PostSummary>> {
        return try {
            val accessToken = TokenStore.getAccessToken(context)
                ?: return Result.failure(Exception("로그인이 필요합니다"))

            val response = apiService.getUserPosts(
                userId = userId,
                offset = offset,
                limit = limit,
                authorization = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    // ✅ Response DTO 리스트를 Domain Model 리스트로 변환
                    val postSummaries = apiResponse.data.map { it.toDomainModel() }
                    Result.success(postSummaries)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "게시글을 불러올 수 없습니다"))
                }
            } else {
                Result.failure(Exception("서버 오류가 발생했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
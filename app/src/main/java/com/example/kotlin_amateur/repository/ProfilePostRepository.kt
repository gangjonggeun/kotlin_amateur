package com.example.kotlin_amateur.repository

import android.content.Context
import android.util.Log
import com.example.kotlin_amateur.core.PostListType
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.exception.safeApiCall
import com.example.kotlin_amateur.remote.api.ProfilePostApiService
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.state.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎯 프로필 관련 게시글 Repository
 * - 내 게시글, 좋아요한 글, 최근 본 글 전용 Repository
 * - 메모리 안전: safeApiCall로 Exception 방지 (Result 패턴)
 * - 토큰 관리: TokenStore를 통한 안전한 인증 처리
 */
@Singleton
class ProfilePostRepository @Inject constructor(
    private val profilePostApi: ProfilePostApiService
) {

    companion object {
        private const val TAG = "ProfilePostRepository"
        private const val DEFAULT_PAGE_SIZE = 5
    }

    /**
     * 🎯 PostListType에 따른 게시글 목록 조회
     * - MY_POSTS: 내 게시글
     * - LIKED_POSTS: 좋아요한 글
     * - RECENT_VIEWED: 최근 본 글
     * @param context 컨텍스트 (토큰 읽기용)
     * @param postListType 게시글 목록 타입
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기 (기본 20)
     */
    suspend fun getPostsByType(
        context: Context,
        postListType: PostListType,
        page: Int = 0,
        size: Int = DEFAULT_PAGE_SIZE
    ): ApiResult<List<PostListResponse>> {
        return safeApiCall {
            Log.d(TAG, "🚀 getPostsByType: ${postListType.displayName}, page: $page, size: $size")
            
            // 🔐 토큰 확인
            val token = TokenStore.getAccessToken(context)
                ?: throw Exception("로그인이 필요합니다")
            
            val bearerToken = "Bearer $token"
            
            // 🎯 PostListType에 따른 API 호출
            val response = when (postListType) {
                PostListType.MY_POSTS -> {
                    Log.d(TAG, "📝 내 게시글 조회")
                    profilePostApi.getMyPosts(bearerToken,page, size)
                }
                PostListType.LIKED_POSTS -> {
                    Log.d(TAG, "❤️ 좋아요한 글 조회") 
                    profilePostApi.getLikedPosts(bearerToken,page, size)
                }
                PostListType.RECENT_VIEWED -> {
                    Log.d(TAG, "👀 최근 본 글 조회")
                    profilePostApi.getRecentViewedPosts(bearerToken,page, size)
                }
                PostListType.HOME -> {
                    throw IllegalArgumentException("HOME 타입은 PostRepository를 사용하세요")
                }
            }
            
            // 🔍 응답 처리
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d(TAG, "✅ API 응답 성공: success=${apiResponse.success}")
                    if (apiResponse.success) {
                        // 🎯 명시적 타입 캐스팅으로 해결
                        val posts = (apiResponse.data as? List<PostListResponse>) ?: emptyList()
                        Log.d(TAG, "📊 조회된 게시글 수: ${posts.size}")
                        posts
                    } else {
                        val errorMsg = apiResponse.message ?: "${postListType.displayName} 조회 실패"
                        Log.e(TAG, "❌ API 에러: $errorMsg")
                        throw Exception(errorMsg)
                    }
                } ?: run {
                    Log.e(TAG, "❌ 응답 데이터가 null")
                    throw Exception("응답 데이터가 없습니다")
                }
            } else {
                val errorMsg = "네트워크 오류: ${response.code()} - ${response.message()}"
                Log.e(TAG, "❌ $errorMsg")
                throw Exception(errorMsg)
            }
        }
    }

    /**
     * 📝 내 게시글 목록 조회
     */
    suspend fun getMyPosts(
        context: Context,
        page: Int = 0,
        size: Int = DEFAULT_PAGE_SIZE
    ): ApiResult<List<PostListResponse>> {
        return getPostsByType(context, PostListType.MY_POSTS, page, size)
    }

    /**
     * ❤️ 좋아요한 게시글 목록 조회
     */
    suspend fun getLikedPosts(
        context: Context,
        page: Int = 0,
        size: Int = DEFAULT_PAGE_SIZE
    ): ApiResult<List<PostListResponse>> {
        return getPostsByType(context, PostListType.LIKED_POSTS, page, size)
    }

    /**
     * 👀 최근 본 게시글 목록 조회
     */
    suspend fun getRecentViewedPosts(
        context: Context,
        page: Int = 0,
        size: Int = DEFAULT_PAGE_SIZE
    ): ApiResult<List<PostListResponse>> {
        return getPostsByType(context, PostListType.RECENT_VIEWED, page, size)
    }

    /**
     * 🗑️ 최근 본 글 개별 삭제
     * @param context 컨텍스트
     * @param postId 삭제할 게시글 ID
     */
    suspend fun removeRecentViewedPost(
        context: Context,
        postId: String
    ): ApiResult<Unit> {
        return safeApiCall {
            Log.d(TAG, "🗑️ 최근 본 글 개별 삭제: $postId")
            
            val token = TokenStore.getAccessToken(context)
                ?: throw Exception("로그인이 필요합니다")
            
            val response = profilePostApi.removeRecentViewedPost(token, postId)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        Log.d(TAG, "✅ 최근 본 글 삭제 성공")
                        // 🎯 Unit 타입 명시적 반환
                        Unit
                    } else {
                        val errorMsg = apiResponse.message ?: "최근 본 글 삭제 실패"
                        Log.e(TAG, "❌ $errorMsg")
                        throw Exception(errorMsg)
                    }
                } ?: throw Exception("응답 데이터가 없습니다")
            } else {
                val errorMsg = "삭제 실패: ${response.code()}"
                Log.e(TAG, "❌ $errorMsg")
                throw Exception(errorMsg)
            }
        }
    }

    /**
     * 🗑️ 최근 본 글 전체 삭제
     * @param context 컨텍스트
     */
    suspend fun clearAllRecentViewedPosts(
        context: Context
    ): ApiResult<Unit> {
        return safeApiCall {
            Log.d(TAG, "🗑️ 최근 본 글 전체 삭제")
            
            val token = TokenStore.getAccessToken(context)
                ?: throw Exception("로그인이 필요합니다")
            
            val response = profilePostApi.clearAllRecentViewedPosts(token)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        Log.d(TAG, "✅ 최근 본 글 전체 삭제 성공")
                        // 🎯 Unit 타입 명시적 반환
                        Unit
                    } else {
                        val errorMsg = apiResponse.message ?: "전체 삭제 실패"
                        Log.e(TAG, "❌ $errorMsg")
                        throw Exception(errorMsg)
                    }
                } ?: throw Exception("응답 데이터가 없습니다")
            } else {
                val errorMsg = "전체 삭제 실패: ${response.code()}"
                Log.e(TAG, "❌ $errorMsg")
                throw Exception(errorMsg)
            }
        }
    }

    /**
     * 📊 프로필 게시글 통계 조회
     * - 서버 통신 없이 클라이언트에서 직접 계산
     * - 각 타입별 첫 페이지 조회 후 리스트 크기로 계산
     * - CPU/GPU 사용량 최소화
     */
    suspend fun getProfilePostsCount(context: Context): ApiResult<ProfilePostsCount> {
        return safeApiCall {
            Log.d(TAG, "📊 프로필 게시글 통계 조회 (클라이언트 계산)")
            
            // 🔄 각 타입별로 첫 페이지 데이터 조회 (전체 데이터 크기 파악)
            val myPostsResult = getMyPosts(context, 0, 100) // 첫 100개만 조회
            val likedPostsResult = getLikedPosts(context, 0, 100)
            val recentViewedResult = getRecentViewedPosts(context, 0, 100)
            
            ProfilePostsCount(
                myPostsCount = when (myPostsResult) {
                    is ApiResult.Success -> {
                        val count = myPostsResult.data.size
                        Log.d(TAG, "📝 내 게시글 수: $count")
                        count
                    }
                    is ApiResult.Error -> {
                        Log.w(TAG, "⚠️ 내 게시글 조회 실패 [${myPostsResult.code}]: ${myPostsResult.message}")
                        0
                    }
                    is ApiResult.Loading -> {
                        Log.d(TAG, "🔄 내 게시글 로딩 중...")
                        0 // 로딩 중이면 0으로 처리
                    }
                },
                likedPostsCount = when (likedPostsResult) {
                    is ApiResult.Success -> {
                        val count = likedPostsResult.data.size
                        Log.d(TAG, "❤️ 좋아요한 글 수: $count")
                        count
                    }
                    is ApiResult.Error -> {
                        Log.w(TAG, "⚠️ 좋아요한 글 조회 실패 [${likedPostsResult.code}]: ${likedPostsResult.message}")
                        0
                    }
                    is ApiResult.Loading -> {
                        Log.d(TAG, "🔄 좋아요한 글 로딩 중...")
                        0
                    }
                },
                recentViewedCount = when (recentViewedResult) {
                    is ApiResult.Success -> {
                        val count = recentViewedResult.data.size
                        Log.d(TAG, "👀 최근 본 글 수: $count")
                        count
                    }
                    is ApiResult.Error -> {
                        Log.w(TAG, "⚠️ 최근 본 글 조회 실패 [${recentViewedResult.code}]: ${recentViewedResult.message}")
                        0
                    }
                    is ApiResult.Loading -> {
                        Log.d(TAG, "🔄 최근 본 글 로딩 중...")
                        0
                    }
                }
            ).also { stats ->
                Log.d(TAG, "📊 최종 통계: 내글=${stats.myPostsCount}, 좋아요=${stats.likedPostsCount}, 최근보기=${stats.recentViewedCount}")
            }
        }
    }
}

/**
 * 📊 프로필 게시글 통계 데이터 클래스
 * - 클라이언트에서 리스트 크기로 직접 계산
 * - 메모리 효율적: 간단한 Int 값들만 저장
 */
data class ProfilePostsCount(
    val myPostsCount: Int = 0,
    val likedPostsCount: Int = 0,
    val recentViewedCount: Int = 0
)

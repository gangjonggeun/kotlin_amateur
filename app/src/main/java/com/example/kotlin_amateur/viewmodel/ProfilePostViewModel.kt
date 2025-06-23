package com.example.kotlin_amateur.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kotlin_amateur.core.PostListType
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.repository.ProfilePostRepository
import com.example.kotlin_amateur.repository.ProfilePostsCount
import com.example.kotlin_amateur.post.PostPagingSource // ProfilePostPagingSource 대신 PostPagingSource 사용
import com.example.kotlin_amateur.state.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🎯 프로필 게시글 전용 ViewModel
 * - 내 게시글, 좋아요한 글, 최근 본 글 관리
 * - HomeViewModel과 분리하여 프로필 특화 기능 제공
 * - 메모리 최적화: Paging3 + Result 패턴 사용
 */
@HiltViewModel
class ProfilePostViewModel @Inject constructor(
    private val profilePostRepository: ProfilePostRepository,
    private val application: Application
) : ViewModel() {

    companion object {
        private const val TAG = "ProfilePostViewModel"
        private const val PAGE_SIZE = 20 // 프로필에서는 더 많이 로딩
    }

    // 🎯 현재 프로필 게시글 타입
    private val _postListType = MutableStateFlow(PostListType.MY_POSTS)
    val postListType: StateFlow<PostListType> = _postListType.asStateFlow()

    // 📊 프로필 게시글 통계
    private val _profileStats = MutableStateFlow<ApiResult<ProfilePostsCount>>(ApiResult.Loading())
    val profileStats: StateFlow<ApiResult<ProfilePostsCount>> = _profileStats.asStateFlow()

    // 🗑️ 최근 본 글 관리 상태
    private val _recentViewManageMode = MutableStateFlow(false)
    val recentViewManageMode: StateFlow<Boolean> = _recentViewManageMode.asStateFlow()

    // 🔄 무한 스크롤 Paging 데이터 (프로필 타입별)
    val profilePostsPagingFlow: Flow<PagingData<PostListResponse>> = 
        _postListType.flatMapLatest { postType ->
            Log.d(TAG, "🚀 Paging 생성: ${postType.displayName}")
            
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = 5,       // 5개 남았을 때 미리 로딩
                    enablePlaceholders = false, // 메모리 절약
                    initialLoadSize = PAGE_SIZE,
                    maxSize = PAGE_SIZE * 10    // 최대 200개까지 메모리에 유지
                ),
                pagingSourceFactory = {
                    // 🎯 기존 PostPagingSource 사용 (ProfilePostRepository 전달)
                    PostPagingSource(
                        context = application.applicationContext,
                        postRepository = null, // 프로필에서는 사용 안함
                        profilePostRepository = profilePostRepository, // 프로필 API 전달
                        query = null, // 프로필에서는 검색 안함
                        postListType = postType
                    )
                }
            ).flow.cachedIn(viewModelScope)
        }

//    init {
//        // 🚀 초기 통계 로딩
////        loadProfileStats()
//    }

    /**
     * 🎯 프로필 게시글 타입 변경
     * @param type MY_POSTS, LIKED_POSTS, RECENT_VIEWED 중 하나
     */
    fun setPostListType(type: PostListType) {
        if (type in listOf(PostListType.MY_POSTS, PostListType.LIKED_POSTS, PostListType.RECENT_VIEWED)) {
            if (_postListType.value != type) {
                _postListType.value = type
                _recentViewManageMode.value = false // 타입 변경 시 관리 모드 해제
                Log.d(TAG, "🎯 프로필 게시글 타입 변경: ${type.displayName}")
            }
        } else {
            Log.w(TAG, "⚠️ 지원하지 않는 프로필 게시글 타입: $type")
        }
    }

    /**
     * 📊 프로필 게시글 통계 조회
     * - 내 게시글 수, 좋아요한 글 수, 최근 본 글 수
     */
    fun loadProfileStats() {
        viewModelScope.launch {
            Log.d(TAG, "📊 프로필 통계 로딩 시작")
            _profileStats.value = ApiResult.Loading("통계 조회 중...")
            
            try {
                val result = profilePostRepository.getProfilePostsCount(application.applicationContext)
                _profileStats.value = result
                
                when (result) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "✅ 프로필 통계 로딩 성공: ${result.data}")
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "❌ 프로필 통계 로딩 실패 [${result.code}]: ${result.message}")
                    }
                    is ApiResult.Loading -> {
                        Log.d(TAG, "🔄 프로필 통계 로딩 중: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 프로필 통계 로딩 예외: ${e.message}", e)
                _profileStats.value = ApiResult.Error(500, "통계 조회 중 오류가 발생했습니다")
            }
        }
    }

    /**
     * 🗑️ 최근 본 글 개별 삭제
     * @param postId 삭제할 게시글 ID
     * @param onResult 결과 콜백 (success: Boolean, message: String)
     */
    fun removeRecentViewedPost(postId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "🗑️ 최근 본 글 개별 삭제: $postId")
            
            try {
                val result = profilePostRepository.removeRecentViewedPost(
                    application.applicationContext, 
                    postId
                )
                
                when (result) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "✅ 최근 본 글 삭제 성공")
                        onResult(true, "삭제되었습니다")
                        // 통계 새로고침
//                        loadProfileStats()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "❌ 최근 본 글 삭제 실패 [${result.code}]: ${result.message}")
                        onResult(false, result.message)
                    }
                    is ApiResult.Loading -> {
                        // Loading 상태는 UI에서 처리
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 최근 본 글 삭제 예외: ${e.message}", e)
                onResult(false, "삭제 중 오류가 발생했습니다")
            }
        }
    }

    /**
     * 🧹 최근 본 글 전체 삭제
     * @param onResult 결과 콜백
     */
    fun clearAllRecentViewedPosts(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "🧹 최근 본 글 전체 삭제")
            
            try {
                val result = profilePostRepository.clearAllRecentViewedPosts(
                    application.applicationContext
                )
                
                when (result) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "✅ 최근 본 글 전체 삭제 성공")
                        onResult(true, "모든 기록이 삭제되었습니다")
                        // 통계 새로고침
//                        loadProfileStats()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "❌ 최근 본 글 전체 삭제 실패 [${result.code}]: ${result.message}")
                        onResult(false, result.message)
                    }
                    is ApiResult.Loading -> {
                        // Loading 상태는 UI에서 처리
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 최근 본 글 전체 삭제 예외: ${e.message}", e)
                onResult(false, "삭제 중 오류가 발생했습니다")
            }
        }
    }

    /**
     * 🔧 최근 본 글 관리 모드 토글
     * - 관리 모드에서는 개별 삭제 가능
     */
    fun toggleRecentViewManageMode() {
        _recentViewManageMode.value = !_recentViewManageMode.value
        Log.d(TAG, "🔧 최근 본 글 관리 모드: ${_recentViewManageMode.value}")
    }

    /**
     * 🔄 새로고침
     * - Paging 데이터와 통계 모두 새로고침
     */
    fun refresh() {
        Log.d(TAG, "🔄 프로필 게시글 새로고침")
//        loadProfileStats()
        // Paging은 자동으로 새로고침됨 (swipe-to-refresh)
    }

    /**
     * 📊 특정 타입의 게시글 수 반환 (UI에서 빠른 접근용)
     */
    fun getPostCountByType(type: PostListType): Int {
        return when (val stats = _profileStats.value) {
            is ApiResult.Success -> {
                when (type) {
                    PostListType.MY_POSTS -> stats.data.myPostsCount
                    PostListType.LIKED_POSTS -> stats.data.likedPostsCount
                    PostListType.RECENT_VIEWED -> stats.data.recentViewedCount
                    else -> 0
                }
            }
            else -> 0
        }
    }



    // 🔥 메모리 정리 (ViewModel 소멸 시)
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ProfilePostViewModel 메모리 정리")
    }
}

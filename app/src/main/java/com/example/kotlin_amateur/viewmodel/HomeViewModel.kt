package com.example.kotlin_amateur.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.repository.PostRepository
import com.example.kotlin_amateur.post.PostPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val application: Application
) : ViewModel() {

    // 🔍 검색어 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 🔄 무한 스크롤 Paging 데이터
    val postsPagingFlow: Flow<PagingData<PostListResponse>> = 
        searchQuery.flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,              // 한 번에 20개씩 로딩
                    prefetchDistance = 5,       // 5개 남았을 때 미리 로딩
                    enablePlaceholders = false  // 플레이스홀더 비활성화 (메모리 절약)
                ),
                pagingSourceFactory = {
                    PostPagingSource(
                        context = application.applicationContext,
                        postRepository = postRepository,
                        query = query.ifEmpty { null }
                    )
                }
            ).flow.cachedIn(viewModelScope) // ✅ 메모리에 캐시 (화면 회전 등에서 유지)
        }

    // 🔍 검색어 업데이트
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    // 🔄 새로고침 (Paging3에서 자동 처리)
    fun refresh() {
        // Paging3에서 자동으로 처리됨 (swipe to refresh)
    }

    /**
     * 💖 게시글 좋아요 토글 (기존 기능 유지)
     * @param postId 게시글 ID
     * @param isLiked 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
     * @param callback 결과 콜백 (success: Boolean)
     */
    fun toggleLike(postId: String, isLiked: Boolean, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val accessToken = TokenStore.getAccessToken(application.applicationContext)
                    ?: throw Exception("로그인이 필요합니다")

                val response = if (isLiked) {
                    postRepository.likePost(accessToken, postId)
                } else {
                    postRepository.unlikePost(accessToken, postId)
                }

                if (response.isSuccessful) {
                    Log.d("HomeViewModel", "💖 좋아요 상태 변경 성공: $isLiked")
                    callback(true)
                } else {
                    Log.e("HomeViewModel", "❌ 좋아요 상태 변경 실패: ${response.code()}")
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ 좋아요 처리 중 오류: ${e.message}", e)
                callback(false)
            }
        }
    }

    // 🔥 기존 API를 사용하는 메서드들 (역호환성을 위해 유지)
    // 이제 Paging3를 사용하므로 직접 호출할 필요 없음
    @Deprecated("무한 스크롤로 대체됨. postsPagingFlow 사용 권장")
    fun loadDataFromServer() {
        // Paging3로 대체되었으므로 비우거나 제거 예정
        Log.d("HomeViewModel", "무한 스크롤로 대체되었습니다. postsPagingFlow를 사용하세요.")
    }

    // 🔄 기존 새로고침 메서드 (호환성 유지)
    fun refreshData() {
        refresh()
    }
}
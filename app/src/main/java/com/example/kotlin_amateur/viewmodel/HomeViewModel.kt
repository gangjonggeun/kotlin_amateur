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
import com.example.kotlin_amateur.model.SearchHistory
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.repository.PostRepository
import com.example.kotlin_amateur.repository.SearchHistoryRepository
import com.example.kotlin_amateur.post.PostPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val application: Application
) : ViewModel() {

    // 🔍 검색어 상태 (타이핑 중인 텍스트)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 🎯 실제 검색 실행용 상태 (지연 검색)
    private val _actualSearchQuery = MutableStateFlow("")
    
    // 🔍 최근 검색어 조회
    val recentSearches: Flow<List<SearchHistory>> = searchHistoryRepository.getRecentSearches()

    // 🛡️ 메모리 최적화된 지연 검색 (500ms 후 자동 검색)
    init {
        viewModelScope.launch {
            searchQuery
                .debounce(500) // 500ms 지연
                .distinctUntilChanged() // 같은 값 필터링
                .collect { query ->
                    if (query.length >= 2) { // 2글자 이상만 자동 검색
                        _actualSearchQuery.value = query
                        Log.d("HomeViewModel", "🔍 자동 검색 실행: '$query'")
                    } else if (query.isEmpty()) {
                        _actualSearchQuery.value = "" // 빈 문자열이면 전체 조회
                    }
                }
        }
    }

    // 🔄 무한 스크롤 Paging 데이터 (지연 검색 적용)
    val postsPagingFlow: Flow<PagingData<PostListResponse>> = 
        _actualSearchQuery.flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 5,               // 서버와 맞춰 5개씩 로딩
                    prefetchDistance = 2,       // 2개 남았을 때 미리 로딩
                    enablePlaceholders = false, // 플레이스홀더 비활성화 (메모리 절약)
                    initialLoadSize = 5         // 초기 로드 크기도 5개
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

    // 🔍 검색어 업데이트 (타이핑만, 히스토리 저장 안함)
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // ❌ 타이핑 중에는 히스토리 저장 안함 (메모리 절약)
    }
    
    // 🎯 명시적 검색 실행 (엔터키, 검색 버튼 클릭 시)
    fun performManualSearch() {
        val query = _searchQuery.value.trim()
        if (query.isNotEmpty()) {
            _actualSearchQuery.value = query
            
            // ✅ 수동 검색만 히스토리 저장 (의미있는 검색어만)
            viewModelScope.launch {
                searchHistoryRepository.saveSearch(query)
                Log.d("HomeViewModel", "🎯 수동 검색 실행 + 히스토리 저장: '$query'")
            }
        }
    }
    
    // 🔍 최근 검색어 클릭 시
    fun onRecentSearchClick(searchHistory: SearchHistory) {
        _searchQuery.value = searchHistory.query
        _actualSearchQuery.value = searchHistory.query
        
        // ✅ 선택한 검색어도 시간 업데이트
        viewModelScope.launch {
            searchHistoryRepository.saveSearch(searchHistory.query)
            Log.d("HomeViewModel", "🔍 최근 검색어 선택: '${searchHistory.query}'")
        }
    }
    
    // ❌ 검색 기록 삭제
    fun deleteSearchHistory(query: String) {
        viewModelScope.launch {
            searchHistoryRepository.deleteSearch(query)
            Log.d("HomeViewModel", "🗑️ 검색 기록 삭제: '$query'")
        }
    }
    
    // 🧹 모든 검색 기록 삭제
    fun clearAllSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearAllHistory()
            Log.d("HomeViewModel", "🧹 모든 검색 기록 삭제")
        }
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
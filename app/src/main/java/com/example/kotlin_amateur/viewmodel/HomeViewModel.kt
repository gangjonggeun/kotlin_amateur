package com.example.kotlin_amateur.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.exception.TokenNotFoundException
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val application: Application
) : ViewModel() {

    // 🔥 기존 LiveData
    private val _dataList = MutableLiveData<List<PostListResponse>>()
    val dataList: LiveData<List<PostListResponse>> get() = _dataList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // 🆕 StateFlow 추가 (Compose용)
    private val _posts = MutableStateFlow<List<PostListResponse>>(emptyList())
    val posts: StateFlow<List<PostListResponse>> = _posts.asStateFlow()

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow.asStateFlow()

    private val _errorMessageFlow = MutableStateFlow<String?>(null)
    val errorMessageFlow: StateFlow<String?> = _errorMessageFlow.asStateFlow()

    // 🆕 검색 기능
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 🆕 필터된 게시글
    val filteredPosts: StateFlow<List<PostListResponse>> = combine(
        _posts,
        _searchQuery
    ) { posts, query ->
        if (query.isBlank()) {
            posts
        } else {
            posts.filter { post ->
                post.postTitle.contains(query, ignoreCase = true) ||
                        post.postContent.contains(query, ignoreCase = true) ||
                        post.authorNickname.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadDataFromServer()
    }

    // 🆕 검색어 업데이트
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // 🆕 검색어 클리어
    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun loadDataFromServer() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLoadingFlow.value = true
            _errorMessage.value = null
            _errorMessageFlow.value = null

            try {
                val accessToken = TokenStore.getAccessToken(application.applicationContext)
                    ?: throw TokenNotFoundException()

                val response = postRepository.getPostsList(accessToken)

                if (response.isSuccessful) {
                    val postList = response.body() ?: emptyList()

                    // 🔥 이미지 URL 로깅 추가
                    Log.d("HomeViewModel", "데이터 로드 성공: ${postList.size}개 게시글")
                    postList.forEachIndexed { index, post ->
                        Log.d("HomeViewModel", "[게시글 $index] ID: ${post.postId}")
                        Log.d("HomeViewModel", "[게시글 $index] 제목: ${post.postTitle}")
                        Log.d("HomeViewModel", "[게시글 $index] 이미지 존재: ${post.hasImage}")
                        Log.d("HomeViewModel", "[게시글 $index] 이미지 URL 리스트: ${post.imageUrls}")
                        Log.d("HomeViewModel", "[게시글 $index] 대표 이미지: ${post.representativeImageUrl}")
                        Log.d("HomeViewModel", "[게시글 $index] 프로필 이미지: ${post.authorProfileImageUrl}")
                        Log.d("HomeViewModel", "---")
                    }

                    // 🔥 LiveData와 StateFlow 둘 다 업데이트
                    _dataList.value = postList
                    _posts.value = postList
                } else {
                    val errorMsg = "서버 응답 실패: ${response.code()}"
                    _errorMessage.value = errorMsg
                    _errorMessageFlow.value = errorMsg
                    Log.e("HomeViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "네트워크 오류 발생: ${e.message}"
                _errorMessage.value = errorMsg
                _errorMessageFlow.value = errorMsg
                Log.e("HomeViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
                _isLoadingFlow.value = false
            }
        }
    }

    /**
     * 게시글 좋아요 토글
     * @param postId 게시글 ID
     * @param isLiked 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
     * @param callback 결과 콜백 (success: Boolean)
     */
    fun toggleLike(postId: String, isLiked: Boolean, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val accessToken = TokenStore.getAccessToken(application.applicationContext)
                    ?: throw TokenNotFoundException()

                val response = if (isLiked) {
                    postRepository.likePost(accessToken, postId)
                } else {
                    postRepository.unlikePost(accessToken, postId)
                }

                if (response.isSuccessful) {
                    Log.d("HomeViewModel", "좋아요 상태 변경 성공: $isLiked")

                    // 🔥 로컬 상태 즉시 업데이트 (LiveData와 StateFlow 둘 다)
                    updatePostLikeStatus(postId, isLiked)

                    callback(true)
                } else {
                    Log.e("HomeViewModel", "좋아요 상태 변경 실패: ${response.code()}")
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "좋아요 처리 중 네트워크 오류", e)
                callback(false)
            }
        }
    }

    // 🔥 로컬 상태 업데이트 함수
    private fun updatePostLikeStatus(postId: String, isLiked: Boolean) {
        // LiveData 업데이트
        val currentList = _dataList.value?.toMutableList() ?: return
        val updatedList = currentList.map { post ->
            if (post.postId == postId) {
                post.copy(
                    isLikedByCurrentUser = isLiked,
                    likeCount = if (isLiked) post.likeCount + 1 else maxOf(0, post.likeCount - 1)
                )
            } else {
                post
            }
        }
        _dataList.value = updatedList

        // StateFlow 업데이트
        val currentPosts = _posts.value.toMutableList()
        val updatedPosts = currentPosts.map { post ->
            if (post.postId == postId) {
                post.copy(
                    isLikedByCurrentUser = isLiked,
                    likeCount = if (isLiked) post.likeCount + 1 else maxOf(0, post.likeCount - 1)
                )
            } else {
                post
            }
        }
        _posts.value = updatedPosts
    }

    fun refreshData() {
        loadDataFromServer()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
        _errorMessageFlow.value = null
    }
}
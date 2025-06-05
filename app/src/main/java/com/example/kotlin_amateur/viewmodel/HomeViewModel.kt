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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val application: Application
) : ViewModel() {

    private val _dataList = MutableLiveData<List<PostListResponse>>()
    val dataList: LiveData<List<PostListResponse>> get() = _dataList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        loadDataFromServer()
    }

    fun loadDataFromServer() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val accessToken = TokenStore.getAccessToken(application.applicationContext)
                    ?: throw TokenNotFoundException()

                val response = postRepository.getPostsList(accessToken)

                if (response.isSuccessful) {
                    _dataList.value = response.body() ?: emptyList()
                    Log.d("HomeViewModel", "데이터 로드 성공: ${response.body()?.size}개 게시글")
                } else {
                    val errorMsg = "서버 응답 실패: ${response.code()}"
                    _errorMessage.value = errorMsg
                    Log.e("HomeViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "네트워크 오류 발생: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("HomeViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
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
                    callback(true)

                    // 선택적: 전체 리스트 새로고침으로 최신 데이터 동기화
                    // loadDataFromServer()
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
    fun refreshData() {
        loadDataFromServer()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }


}
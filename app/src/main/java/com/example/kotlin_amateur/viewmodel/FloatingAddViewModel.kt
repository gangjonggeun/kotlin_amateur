package com.example.kotlin_amateur.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.core.util.toMultipart
import com.example.kotlin_amateur.remote.request.PostRequest
import com.example.kotlin_amateur.repository.FloatingAddRepository
import com.example.kotlin_amateur.state.SubmitResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FloatingAddViewModel @Inject constructor(
    private val repository: FloatingAddRepository,
    private val application: Application
) : ViewModel() {

    // 🔥 Compose State 최적화 - mutableStateOf 사용
    var title by mutableStateOf("")
        private set
        
    var content by mutableStateOf("")
        private set
        
    var selectedImages by mutableStateOf<List<Uri>>(emptyList())
        private set
        
    var selectedCommunity by mutableStateOf("")
        private set
    
    // 🔥 StateFlow로 UI 상태 관리 (메모리 효율적)
    private val _submitResult = MutableStateFlow<SubmitResult>(SubmitResult.Idle)
    val submitResult: StateFlow<SubmitResult> = _submitResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 🎯 UI 이벤트 처리 함수들
    fun updateTitle(newTitle: String) {
        title = newTitle
    }
    
    fun updateContent(newContent: String) {
        content = newContent
    }
    
    fun updateSelectedImages(newImages: List<Uri>) {
        selectedImages = newImages
    }
    
    fun updateSelectedCommunity(community: String) {
        selectedCommunity = community
    }
    
    fun addImages(newImages: List<Uri>) {
        val remainingSlots = 6 - selectedImages.size
        if (remainingSlots > 0) {
            selectedImages = selectedImages + newImages.take(remainingSlots)
        }
    }
    
    fun removeImage(index: Int) {
        if (index in selectedImages.indices) {
            selectedImages = selectedImages.toMutableList().apply { removeAt(index) }
        }
    }
    
    fun reorderImages(fromIndex: Int, toIndex: Int) {
        if (fromIndex in selectedImages.indices && toIndex in selectedImages.indices) {
            selectedImages = selectedImages.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        }
    }
    
    // 🚀 게시글 제출 (메모리 최적화 적용)
    fun submitPost() {
        // 🔥 입력 검증 (Result 패턴 사용, Exception 방지)
        val validationResult = validateInput()
        if (validationResult != null) {
            _submitResult.value = SubmitResult.Failure(Exception(validationResult))
            return
        }
        
        viewModelScope.launch {
            _submitResult.value = SubmitResult.Loading
            _isLoading.value = true
            
            try {
                // 1. URI → Multipart 변환 (메모리 효율적)
                val parts = selectedImages.mapNotNull { uri ->
                    uri.toMultipart(application.applicationContext)
                }
                
                val accessToken = TokenStore.getAccessToken(application.applicationContext)
                if (accessToken == null) {
                    _submitResult.value = SubmitResult.Failure(Exception("로그인이 필요합니다"))
                    return@launch
                }
                
                Log.d("FloatingAddViewModel", "Access Token: $accessToken")
                
                // 2. 이미지 업로드
                val imageResponse = repository.uploadImages(accessToken, parts)
                val imageUrls = if (imageResponse.success) {
                    imageResponse.imageUrls
                } else {
                    throw Exception(imageResponse.message ?: "이미지 업로드 실패")
                }
                
                // 3. 게시글 데이터 생성
                val postRequest = PostRequest(
                    postTitle = title.trim(),
                    postContent = content.trim(),
                    postImageUrls = imageUrls
                )
                
                // 4. 게시글 업로드
                val response = repository.uploadPost(accessToken, postRequest)
                
                if (response.isSuccessful) {
                    Log.d("FloatingAddViewModel", "게시글 업로드 성공!")
                    _submitResult.value = SubmitResult.Success
                    clearForm() // 🔥 성공 시 폼 초기화
                } else {
                    Log.e("FloatingAddViewModel", "업로드 실패: ${response.code()}")
                    _submitResult.value = SubmitResult.Failure(
                        Exception("업로드 실패: ${response.code()}")
                    )
                }
                
            } catch (e: Exception) {
                Log.e("FloatingAddViewModel", "에러 발생: ${e.message}", e)
                _submitResult.value = SubmitResult.Failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 🔍 입력 검증 (메모리 효율적 - Exception 대신 String 반환)
    private fun validateInput(): String? {
        return when {
            title.isBlank() -> "제목을 입력해주세요"
            content.isBlank() -> "내용을 입력해주세요"
            selectedImages.isEmpty() -> "사진을 한 장 이상 선택해주세요"
            selectedCommunity.isBlank() -> "커뮤니티를 선택해주세요"
            else -> null
        }
    }
    
    // 🧹 폼 초기화 (메모리 정리)
    private fun clearForm() {
        title = ""
        content = ""
        selectedImages = emptyList()
        selectedCommunity = ""
    }
    
    // 🔄 결과 상태 초기화
    fun clearSubmitResult() {
        _submitResult.value = SubmitResult.Idle
    }
    
    // 🧹 ViewModel 정리 시 리소스 해제
    override fun onCleared() {
        super.onCleared()
        clearForm()
        Log.d("FloatingAddViewModel", "ViewModel cleared - 메모리 정리 완료")
    }
}
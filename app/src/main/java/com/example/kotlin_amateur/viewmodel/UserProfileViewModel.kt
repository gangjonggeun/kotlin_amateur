package com.example.kotlin_amateur.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.model.PostSummary
import com.example.kotlin_amateur.model.UserProfile
import com.example.kotlin_amateur.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UserProfileViewModel - 사용자 프로필 다이얼로그용 ViewModel
 *
 * 🎯 현재 구현 기능:
 * - 사용자 프로필 정보 로드
 * - 사용자가 작성한 게시글 목록 로드
 * - 에러 처리 및 로딩 상태 관리
 *
 * 🚫 나중에 추가할 기능들:
 * - 팔로우/언팔로우 기능
 * - 팔로우 상태 관리
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    companion object {
        private const val TAG = "🔥UserProfileViewModel"
    }

    // 사용자 프로필 정보
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    // 사용자의 게시글 목록
    private val _userPosts = MutableStateFlow<List<PostSummary>>(emptyList())
    val userPosts = _userPosts.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 에러 상태
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    /**
     * 사용자 프로필 정보 로드
     * @param userId 조회할 사용자 ID
     */
    fun loadUserProfile(userId: String) {
        Log.d(TAG, "📥 사용자 프로필 로드 시작 - userId: $userId")

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 프로필 정보와 게시글을 병렬로 로드
                val profileResult = repository.getUserProfile(userId)
                val postsResult = repository.getUserPosts(userId, limit = 5) // 최근 5개만

                profileResult
                    .onSuccess { profile ->
                        Log.d(TAG, "✅ 프로필 로드 성공 - nickname: ${profile.nickname}")
                        _userProfile.value = profile
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "❌ 프로필 로드 실패: ${exception.message}")
                        _error.value = "프로필을 불러올 수 없습니다: ${exception.message}"
                    }

                postsResult
                    .onSuccess { posts ->
                        Log.d(TAG, "✅ 게시글 로드 성공 - 게시글 수: ${posts.size}")
                        _userPosts.value = posts
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "❌ 게시글 로드 실패: ${exception.message}")
                        // 게시글 로드 실패는 치명적이지 않으므로 에러 상태 설정 안함
                        _userPosts.value = emptyList()
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 사용자 프로필 로드 중 예외 발생", e)
                _error.value = "프로필 로드 중 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 사용자의 더 많은 게시글 로드
     * @param userId 사용자 ID
     */
    fun loadMorePosts(userId: String) {
        Log.d(TAG, "📥 더 많은 게시글 로드 - userId: $userId")

        viewModelScope.launch {
            try {
                val result = repository.getUserPosts(
                    userId = userId,
                    offset = _userPosts.value.size,
                    limit = 10
                )

                result
                    .onSuccess { newPosts ->
                        Log.d(TAG, "✅ 추가 게시글 로드 성공 - 새 게시글 수: ${newPosts.size}")
                        _userPosts.value = _userPosts.value + newPosts
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "❌ 추가 게시글 로드 실패: ${exception.message}")
                        _error.value = "게시글을 더 불러올 수 없습니다"
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 추가 게시글 로드 중 예외 발생", e)
                _error.value = "게시글 로드 중 오류가 발생했습니다"
            }
        }
    }

    /**
     * 에러 상태 클리어
     */
    fun clearError() {
        Log.d(TAG, "🧹 에러 상태 클리어")
        _error.value = null
    }

    /**
     * 프로필 새로고침
     * @param userId 사용자 ID
     */
    fun refreshProfile(userId: String) {
        Log.d(TAG, "🔄 프로필 새로고침 - userId: $userId")

        // 기존 데이터 클리어 후 다시 로드
        _userProfile.value = null
        _userPosts.value = emptyList()

        loadUserProfile(userId)
    }

    /**
     * ViewModel 클리어 (다이얼로그 닫힐 때)
     */
    fun clearData() {
        Log.d(TAG, "🧹 ViewModel 데이터 클리어")

        _userProfile.value = null
        _userPosts.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}
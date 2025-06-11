package com.example.kotlin_amateur.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.exception.ImageSaveFailedException
import com.example.kotlin_amateur.exception.TokenNotFoundException
import com.example.kotlin_amateur.exception.ProfileException
import com.example.kotlin_amateur.exception.ProfileLoadFailedException
import com.example.kotlin_amateur.model.UserInfo
import com.example.kotlin_amateur.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModel() {

    // ✅ StateFlow 최적화 - 초기값을 명시적으로 설정
    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ✅ 메모리 누수 방지를 위한 Job 관리
    private var profileJob: Job? = null

    fun setupProfile(nickname: String, imagePart: MultipartBody.Part?) {
        // 이전 작업 취소 (메모리 절약)
        profileJob?.cancel()

        profileJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val accessToken = TokenStore.getAccessToken(application.applicationContext)
                    ?: throw TokenNotFoundException()

                val profileResponse = userRepository.setupProfile(accessToken, nickname, imagePart)

                // ✅ 메모리 효율적인 객체 생성
                val updatedInfo = UserInfo(
                    nickname = profileResponse.nickname,
                    profileImageUrl = profileResponse.profileImageUrl
                )

                _userInfo.value = updatedInfo
                _updateSuccess.value = true
                _errorMessage.value = null

            } catch (e: ProfileException) {
                handleProfileException(e)
            } catch (e: Exception) {
                handleGenericException(e, "프로필 설정")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMyProfile(context: Context) {
        // 이전 작업 취소 (메모리 절약)
        profileJob?.cancel()

        profileJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val accessToken = TokenStore.getAccessToken(context)
                    ?: throw TokenNotFoundException()

                val profile = userRepository.getMyProfile(accessToken)

                // ✅ 메모리 효율적인 객체 생성 - 불필요한 객체 생성 최소화
                _userInfo.value = UserInfo(
                    nickname = profile.nickname,
                    profileImageUrl = profile.profileImageUrl
                )

                _errorMessage.value = null
                Log.d("ProfileViewModel", "✅ 프로필 로드 성공: ${profile.nickname}")

            } catch (e: ProfileException) {
                handleProfileException(e)
            } catch (e: Exception) {
                handleGenericException(e, "프로필 조회")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ 에러 처리 함수 분리 - 코드 중복 제거 및 메모리 효율성
    private fun handleProfileException(e: ProfileException) {
        val msg = when (e) {
            is TokenNotFoundException -> {
                Log.w("ProfileViewModel", "토큰 없음", e)
                "로그인이 필요합니다."
            }
            is ImageSaveFailedException -> {
                Log.e("ProfileViewModel", "이미지 저장 실패", e)
                "이미지 저장에 실패했습니다."
            }
            is ProfileLoadFailedException -> {
                Log.e("ProfileViewModel", "프로필 로드 실패", e)
                "프로필 불러오기에 실패했습니다."
            }
        }
        _errorMessage.value = msg
        _updateSuccess.value = false
    }

    private fun handleGenericException(e: Exception, operation: String) {
        Log.e("ProfileViewModel", "$operation 중 예기치 못한 오류", e)
        _errorMessage.value = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        _updateSuccess.value = false
    }

    // ✅ 메모리 정리 함수들
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    // ✅ 메모리 정리 - 프로필 정보 클리어
    fun clearUserInfo() {
        _userInfo.value = null
    }

    // ✅ ViewModel 종료 시 리소스 정리
    override fun onCleared() {
        super.onCleared()
        profileJob?.cancel()
        Log.d("ProfileViewModel", "✅ ViewModel 리소스 정리 완료")
    }
}
package com.example.kotlin_amateur.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModel() {

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun setupProfile(nickname: String, imagePart: MultipartBody.Part?) {
        viewModelScope.launch {
            try {
                val accessToken = TokenStore.getAccessToken(application.applicationContext)
                    ?: throw TokenNotFoundException()

                val profileResponse = userRepository.setupProfile(accessToken, nickname, imagePart)

                val updatedInfo = UserInfo(
                    nickname = profileResponse.nickname,
                    profileImageUrl = profileResponse.profileImageUrl
                )

                _userInfo.value = updatedInfo
                _updateSuccess.value = true
                _errorMessage.value = null

            } catch (e: ProfileException) {
                val msg = when (e) {
                    is TokenNotFoundException -> {
                        Log.e("🔥 ProfileViewModel", "❌ TokenNotFound", e)
                        "로그인이 필요합니다."
                    }
                    is ImageSaveFailedException -> {
                        Log.e("🔥 ProfileViewModel", "❌ ImageSaveFailed", e)
                        "이미지 저장에 실패했습니다."
                    }
                    is ProfileLoadFailedException -> {
                        Log.e("🔥 ProfileViewModel", "❌ 프로필 로드 실패", e)
                        "프로필 정보 업데이트에 실패했습니다."
                    }
                }
                _errorMessage.value = msg
                _updateSuccess.value = false

            } catch (e: Exception) {
                Log.e("🔥 ProfileViewModel", "❌ 예기치 못한 오류", e)
                _errorMessage.value = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                _updateSuccess.value = false
            }
        }
    }

    fun fetchMyProfile(context: Context) {
        viewModelScope.launch {
            try {
                val accessToken = TokenStore.getAccessToken(context)
                    ?: throw TokenNotFoundException()

                val profile = userRepository.getMyProfile(accessToken)

                val info = UserInfo(
                    nickname = profile.nickname,
                    profileImageUrl = profile.profileImageUrl
                )

                Log.e("🔥 ProfileViewModel", "profile info ${profile.nickname}, ${ profile.profileImageUrl}")

                _userInfo.postValue(info)
                _errorMessage.value = null

            } catch (e: ProfileException) {
                val msg = when (e) {
                    is TokenNotFoundException -> {
                        Log.e("🔥 ProfileViewModel", "❌ TokenNotFound", e)
                        "로그인이 필요합니다."
                    }
                    is ProfileLoadFailedException -> {
                        Log.e("🔥 ProfileViewModel", "❌ 프로필 로드 실패", e)
                        "프로필 불러오기에 실패했습니다."
                    }
                    else -> {
                        Log.e("🔥 ProfileViewModel", "❌ 기타 ProfileException", e)
                        "프로필 관련 오류가 발생했습니다."
                    }
                }
                _errorMessage.value = msg

            } catch (e: Exception) {
                Log.e("🔥 ProfileViewModel", "❌ 예기치 못한 오류", e)
                _errorMessage.value = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            }
        }
    }
}
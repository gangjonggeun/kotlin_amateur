package com.example.kotlin_amateur.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.core.auth.TokenStore
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

    fun setupProfile(nickname: String, imagePart: MultipartBody.Part?) {
        viewModelScope.launch {
            try {
                val accessToken = TokenStore.getAccessToken(application.applicationContext)
                if (accessToken == null) {
                    _updateSuccess.value = false
                    return@launch
                }

                Log.d("Acces Token: ProfileViewmodel","$accessToken")

                userRepository.setupProfile(accessToken, nickname, imagePart)

                _updateSuccess.value = true
            } catch (e: Exception) {
                Log.e("🔥 ProfileViewModel", "업데이트 실패", e)
                _updateSuccess.value = false
            }
        }
    }
}
package com.example.kotlin_amateur.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.remote.request.SetupProfileRequest
import com.example.kotlin_amateur.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    fun setupProfile(nickname: String, profileImageUrl: String, accessToken: String) {
        viewModelScope.launch {
            try {
                val request = SetupProfileRequest(nickname, profileImageUrl)
                userRepository.setupProfile(accessToken, request)
                _updateSuccess.value = true
            } catch (e: Exception) {
                _updateSuccess.value = false
            }
        }
    }
}
package com.example.kotlin_amateur.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.remote.request.LoginRequest
import com.example.kotlin_amateur.remote.api.BackendApiService
import com.example.kotlin_amateur.state.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: BackendApiService
) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> get() = _loginResult

    fun loginWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.loginWithGoogle(LoginRequest(idToken))
                val body = response.body()

                val result = when {

                    (!response.isSuccessful || body == null) -> {
                        LoginResult.Failure(Exception("응답 실패 또는 body 없음"))
                    }

                    body.isNewUser -> {
                        val email = body.email ?: ""
                        val sub = body.googleSub ?: ""
                        val name = body.name ?: ""
                        val accessToken = body.accessToken
                        LoginResult.NeedNickname(email, sub, name, accessToken)
                    }

                    else -> {
                        LoginResult.Success(body.accessToken)
                    }
                }

                _loginResult.postValue(result)

            } catch (e: Exception) {

                _loginResult.postValue(LoginResult.Failure(e))
            }
        }
    }
}


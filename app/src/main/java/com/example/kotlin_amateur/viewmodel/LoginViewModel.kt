package com.example.kotlin_amateur.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.remote.request.IdTokenRequest
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
                val response = apiService.loginWithGoogle(IdTokenRequest(idToken))
                val body = response.body()

                val result = when {
                    !response.isSuccessful || body == null -> {
                        LoginResult.Failure(Exception("로그인 실패 또는 응답 없음"))
                    }
                    body.nickname.isNullOrBlank() -> {
                        LoginResult.NeedNickname(
                            email = body.email.orEmpty(),
                            googleSub = body.googleSub.orEmpty(),
                            name = body.name.orEmpty(),
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                    else -> {
                        LoginResult.Success(
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                }

                _loginResult.postValue(result)

            } catch (e: Exception) {
                Log.e("Login", "예외 발생", e)
                _loginResult.postValue(LoginResult.Failure(e))
            }
        }
    }

    fun registerWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.registerWithGoogle(IdTokenRequest(idToken))
                val body = response.body()

                val result = when {
                    !response.isSuccessful || body == null -> {
                        LoginResult.Failure(Exception("회원가입 실패 또는 응답 없음"))
                    }
                    body.nickname.isNullOrBlank() -> {
                        LoginResult.NeedNickname(
                            email = body.email.orEmpty(),
                            googleSub = body.googleSub.orEmpty(),
                            name = body.name.orEmpty(),
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                    else -> {
                        LoginResult.Success(
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                }

                _loginResult.postValue(result)

            } catch (e: Exception) {
                Log.e("Register", "예외 발생", e)
                _loginResult.postValue(LoginResult.Failure(e))
            }
        }
    }
}

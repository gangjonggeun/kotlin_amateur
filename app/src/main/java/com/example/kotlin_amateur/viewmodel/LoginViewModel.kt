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

    fun loginWithGoogleToken(idToken: String, isTestAccount: Boolean, isLogin: Boolean) {
        viewModelScope.launch {
            try {
                if (isTestAccount && isLogin) {
                    handleTestLogin(idToken)
                } else {
                    handleLoginOrRegister(idToken)
                }
            } catch (e: Exception) {

                Log.e("Login","${e.printStackTrace()}")
                _loginResult.postValue(LoginResult.Failure(e))
            }
        }
    }

    private suspend fun handleLoginOrRegister(idToken: String) {
        val response = apiService.loginOrRegisterWithGoogle(IdTokenRequest(idToken))
        val body = response.body()

        val result = when {
            !response.isSuccessful || body == null -> {
                LoginResult.Failure(Exception("응답 실패 또는 body 없음"))
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

            else -> LoginResult.Success(body.accessToken, refreshToken = body.refreshToken)
        }

        _loginResult.postValue(result)
    }

    private suspend fun handleTestLogin(idToken: String) {
        val response = apiService.getTestUserList(IdTokenRequest(idToken))
        val body = response.body()

        if (response.isSuccessful && body != null) {
            _loginResult.postValue(LoginResult.SelectUser(body)) // ✅ 그대로 넘기기
        } else {
            _loginResult.postValue(LoginResult.Failure(Exception("테스트 계정 불러오기 실패")))
        }
    }
}

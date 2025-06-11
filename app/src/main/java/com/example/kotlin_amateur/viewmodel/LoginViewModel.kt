package com.example.kotlin_amateur.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.remote.request.IdTokenRequest
import com.example.kotlin_amateur.remote.api.BackendApiService
import com.example.kotlin_amateur.state.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: BackendApiService
) : ViewModel() {

    // 🔥 LiveData 대신 StateFlow 사용 (메모리 효율적)
    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 🔥 진행 중인 작업 추적 (취소 가능)
    private var loginJob: Job? = null
    private var registerJob: Job? = null

    fun loginWithGoogleToken(idToken: String) {
        // 🔥 중복 요청 방지
        if (_isLoading.value) {
            Log.w("LoginViewModel", "이미 로그인 진행 중입니다")
            return
        }

        // 🔥 이전 작업 취소
        loginJob?.cancel()
        
        loginJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _loginResult.value = null // 이전 결과 초기화
                
                Log.d("LoginViewModel", "🔥 Google 로그인 시작")
                logMemoryUsage("loginStart")

                val response = apiService.loginWithGoogle(IdTokenRequest(idToken))
                val body = response.body()

                Log.d("LoginViewModel", "✅ API 응답 받음: ${response.isSuccessful}")
                Log.d("LoginViewModel", "응답 코드: ${response.code()}")

                val result = when {
                    !response.isSuccessful || body == null -> {
                        Log.e("LoginViewModel", "❌ 로그인 실패 - 응답 없음")
                        LoginResult.Failure(Exception("로그인 실패 또는 응답 없음 (${response.code()})"))
                    }
                    body.nickname.isNullOrBlank() -> {
                        Log.d("LoginViewModel", "🔥 닉네임 설정 필요")
                        LoginResult.NeedNickname(
                            email = body.email.orEmpty(),
                            googleSub = body.googleSub.orEmpty(),
                            name = body.name.orEmpty(),
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                    else -> {
                        Log.d("LoginViewModel", "✅ 로그인 성공")
                        LoginResult.Success(
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                }

                _loginResult.value = result
                logMemoryUsage("loginEnd")

            } catch (e: CancellationException) {
                Log.d("LoginViewModel", "로그인 취소됨")
                // 취소는 UI에 알리지 않음
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ 로그인 예외 발생", e)
                _loginResult.value = LoginResult.Failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerWithGoogleToken(idToken: String) {
        // 🔥 중복 요청 방지
        if (_isLoading.value) {
            Log.w("LoginViewModel", "이미 회원가입 진행 중입니다")
            return
        }

        // 🔥 이전 작업 취소
        registerJob?.cancel()
        
        registerJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _loginResult.value = null // 이전 결과 초기화
                
                Log.d("LoginViewModel", "🔥 Google 회원가입 시작")
                logMemoryUsage("registerStart")

                val response = apiService.registerWithGoogle(IdTokenRequest(idToken))
                val body = response.body()

                Log.d("LoginViewModel", "✅ API 응답 받음: ${response.isSuccessful}")
                Log.d("LoginViewModel", "응답 코드: ${response.code()}")

                val result = when {
                    !response.isSuccessful || body == null -> {
                        Log.e("LoginViewModel", "❌ 회원가입 실패 - 응답 없음")
                        LoginResult.Failure(Exception("회원가입 실패 또는 응답 없음 (${response.code()})"))
                    }
                    body.nickname.isNullOrBlank() -> {
                        Log.d("LoginViewModel", "🔥 닉네임 설정 필요")
                        LoginResult.NeedNickname(
                            email = body.email.orEmpty(),
                            googleSub = body.googleSub.orEmpty(),
                            name = body.name.orEmpty(),
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                    else -> {
                        Log.d("LoginViewModel", "✅ 회원가입 성공")
                        LoginResult.Success(
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                }

                _loginResult.value = result
                logMemoryUsage("registerEnd")

            } catch (e: CancellationException) {
                Log.d("LoginViewModel", "회원가입 취소됨")
                // 취소는 UI에 알리지 않음
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ 회원가입 예외 발생", e)
                _loginResult.value = LoginResult.Failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🔥 진행 중인 요청 취소 (메모리 누수 방지)
     */
    fun cancelOngoingRequests() {
        loginJob?.cancel()
        registerJob?.cancel()
        
        Log.d("LoginViewModel", "🧹 진행 중인 요청 취소됨")
    }

    /**
     * 🔥 ViewModel 정리 (메모리 누수 방지)
     */
    fun cleanup() {
        cancelOngoingRequests()
        
        // StateFlow 초기화
        _loginResult.value = null
        _isLoading.value = false
        
        loginJob = null
        registerJob = null
        
        Log.d("LoginViewModel", "🧹 ViewModel 정리 완료")
        logMemoryUsage("cleanup")
    }

    /**
     * 🔥 메모리 사용량 로깅
     */
    private fun logMemoryUsage(tag: String) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
            
            Log.d("MemoryUsage", "🔍 [ViewModel-$tag] 메모리: ${usedMemInMB}MB/${maxHeapSizeInMB}MB")
        } catch (e: Exception) {
            Log.e("MemoryUsage", "메모리 측정 실패", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("LoginViewModel", "🧹 ViewModel onCleared 호출")
        cleanup()
    }
}

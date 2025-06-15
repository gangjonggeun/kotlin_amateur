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
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: BackendApiService
) : ViewModel() {

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var loginJob: Job? = null
    private var registerJob: Job? = null
    private var cleanupJob: Job? = null

    fun loginWithGoogleToken(idToken: String) {
        if (_isLoading.value) {
            Log.w("LoginViewModel", "로그인 이미 진행 중")
            return
        }

        cancelAllOperations()
        
        loginJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _loginResult.value = null
                
                // 🔥 안전한 메모리 로깅 (GC 호출 제거)
                logMemoryUsage("login_start")

                Log.d("LoginViewModel", "🔥 Google 로그인 시작")

                val response = apiService.loginWithGoogle(IdTokenRequest(idToken))
                val body = response.body()

                Log.d("LoginViewModel", "✅ API 응답: ${response.isSuccessful}, 코드: ${response.code()}")

                val result = when {
                    !response.isSuccessful || body == null -> {
                        Log.e("LoginViewModel", "❌ 로그인 실패 - 응답 없음")
                        LoginResult.Failure(createLightweightException("로그인 실패 (${response.code()})"))
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
                scheduleMemoryCleanup()

            } catch (e: CancellationException) {
                Log.d("LoginViewModel", "로그인 취소됨")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ 로그인 예외: ${e.message}")
                
                val lightweightException = createLightweightException(e.message ?: "알 수 없는 오류")
                _loginResult.value = LoginResult.Failure(lightweightException)
                
                // 🔥 안전한 메모리 로깅 (GC 호출 제거)
                logMemoryUsage("login_error")
                
            } finally {
                _isLoading.value = false
                loginJob = null
            }
        }
    }

    fun registerWithGoogleToken(idToken: String) {
        if (_isLoading.value) {
            Log.w("LoginViewModel", "회원가입 이미 진행 중")
            return
        }

        cancelAllOperations()
        
        registerJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _loginResult.value = null
                
                logMemoryUsage("register_start")

                Log.d("LoginViewModel", "🔥 Google 회원가입 시작")

                val response = apiService.registerWithGoogle(IdTokenRequest(idToken))
                val body = response.body()

                Log.d("LoginViewModel", "✅ API 응답: ${response.isSuccessful}, 코드: ${response.code()}")

                val result = when {
                    !response.isSuccessful || body == null -> {
                        Log.e("LoginViewModel", "❌ 회원가입 실패 - 응답 없음")
                        LoginResult.Failure(createLightweightException("회원가입 실패 (${response.code()})"))
                    }
                    body.nickname.isNullOrBlank() -> {

                        Log.d("LoginViewModel", "🔍 닉네임 필요함 체크 상세:")
                        Log.d("LoginViewModel", "   - nickname 원본: '${body.nickname}'")
                        Log.d("LoginViewModel", "   - nickname == null: ${body.nickname == null}")
                        Log.d("LoginViewModel", "   - nickname.isEmpty(): ${body.nickname?.isEmpty()}")
                        Log.d("LoginViewModel", "   - nickname.isBlank(): ${body.nickname?.isBlank()}")
                        Log.d("LoginViewModel", "   - isNullOrBlank() 결과: ${body.nickname.isNullOrBlank()}")

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
                        Log.d("LoginViewModel", "🔍 회원가입 체크 상세:")
                        Log.d("LoginViewModel", "   - nickname 원본: '${body.nickname}'")
                        Log.d("LoginViewModel", "   - nickname == null: ${body.nickname == null}")
                        Log.d("LoginViewModel", "   - nickname.isEmpty(): ${body.nickname?.isEmpty()}")
                        Log.d("LoginViewModel", "   - nickname.isBlank(): ${body.nickname?.isBlank()}")
                        Log.d("LoginViewModel", "   - isNullOrBlank() 결과: ${body.nickname.isNullOrBlank()}")

                        Log.d("LoginViewModel", "✅ 회원가입 성공")
                        LoginResult.Success(
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken
                        )
                    }
                }

                _loginResult.value = result
                scheduleMemoryCleanup()

            } catch (e: CancellationException) {
                Log.d("LoginViewModel", "회원가입 취소됨")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ 회원가입 예외: ${e.message}")
                
                val lightweightException = createLightweightException(e.message ?: "알 수 없는 오류")
                _loginResult.value = LoginResult.Failure(lightweightException)
                
                logMemoryUsage("register_error")
                
            } finally {
                _isLoading.value = false
                registerJob = null
            }
        }
    }

    /**
     * 🔥 Create lightweight exception without heavy stack trace
     */
    private fun createLightweightException(message: String): Exception {
        return Exception(message).apply {
            stackTrace = emptyArray()
        }
    }

    /**
     * 🔥 안전한 메모리 로깅 (GC 호출 완전 제거)
     */
    private fun logMemoryUsage(tag: String) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            val maxMemInMB = runtime.maxMemory() / 1048576L
            val usagePercent = (usedMemInMB * 100 / maxMemInMB)
            
            Log.d("MemoryUsage", "📊 [$tag] 메모리: ${usedMemInMB}MB/${maxMemInMB}MB (${usagePercent}%)")
            
            // 🔥 경고만 출력, GC는 절대 호출하지 않음
            if (usedMemInMB > 200) {
                Log.w("MemoryUsage", "⚠️ 메모리 사용량 높음: ${usedMemInMB}MB - 자연스러운 정리 대기")
            }
            
        } catch (e: Exception) {
            Log.e("MemoryUsage", "메모리 측정 실패", e)
        }
    }

    /**
     * 🔥 안전한 자동 정리 (GC 호출 제거)
     */
    private fun scheduleMemoryCleanup() {
        cleanupJob?.cancel()
        cleanupJob = viewModelScope.launch {
            delay(5000) // 5초 후 정리
            
            // StateFlow만 정리 (안전함)
            _loginResult.value = null
            
            Log.d("LoginViewModel", "🧹 스케줄된 정리 완료")
        }
    }

    /**
     * 🔥 안전한 요청 취소
     */
    fun cancelOngoingRequests() {
        cancelAllOperations()
        Log.d("LoginViewModel", "🧹 진행 중인 요청 취소")
    }

    /**
     * 🔥 Job 취소
     */
    private fun cancelAllOperations() {
        loginJob?.cancel()
        registerJob?.cancel()
        cleanupJob?.cancel()
        
        loginJob = null
        registerJob = null
        cleanupJob = null
        
        Log.d("LoginViewModel", "🧹 모든 작업 취소됨")
    }

    /**
     * 🔥 안전한 ViewModel 정리
     */
    fun cleanup() {
        cancelAllOperations()
        
        // StateFlow만 정리 (안전함)
        _loginResult.value = null
        _isLoading.value = false
        
        Log.d("LoginViewModel", "🧹 ViewModel 정리 완료")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("LoginViewModel", "🧹 ViewModel onCleared 호출")
        cleanup()
    }
}

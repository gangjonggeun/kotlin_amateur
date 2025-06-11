package com.example.kotlin_amateur.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kotlin_amateur.MainActivity
// import com.example.kotlin_amateur.R // 🔥 임시 주석
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.databinding.ActivityLoginBinding
// import com.example.kotlin_amateur.remote.api.ApiConstants // 🔥 임시 주석
import com.example.kotlin_amateur.state.LoginResult
import com.example.kotlin_amateur.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() { // 🔥 임시로 인터페이스 제거

    // 🔥 메모리 효율적인 바인딩 (nullable로 누수 방지)
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    // 🔥 GoogleSignInClient를 lazy로 초기화 (메모리 효율적)
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("357022877460-i42fkd06mv9aq6kn3ub7ma34250dt6ur.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    private val viewModel: LoginViewModel by viewModels()

    // 🔥 로그인/회원가입 구분 (메모리에 저장)
    private var isLogin = true

    // 🔥 ActivityResultLauncher - 메모리 안전한 방식
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    // 🔥 네트워크 테스트 Job 추적 (취소 가능)
    private var networkTestJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeLoginResult()
        
        // 🔥 메모리 사용량 로깅
        logMemoryUsage("onCreate")
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            isLogin = true
            startGoogleLogin()
        }

        binding.googleSignUpButton.setOnClickListener {
            isLogin = false
            startGoogleLogin()
        }
    }

    private fun startGoogleLogin() {
        try {
            // 🔥 메모리 사용량 체크
            logMemoryUsage("startGoogleLogin")
            
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Log.e("LoginActivity", "Google 로그인 시작 실패", e)
            showError("Google 로그인을 시작할 수 없습니다: ${e.message}")
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            Log.d("GoogleLogin", "✅ Google 로그인 성공")
            Log.d("GoogleLogin", "이메일: ${account.email}")
            Log.d("GoogleLogin", "이름: ${account.displayName}")

            if (idToken != null) {
                Log.d("GoogleLogin", "✅ idToken 획득: ${idToken.take(50)}...")
                
                // 🔥 메모리 사용량 체크
                logMemoryUsage("handleSignInResult")

                if (isLogin) {
                    viewModel.loginWithGoogleToken(idToken)
                } else {
                    viewModel.registerWithGoogleToken(idToken)
                }
            } else {
                Log.e("GoogleLogin", "❌ idToken이 null")
                showError("Google 인증 토큰을 가져올 수 없습니다")
            }

        } catch (e: ApiException) {
            Log.e("GoogleLogin", "❌ Google 로그인 실패: ${e.statusCode}", e)
            showError("Google 로그인 실패: ${e.statusCode}")
        } catch (e: Exception) {
            Log.e("GoogleLogin", "❌ 예상치 못한 오류", e)
            showError("로그인 중 오류가 발생했습니다")
        }
    }

    // 🔥 메모리 안전한 StateFlow Observer (repeatOnLifecycle)
    private fun observeLoginResult() {
        // LoginResult StateFlow 관찰
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResult.collect { result ->
                    handleLoginResult(result)
                }
            }
        }
        
        // 로딩 상태 StateFlow 관찰
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    updateLoadingState(isLoading)
                }
            }
        }
    }

    private fun handleLoginResult(result: LoginResult?) {
        result ?: return

        // 🔥 메모리 사용량 체크
        logMemoryUsage("handleLoginResult")

        when (result) {
            is LoginResult.Success -> {
                Log.d("LoginActivity", "✅ 로그인 성공")
                lifecycleScope.launch {
                    try {
                        TokenStore.saveTokens(
                            applicationContext,
                            result.accessToken,
                            result.refreshToken
                        )
                        showSuccess("✅ 로그인 성공")
                        navigateToMain()
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "토큰 저장 실패", e)
                        showError("토큰 저장 중 오류가 발생했습니다")
                    }
                }
            }

            is LoginResult.NeedNickname -> {
                Log.d("LoginActivity", "🔥 닉네임 설정 필요")
                lifecycleScope.launch {
                    try {
                        if (result.accessToken != null && result.refreshToken != null) {
                            TokenStore.saveTokens(
                                applicationContext,
                                result.accessToken,
                                result.refreshToken
                            )
                        }
                        showInfo("👋 닉네임을 먼저 설정해주세요!")
                        Log.d("actoken", "${result.accessToken}")
                        showProfileSetup()
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "닉네임 설정 준비 실패", e)
                        showError("프로필 설정 중 오류가 발생했습니다")
                    }
                }
            }

            is LoginResult.Failure -> {
                Log.e("LoginActivity", "❌ 로그인 실패", result.exception)
                showError("❌ 로그인 실패: ${result.exception.message}")
            }
        }
    }

    // 🔥 로딩 상태 업데이트 (StateFlow 기반)
    private fun updateLoadingState(isLoading: Boolean) {
        _binding?.let { binding ->
            binding.loginButton.isEnabled = !isLoading
            binding.googleSignUpButton.isEnabled = !isLoading
            
            // 로딩 텍스트 업데이트
            if (isLoading) {
                binding.loginButton.text = "로그인 중..."
                binding.googleSignUpButton.text = "가입 중..."
            } else {
                binding.loginButton.text = "Google 로그인"
                binding.googleSignUpButton.text = "Google 회원가입"
            }
        }
    }

    private fun showProfileSetup() {
        try {
            // 🔥 임시로 간단한 처리
            showInfo("프로필 설정이 필요합니다")
            navigateToMain() // 임시로 바로 메인으로
            
            // TODO: ProfileSetupBottomSheet 구현 후 활성화
            // val sheet = ProfileSetupBottomSheet()
            // sheet.isCancelable = false
            // sheet.show(supportFragmentManager, "ProfileSetup")
        } catch (e: Exception) {
            Log.e("LoginActivity", "프로필 설정 처리 실패", e)
            showError("프로필 설정 중 오류가 발생했습니다")
        }
    }

    // 🔥 임시 메소드 (ProfileSetupBottomSheet 인터페이스 구현 시 제거)
    fun onProfileSetupComplete() {
        Log.d("LoginSuccess", "Login Success")
        navigateToMain()
    }

    private fun navigateToMain() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish() // 🔥 메모리 누수 방지
        } catch (e: Exception) {
            Log.e("LoginActivity", "메인 화면 이동 실패", e)
            showError("메인 화면으로 이동할 수 없습니다")
        }
    }

    // 🔥 메모리 효율적인 UI 메시지 (Snackbar)
    private fun showSuccess(message: String) {
        _binding?.let { binding ->
            try {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(android.R.color.holo_green_light))
                    .show()
            } catch (e: Exception) {
                // Snackbar 실패 시 Toast로 대체
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String) {
        _binding?.let { binding ->
            try {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(android.R.color.holo_red_light))
                    .show()
            } catch (e: Exception) {
                // Snackbar 실패 시 Toast로 대체
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showInfo(message: String) {
        _binding?.let { binding ->
            try {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(android.R.color.holo_blue_light))
                    .show()
            } catch (e: Exception) {
                // Snackbar 실패 시 Toast로 대체
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔥 메모리 사용량 로깅
    private fun logMemoryUsage(tag: String) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
            val freeMemInMB = runtime.freeMemory() / 1048576L
            
            Log.d("MemoryUsage", "🔍 [$tag] 메모리 사용량")
            Log.d("MemoryUsage", "   - 사용: ${usedMemInMB}MB")
            Log.d("MemoryUsage", "   - 최대: ${maxHeapSizeInMB}MB")
            Log.d("MemoryUsage", "   - 여유: ${freeMemInMB}MB")
            Log.d("MemoryUsage", "   - 사용률: ${(usedMemInMB * 100 / maxHeapSizeInMB)}%")
            
            // 🚨 메모리 사용량이 과도할 때 경고
            if (usedMemInMB > 300) {
                Log.w("MemoryUsage", "⚠️ 메모리 사용량이 높습니다: ${usedMemInMB}MB")
                System.gc() // 강제 가비지 컬렉션
            }
        } catch (e: Exception) {
            Log.e("MemoryUsage", "메모리 사용량 측정 실패", e)
        }
    }

    // 🔥 네트워크 연결 테스트 (메모리 안전) - ApiConstants 없이 처리
    private fun testNetworkConnection() {
        networkTestJob?.cancel() // 이전 테스트 취소
        networkTestJob = lifecycleScope.launch {
            try {
                Log.d("NetworkTest", "🔍 네트워크 연결 테스트 시작...")

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                // 🔥 하드코딩으로 임시 처리
                val request = Request.Builder()
                    .url("https://your-api-server.com/api/ping") // 임시 URL
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    Log.d("NetworkTest", "✅ 서버 연결 성공!")
                    showSuccess("✅ 서버 연결 성공")
                } else {
                    Log.e("NetworkTest", "❌ 서버 응답 오류: ${response.code}")
                    showError("❌ 서버 응답 오류: ${response.code}")
                }

            } catch (e: Exception) {
                Log.e("NetworkTest", "❌ 네트워크 연결 실패", e)
                showError("❌ 네트워크 연결 실패: ${e.message}")
            }
        }
    }

    private fun logNetworkStatus() {
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

                Log.d("NetworkStatus", "📶 네트워크 상태:")
                Log.d("NetworkStatus", "  - 활성 네트워크: $network")
                Log.d("NetworkStatus", "  - WiFi: ${networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}")
                Log.d("NetworkStatus", "  - 셀룰러: ${networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}")
                Log.d("NetworkStatus", "  - 인터넷: ${networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
            }
        } catch (e: Exception) {
            Log.e("NetworkStatus", "네트워크 상태 확인 실패", e)
        }
    }

    // 🔥 메모리 누수 방지를 위한 생명주기 관리
    override fun onPause() {
        super.onPause()
        // 백그라운드로 갈 때 진행 중인 작업 취소
        networkTestJob?.cancel()
        
        // ViewModel 메소드가 있으면 호출
        try {
            viewModel.cancelOngoingRequests()
        } catch (e: Exception) {
            Log.e("LoginActivity", "ViewModel 취소 실패", e)
        }
        
        // 메모리 사용량 체크
        logMemoryUsage("onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // 🔥 메모리 누수 방지를 위한 명시적 정리
        networkTestJob?.cancel()
        networkTestJob = null
        
        // ViewBinding 정리
        _binding = null
        
        // ViewModel 정리 (메소드가 있으면)
        try {
            viewModel.cleanup()
        } catch (e: Exception) {
            Log.e("LoginActivity", "ViewModel 정리 실패", e)
        }
        
        Log.d("MemoryUsage", "🧹 LoginActivity 정리 완료")
        logMemoryUsage("onDestroy")
    }
}

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
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.databinding.ActivityLoginBinding
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
class LoginActivity : AppCompatActivity(), ProfileSetupBottomSheet.OnProfileSetupCompleteListener {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private var _googleSignInClient: GoogleSignInClient? = null
    private val googleSignInClient: GoogleSignInClient
        get() {
            if (_googleSignInClient == null) {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("357022877460-i42fkd06mv9aq6kn3ub7ma34250dt6ur.apps.googleusercontent.com")
                    .requestEmail()
                    .build()
                _googleSignInClient = GoogleSignIn.getClient(this, gso)
            }
            return _googleSignInClient!!
        }

    private val viewModel: LoginViewModel by viewModels()
    private var isLogin = true

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleActivityResult(result.data)
    }

    private var networkTestJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeLoginResult()
        
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
            logMemoryUsage("startGoogleLogin")
            
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            }
            
        } catch (e: Exception) {
            Log.e("LoginActivity", "Google 로그인 시작 실패", e)
            showError("Google 로그인을 시작할 수 없습니다")
        }
    }

    private fun handleActivityResult(data: android.content.Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } catch (e: Exception) {
            Log.e("LoginActivity", "ActivityResult 처리 실패", e)
            showError("로그인 결과 처리 중 오류가 발생했습니다")
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            Log.d("GoogleLogin", "✅ Google 로그인 성공")
            Log.d("GoogleLogin", "이메일: ${account.email}")

            if (idToken != null) {
                Log.d("GoogleLogin", "✅ idToken 획득")
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
            Log.e("GoogleLogin", "❌ Google 로그인 실패: ${e.statusCode}")
            
            val errorMessage = when (e.statusCode) {
                12501 -> "사용자가 로그인을 취소했습니다"
                12502 -> "네트워크 오류가 발생했습니다"
                else -> "Google 로그인 실패 (${e.statusCode})"
            }
            showError(errorMessage)
            
        } catch (e: Exception) {
            Log.e("GoogleLogin", "❌ 예상치 못한 오류", e)
            showError("로그인 중 오류가 발생했습니다")
        }
    }

    private fun observeLoginResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResult.collect { result ->
                    handleLoginResult(result)
                }
            }
        }
        
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

        logMemoryUsage("handleLoginResult_start")

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
                        cleanupAndNavigate()
                        
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
                        showProfileSetup()
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "닉네임 설정 준비 실패", e)
                        showError("프로필 설정 중 오류가 발생했습니다")
                    }
                }
            }

            is LoginResult.Failure -> {
                Log.e("LoginActivity", "❌ 로그인 실패: ${result.exception.message}")
                showError("❌ 로그인 실패: ${result.exception.message}")
                resetUIState()
            }
        }
        
        logMemoryUsage("handleLoginResult_end")
    }

    private fun resetUIState() {
        _binding?.let { binding ->
            binding.loginButton.isEnabled = true
            binding.googleSignUpButton.isEnabled = true
            binding.loginButton.text = "Google 로그인"
            binding.googleSignUpButton.text = "Google 회원가입"
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        _binding?.let { binding ->
            binding.loginButton.isEnabled = !isLoading
            binding.googleSignUpButton.isEnabled = !isLoading
            
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
            showInfo("프로필 설정이 필요합니다")
            val bottomSheet = ProfileSetupBottomSheet()
            bottomSheet.show(supportFragmentManager, "ProfileSetupBottomSheet")

            
        } catch (e: Exception) {
            Log.e("LoginActivity", "프로필 설정 처리 실패", e)
            showError("프로필 설정 중 오류가 발생했습니다")
        }
    }

    override fun onProfileSetupComplete() {
        Log.d("LoginSuccess", "✅ 프로필 설정 완료! 메인으로 이동")
        cleanupAndNavigate()
    }

    private fun cleanupAndNavigate() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("LoginActivity", "메인 화면 이동 실패", e)
            showError("메인 화면으로 이동할 수 없습니다")
        }
    }

    private fun showSuccess(message: String) {
        showMessage(message, android.R.color.holo_green_light)
    }

    private fun showError(message: String) {
        showMessage(message, android.R.color.holo_red_light)
    }

    private fun showInfo(message: String) {
        showMessage(message, android.R.color.holo_blue_light)
    }

    private fun showMessage(message: String, colorResId: Int) {
        _binding?.let { binding ->
            try {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(colorResId))
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔥 안전한 메모리 사용량 로깅 (GC 호출 완전 제거)
    private fun logMemoryUsage(tag: String) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
            val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
            val usagePercent = (usedMemInMB * 100 / maxHeapSizeInMB)
            
            Log.d("MemoryUsage", "📊 [$tag] 메모리: ${usedMemInMB}MB/${maxHeapSizeInMB}MB (${usagePercent}%)")
            
            // 🔥 경고만 출력, GC는 절대 호출하지 않음 (Android 가이드라인 준수)
            if (usedMemInMB > 200) {
                Log.w("MemoryUsage", "⚠️ 메모리 사용량 높음: ${usedMemInMB}MB - 시스템이 자동으로 관리합니다")
            }
            
        } catch (e: Exception) {
            Log.e("MemoryUsage", "메모리 측정 실패", e)
        }
    }

    override fun onPause() {
        super.onPause()
        
        networkTestJob?.cancel()
        viewModel.cancelOngoingRequests()
        
        try {
            _googleSignInClient?.signOut()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Google Sign-In 정리 실패", e)
        }
        
        logMemoryUsage("onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        
        networkTestJob?.cancel()
        networkTestJob = null
        
        _binding = null
        _googleSignInClient = null
        
        viewModel.cleanup()
        
        Log.d("MemoryUsage", "🧹 LoginActivity 정리 완료")
    }
}

package com.example.kotlin_amateur.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kotlin_amateur.MainActivity
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.databinding.ActivityLoginBinding
import com.example.kotlin_amateur.remote.api.ApiConstants
import com.example.kotlin_amateur.state.LoginResult
import com.example.kotlin_amateur.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), ProfileSetupBottomSheet.OnProfileSetupCompleteListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: LoginViewModel by viewModels()

    private var isLogin = true // ✅ 로그인/회원가입 구분용 플래그

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("357022877460-i42fkd06mv9aq6kn3ub7ma34250dt6ur.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        observeLoginResult()

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
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            println("🔥 [클라이언트] Google 로그인 성공")
            println("🔍 [클라이언트] account 정보:")
            println("   - email: ${account.email}")
            println("   - displayName: ${account.displayName}")
            println("   - id: ${account.id}")
            println("   - serverAuthCode: ${account.serverAuthCode}")

            Log.d("GoogleLogin", "✅ idToken: $idToken")

            if (idToken != null) {
                println("🔍 [클라이언트] idToken 상세 정보:")
                println("   - 길이: ${idToken.length}")
                println("   - 시작: ${idToken.take(50)}...")

                // JWT 토큰 디코딩해서 만료시간 확인
                try {
                    val parts = idToken.split(".")
                    if (parts.size >= 2) {
                        val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                        println("🔍 [클라이언트] JWT payload: $payload")

                        // JSON 파싱해서 만료시간 확인
                        val currentTime = System.currentTimeMillis() / 1000
                        println("🔍 [클라이언트] 현재 시간: $currentTime")
                    }
                } catch (e: Exception) {
                    println("⚠️ [클라이언트] JWT 디코딩 실패: ${e.message}")
                }

                if (isLogin) {
                    println("🔥 [클라이언트] 로그인 모드 - ViewModel 호출")
                    viewModel.loginWithGoogleToken(idToken)
                } else {
                    println("🔥 [클라이언트] 회원가입 모드 - ViewModel 호출")
                    viewModel.registerWithGoogleToken(idToken)
                }
            } else {
                println("❌ [클라이언트] idToken이 null")
                Toast.makeText(this, "ID Token 없음", Toast.LENGTH_SHORT).show()
            }

        } catch (e: ApiException) {
            println("❌ [클라이언트] Google 로그인 실패")
            println("❌ [클라이언트] ApiException:")
            println("   - statusCode: ${e.statusCode}")
            println("   - message: ${e.message}")
            println("   - localizedMessage: ${e.localizedMessage}")
            Toast.makeText(this, "구글 로그인 실패: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { result ->
            println("🔥 [클라이언트] LoginResult 받음: ${result.javaClass.simpleName}")

            when (result) {
                is LoginResult.Success -> {
                    println("✅ [클라이언트] 로그인 성공")
                    println("🔍 [클라이언트] accessToken 길이: ${result.accessToken.length}")
                    println("🔍 [클라이언트] refreshToken 길이: ${result.refreshToken.length}")

                    lifecycleScope.launch {
                        TokenStore.saveTokens(
                            applicationContext,
                            result.accessToken,
                            result.refreshToken
                        )
                        Toast.makeText(this@LoginActivity, "✅ 로그인 성공", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }

                is LoginResult.NeedNickname -> {
                    println("🔥 [클라이언트] 닉네임 설정 필요")
                    println("🔍 [클라이언트] NeedNickname 정보:")
                    println("   - email: ${result.email}")
                    println("   - name: ${result.name}")
                    println("   - googleSub: ${result.googleSub}")
                    println("   - accessToken 길이: ${result.accessToken?.length ?: 0}")

                    lifecycleScope.launch {
                        if (result.accessToken != null && result.refreshToken != null) {
                            TokenStore.saveTokens(
                                applicationContext,
                                result.accessToken,
                                result.refreshToken
                            )
                        }
                        Toast.makeText(this@LoginActivity, "👋 닉네임을 먼저 설정해주세요!", Toast.LENGTH_SHORT)
                            .show()
                        Log.d("actoken", "${result.accessToken}")
                        val sheet = ProfileSetupBottomSheet()
                        sheet.isCancelable = false
                        sheet.show(supportFragmentManager, "ProfileSetup")
                    }
                }

                is LoginResult.Failure -> {
                    println("❌ [클라이언트] 로그인 실패")
                    println("❌ [클라이언트] 실패 정보:")
                    println("   - exception: ${result.exception.javaClass.simpleName}")
                    println("   - message: ${result.exception.message}")
                    println("   - localizedMessage: ${result.exception.localizedMessage}")
                    result.exception.printStackTrace()

                    Toast.makeText(
                        this,
                        "❌ 로그인 실패: ${result.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LoginActivity", "Login failure ${result.exception.message}")
                }
            }
        }
    }

    override fun onProfileSetupComplete() {
        Log.d("LoginSuccess", "Login Success")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun logNetworkStatus() {
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
    }

    private fun testNetworkConnection() {
        lifecycleScope.launch {
            try {
                Log.d("NetworkTest", "🔍 네트워크 연결 테스트 시작...")

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url("${ApiConstants.BASE_URL}/api/ping")
                    .build()

                withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("NetworkTest", "✅ 서버 연결 성공!")
                            Toast.makeText(this@LoginActivity, "✅ 서버 연결 성공", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("NetworkTest", "❌ 서버 응답 오류: ${response.code}")
                            Toast.makeText(this@LoginActivity, "❌ 서버 응답 오류: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("NetworkTest", "❌ 네트워크 연결 실패", e)
                Toast.makeText(this@LoginActivity, "❌ 네트워크 연결 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

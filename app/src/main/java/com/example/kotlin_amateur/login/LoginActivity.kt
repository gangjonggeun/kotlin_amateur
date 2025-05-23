package com.example.kotlin_amateur.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kotlin_amateur.MainActivity
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.databinding.ActivityLoginBinding
import com.example.kotlin_amateur.remote.response.LoginResponse
import com.example.kotlin_amateur.state.LoginResult
import com.example.kotlin_amateur.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), ProfileSetupBottomSheet.OnProfileSetupCompleteListener {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var googleSignInClient: GoogleSignInClient

    private val viewModel: LoginViewModel by viewModels()

    private var isLogin :Boolean = false

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

        observeLoginResult()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        
        //구글 회원가입 버튼 클릭 리스너
        findViewById<Button>(R.id.googleSignUpButton).setOnClickListener {
            isLogin = false
            startGoogleLogin()
        }

        //로그인 버튼 클릭 리스너
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            isLogin = true
            startGoogleLogin()
        }


    }

    private fun startGoogleLogin() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }


    /**
     * Google 로그인 결과를 처리하는 함수입니다.
     *
     * Google 계정 인증이 성공하면 ID Token을 추출하여 ViewModel로 전달하고,
     * 실패 시 사용자에게 오류 메시지를 표시합니다.
     *
     * @param task Google 로그인 결과를 담고 있는 Task 객체
     */

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            val email = account.email

            Log.d("GoogleLogin", "✅ idToken: $idToken")

            if (idToken != null && email != null) {
                val isTestAccount = (email == "whdrms185900@gmail.com") // ✅ 테스트 계정 체크
                viewModel.loginWithGoogleToken(idToken, isTestAccount, isLogin)
            } else {
                Toast.makeText(this, "ID Token 또는 이메일 없음", Toast.LENGTH_SHORT).show()
            }

        } catch (e: ApiException) {
            Toast.makeText(this, "구글 로그인 실패: ${e.statusCode}", Toast.LENGTH_SHORT).show()

        }
    }

    /**
     * 로그인 결과를 관찰하여 성공 시 메인 화면으로 이동하고,
     * 실패 시 사용자에게 실패 메시지를 표시합니다.
     */
    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Success -> {
                    lifecycleScope.launch {
                        TokenStore.saveTokens(applicationContext, result.accessToken, result.refreshToken)
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }
                is LoginResult.NeedNickname -> {
                    lifecycleScope.launch {
                        TokenStore.saveTokens(applicationContext, result.accessToken, result.refreshToken)
                        Log.d("actoken","${result.accessToken}")
                        val sheet = ProfileSetupBottomSheet()
                        sheet.isCancelable = false
                        sheet.show(supportFragmentManager, "ProfileSetup")
                    }
                }
                is LoginResult.Failure -> {
                    Toast.makeText(this, "로그인 실패: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Login failure ${result.exception.message}")
                }
                is LoginResult.SelectUser ->{
                    // 테스트 계정 유저 리스트 다이얼로그 등으로 선택하게 만들기
                    showUserSelectionDialog(result.testUsers)
                }
            }
        }
    }
    /**
     * 테스트 유저 계정 정보들 보여줄 다이얼로그
     * */
    fun showUserSelectionDialog(users: List<LoginResponse>) {
        val nicknames = users.map { it.nickname ?: "익명" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("사용할 계정을 선택하세요")
            .setItems(nicknames) { _, index ->
                val selected = users[index]
                lifecycleScope.launch {
                    TokenStore.saveTokens(
                        context = applicationContext,
                        access = selected.accessToken,
                        refresh = selected.refreshToken
                    )
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 로그인 및 회원 가입 성공시 메인액티비티로 이동할 콜백 함수 
     * (중간 단계 프래그먼트에서 이동 시 액티비티 꼬일까봐)
     * */
    override fun onProfileSetupComplete() {
        Log.d("LoginSucces","Login Succes")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    /**
     * 토큰 저장
     * */

}
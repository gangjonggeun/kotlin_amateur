package com.example.kotlin_amateur.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlin_amateur.MainActivity
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.databinding.ActivityLoginBinding
import com.example.kotlin_amateur.state.LoginResult
import com.example.kotlin_amateur.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private var isSignUpMode = false
    private val nicknameRegex = Regex("^[a-zA-Z0-9가-힣]{1,10}$")

    private val viewModel: LoginViewModel by viewModels()

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
            isSignUpMode = true
            startGoogleLogin()
        }

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            isSignUpMode = false
            startGoogleLogin()
        }
    }


    private fun showNicknameDialog(email: String, id: String, name: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nickname, null)
        val editText = dialogView.findViewById<EditText>(R.id.nicknameEditText)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        confirmButton.setOnClickListener {
            val nickname = editText.text.toString().trim()
            if (nickname.isEmpty()) {
                editText.error = "닉네임을 입력해주세요."
            }else if (!nicknameRegex.matches(nickname)) {
                editText.error = "한글, 영어, 숫자만 입력 가능 (최대 10자)"
            }
            else {
                // TODO: 서버 저장 or SharedPreferences 처리
                dialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
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

            Log.d("🔥IDTOKEN", "idToken = ${idToken ?: "NULL!"}")

            if (idToken != null) {
                viewModel.loginWithGoogleToken(idToken)
            } else {
                Toast.makeText(this, "ID Token 없음", Toast.LENGTH_SHORT).show()
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
                    // 토큰 저장하고 메인으로 이동
                }
                is LoginResult.NeedNickname -> {
                    // 닉네임 다이얼로그 띄우기
                    showNicknameDialog(result.email, result.googleSub, result.name)
                }
                is LoginResult.Failure -> {
                    Toast.makeText(this, "로그인 실패: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
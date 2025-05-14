package com.example.kotlin_amateur.login

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.kotlin_amateur.MainActivity
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.viewmodel.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSetupBottomSheet : BottomSheetDialogFragment() {

    private var shouldNavigate = false
    interface OnProfileSetupCompleteListener {
        fun onProfileSetupComplete()
    }
    private var listener: OnProfileSetupCompleteListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProfileSetupCompleteListener) {
            listener = context
            Log.d("ListenerCheck", "✅ listener conect!")
        } else {
            Log.e("ListenerCheck", "❌ LoginActivity is not have interface")
        }
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    val nicknameRegex = Regex("^[가-힣a-zA-Z0-9]{1,10}$")
    override fun getTheme(): Int = R.style.FullScreenBottomSheetDialog
    private val viewModel: ProfileViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_profile_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val confirmBtn = view.findViewById<Button>(R.id.confirmButton)
//        val nicknameInput = view.findViewById<EditText>(R.id.nicknameEditText)

        confirmBtn.setOnClickListener {
           setupProfile()
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            Log.d("✅ observe doing", "result: $success")
            if (success) {
                shouldNavigate = true
                dismiss()
            } else {
                Toast.makeText(requireContext(), "프로필 설정 실패", Toast.LENGTH_SHORT).show()
            }
        }

    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (shouldNavigate) {
            Log.d("🔥 ViewModel", "onDismiss -> MainActivity로 이동 시도")
            listener?.onProfileSetupComplete()
        }
    }
    /**
     * 닉네임 생성
     */
    fun setupProfile(){
        val nicknameInput = view?.findViewById<EditText>(R.id.nicknameEditText) ?: return
        val rawNickname = nicknameInput.text.toString().trim()

        if (rawNickname.isEmpty()) {
            showNicknameWarningDialog(nicknameInput)
            return
        }

        if (!nicknameRegex.matches(rawNickname)) {
            nicknameInput.error = "한글, 영어, 숫자만 입력 가능 (최대 10자)"
            return
        }

        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", "") ?: ""

        Log.d("🔥 savedToken", accessToken)
        // TODO : 후에 기본 프로필 이미지 넣기
        val profileImageUrl = "" //

        // ✅ 유효하면 바로 서버 전송
        viewModel.setupProfile(rawNickname, profileImageUrl, accessToken)
    }//닉네임 생성 끝
    /**
     * 경고 다이얼로그
     */
    fun showNicknameWarningDialog(nicknameInput: EditText) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("닉네임이 비어있습니다")
            .setMessage("닉네임을 입력하지 않으면 랜덤 닉네임이 부여됩니다.\n계속하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                val randomNickname = generateRandomNickname()
                Toast.makeText(requireContext(), "랜덤 닉네임: $randomNickname", Toast.LENGTH_SHORT).show()
                nicknameInput.setText(randomNickname)

            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    } //경고 다이얼로그 끝

    /**
     * 랜덤 닉네임 생성
     */
    fun generateRandomNickname(): String {
        val adjectives = listOf("귀여운", "용감한", "재빠른", "똑똑한", "신비한")
        val animals = listOf("호랑이", "토끼", "여우", "사자", "펭귄")
        return adjectives.random() + animals.random() + (100..999).random()
    } //랜덤 닉네임 생성 끝
}
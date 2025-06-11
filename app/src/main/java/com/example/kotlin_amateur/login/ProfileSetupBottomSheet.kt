package com.example.kotlin_amateur.login

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.viewmodel.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import com.example.kotlin_amateur.core.util.toMultipart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

@AndroidEntryPoint
class ProfileSetupBottomSheet : BottomSheetDialogFragment() {

    private var shouldNavigate = false
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            view?.findViewById<ImageView>(R.id.profileImage)?.setImageURI(it)
        }
    }

    interface OnProfileSetupCompleteListener {
        fun onProfileSetupComplete()
    }

    private var listener: OnProfileSetupCompleteListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProfileSetupCompleteListener) {
            listener = context
            Log.d("ListenerCheck", "✅ listener connected!")
        } else {
            Log.e("ListenerCheck", "❌ LoginActivity is not implementing OnProfileSetupCompleteListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private val nicknameRegex = Regex("^[가-힣a-zA-Z0-9]{1,10}$")
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

        confirmBtn.setOnClickListener {
            setupProfile()
        }
        val profileImageView = view.findViewById<ImageView>(R.id.profileImage)
        profileImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateSuccess.collect { success ->
                Log.d("✅ collect", "result: $success")
                if (success) {
                    shouldNavigate = true
                    dismiss()
                    // 상태 리셋 (필요시)
                    viewModel.resetUpdateSuccess()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    // 에러 메시지 클리어 (필요시)
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (shouldNavigate) {
            Log.d("🔥 ViewModel", "onDismiss -> MainActivity로 이동")
            listener?.onProfileSetupComplete()
        }
    }

    private fun setupProfile() {
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


        val imagePart =  selectedImageUri?.toMultipart(requireContext(), "profileImage")
        viewModel.setupProfile(rawNickname, imagePart)
    }

    private fun showNicknameWarningDialog(nicknameInput: EditText) {
        lifecycleScope.launch {
            val ctx = requireContext()
            val randomNickname = generateRandomNickname()

            val dialog = AlertDialog.Builder(ctx, com.google.android.material.R.style.MaterialAlertDialog_Material3)
                .setTitle("닉네임이 비어있습니다")
                .setMessage("닉네임을 입력하지 않으면 랜덤 닉네임이 부여됩니다.\n계속하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    Toast.makeText(ctx, "랜덤 닉네임: $randomNickname", Toast.LENGTH_SHORT).show()
                    nicknameInput.setText(randomNickname)
                }
                .setNegativeButton("취소", null)
                .create()

            dialog.setOnShowListener {
                val width = (resources.displayMetrics.widthPixels * 0.75).toInt()  // 75% 너비로 조정
                dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            dialog.show()
        }
    }

    private fun generateRandomNickname(): String {
        val adjectives = listOf("귀여운", "용감한", "재빠른", "똑똑한", "신비한")
        val animals = listOf("호랑이", "토끼", "여우", "사자", "펭귄")
        return adjectives.random() + animals.random() + (100..999).random()
    }
}

package com.example.kotlin_amateur.navi.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.login.ProfileSetupBottomSheet

class RightSheetDialogFragment : DialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            // ✅ 1. 시스템 창 내부에 그리도록 강제 (필수)
            WindowCompat.setDecorFitsSystemWindows(window, true)

            // ✅ 2. 전체화면 비활성화
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            // ✅ 3. 상태바 색상 명시적으로 지정
            window.statusBarColor = Color.WHITE

            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.7).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            window.setGravity(Gravity.END)
            window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#80000000")))
            window.setWindowAnimations(R.style.RightSheetAnimation)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RightSheetDialogTheme)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // RightSheetMenu에 전달할 콜백들 연결
                RightSheetMenu(
                    onEditProfileClick = {
                        Log.d("RightSheet", "🔥 프로필 편집 클릭됨")
                        // dismiss() 하지 말고 바로 BottomSheet 띄우기
                        showProfileEditBottomSheet() },
                    onEditNicknameClick = { /* 닉네임 수정 바텀시트 띄우기 */ },
                    onMyPostsClick = { /* 내 게시글 프래그먼트로 이동 */ },
                    onLikedPostsClick = { /* 좋아요한 글 */ },
                    onMyCommentsClick = { /* 댓글 보기 */ },
                    onRecentViewsClick = { /* 최근 본 글 */ },
                    onSettingsClick = { /* 설정 화면 */ },
                    onLogoutClick = { dismiss() } // 예: 로그아웃 후 시트 닫기
                )
            }
        }
    }
    private fun showProfileEditBottomSheet() {
        Log.d("RightSheet", "🔥 showProfileEditBottomSheet 호출됨")

        try {
            // 1. 먼저 BottomSheet 띄우기 (dismiss() 전에!)
            val bottomSheet = ProfileSetupBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileEditBottomSheet")
            Log.d("RightSheet", "✅ BottomSheet show() 호출 성공")

            // 2. BottomSheet가 띄워진 후 RightSheet 닫기
            Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
            }, 100) // 짧은 딜레이 후 닫기

        } catch (e: Exception) {
            Log.e("RightSheet", "❌ BottomSheet show() 실패: ${e.message}", e)
        }
    }
}
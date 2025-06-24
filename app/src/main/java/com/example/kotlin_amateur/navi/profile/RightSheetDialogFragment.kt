package com.example.kotlin_amateur.navi.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.login.ProfileSetupBottomSheet
import com.example.kotlin_amateur.core.PostListType
import com.example.kotlin_amateur.navi.home.ModernHomeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                // 🎯 현재 표시할 화면 상태 관리
                var currentScreen by remember { mutableStateOf<PostListType?>(null) }
                
                when (currentScreen) {
                    null -> {
                        // 🏠 메인 메뉴 화면
                        RightSheetMenu(
                            onEditProfileClick = {
                                Log.d("RightSheet", "🔥 프로필 편집 클릭됨")
                                showProfileEditBottomSheet()
                            },
                            onEditNicknameClick = { /* 닉네임 수정 바텀시트 */ },
                            onMyPostsClick = {
                                navigateToProfilePostList(PostListType.MY_POSTS)
                                dismiss()
                            },
                    onLikedPostsClick = {
                        navigateToProfilePostList(PostListType.LIKED_POSTS) 
                        dismiss()
                    },
                    onMyCommentsClick = { /* 댓글 보기 - 추후 구현 */ },
                    onRecentViewsClick = {
                        navigateToProfilePostList(PostListType.RECENT_VIEWED)
                        dismiss()
                    },
                            onSettingsClick = { /* 설정 화면 */ },
                            onLogoutClick = { dismiss() },
                            // 🎯 Navigation 추가
                            onNavigateToPostList = { postListType ->
                                currentScreen = postListType
                            }
                        )
                    }
                    
                    else -> {
                        // 📝 게시글 목록 화면 (ModernHomeScreen 재사용)
                        ModernHomeScreen(
                            postListType = currentScreen!!, // Non-null assertion 안전
                            onBackClick = {
                                // 🔙 메뉴로 돌아가기
                                currentScreen = null
                            },
                            onNavigateToAddPost = {
                                // 글 작성 기능 (내 게시글에서만)
                                Log.d("RightSheet", "✏️ 글 작성 클릭")
                            },
                            onNavigateToPostDetail = { postId, title ->
                                // 게시글 상세 보기
                                Log.d("RightSheet", "📖 게시글 상세: $postId")
                                // 여기서 게시글 상세 화면으로 네비게이션 가능
                            },
                            onNavigateToStorePromotion = TODO(),
                            homeViewModel = TODO(),
                            profileViewModel = TODO()
                        )
                        
                        // 🔙 뒤로가기 효과 (메뉴로 돌아가기)
                        // 실제로는 뒤로가기 버튼을 추가하거나, 스와이프 제스처 등으로 처리 가능
                    }
                }
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
    
    /**
     * 🎯 프로필 게시글 목록 화면으로 이동
     * - Navigation Component 사용으로 안전한 이동
     * - 메모리 안전: Fragment 사용으로 생명주기 관리
     */
    private fun navigateToProfilePostList(postListType: PostListType) {
        try {
            Log.d("RightSheet", "🚀 navigateToProfilePostList: ${postListType.displayName}")
            
            // ✅ 올바른 Navigation Component 사용
            val navController = findNavController()
            
            // 📦 Bundle로 argument 정확히 전달
            val bundle = Bundle().apply {
                putString("postListType", postListType.name)
                Log.d("RightSheet", "📦 Bundle 생성: postListType=${postListType.name}")
            }
            
            // 🎯 nav_graph.xml에 정의된 postListFragment로 이동
            navController.navigate(R.id.postListFragment, bundle)
            Log.d("RightSheet", "✅ Navigation 성공")
            
        } catch (e: Exception) {
            Log.e("RightSheet", "❌ Navigation 실패: ${e.message}", e)
        }
    }
}
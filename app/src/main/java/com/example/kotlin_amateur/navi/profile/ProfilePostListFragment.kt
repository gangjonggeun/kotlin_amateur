package com.example.kotlin_amateur.navi.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kotlin_amateur.core.PostListType
import com.example.kotlin_amateur.navi.home.ModernHomeScreen
import com.example.kotlin_amateur.viewmodel.ProfilePostViewModel
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.navi.profile.ProfilePostListFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

/**
 * 🎯 프로필 게시글 목록 Fragment
 * - ProfilePostViewModel 사용으로 프로필 전용 기능 제공
 * - ModernHomeScreen 재사용으로 일관된 UI 유지
 * - 메모리 안전: Fragment 생명주기 기반 ViewModel 관리
 */
@AndroidEntryPoint
class ProfilePostListFragment : Fragment() {

    companion object {
        private const val ARG_POST_LIST_TYPE = "post_list_type"
        private const val TAG = "ProfilePostListFragment"

        fun newInstance(postListType: PostListType): ProfilePostListFragment {
            return ProfilePostListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_LIST_TYPE, postListType.name)
                }
            }
        }
    }

    // 🎯 ProfilePostViewModel 주입
    private val viewModel: ProfilePostViewModel by viewModels()

    private val postListType: PostListType by lazy {
        // ✅ Navigation Component argument 사용 (nav_graph.xml에 정의된 이름)
        val typeName = arguments?.getString("postListType") 
            ?: arguments?.getString(ARG_POST_LIST_TYPE) // 폴백: 기존 방식도 지원
            ?: PostListType.MY_POSTS.name
        
        Log.d(TAG, "📦 Argument 수신: $typeName")
        PostListType.valueOf(typeName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // 🎯 ViewModel에 타입 설정
                viewModel.setPostListType(postListType)
                Log.d(TAG, "🚀 ProfilePostListFragment 생성 - 타입: ${postListType.displayName}")
                
                // 🎯 전체화면으로 ModernHomeScreen 표시
                ModernHomeScreen(
                    postListType = postListType,
                    onBackClick = {
                        // 🔙 뒤로가기 시 Fragment 종료
                        Log.d(TAG, "🔙 뒤로가기 클릭")
                        requireActivity().supportFragmentManager.popBackStack()
                    },
                    onNavigateToAddPost = {
                        // ✏️ 글 작성 (내 게시글에서만 필요하면 구현)
                        Log.d(TAG, "✏️ 글 작성 클릭")
                    },
                    onNavigateToPostDetail = { postId, title ->
                        // 📖 게시글 상세 보기 - ProfilePostListFragment의 action 사용
                        Log.d(TAG, "📖 프로파일 포스트 프래그먼트에서 게시글 상세 이동: postId=$postId, title=$title")
                        navigateToPostDetail(postId, title)
                    },
                    // 🔥 ProfilePostViewModel 명시적 전달
                    profileViewModel = viewModel,
                    homeViewModel = null,
                    onNavigateToStorePromotion = TODO() // Profile에서는 HomeViewModel 사용 안함
                )
            }
        }
    }


    // 🎯 게시글 상세 페이지 네비게이션 (메모리 안전)
    private fun navigateToPostDetail(postId: String, title: String? = null) {
        try {
            // ✅ Safe Navigation: null 체크
            val navController = findNavController()

            // 🎯 올바른 Navigation 방식: ProfilePostListFragment의 action 사용
            val action = ProfilePostListFragmentDirections.actionPostListToPostDetail(
                postId = postId,
                title = title ?: "게시글 상세" // 기본 제목
            )

            navController.navigate(action)

        } catch (e: Exception) {
            // ❌ Exception 대신 가벼운 로깅 (50바이트 vs 3MB)
            Log.e("ProfilePostListFragment", "네비게이션 실패: postId=$postId")
        }
    }
//    override fun onResume() {
//        super.onResume()
//        // 📊 화면 복귀 시 통계 새로고침
//        viewModel.loadProfileStats()
//    }
}

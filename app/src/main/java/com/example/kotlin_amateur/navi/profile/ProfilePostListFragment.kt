package com.example.kotlin_amateur.navi.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kotlin_amateur.core.PostListType
import com.example.kotlin_amateur.navi.home.ModernHomeScreen
import com.example.kotlin_amateur.viewmodel.ProfilePostViewModel
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
        val typeName = arguments?.getString(ARG_POST_LIST_TYPE) ?: PostListType.MY_POSTS.name
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
                        // 📖 게시글 상세 보기
                        Log.d(TAG, "📖 게시글 상세: $postId, 제목: $title")
                        // 여기서 Navigation으로 상세 화면 이동 가능
                    }
                    // 🔥 ModernHomeScreen이 ProfilePostViewModel을 자동으로 감지하도록
                    // hiltViewModel()를 내부에서 사용하게 되어 있다면 별도 전달 불필요
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 📊 화면 복귀 시 통계 새로고침
        viewModel.loadProfileStats()
    }
}

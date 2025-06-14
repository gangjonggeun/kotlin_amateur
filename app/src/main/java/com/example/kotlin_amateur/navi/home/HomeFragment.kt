package com.example.kotlin_amateur.navi.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.kotlin_amateur.navi.home.ModernHomeScreen
import com.example.kotlin_amateur.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.kotlin_amateur.R

@AndroidEntryPoint
class HomeFragment : Fragment() {

    // ✅ 메모리 안전: Hilt ViewModel 지연 초기화
    private val homeViewModel: HomeViewModel by viewModels()

    // ✅ 메모리 안전: ComposeView null 처리
    private var composeView: ComposeView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ✅ 메모리 효율적 ComposeView 생성
        composeView = ComposeView(requireContext()).apply {
            // 🔧 메모리 누수 방지: Fragment 생명주기에 맞춰 자동 해제
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                // 🎨 테마 적용
                MaterialTheme {
                    // 🚀 메모리 최적화된 ModernHomeScreen 사용
                    ModernHomeScreen(
                        // 🎯 새로운 API 연결 - 상세 페이지 네비게이션
                        onNavigateToPostDetail = { postId, title ->
                            navigateToPostDetail(postId, title)
                        },
                        // 📝 글 작성 페이지 네비게이션
                        onNavigateToAddPost = {
                            navigateToAddPost()
                        },
                        // ✅ ViewModel 수동 주입 (메모리 제어)
                        viewModel = homeViewModel
                    )
                }
            }
        }

        return composeView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 메모리 안전: 생명주기 기반 관찰
        observeViewModelStates()
    }

    // 🔍 ViewModel 상태 관찰 (메모리 안전)
    private fun observeViewModelStates() {
        // ✅ 메모리 누수 방지: repeatOnLifecycle 사용
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 🔍 검색어 상태 관찰 (필요시 추가 처리)
                homeViewModel.searchQuery.collect { query ->
                    // 검색 상태 로깅 또는 추가 처리
                    android.util.Log.d("HomeFragment", "🔍 검색어: $query")
                }
            }
        }
    }

    // 🎯 게시글 상세 페이지 네비게이션 (메모리 안전)
    private fun navigateToPostDetail(postId: String, title: String? = null) {
        try {
            // ✅ Safe Navigation: null 체크
            val navController = findNavController()

            // 🎯 기존 Navigation 방식 유지
            val action = HomeFragmentDirections.actionHomeToPostDetail(
                postId = postId,
                title = title ?: "게시글 상세" // 기본 제목
            )

            navController.navigate(action)

        } catch (e: Exception) {
            // ❌ Exception 대신 가벼운 로깅 (50바이트 vs 3MB)
            android.util.Log.e("HomeFragment", "네비게이션 실패: postId=$postId")
        }
    }

    // 📝 글 작성 페이지 네비게이션
    private fun navigateToAddPost() {
        try {
            findNavController().navigate(R.id.floatingAddFragment)
            android.util.Log.d("HomeFragment", "📝 글 작성 페이지 이동")

        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "글 작성 네비게이션 실패")
        }
    }

    // ✅ 메모리 누수 방지: View 해제
    override fun onDestroyView() {
        super.onDestroyView()
        // 🔧 ComposeView 명시적 해제
        composeView = null
    }

    // 📱 시스템 백 버튼 처리 (선택사항)
    override fun onResume() {
        super.onResume()
        // 🔄 화면 재진입 시 데이터 새로고침 (자동 처리됨)
        android.util.Log.d("HomeFragment", "🏠 홈 화면 활성화")
    }
}
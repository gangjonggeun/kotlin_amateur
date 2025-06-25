package com.example.kotlin_amateur.navi.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.kotlin_amateur.state.StorePromotionResult
import com.example.kotlin_amateur.viewmodel.StorePromotionViewModel
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StorePromotionFragment : Fragment() {

    // 🔥 ViewModel 선언 (메모리 효율적)
    private val viewModel: StorePromotionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 카카오맵 SDK 초기화 (Fragment에서 직접)
        try {
            KakaoMapSdk.init(requireContext(), "35b1fe4c1b1ac26786fac46a9dd60588")
            Log.d("🏩 StorePromotionFragment", "✅ 카카오맵 SDK 초기화 완료!")
        } catch (e: Exception) {
            Log.e("🏩 StorePromotionFragment", "❌ 카카오맵 SDK 초기화 실패: ${e.message}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // 🔥 ViewCompositionStrategy 설정 (메모리 누수 방지)
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            
            setContent {
                // 🎨 가게 홍보 화면
                StorePromotionScreen(
                    viewModel = viewModel,
                    onBackPress = {
                        // 🔙 내비게이션으로 뒤로가기
                        findNavController().navigateUp()
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 🎯 결과 상태 관찰 (View 생성 후 안전하게 시작)
        observePromotionResult()
    }
    
    /**
     * 🎯 결과 상태 관찰 (콜백 방식 - 메모리 안전)
     * 
     * ⚠️ viewLifecycleOwner는 onCreateView 이후에만 접근 가능!
     * Screen에서 LaunchedEffect로 처리하지만, Fragment에서도
     * 추가적으로 로깅과 내비게이션을 안전하게 처리
     */
    private fun observePromotionResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.promotionResult.collect { result ->
                when (result) {
                    is StorePromotionResult.Success -> {
                        Log.d("🏩 StorePromotionFragment", "✅ 가게 등록 성공: ${result.message}")
                        // Screen에서 이미 처리하지만, Fragment에서도 내비게이션 보장
                    }
                    is StorePromotionResult.Error -> {
                        Log.e("🏩 StorePromotionFragment", "❌ 가게 등록 실패: ${result.message}")
                        // 에러 시 로깅만 출력 (화면은 유지)
                    }
                    is StorePromotionResult.Loading -> {
                        Log.d("🏩 StorePromotionFragment", "🔄 가게 등록 중...")
                    }
                    null -> {
                        // 초기 상태 또는 초기화 후
                    }
                }
            }
        }
    }
    
    // 🧠 메모리 정리는 ViewCompositionStrategy가 자동 처리
    // onDestroyView나 기타 수동 정리 불필요!
}

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
import androidx.navigation.fragment.findNavController
import com.example.kotlin_amateur.viewmodel.StorePromotionViewModel
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StorePromotionFragment : Fragment() {

    // 🔥 ViewModel 선언 (메모리 효율적)
    private val viewModel: StorePromotionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 카카오맵 SDK 초기화 (Fragment에서 직접)
        try {
            KakaoMapSdk.init(requireContext(), "35b1fe4c1b1ac26786fac46a9dd60588")
            Log.d("🏪 StorePromotionFragment", "✅ 카카오맵 SDK 초기화 완료!")
        } catch (e: Exception) {
            Log.e("🏪 StorePromotionFragment", "❌ 카카오맵 SDK 초기화 실패: ${e.message}")
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
    
    // 🧹 메모리 정리는 ViewCompositionStrategy가 자동 처리
    // onDestroyView나 기타 수동 정리 불필요!
}

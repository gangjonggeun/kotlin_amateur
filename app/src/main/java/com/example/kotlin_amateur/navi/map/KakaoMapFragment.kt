package com.example.kotlin_amateur.navi.map

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
import com.example.kotlin_amateur.viewmodel.MapRecommendViewModel
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class KakaoMapFragment : Fragment() {

    private val viewModel: MapRecommendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 카카오맵 SDK 초기화 (Fragment에서 직접)
        try {
            KakaoMapSdk.init(requireContext(), "35b1fe4c1b1ac26786fac46a9dd60588")
            Log.d("🗺️ KakaoMapFragment", "✅ 카카오맵 SDK 초기화 완료!")
        } catch (e: Exception) {
            Log.e("🗺️ KakaoMapFragment", "❌ 카카오맵 SDK 초기화 실패: ${e.message}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("🗺️ KakaoMapFragment", "onCreateView called")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                KakaoMapRecommendScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        Log.d("🗺️ KakaoMapFragment", "Navigating back")
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }
}
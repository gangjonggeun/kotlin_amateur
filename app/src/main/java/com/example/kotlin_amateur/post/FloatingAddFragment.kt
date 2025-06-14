package com.example.kotlin_amateur.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kotlin_amateur.post.components.FloatingAddScreen
import com.example.kotlin_amateur.viewmodel.FloatingAddViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FloatingAddFragment : Fragment() {

    // 🔥 ViewModel 선언 (메모리 효율적)
    private val viewModel: FloatingAddViewModel by viewModels()

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
                // 🎨 트렌디한 Compose 화면 사용
                FloatingAddScreen(
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

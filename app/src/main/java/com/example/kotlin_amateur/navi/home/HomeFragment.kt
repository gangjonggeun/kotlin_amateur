package com.example.kotlin_amateur.navi.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                ModernHomeScreen(
                    onNavigateToAddPost = {
                        // 기존 네비게이션 로직 유지
                        findNavController().navigate(
                            com.example.kotlin_amateur.R.id.addPostFragment
                        )
                    },
                    onNavigateToPostDetail = { postId, title ->
                        // 기존 네비게이션 로직 유지
                        val action = HomeFragmentDirections.actionHomeToPostDetail(
                            postId = postId,
                            title = title
                        )
                        findNavController().navigate(action)
                    }
                )
            }
        }
    }
}
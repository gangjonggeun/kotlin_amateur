package com.example.kotlin_amateur.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.post.components.PostDetailComposeScreen
import com.example.kotlin_amateur.viewmodel.PostDetailViewModel
import com.example.kotlin_amateur.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    private val args: PostDetailFragmentArgs by navArgs()
    private val viewModel: PostDetailViewModel by viewModels()
    private val userProfileViewModel: UserProfileViewModel by viewModels()

    // 🔧 Fragment 레벨에서 dialogManager 관리
    private val dialogManager = ProfileDialogManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 전달받은 스택 복원 로직 개선
        restoreProfileStackIfExists()
    }

    /**
     * 🔄 프로필 스택 복원 (Bundle에서)
     */
    private fun restoreProfileStackIfExists() {
        val profileStack = arguments?.getStringArray("profileStack")

        if (profileStack != null && profileStack.isNotEmpty()) {
            dialogManager.restoreStack(profileStack.toList())
            println("📥 [PostDetail] 프로필 스택 복원: ${profileStack.size}개")
        } else {
            println("📥 [PostDetail] 새로운 스택 시작")
        }

        dialogManager.printStackState()
    }

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
                // 🔥 상태 관리
                val userProfile by userProfileViewModel.userProfile.collectAsState()
                val userPosts by userProfileViewModel.userPosts.collectAsState()
                val isProfileLoading by userProfileViewModel.isLoading.collectAsState()
                val profileError by userProfileViewModel.error.collectAsState()

                var showUserProfileDialog by remember { mutableStateOf(false) }
                var selectedUserId by remember { mutableStateOf<String?>(null) }
                var currentDepth by remember { mutableStateOf(dialogManager.getCurrentDepth()) }

                /**
                 * 🎯 메인 PostDetail 화면
                 */
                PostDetailComposeScreen(
                    postId = args.postId,
                    viewModel = viewModel,
                    onBackPressed = {
                        findNavController().popBackStack()
                    },
                    onShowToast = { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    },
                    onProfileClick = { userId ->
                        handleProfileClick(userId) { canOpen ->
                            if (canOpen) {
                                selectedUserId = userId
                                userProfileViewModel.loadUserProfile(userId)
                                showUserProfileDialog = true
                                currentDepth = dialogManager.getCurrentDepth()
                            } else {
                                showProfileClickFailedMessage(userId)
                            }
                        }
                    }
                )

                /**
                 * 🏠 사용자 프로필 다이얼로그
                 */
                if (showUserProfileDialog && userProfile != null) {
                    UserProfileDialog(
                        userProfile = userProfile!!,
                        userPosts = userPosts,
                        currentDepth = currentDepth,
                        maxDepth = ProfileDialogManager.MAX_STACK_DEPTH,
                        showStackInfo = true,
                        onDismiss = {
                            dialogManager.popDialog()
                            showUserProfileDialog = false
                            selectedUserId = null
                            userProfileViewModel.clearData()
                            currentDepth = dialogManager.getCurrentDepth()
                            dialogManager.printStackState()
                        },
                        onPostClick = { postId ->
                            handlePostClickFromProfile(postId)
                        },
                        onProfileClick = { userId ->
                            handleNestedProfileClick(userId) { canOpen ->
                                if (canOpen) {
                                    // 현재 다이얼로그 닫고 새로운 프로필 열기
                                    showUserProfileDialog = false
                                    userProfileViewModel.clearData()

                                    selectedUserId = userId
                                    userProfileViewModel.loadUserProfile(userId)
                                    showUserProfileDialog = true
                                    currentDepth = dialogManager.getCurrentDepth()
                                } else {
                                    showProfileClickFailedMessage(userId)
                                }
                            }
                        },
                        onFollowClick = {
                            Toast.makeText(requireContext(), "팔로우 기능 준비중입니다", Toast.LENGTH_SHORT).show()
                        },
                        onMessageClick = {
                            Toast.makeText(requireContext(), "메시지 기능 준비중입니다", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                /**
                 * 🔄 로딩 상태 처리
                 */
                if (showUserProfileDialog && isProfileLoading) {
                    AlertDialog(
                        onDismissRequest = { /* 로딩 중에는 닫기 불가 */ },
                        title = {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("프로필 불러오는 중...")
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showUserProfileDialog = false
                                    selectedUserId = null
                                    userProfileViewModel.clearData()
                                }
                            ) {
                                Text("취소")
                            }
                        }
                    )
                }

                /**
                 * ❌ 에러 상태 처리 (수정된 버전)
                 */
                profileError?.let { error ->
                    LaunchedEffect(error) {
                        // 🔥 에러 처리를 Fragment 외부에서 할 수 없으므로
                        // SideEffect로 처리하거나 다른 방법 사용
                    }

                    // 🔥 에러 발생 시 즉시 토스트 표시 (LaunchedEffect 밖에서)
                    DisposableEffect(error) {
                        val toast = Toast.makeText(
                            requireContext(),
                            "프로필 로드 실패: $error",
                            Toast.LENGTH_LONG
                        )
                        toast.show()

                        // 상태 초기화
                        userProfileViewModel.clearError()
                        showUserProfileDialog = false
                        selectedUserId = null

                        onDispose { }
                    }
                }
            }
        }
    }

    /**
     * 🔥 프로필 클릭 처리 (최초 프로필)
     */
    private fun handleProfileClick(userId: String, callback: (Boolean) -> Unit) {
        val canOpen = dialogManager.pushDialog(userId)
        dialogManager.printStackState()
        callback(canOpen)
    }

    /**
     * 🔥 중첩 프로필 클릭 처리
     */
    private fun handleNestedProfileClick(userId: String, callback: (Boolean) -> Unit) {
        val canOpen = dialogManager.pushDialog(userId)
        dialogManager.printStackState()
        callback(canOpen)
    }

    /**
     * 🔥 프로필에서 게시글 클릭 처리 (스택과 함께 이동)
     */
    private fun handlePostClickFromProfile(postId: String) {
        // 현재 게시글과 같으면 무시 (무한 루프 방지)
        if (postId == args.postId) {
            Toast.makeText(requireContext(), "현재 보고 있는 게시글입니다", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 현재 스택 상태를 새 Fragment로 전달
            val currentStack = dialogManager.getStackAsArray()

            findNavController().navigate(
                R.id.postDetailFragment,
                bundleOf(
                    "postId" to postId,
                    "profileStack" to currentStack // 🔥 스택 상태 전달
                )
            )

            println("📤 [Navigation] 스택을 다음 PostDetail로 전달: ${currentStack.size}개")

        } catch (e: Exception) {
            println("❌ [Navigation] 게시글 이동 실패: ${e.message}")
            Toast.makeText(requireContext(), "게시글 이동 실패", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 🚨 프로필 클릭 실패 메시지 표시
     */
    private fun showProfileClickFailedMessage(userId: String) {
        val message = when {
            dialogManager.isInStack(userId) -> "이미 열린 프로필입니다"
            dialogManager.isFull() -> "프로필은 최대 ${ProfileDialogManager.MAX_STACK_DEPTH}단계까지만 열 수 있습니다"
            else -> "프로필을 열 수 없습니다"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogManager.clear() // 메모리 정리
        println("🧹 [PostDetail] DialogManager 정리 완료")
    }
}
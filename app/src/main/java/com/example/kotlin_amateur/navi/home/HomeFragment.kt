package com.example.kotlin_amateur.navi.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.adapter.HomeRecyclerViewAdapter
import com.example.kotlin_amateur.databinding.FragmentHomeBinding
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.viewmodel.HomeViewModel
import com.leinardi.android.speeddial.SpeedDialActionItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var isFabOpen = false // FAB이 열려있는 상태인지 체크

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // Hilt를 사용한 ViewModel 주입
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var adapter: HomeRecyclerViewAdapter

    private var isReturningFromOtherScreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    // HomeFragment.kt

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupSwipeRefresh()
        setupSpeedDial()
        setupScrollListener()
        observeViewModel()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = HomeRecyclerViewAdapter(
            postList = arrayListOf(),
            onItemClick = { post ->
                // 상세 페이지로 이동
                navigateToPostDetailWithAnimation(post.postId, post.postTitle)
            },
            onLikeClick = { post, position ->
                // 좋아요 처리
                handleLikeClick(post, position)
            },
            onCommentClick = { post ->
                // 댓글 화면으로 이동 또는 상세 페이지로 이동

            },
            onShareClick = { post ->
                // 공유 기능
                sharePost(post)
            },
            onBookmarkClick = { post ->
                // 북마크 기능 (추후 구현)
                Toast.makeText(context, "북마크 기능 준비중", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter

            // 아이템 간격 설정
            val itemDecoration = object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.bottom = 16.dp // 아이템 간 간격
                }
            }
            addItemDecoration(itemDecoration)
        }
    }
    /**
     * 🔥 게시글 상세 페이지로 이동하는 메서드
     * @param postId 게시글 ID (String 타입)
     * @param title 게시글 제목 (선택사항)
     */
    private fun navigateToPostDetail(postId: String, title: String? = null) {
        try {
            // Navigation Safe Args 사용
            val action = HomeFragmentDirections.actionHomeToPostDetail(
                postId = postId,
                title = title
            )
            findNavController().navigate(action)

            Log.d("HomeFragment", "✅ 게시글 상세로 이동: postId=$postId, title=$title")

        } catch (e: Exception) {
            Log.e("HomeFragment", "❌ 네비게이션 오류: ${e.message}")
            Toast.makeText(context, "페이지 이동 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * 🚀 추가 개선사항: 애니메이션과 함께 이동
     */
    private fun navigateToPostDetailWithAnimation(postId: String, title: String? = null) {
        try {
            val action = HomeFragmentDirections.actionHomeToPostDetail(postId, title)

            // 커스텀 애니메이션 설정
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.post_slide_in_right)
                .setExitAnim(R.anim.post_slide_out_left)
                .setPopEnterAnim(R.anim.post_slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()

            findNavController().navigate(action, navOptions)

        } catch (e: Exception) {
            Log.e("HomeFragment", "❌ 애니메이션 네비게이션 오류: ${e.message}")
            // 애니메이션 실패 시 기본 이동
            navigateToPostDetail(postId, title)
        }
    }
    private fun handleLikeClick(post: PostListResponse, position: Int) {
        // 방법 1: adapter.posts 사용
        val updatedPost = post.toggleLike()
        adapter.posts[position] = updatedPost
        adapter.notifyItemChanged(position)

        // 또는 방법 2: adapter 메서드 사용
        // val updatedPost = post.toggleLike()
        // adapter.updatePostAtPosition(position, updatedPost)

        // 서버에 좋아요 요청 (백그라운드)
        viewModel.toggleLike(post.postId, !post.isLikedByCurrentUser) { success ->
            if (!success) {
                // 실패 시 원상복구
                val revertedPost = updatedPost.toggleLike()
                adapter.posts[position] = revertedPost
                adapter.notifyItemChanged(position)

                // 또는 방법 2 사용시:
                // adapter.updatePostAtPosition(position, revertedPost)

                Toast.makeText(context, "좋아요 처리 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sharePost(post: PostListResponse) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${post.authorNickname}님의 게시글\n\n${post.postContent}")
        }
        startActivity(Intent.createChooser(shareIntent, "게시글 공유"))
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout = binding.swipeRefreshLayout

        // 새로고침 리스너
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    private fun setupSpeedDial() {
        // 스피드 다이얼 설정
        binding.speedDial.addActionItem(speedDialAddSetting())
        binding.speedDial.addActionItem(speedDialRegionSetting())

        binding.speedDial.setOnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_option_add -> {
                    callAddFragment()
                    true
                }

                R.id.fab_option_region -> {
                    // 지역 설정 기능 구현 예정
                    Toast.makeText(context, "지역 설정 기능 준비중", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }

    private fun observeViewModel() {
        // 데이터 리스트 관찰
        viewModel.dataList.observe(viewLifecycleOwner) { postListResponses ->
            adapter.updateList(ArrayList(postListResponses))
        }

        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    // 스피드 다이얼 세팅 시작
    private fun speedDialAddSetting(): SpeedDialActionItem {
        return SpeedDialActionItem.Builder(R.id.fab_option_add, R.drawable.baseline_add_24)
            .setLabel("글 추가하기")
            .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_green))
            .setLabelColor(ContextCompat.getColor(requireContext(), R.color.black))
            .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.white))
            .create()
    }

    private fun speedDialRegionSetting(): SpeedDialActionItem {
        return SpeedDialActionItem.Builder(R.id.fab_option_region, R.drawable.ic_location)
            .setLabel("지역 설정")
            .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setLabelColor(ContextCompat.getColor(requireContext(), R.color.black))
            .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.pastel_blue))
            .create()
    }

    // 글 추가하기
    private fun callAddFragment() {
        findNavController().navigate(R.id.addPostFragment)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    // 스크롤시 플로팅버튼 안보이게
    private fun setupScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                handleScroll(dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                handleScrollState(newState)
            }
        })
    }

    // 스크롤 중일 때 호출되는 메서드
    private fun handleScroll(dy: Int) {
        if (dy != 0 && binding.speedDial.isShown) {
            binding.speedDial.hide()
            isFabOpen = false
        }
    }

    // 스크롤 상태가 변경될 때 호출되는 메서드
    private fun handleScrollState(newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            binding.speedDial.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // dp를 px로 변환하는 확장 프로퍼티
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
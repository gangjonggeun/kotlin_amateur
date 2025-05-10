package com.example.kotlin_amateur.navi.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.adapter.CommentAdapter
import com.example.kotlin_amateur.adapter.LectureDetailimageAdapter
import com.example.kotlin_amateur.databinding.FragmentLectureDetailsBinding
import com.example.kotlin_amateur.model.DataModel
import com.example.kotlin_amateur.viewmodel.LectureDetailViewModel

class LectureDetailFragment : Fragment() {
    private var _binding: FragmentLectureDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPagerAdapter: LectureDetailimageAdapter

    private var isLiked = false // 좋아요 상태 저장용

    private lateinit var postViewModel: LectureDetailViewModel
    private lateinit var post: DataModel

    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLectureDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        post =
            LectureDetailFragmentArgs.fromBundle(requireArguments()).post //arguments data_molde 들어있는

        viewPagerAdapter = LectureDetailimageAdapter(post.images)
        binding.detailViewPager.adapter = viewPagerAdapter


        postViewModel = ViewModelProvider(this).get(LectureDetailViewModel::class.java)
        postViewModel.initLikeAndComment(post.likes, post.comments) // 하트 및 댓글 수 넣기

        setupCommentRecyclerView()
        postViewModel.loadComments(post.id) // 댓글 로딩

        setupIndicators(post.images.size)   // 이미지 개수만큼 인디케이터 세팅
        setCurrentIndicator(0)         // 초기 포지션 0 활성화


        //댓글 보내기
        binding.sendCommentButton.setOnClickListener {
            val text = binding.commentEditText.text.toString()
            if (text.isNotBlank()) {
                postViewModel.addComment(post.id, text)
                binding.commentEditText.text.clear()
            }
        }



        // ViewPager2 페이지 변경 리스너
        binding.detailViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)  // 페이지 바뀔 때 인디케이터 업데이트
            }
        })

        //백버튼 리스너
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        //  하트 클릭 리스너
        binding.likeButton.setOnClickListener {
            toggleHeart()
        }

        likesAndCommentObserve()
    }

    private fun toggleHeart() {
        if (isLiked) {
            binding.likeButton.setImageResource(R.drawable.ic_heart_empty)
            postViewModel.sendDecreaseLikeRequest(post.id)
        } else {
            binding.likeButton.setImageResource(R.drawable.ic_heart_filled)
            postViewModel.sendIncreaseLikeRequest(post.id)
        }
        isLiked = !isLiked

        // ❤️ 커졌다가 원래 크기로 줄어드는 기본 애니메이션
        binding.likeButton.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction {
                binding.likeButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .withEndAction {
                        // 🎯 여기서 흔들림 추가 (회전 왔다갔다)
                        binding.likeButton.animate()
                            .rotationBy(15f)
                            .setDuration(50)
                            .withEndAction {
                                binding.likeButton.animate()
                                    .rotationBy(-30f)
                                    .setDuration(100)
                                    .withEndAction {
                                        binding.likeButton.animate()
                                            .rotationBy(15f)
                                            .setDuration(50)
                                            .start()
                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun likesAndCommentObserve() {
        // 좋아요 수 관찰
        postViewModel.likeCount.observe(viewLifecycleOwner) { count ->
            commentAdapter.updateLikeCount(count)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //인디케이터 설정
    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0) // 점 간 간격
        }

        for (i in 0 until count) {
            indicators[i] = ImageView(requireContext()).apply {
                setImageResource(R.drawable.indicator_inactive) // 기본은 비활성 점
                this.layoutParams = layoutParams
            }
            binding.indicatorLayout.addView(indicators[i])
        }
    }//인디케이터 설정끝


    //인디케이터 현재 위치 설정
    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = binding.indicatorLayout.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageResource(R.drawable.indicator_active) // 현재 선택된 점
            } else {
                imageView.setImageResource(R.drawable.indicator_inactive)
            }
        }
    }// 인디케이터 설정끝

    private fun setupCommentRecyclerView() {
        commentAdapter = CommentAdapter(
            context = requireContext(),
            comments = emptyList(),
            userPost =  post, // ✅ 헤더용 데이터 전달
            onReplySubmit = { commentId, replyContent ->
                postViewModel.addReply(post.id, commentId, replyContent)
            }
        )

        binding.commentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.commentRecyclerView.adapter = commentAdapter

        postViewModel.comments.observe(viewLifecycleOwner) { updatedList ->
            commentAdapter.updateList(updatedList)
        }
    }

}




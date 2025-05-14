package com.example.kotlin_amateur.navi.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.adapter.HomeRecyclerViewAdapter
import com.example.kotlin_amateur.databinding.FragmentHomeBinding
import com.example.kotlin_amateur.model.PostModel
import com.example.kotlin_amateur.viewmodel.HomeViewModel
import com.leinardi.android.speeddial.SpeedDialActionItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var isFabOpen = false // FAB이 열려있는 상태인지 체크

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeRecyclerViewAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //리사이클 뷰 및 초기화
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        adapter = HomeRecyclerViewAdapter(arrayListOf()){ selectedItem ->

            val action = HomeFragmentDirections.actionHomeToLectureDetail(post = selectedItem)
            findNavController().navigate(action)
        }


        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        swipeRefreshLayout = binding.swipeRefreshLayout

        viewModel.dataList.observe(viewLifecycleOwner) { newList ->
            val recyclerList = newList.map {
                PostModel(
                    id = it.id,
                    images = it.images, // 이미지 URL (String)
                    title = it.title,
                    content = it.content,
                    likes = it.likes,
                    comments = it.comments
                )
            }

            adapter.updateList(ArrayList(recyclerList)) // 변환된 리스트를 넘김
            swipeRefreshLayout.isRefreshing = false
        } //리사이클 뷰 데이터 넣기 끝



        // 새로고침 리스너
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData() // 서버에서 최신 데이터 다시 불러오기
        }

        // 스피드 다이얼 설정
        binding.speedDial.addActionItem(
            speedDialAddSetting()
        )

        binding.speedDial.addActionItem(
            speedDialRegionSetting()
        )
        //스피드 다이얼 설정 끝

        binding.speedDial.setOnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_option_add -> {
                    // "글 추가하기" FAB 눌렀을 때 동작
                    callAddFragment()
                    true
                }
                R.id.fab_option_region -> {
                    // "지역 설정" FAB 눌렀을 때 동작
                    true
                }
                else -> false
            }
        }
        
        // 스크롤 리스너 설정 메서드 호출
        setupScrollListener()

        return binding.root
    }
    
    //스피드 다이얼 세팅 시작
   fun speedDialAddSetting(): SpeedDialActionItem? {
       return SpeedDialActionItem.Builder(R.id.fab_option_add, R.drawable.baseline_add_24)
           .setLabel("글 추가하기")
           .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_green))
           .setLabelColor(ContextCompat.getColor(requireContext(), R.color.black))
           .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.white))
           .create()
   }
    fun speedDialRegionSetting(): SpeedDialActionItem? {
        return SpeedDialActionItem.Builder(R.id.fab_option_region, R.drawable.ic_location)
            .setLabel("지역 설정")
            .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setLabelColor(ContextCompat.getColor(requireContext(), R.color.black))
            .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.pastel_blue))
            .create()
    }//스피드다이얼 세팅 끝



//FloatingAddFragment()
    //글 추가하기
    private fun callAddFragment(){
          findNavController().navigate(R.id.addPostFragment)
    }

    override fun onResume() {
        super.onResume()
        // 자동 새로고침 트리거
        swipeRefreshLayout.isRefreshing = true
        viewModel.refreshData()
    }

    //스크롤시 플로팅버튼 안보이게
    private fun setupScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                handleScroll(dy) // 스크롤 중인 경우 처리
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                handleScrollState(newState) // 스크롤 상태 변화 처리
            }
        })
    }

    // 스크롤 중일 때 호출되는 메서드
    private fun handleScroll(dy: Int) {
        if (dy != 0 && binding.speedDial.isShown) {
            binding.speedDial.hide() // 스크롤 중이면 FAB 숨기기
//            binding.fabOptionAdd.hide()
//            binding.fabOptionRegion.hide()
            isFabOpen = false
        }
    }

    // 스크롤 상태가 변경될 때 호출되는 메서드
    private fun handleScrollState(newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            binding.speedDial.show() // 스크롤이 멈추면 FAB 다시 보이기
        }
    } // 스크롤시 fab 꺼짐 끝


}//메인

package com.example.kotlin_amateur.navi.home


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlin_amateur.adapter.FloatingAddImageAdapter
import com.example.kotlin_amateur.databinding.FragmentFloatingAddBinding
import com.example.kotlin_amateur.state.SubmitState
import com.example.kotlin_amateur.viewmodel.FloatingAddViewModel

class FloatingAddFragment : Fragment() {

    private var _binding: FragmentFloatingAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageAdapter: FloatingAddImageAdapter
    private val imagesUriList = mutableListOf<Uri>()

    private val viewModel: FloatingAddViewModel by viewModels()

    // 결과 받을 런처 등록
    private val pickMultipleMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (!uris.isNullOrEmpty()) {
                Log.d("FloatingAddFragment", "선택된 이미지 수: ${uris.size}")
                imagesUriList.addAll(uris.take(6 - imagesUriList.size)) // 최대 6개 유지
                binding.cntImageTv.text = "${imagesUriList.size}/6"
                imageAdapter.notifyDataSetChanged()
            } else {
                Log.d("FloatingAddFragment", "이미지 선택 안함")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFloatingAddBinding.inflate(inflater, container, false)

        imageAdapter = FloatingAddImageAdapter(imagesUriList) { position ->
            if (position in imagesUriList.indices) {
                imagesUriList.removeAt(position)
                binding.addRecyclerView.post {
                    imageAdapter.notifyItemRemoved(position)
                    imageAdapter.notifyItemRangeChanged(position, imagesUriList.size)
                }
                binding.cntImageTv.text = "${imagesUriList.size}/6"
            }
        }

        binding.addRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.addRecyclerView.adapter = imageAdapter



        binding.submitBtn.setOnClickListener { submit() }
        binding.imageAddBtnLayout.setOnClickListener { addImage() }

        observeSubmitState()

        return binding.root
    }

    private fun submit() {
        val titleText = binding.editTextTitle.text.toString()
        val contentText = binding.editTextContent.text.toString()

        when {
            titleText.isBlank() -> Toast.makeText(
                requireContext(),
                "제목을 입력해주세요",
                Toast.LENGTH_SHORT
            ).show()

            contentText.isBlank() -> Toast.makeText(
                requireContext(),
                "내용을 입력해주세요",
                Toast.LENGTH_SHORT
            ).show()

            imagesUriList.isEmpty() -> Toast.makeText(
                requireContext(),
                "사진을 한 장 이상 선택해주세요",
                Toast.LENGTH_SHORT
            ).show()

            else -> {
             //   submitDataToServer(titleText, contentText, imagesUriList, requireContext())
                viewModel.submitPost(titleText, contentText, imagesUriList, requireContext())
                Toast.makeText(requireContext(), "입력 완료!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }


    private fun observeSubmitState() {
        viewModel.submitState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SubmitState.Loading -> {
                    // 로딩 표시
                    binding.progressBar.visibility = View.VISIBLE
                }
                is SubmitState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "업로드 성공!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is SubmitState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "에러: ${state.exception.message}", Toast.LENGTH_SHORT).show()
                }
                SubmitState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun addImage() {
        Log.d("FloatingAddFragment", "addImage() 호출됨")

        if (imagesUriList.size >= 6) {
            Toast.makeText(requireContext(), "이미지는 최대 6장까지 선택할 수 있어요.", Toast.LENGTH_SHORT).show()
            return
        }

        pickMultipleMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

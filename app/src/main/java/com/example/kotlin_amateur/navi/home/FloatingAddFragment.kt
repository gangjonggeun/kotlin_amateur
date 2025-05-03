package com.example.kotlin_amateur.navi.home


import RetrofitClient
import SubmitResponse
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlin_amateur.databinding.FragmentFloatingAddBinding
import com.example.kotlin_amateur.model.DataModel
import com.example.kotlin_amateur.util.FloatingAddImageAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FloatingAddFragment : Fragment() {

    private var _binding: FragmentFloatingAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageAdapter: FloatingAddImageAdapter
    private val imagesUriList = mutableListOf<Uri>()

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
                submitDataToServer(titleText, contentText, imagesUriList, requireContext())
                Toast.makeText(requireContext(), "입력 완료!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    //
//    private fun submitDataToServer(title: String, content: String, imageUris: List<Uri>, context: Context) {
//        val imageUrls = mutableListOf<String>()
//        val client = RetrofitClient.apiService
//        val contentResolver = context.contentResolver
//        val id = UUID.randomUUID().toString()
//
//        CoroutineScope(Dispatchers.IO).launch {
//            imageUris.forEach { uri ->
//                val inputStream = contentResolver.openInputStream(uri)
//                val file = File.createTempFile("upload", ".jpg", context.cacheDir)
//                val outputStream = FileOutputStream(file)
//                inputStream?.copyTo(outputStream)
//                inputStream?.close()
//                outputStream.close()
//
//                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
//
//                val response = client.uploadImage(body).execute()
//                if (response.isSuccessful) {
//                    val url = response.body()?.image_url ?: ""
//                    imageUrls.add(url)
//                }
//            }
//
//            val dataModel = DataModel(id = id, title = title, content = content, images = imageUrls)
//            client.submitData(dataModel).enqueue(object : Callback<SubmitResponse> {
//                override fun onResponse(call: Call<SubmitResponse>, response: Response<SubmitResponse>) {
//                    if (response.isSuccessful) {
//                        Log.d("Submit", "Success")
//                    }
//                }
//
//                override fun onFailure(call: Call<SubmitResponse>, t: Throwable) {
//                    Log.e("Submit", "Error", t)
//                }
//            })
//        }
//    }
    private fun submitDataToServer(
        title: String,
        content: String,
        imageUris: List<Uri>,
        context: Context
    ) {
        val imageUrls = mutableListOf<String>()
        val client = RetrofitClient.apiService
        val contentResolver = context.contentResolver
        val id = UUID.randomUUID().toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 이미지 업로드 (forEach 비동기 처리)
                for (uri in imageUris) {
                    val inputStream = contentResolver.openInputStream(uri)
                    val file = File.createTempFile("upload", ".jpg", context.cacheDir)
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

                    val response = client.uploadImage(body).execute()  // 여긴 여전히 Call이라 유지 가능
                    if (response.isSuccessful) {
                        val url = response.body()?.image_url ?: ""
                        imageUrls.add(url)
                    }
                }

                // 서버에 전체 데이터 전송
                val dataModel =
                    DataModel(id = id, title = title, content = content, images = imageUrls)
                val submitResponse = client.submitData(dataModel) // ✅ suspend fun 이므로 바로 호출

                if (submitResponse.isSuccessful) {
                    Log.d("Submit", "Success")
                } else {
                    Log.e("Submit", "서버 응답 실패: ${submitResponse.code()}")
                }

            } catch (e: Exception) {
                Log.e("Submit", "예외 발생", e)
            }
        }
    } // submitData 끝

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

package com.example.kotlin_amateur.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.model.DataModel
import com.example.kotlin_amateur.repository.FloatingAddRepository
import com.example.kotlin_amateur.state.SubmitState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
@HiltViewModel
class FloatingAddViewModel @Inject constructor(
    private val postRepository: FloatingAddRepository
) : ViewModel() {

    private val _submitState = MutableLiveData<SubmitState>(SubmitState.Idle)
    val submitState: LiveData<SubmitState> get() = _submitState

    fun submitPost(title: String, content: String, imageUris: List<Uri>, context: Context) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                val imageUrls = postRepository.uploadImages(imageUris, context)
                val dataModel = DataModel(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    images = imageUrls
                )
                val response = postRepository.submitPost(dataModel)
                if (response.isSuccessful) {
                    _submitState.value = SubmitState.Success
                } else {
                    _submitState.value = SubmitState.Error(Exception("서버 응답 실패"))
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e)
            }
        }
    }
}


/*
* private fun submitPost(
        title: String,
        content: String,
        imageUris: List<Uri>,
        context: Context
    ) {
        val imageUrls = mutableListOf<String>()
        val client = apiService
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
                    DataModel(id = id, title = title, content = content, images = imageUrls )
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
    * */
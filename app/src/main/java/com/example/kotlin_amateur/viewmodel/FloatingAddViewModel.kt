package com.example.kotlin_amateur.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.core.util.toMultipart
import com.example.kotlin_amateur.remote.request.PostRequest
import com.example.kotlin_amateur.repository.FloatingAddRepository
import com.example.kotlin_amateur.state.SubmitResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FloatingAddViewModel @Inject constructor(
    private val repository: FloatingAddRepository,
    private val application: Application
) : ViewModel() {

    val title = MutableLiveData<String>()
    val content = MutableLiveData<String>()
    val imageUriList = MutableLiveData<List<Uri>>()

    private val _submitResult = MutableLiveData<SubmitResult>()
    val submitResult: LiveData<SubmitResult> get() = _submitResult

    fun submitPost() {
        viewModelScope.launch {
            _submitResult.value = SubmitResult.Loading

            try {
                // 1. URI → Multipart 변환
                val parts = imageUriList.value.orEmpty().mapNotNull {
                    it.toMultipart(application.applicationContext)
                }
                val accessToken = TokenStore.getAccessToken(application.applicationContext)

                if (accessToken == null) {
                    _submitResult.value = SubmitResult.Failure(Exception("AccessToken is null"))
                    return@launch
                }


                Log.d("Acces Token: FloatingAdd","$accessToken")

                // 2. 이미지 먼저 서버로 전송 → URL 리스트 응답 받기
                val imageUrls = repository.uploadImages(accessToken, parts)

                // 3. PostRequest 생성
                val request = PostRequest(
                    postTitle = title.value.orEmpty(),
                    postContent = content.value.orEmpty(),
                    postImageUrls = imageUrls
                )

                // 4. 서버에 게시글 전송
                val response = repository.uploadPost(accessToken, request)

                if (response.isSuccessful) {
                    Log.e("post data to server?","upload!")
                    _submitResult.value = SubmitResult.Success
                } else {

                    Log.e("post data to server?","not upload")
                    _submitResult.value = SubmitResult.Failure(Exception("업로드 실패: ${response.code()}"))
                }

            } catch (e: Exception) {
                Log.e("post data to server?","${e.toString()}")
                _submitResult.value = SubmitResult.Failure(e)
            }
        }
    }

}
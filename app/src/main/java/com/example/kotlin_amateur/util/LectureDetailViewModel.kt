package com.example.kotlin_amateur.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LectureDetailViewModel : ViewModel() {

    private val _likeCount = MutableLiveData<Int>(0) // 초기값 0
    val likeCount: LiveData<Int> get() = _likeCount

    private val _commentCount = MutableLiveData<Int>(0)
    val commentCount: LiveData<Int> get() = _commentCount

    //좋아요 및 해제 서버 전송
   fun sendIncreaseLikeRequest(postId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.increaseLikes(mapOf("id" to postId))
                if (response.isSuccessful) {
                    Log.d("Retrofit", "좋아요 증가 성공")
                    _likeCount.value = (_likeCount.value ?: 0) + 1
                } else {
                    Log.e("Retrofit", "좋아요 증가 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "네트워크 오류: ${e.message}")
            }
        }
    }

    fun sendDecreaseLikeRequest(postId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.decreaseLikes(mapOf("id" to postId))
                if (response.isSuccessful) {
                    Log.d("Retrofit", "좋아요 감소 성공")
                    _likeCount.value = (_likeCount.value ?: 0) - 1
                } else {
                    Log.e("Retrofit", "좋아요 감소 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "네트워크 오류: ${e.message}")
            }
        }
    }
}
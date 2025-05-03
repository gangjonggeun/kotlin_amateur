package com.example.kotlin_amateur.util

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.model.CommentModel
import com.example.kotlin_amateur.model.DataModel
import com.example.kotlin_amateur.model.ReplyModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale
import java.util.UUID

class LectureDetailViewModel : ViewModel() {

    private val _likeCount = MutableLiveData<Int>(0) // 초기값 0
    val likeCount: LiveData<Int> get() = _likeCount
    private val _commentCount = MutableLiveData<Int>(0)
    val commentCount: LiveData<Int> get() = _commentCount

    private val _comments = MutableLiveData<List<CommentModel>>()
    val comments: LiveData<List<CommentModel>> get() = _comments


    fun initLikeAndComment(likes: Int, comments: Int) {
        _likeCount.value = likes
        _commentCount.value = comments
    }

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

    fun loadComments(postId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getData()
                if (response.isSuccessful) {
                    val post = response.body()?.find { it.id == postId }
                    _comments.value = post?.commentList?.sortedByDescending { it.commentTimestamp } ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("댓글 불러오기 실패", e.toString())
            }
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            try {
                val newComment = CommentModel(
                    commentId = UUID.randomUUID().toString(),
                    commentContent = content,
                    commentTimestamp = getCurrentTime(),
                    replies = emptyList()
                )

                val response = RetrofitClient.apiService.addComment(
                    mapOf(
                        "postId" to postId,
                        "commentContent" to content,
                        "commentTimestamp" to newComment.commentTimestamp
                    )
                )

                if (response.isSuccessful) {
                    val current = _comments.value ?: emptyList()
                    _comments.value = listOf(newComment) + current
                }
            } catch (e: Exception) {
                Log.e("댓글 등록 실패", e.toString())
            }
        }
    }

    fun addReply(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            try {
                val newReply = ReplyModel(
                    replyId = UUID.randomUUID().toString(),
                    replyContent = content,
                    replyTimestamp = getCurrentTime()
                )
                Log.d("Reply", "서버에 요청 보냄 시작")
                val response = RetrofitClient.apiService.addReply(
                    mapOf(
                        "postId" to postId,
                        "commentId" to commentId,
                        "replyContent" to content,
                        "replyTimestamp" to newReply.replyTimestamp
                    )
                )
                Log.d("Reply", "응답: ${response.code()}")
                if (response.isSuccessful) {
                    Log.d("Reply", "성공")
                    // 기존 댓글 리스트에서 해당 댓글 찾아서 replies 업데이트
                    val updated = _comments.value?.map { comment ->
                        if (comment.commentId == commentId) {
                            comment.copy(replies = comment.replies + newReply)
                        } else comment
                    } ?: emptyList()

                    _comments.value = updated
                }
            } catch (e: Exception) {
                Log.e("답글 등록 실패", e.toString())
            }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }
}
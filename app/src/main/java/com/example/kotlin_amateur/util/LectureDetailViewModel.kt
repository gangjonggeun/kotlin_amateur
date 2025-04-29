package com.example.kotlin_amateur.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LectureDetailViewModel : ViewModel() {

    private val _likeCount = MutableLiveData<Int>(0) // 초기값 0
    val likeCount: LiveData<Int> get() = _likeCount

    private val _commentCount = MutableLiveData<Int>(0)
    val commentCount: LiveData<Int> get() = _commentCount

    // 좋아요 수 증가
    fun increaseLike() {
        _likeCount.value = (_likeCount.value ?: 0) + 1
    }

    // 좋아요 수 감소
    fun decreaseLike() {
        _likeCount.value = (_likeCount.value ?: 0) - 1
    }

    // 댓글 수 증가
    fun increaseComment() {
        _commentCount.value = (_commentCount.value ?: 0) + 1
    }
}
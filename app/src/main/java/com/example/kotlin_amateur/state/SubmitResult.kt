package com.example.kotlin_amateur.state

sealed class SubmitResult {
    object Idle : SubmitResult()
    object Loading : SubmitResult()
    object Success : SubmitResult()
    data class Error(val exception: Throwable) : SubmitResult()
}


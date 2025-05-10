package com.example.kotlin_amateur.state

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val exception: Throwable) : SubmitState()
}


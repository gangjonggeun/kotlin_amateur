package com.example.kotlin_amateur.state

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val code: Int, val message: String) : ApiResult<T>()
    data class Loading<T>(val message: String = "로딩 중...") : ApiResult<T>()
}

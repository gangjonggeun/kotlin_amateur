package com.example.kotlin_amateur.exception

import com.example.kotlin_amateur.state.ApiResult


suspend inline fun <T> safeApiCall(
    crossinline apiCall: suspend () -> T
): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: retrofit2.HttpException) {
        ApiResult.Error(e.code(), e.message ?: "네트워크 오류")
    } catch (e: java.io.IOException) {
        ApiResult.Error(0, "인터넷 연결을 확인해주세요")
    } catch (e: Exception) {
        ApiResult.Error(-1, e.message ?: "알 수 없는 오류")
    }
}

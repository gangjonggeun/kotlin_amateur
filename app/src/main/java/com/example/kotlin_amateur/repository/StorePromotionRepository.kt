package com.example.kotlin_amateur.repository

import android.content.Context
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.remote.api.StorePromotionApi
import com.example.kotlin_amateur.remote.request.StorePromotionRequest
import com.example.kotlin_amateur.remote.response.StorePromotionResponse
import com.example.kotlin_amateur.remote.response.ApiResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🏪 스토어 프로모션 리포지토리 (메모리 최적화)
 *
 * 📌 메모리 안전 원칙:
 * - ApiResult 패턴 사용 (Exception 대신)
 * - 토큰 자동 관리 (TokenStore Object 활용)
 * - 코루틴으로 비동기 처리
 * - 간단한 영어 변환 (보낼 때만)
 */
@Singleton
class StorePromotionRepository @Inject constructor(
    private val storePromotionApi: StorePromotionApi,
    @ApplicationContext private val context: Context
) {

    /**
     * 🏪 가게 홍보 등록
     */
    suspend fun submitStorePromotion(request: StorePromotionRequest): ApiResult<StorePromotionResponse> {
        return try {
            // 🔐 토큰 가져오기
            val token = TokenStore.getAccessToken(context)
                ?: return ApiResult.Error(401, "로그인이 필요합니다")

            // 📝 로그와 함께 API 호출
            println("📝 [리포지토리] 전송 데이터: $request")
            val response = storePromotionApi.createStorePromotion("Bearer $token", request)
            
            val result = handleApiResponse(response)
            if (result is ApiResult.Success) {
                println("📝 [리포지토리] 서버 응답: ${result.data}")
            }
            result

        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * 📍 근처 가게 검색
     */
    suspend fun getNearbyStores(
        lat: Double,
        lng: Double,
        radius: Double = 5.0
    ): ApiResult<List<StorePromotionResponse>> {
        return try {
            val token = TokenStore.getAccessToken(context)
                ?: return ApiResult.Error(401, "로그인이 필요합니다")

            val response = storePromotionApi.getNearbyStores("Bearer $token", lat, lng, radius)
            handleApiResponse(response)

        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * 🔍 가게 검색
     */
    suspend fun searchStores(keyword: String): ApiResult<List<StorePromotionResponse>> {
        return try {
            val token = TokenStore.getAccessToken(context)
                ?: return ApiResult.Error(401, "로그인이 필요합니다")

            val response = storePromotionApi.searchStores("Bearer $token", keyword.trim())
            handleApiResponse(response)

        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * 👤 내 가게 목록
     */
    suspend fun getMyStores(): ApiResult<List<StorePromotionResponse>> {
        return try {
            val token = TokenStore.getAccessToken(context)
                ?: return ApiResult.Error(401, "로그인이 필요합니다")

            val response = storePromotionApi.getMyStores("Bearer $token")
            handleApiResponse(response)

        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * 🗑️ 가게 삭제
     */
    suspend fun deleteStorePromotion(storeId: Long): ApiResult<Boolean> {
        return try {
            val token = TokenStore.getAccessToken(context)
                ?: return ApiResult.Error(401, "로그인이 필요합니다")

            val response = storePromotionApi.deleteStorePromotion("Bearer $token", storeId)
            handleApiResponse(response)

        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * 🏪 가게 타입 목록
     */
    suspend fun getStoreTypes(): ApiResult<List<String>> {
        return try {
            val token = TokenStore.getAccessToken(context)
                ?: return ApiResult.Error(401, "로그인이 필요합니다")

            val response = storePromotionApi.getStoreTypes("Bearer $token")
            handleApiResponse(response)

        } catch (e: Exception) {
            handleException(e)
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * 🌐 API 응답 처리 (제네릭)
     */
    private fun <T> handleApiResponse(response: Response<ApiResponse<T>>): ApiResult<T> {
        return when {
            response.isSuccessful -> {
                val apiResponse = response.body()
                when {
                    apiResponse == null -> ApiResult.Error(500, "응답 데이터가 없습니다")
                    apiResponse.success && apiResponse.data != null -> ApiResult.Success(apiResponse.data)
                    else -> ApiResult.Error(400, apiResponse.message ?: "요청 처리에 실패했습니다")
                }
            }
            response.code() == 401 -> ApiResult.Error(401, "로그인이 만료되었습니다")
            response.code() == 403 -> ApiResult.Error(403, "권한이 없습니다")
            response.code() == 404 -> ApiResult.Error(404, "요청한 데이터를 찾을 수 없습니다")
            response.code() in 500..599 -> ApiResult.Error(500, "서버에 일시적인 문제가 발생했습니다")
            else -> ApiResult.Error(response.code(), "네트워크 오류가 발생했습니다")
        }
    }

    /**
     * ⚠️ Exception 처리 (메모리 안전)
     */
    private fun <T> handleException(e: Exception): ApiResult<T> {
        val errorMessage = when {
            e.message?.contains("network", ignoreCase = true) == true -> "네트워크 연결을 확인해주세요"
            e.message?.contains("timeout", ignoreCase = true) == true -> "요청 시간이 초과되었습니다"
            e.message?.contains("host", ignoreCase = true) == true -> "서버에 연결할 수 없습니다"
            else -> "요청 처리 중 오류가 발생했습니다"
        }

        return ApiResult.Error(0, errorMessage)
    }
}

/**
 * 🎯 API 결과 처리용 Sealed Class (기존 스타일 유지)
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val code: Int, val message: String) : ApiResult<T>()
    data class Loading<T>(val message: String = "로딩 중...") : ApiResult<T>()
}

/**
 * 🛠️ ApiResult 확장 함수들
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (Int, String) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(code, message)
    return this
}

fun <T> ApiResult<T>.isSuccess(): Boolean = this is ApiResult.Success
fun <T> ApiResult<T>.isError(): Boolean = this is ApiResult.Error
fun <T> ApiResult<T>.isLoading(): Boolean = this is ApiResult.Loading
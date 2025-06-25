package com.example.kotlin_amateur.remote.api

import com.example.kotlin_amateur.remote.request.StorePromotionRequest
import com.example.kotlin_amateur.remote.response.StorePromotionResponse
import com.example.kotlin_amateur.remote.response.ApiResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 🏪 스토어 프로모션 API 인터페이스 (간소화)
 *
 * 📌 핵심 원칙:
 * - Authorization 헤더 포함 (JWT 토큰)
 * - 단일 응답 타입 (StorePromotionResponse) 사용
 * - 반경 내 모든 데이터 한 번에 전송
 * - 클라이언트에서 필터링/정렬 처리
 * - 불필요한 페이징 제거
 */
interface StorePromotionApi {

    /**
     * 🏪 가게 홍보 등록
     *
     * POST /api/store-promotions
     */
    @POST("api/store-promotions")
    suspend fun createStorePromotion(
        @Header("Authorization") accessToken: String,
        @Body request: StorePromotionRequest
    ): Response<ApiResponse<StorePromotionResponse>>

    /**
     * 📍 근처 가게 전체 검색 (반경 내 모든 데이터)
     *
     * GET /api/store-promotions/nearby
     *
     * @param accessToken JWT 토큰
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @param radius 검색 반경 (km, 기본값: 5, 최대: 50)
     * @return 반경 내 모든 가게 목록
     */
    @GET("api/store-promotions/nearby")
    suspend fun getNearbyStores(
        @Header("Authorization") accessToken: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double = 5.0
    ): Response<ApiResponse<List<StorePromotionResponse>>>

    /**
     * 🔍 가게 이름으로 검색 (전체 검색)
     *
     * GET /api/store-promotions/search
     */
    @GET("api/store-promotions/search")
    suspend fun searchStores(
        @Header("Authorization") accessToken: String,
        @Query("keyword") keyword: String
    ): Response<ApiResponse<List<StorePromotionResponse>>>

    /**
     * 👤 내가 등록한 가게 목록 (전체)
     *
     * GET /api/store-promotions/my
     */
    @GET("api/store-promotions/my")
    suspend fun getMyStores(
        @Header("Authorization") accessToken: String
    ): Response<ApiResponse<List<StorePromotionResponse>>>

    /**
     * 🗑️ 가게 홍보 삭제
     *
     * DELETE /api/store-promotions/{storeId}
     */
    @DELETE("api/store-promotions/{storeId}")
    suspend fun deleteStorePromotion(
        @Header("Authorization") accessToken: String,
        @Path("storeId") storeId: Long
    ): Response<ApiResponse<Boolean>>

    /**
     * 🏪 가게 타입 목록 조회
     *
     * GET /api/store-promotions/types
     */
    @GET("api/store-promotions/types")
    suspend fun getStoreTypes(
        @Header("Authorization") accessToken: String
    ): Response<ApiResponse<List<String>>>
}
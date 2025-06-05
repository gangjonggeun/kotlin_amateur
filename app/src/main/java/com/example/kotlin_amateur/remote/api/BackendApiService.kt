package com.example.kotlin_amateur.remote.api

import com.example.kotlin_amateur.remote.request.IdTokenRequest
import com.example.kotlin_amateur.remote.response.LoginResponse
import com.example.kotlin_amateur.remote.response.PostResponse
import com.example.kotlin_amateur.remote.response.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
interface BackendApiService {


    @GET("/users/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @Multipart
    @PATCH("/users/setup-profile")
    suspend fun setupProfile(
        @Header("Authorization") token: String,
        @Part profileImage: MultipartBody.Part?,
        @Part("nickname") nickname: RequestBody
    ): ProfileResponse

    // ✅ 회원가입 요청
    @POST("/auth/register")
    suspend fun registerWithGoogle(
        @Body request: IdTokenRequest
    ): Response<LoginResponse>

    // ✅ 로그인 요청
    @POST("/auth/login")
    suspend fun loginWithGoogle(
        @Body request: IdTokenRequest
    ): Response<LoginResponse>
}


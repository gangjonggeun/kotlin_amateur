package com.example.kotlin_amateur.remote.api

import com.example.kotlin_amateur.remote.response.AccessTokenResponse
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

interface ReissueApi {
    @POST("/auth/reissue")
    fun reissue(@Header("Authorization") refreshToken: String): Call<AccessTokenResponse>
}

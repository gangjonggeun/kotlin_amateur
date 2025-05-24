package com.example.kotlin_amateur.remote.api

import com.example.kotlin_amateur.model.PostModel
import com.example.kotlin_amateur.remote.request.IdTokenRequest
import com.example.kotlin_amateur.remote.response.LoginResponse
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

    @GET("/getdata")
    suspend fun getData(): Response<List<PostModel>> // ✅ 핵심 수정



    @POST("/increase_likes")
    suspend fun increaseLikes(@Body body: Map<String, String>): Response<Unit>

    @POST("/decrease_likes")
    suspend fun decreaseLikes(@Body body: Map<String, String>): Response<Unit>

    @POST("/add_comment")
    suspend fun addComment(@Body body: Map<String, String>): Response<Unit>

    @POST("/add_reply")
    suspend fun addReply(@Body body: Map<String, String>): Response<Unit>

    @Multipart
    @PATCH("/users/setup-profile")
    suspend fun setupProfile(
        @Header("Authorization") token: String,
        @Part profileImage: MultipartBody.Part?,
        @Part("nickname") nickname: RequestBody
    ): Response<Unit>
    @POST("/auth/google/login-test-users")
    suspend fun getTestUserList( @Body request: IdTokenRequest): Response<List<LoginResponse>>
    @POST("/auth/google/loginOrRegister")
    suspend fun loginOrRegisterWithGoogle( @Body request: IdTokenRequest): Response<LoginResponse>

}



package com.example.kotlin_amateur.network

import com.example.kotlin_amateur.model.DataModel
import com.example.kotlin_amateur.model.LoginRequest
import com.example.kotlin_amateur.model.LoginResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import javax.inject.Singleton

interface BackendApiService {
    @POST("/submit")
    suspend fun submitData(@Body data: DataModel): Response<SubmitResponse> // ✅ 수정

    @GET("/getdata")
    suspend fun getData(): Response<List<DataModel>> // ✅ 핵심 수정

    @Multipart
    @POST("/upload_image")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ImageUploadResponse>

    @POST("/increase_likes")
    suspend fun increaseLikes(@Body body: Map<String, String>): Response<Unit>

    @POST("/decrease_likes")
    suspend fun decreaseLikes(@Body body: Map<String, String>): Response<Unit>

    @POST("/add_comment")
    suspend fun addComment(@Body body: Map<String, String>): Response<Unit>

    @POST("/add_reply")
    suspend fun addReply(@Body body: Map<String, String>): Response<Unit>

    @POST("/auth/google")
    suspend fun loginWithGoogle(@Body request: LoginRequest): Response<LoginResponse>
}

data class ImageUploadResponse(
    val image_url: String
)


data class SubmitResponse(
    val success: Boolean,
    val message: String
)

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "http://192.168.219.103:5000/"  //내 컴퓨터 서버 주소
    private const val EMUL_URL = "http://10.0.2.2:5000/"  // 에뮬 주소

    private const val SPRING_URL = "http://192.168.219.103:8080/" // SSL X 임시 주소


    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(SPRING_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): BackendApiService =
        retrofit.create(BackendApiService::class.java)
}

//
//object RetrofitClient {
//    private const val BASE_URL = "http://192.168.219.103:5000/"  //내 컴퓨터 서버 주소
//    private const val EMUL_URL = "http://10.0.2.2:5000/"  // 에뮬 주소
//
//    private const val SPRING_URL = "http://192.168.219.103:8080/" // SSL X 임시 주소
//
//
//    val retrofit: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(SPRING_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    val apiService: ApiService by lazy {
//        retrofit.create(ApiService::class.java)
//    }
//}
package com.example.kotlin_amateur.remote.api

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val EMUL_URL = "http://10.0.2.2:8080/"  // 에뮬 주소
    private const val SPRING_URL = "http://192.168.219.103:8080/" // SSL  임시 주소 "https://192.168.219.103/"
    private const val NIGIX_EMUL_URL = "http://10.0.2.2/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 🔧 상세 로깅
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // 🔧 에뮬레이터용 관대한 타임아웃
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            // 🔧 디버깅용 인터셉터 추가
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d("OkHttp", "🔗 요청 URL: ${request.url}")
                Log.d("OkHttp", "🔗 요청 메서드: ${request.method}")

                try {
                    val response = chain.proceed(request)
                    Log.d("OkHttp", "✅ 응답 코드: ${response.code}")
                    response
                } catch (e: Exception) {
                    Log.e("OkHttp", "❌ 네트워크 오류: ${e.message}", e)
                    throw e
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = ApiConstants.SPRING_URL
        Log.d("NetworkModule", "🌐 Retrofit Base URL: $baseUrl")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): BackendApiService =
        retrofit.create(BackendApiService::class.java)


    @Provides
    @Singleton
    fun providePostApiService(retrofit: Retrofit): PostApiService {
        return retrofit.create(PostApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReissueApi(retrofit: Retrofit): ReissueApi {
        return retrofit.create(ReissueApi::class.java)
    }

    @Provides
    @Singleton
    fun providePostDetailApiService(retrofit: Retrofit): PostDetailApiService {
        return retrofit.create(PostDetailApiService::class.java)
    }

    // ✅ 추가: UserProfileApiService 제공
    @Provides
    @Singleton
    fun provideUserProfileApiService(retrofit: Retrofit): UserProfileApiService {
        return retrofit.create(UserProfileApiService::class.java)
    }
}

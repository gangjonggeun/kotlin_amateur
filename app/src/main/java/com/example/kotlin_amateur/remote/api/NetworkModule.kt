package com.example.kotlin_amateur.remote.api

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
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


        return  OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(
                ConnectionPool(
                maxIdleConnections = 2, // 🔥 5 → 2로 더 제한
                keepAliveDuration = 1, // 🔥 5분 → 1분으로 단축
                TimeUnit.MINUTES
            )
            )
            .retryOnConnectionFailure(false) // 🔥 재시도 비활성화로 메모리 절약
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

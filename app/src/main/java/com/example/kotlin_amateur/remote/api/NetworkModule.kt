package com.example.kotlin_amateur.remote.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "http://192.168.219.103:5000/"  //내 컴퓨터 서버 주소
    private const val EMUL_URL = "http://10.0.2.2:8080/"  // 에뮬 주소
    private const val SPRING_URL = "http://192.168.219.103:8080/" // SSL  임시 주소 "https://192.168.219.103/"
    private const val NIGIX_EMUL_URL = "http://10.0.2.2/"
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
}

package com.example.kotlin_amateur.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://192.168.219.103/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

//
//suspend fun logout(context: Context) {
//    TokenStore.clear(context)
//    // 이후 로그인 화면으로 이동 등 추가 처리
//}
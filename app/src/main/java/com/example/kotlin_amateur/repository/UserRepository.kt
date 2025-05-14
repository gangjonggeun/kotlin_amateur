package com.example.kotlin_amateur.repository

import com.example.kotlin_amateur.remote.api.BackendApiService
import com.example.kotlin_amateur.remote.request.SetupProfileRequest
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: BackendApiService
) {
    suspend fun setupProfile(accessToken: String,request: SetupProfileRequest) {
        api.setupProfile("Bearer $accessToken",request)
    }
}
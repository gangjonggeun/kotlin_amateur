package com.example.kotlin_amateur.core.util

import com.example.kotlin_amateur.model.UserInfo
import javax.inject.Qualifier


object UserSessionManager {
    private var userInfo: UserInfo? = null

    fun setUserInfo(info: UserInfo) {
        userInfo = info
    }
    fun setUserInfo(nickname: String, profileImageUrl: String?) {
        userInfo = UserInfo(nickname, profileImageUrl)
    }
    fun getUserInfo(): UserInfo? = userInfo

    fun clear() {
        userInfo = null
    }
}
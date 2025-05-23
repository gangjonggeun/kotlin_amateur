package com.example.kotlin_amateur.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



data class UserInfo(
    val accessToken: String,
    val email: String,
    val name: String,
    val googleSub: String,
    val nickname: String = "",
    val tag: String = "",
    val profileImageUrl: String? = null
)


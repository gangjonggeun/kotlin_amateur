package com.example.kotlin_amateur.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



data class UserInfo(
    val nickname: String,
    val profileImageUrl: String? = null
)


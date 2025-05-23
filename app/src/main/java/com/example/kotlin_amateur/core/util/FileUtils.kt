package com.example.kotlin_amateur.core.util

import android.content.Context
import android.net.Uri
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileOutputStream

fun Uri.toMultipart(context: Context): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(this)
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        inputStream?.use { it.copyTo(FileOutputStream(file)) }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("images", file.name, requestFile)
    } catch (e: Exception) {
        null
    }
}
package com.example.kotlin_amateur.core.util

import android.content.Context
import android.net.Uri
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileOutputStream

fun Uri.toMultipart(
    context: Context,
    fieldName: String = "images" // 기본값 지정
): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(this)
        val fileExtension = when (mimeType) {
            "image/png" -> ".png"
            "image/jpeg" -> ".jpg"
            else -> ".jpg"
        }

        val inputStream = contentResolver.openInputStream(this)
        val file = File.createTempFile("upload_", fileExtension, context.cacheDir)
        inputStream?.use { it.copyTo(FileOutputStream(file)) }

        val requestFile = file.asRequestBody(mimeType?.toMediaTypeOrNull() ?: "image/*".toMediaTypeOrNull())
        MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
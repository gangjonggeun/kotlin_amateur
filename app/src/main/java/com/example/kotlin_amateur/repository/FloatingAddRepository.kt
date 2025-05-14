package com.example.kotlin_amateur.repository


import android.content.Context
import android.net.Uri
import com.example.kotlin_amateur.model.PostModel
import com.example.kotlin_amateur.remote.api.BackendApiService
import com.example.kotlin_amateur.remote.api.SubmitResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FloatingAddRepository @Inject constructor(
    private val apiService: BackendApiService
)  {
    suspend fun uploadImages(imageUris: List<Uri>, context: Context): List<String> {
        val contentResolver = context.contentResolver
        val imageUrls = mutableListOf<String>()

        for (uri in imageUris) {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File.createTempFile("upload", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val response = apiService.uploadImage(body).execute()
            if (response.isSuccessful) {
                imageUrls.add(response.body()?.image_url ?: "")
            }
        }

        return imageUrls
    }

    suspend fun submitPost(postModel: PostModel):Response<SubmitResponse> {
        return apiService.submitData(postModel)
    }
}

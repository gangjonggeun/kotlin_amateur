import com.example.kotlin_amateur.model.DataModel
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {
    @POST("/submit")
    fun submitData(@Body data: DataModel): Call<SubmitResponse>

    @GET("/getdata")
    fun getData(): Call<List<DataModel>>

    @Multipart
    @POST("/upload_image")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ImageUploadResponse>

    @POST("/increase_likes")
    suspend fun increaseLikes(@Body body: Map<String, String>): Response<Unit>

    @POST("/decrease_likes")
    suspend fun decreaseLikes(@Body body: Map<String, String>): Response<Unit>
}

data class ImageUploadResponse(
    val image_url: String
)


data class SubmitResponse(
    val success: Boolean,
    val message: String
)



object RetrofitClient {
    private const val BASE_URL = "http://192.168.219.103:5000/"  //내 컴퓨터 서버 주소
    private const val EMUL_URL = "http://10.0.2.2:5000/"  // 에뮬 주소
    
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
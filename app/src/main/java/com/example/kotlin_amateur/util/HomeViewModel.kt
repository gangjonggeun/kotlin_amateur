package com.example.kotlin_amateur.util

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.kotlin_amateur.model.DataModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeViewModel(): ViewModel() {

    private val _dataList = MutableLiveData<List<DataModel>>()
    val dataList: LiveData<List<DataModel>> get() = _dataList

    init {
       // loadDummyData()
        loadDataFromServer()
    }


    fun loadDataFromServer() {
        RetrofitClient.apiService.getData().enqueue(object : Callback<List<DataModel>> {
            override fun onResponse(call: Call<List<DataModel>>, response: Response<List<DataModel>>) {
                if (response.isSuccessful) {
                    _dataList.value = response.body() ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<DataModel>>, t: Throwable) {
                // 실패 시 로그 출력 또는 처리
            }
        })
    }

    fun refreshData() {
        loadDataFromServer()
    }

    private fun loadDummyData() {
        val dummyData = listOf(
            DataModel(
                title = "test",
                content = "1번입니다.",
                images = listOf("img"), // 임시 샘플 이미지
                likes = 10,
                comments = 3
            )
        )

        _dataList.value = dummyData
    }
}
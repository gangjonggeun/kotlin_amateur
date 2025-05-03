package com.example.kotlin_amateur.util

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.model.DataModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeViewModel() : ViewModel() {

    private val _dataList = MutableLiveData<List<DataModel>>()
    val dataList: LiveData<List<DataModel>> get() = _dataList

    init {
        // loadDummyData()
        loadDataFromServer()
    }


    //    fun loadDataFromServer() {
//        RetrofitClient.apiService.getData().enqueue(object : Callback<List<DataModel>> {
//            override fun onResponse(call: Call<List<DataModel>>, response: Response<List<DataModel>>) {
//                if (response.isSuccessful) {
//                    _dataList.value = response.body() ?: emptyList()
//                }
//            }
//
//            override fun onFailure(call: Call<List<DataModel>>, t: Throwable) {
//                // 실패 시 로그 출력 또는 처리
//            }
//        })
//    }
    fun loadDataFromServer() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getData()

                if (response.isSuccessful) {
                    _dataList.value = response.body() ?: emptyList()
                } else {
                    Log.e("HomeViewModel", "서버 응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "네트워크 오류 발생", e)
            }
        }
    }

    fun refreshData() {
        loadDataFromServer()
    }


}
package com.example.kotlin_amateur.repository

import android.content.Context
import android.util.Log
import com.example.kotlin_amateur.model.SearchHistory
import com.example.kotlin_amateur.repository.local.AppDatabase
import com.example.kotlin_amateur.repository.local.SearchHistoryDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val database = AppDatabase.getDatabase(context)
    private val searchHistoryDao: SearchHistoryDao = database.searchHistoryDao()
    
    /**
     * 🔍 최근 검색어 5개 조회
     */
    fun getRecentSearches(): Flow<List<SearchHistory>> {
        return searchHistoryDao.getRecentSearches()
    }
    
    /**
     * ✅ 검색어 저장 (메모리 안전 처리)
     */
    suspend fun saveSearch(query: String) {
        try {
            if (query.isBlank()) return
            
            Log.d("SearchHistory", "🔍 검색어 저장: '$query'")
            
            // 기존 검색어 확인
            val existingSearch = searchHistoryDao.findByQuery(query.trim())
            
            if (existingSearch != null) {
                // 기존 검색어가 있으면 시간만 업데이트
                val updatedSearch = existingSearch.copy(searchedAt = LocalDateTime.now())
                searchHistoryDao.insertSearch(updatedSearch)
                Log.d("SearchHistory", "✅ 기존 검색어 시간 업데이트")
            } else {
                // 새로운 검색어 저장
                val newSearch = SearchHistory(query = query.trim())
                searchHistoryDao.insertSearch(newSearch)
                Log.d("SearchHistory", "✅ 새로운 검색어 저장")
            }
            
            // 5개 초과 시 오래된 기록 삭제
            searchHistoryDao.keepOnlyRecentFive()
            
        } catch (e: Exception) {
            Log.e("SearchHistory", "❌ 검색어 저장 실패: ${e.message}")
        }
    }
    
    /**
     * ❌ 특정 검색어 삭제
     */
    suspend fun deleteSearch(query: String) {
        try {
            searchHistoryDao.deleteByQuery(query)
            Log.d("SearchHistory", "🗑️ 검색어 삭제: '$query'")
        } catch (e: Exception) {
            Log.e("SearchHistory", "❌ 검색어 삭제 실패: ${e.message}")
        }
    }
    
    /**
     * 🧹 모든 검색 기록 삭제
     */
    suspend fun clearAllHistory() {
        try {
            searchHistoryDao.clearAllSearchHistory()
            Log.d("SearchHistory", "🧹 모든 검색 기록 삭제")
        } catch (e: Exception) {
            Log.e("SearchHistory", "❌ 검색 기록 삭제 실패: ${e.message}")
        }
    }
}

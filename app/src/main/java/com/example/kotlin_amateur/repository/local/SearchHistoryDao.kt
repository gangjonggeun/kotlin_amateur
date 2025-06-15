package com.example.kotlin_amateur.repository.local

import androidx.room.*
import com.example.kotlin_amateur.model.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    
    /**
     * 🔍 최근 검색어 5개 조회 (최신순)
     */
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 5")
    fun getRecentSearches(): Flow<List<SearchHistory>>
    
    /**
     * ✅ 검색어 저장 (중복 시 시간 업데이트)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(searchHistory: SearchHistory)
    
    /**
     * 🔍 특정 검색어 존재 확인
     */
    @Query("SELECT * FROM search_history WHERE query = :query LIMIT 1")
    suspend fun findByQuery(query: String): SearchHistory?
    
    /**
     * ❌ 검색어 삭제
     */
    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteByQuery(query: String)
    
    /**
     * 🧹 모든 검색 기록 삭제
     */
    @Query("DELETE FROM search_history")
    suspend fun clearAllSearchHistory()
    
    /**
     * 🗑️ 5개 초과 시 오래된 기록 삭제
     */
    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY searchedAt DESC LIMIT 5)")
    suspend fun keepOnlyRecentFive()
}

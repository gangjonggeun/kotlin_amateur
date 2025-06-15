package com.example.kotlin_amateur.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val query: String,
    
    val searchedAt: LocalDateTime = LocalDateTime.now()
)

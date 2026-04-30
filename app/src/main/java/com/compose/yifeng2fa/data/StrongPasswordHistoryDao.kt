package com.compose.yifeng2fa.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StrongPasswordHistoryDao {
    @Query("SELECT * FROM strong_password_history ORDER BY createdAt DESC")
    fun getAll(): Flow<List<StrongPasswordHistoryEntity>>

    @Insert
    suspend fun insert(entity: StrongPasswordHistoryEntity)

    @Delete
    suspend fun delete(entity: StrongPasswordHistoryEntity)

    @Query("DELETE FROM strong_password_history WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM strong_password_history")
    suspend fun deleteAll()
}

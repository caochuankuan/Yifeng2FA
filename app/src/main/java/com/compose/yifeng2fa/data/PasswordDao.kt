package com.compose.yifeng2fa.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Query("SELECT * FROM password_accounts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM password_accounts WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<PasswordEntity?>

    @Query("SELECT * FROM password_accounts WHERE websiteName LIKE '%' || :query || '%' OR account LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<PasswordEntity>>

    @Insert
    suspend fun insert(entity: PasswordEntity): Long

    @Update
    suspend fun update(entity: PasswordEntity)

    @Delete
    suspend fun delete(entity: PasswordEntity)

    @Query("DELETE FROM password_accounts WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}

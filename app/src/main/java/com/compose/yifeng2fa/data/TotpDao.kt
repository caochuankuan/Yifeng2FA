package com.compose.yifeng2fa.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TotpDao {
    @Query("SELECT * FROM totp_accounts ORDER BY addedAt ASC")
    fun getAllAsc(): Flow<List<TotpEntity>>

    @Query("SELECT * FROM totp_accounts ORDER BY addedAt DESC")
    fun getAllDesc(): Flow<List<TotpEntity>>

    @Query("SELECT * FROM totp_accounts ORDER BY issuer ASC")
    fun getAllByIssuer(): Flow<List<TotpEntity>>

    @Query("SELECT * FROM totp_accounts WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<TotpEntity?>

    @Insert
    suspend fun insert(entity: TotpEntity)

    @Update
    suspend fun update(entity: TotpEntity)

    @Delete
    suspend fun delete(entity: TotpEntity)
}

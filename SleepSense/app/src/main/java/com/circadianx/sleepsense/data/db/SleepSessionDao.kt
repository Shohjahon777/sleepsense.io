package com.circadianx.sleepsense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.circadianx.sleepsense.data.model.SleepSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SleepSession): Long

    @Update
    suspend fun update(session: SleepSession)

    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): SleepSession?

    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SleepSession?

    @Query("UPDATE sleep_sessions SET aiInsight = :insight WHERE id = :id")
    suspend fun updateAiInsight(id: Long, insight: String)
}

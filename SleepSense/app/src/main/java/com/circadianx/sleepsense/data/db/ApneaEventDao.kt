package com.circadianx.sleepsense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.model.ApneaEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ApneaEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ApneaEvent): Long

    @Query("SELECT * FROM apnea_events WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getEventsForSession(sessionId: Long): Flow<List<ApneaEvent>>

    @Query("SELECT COUNT(*) FROM apnea_events WHERE sessionId = :sessionId")
    suspend fun countForSession(sessionId: Long): Int
}

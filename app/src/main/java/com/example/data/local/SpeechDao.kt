package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeechDao {
    @Query("SELECT * FROM children ORDER BY createdAt DESC")
    fun getAllChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id")
    fun getChildById(id: Int): Flow<ChildEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity): Long

    @Update
    suspend fun updateChild(child: ChildEntity)

    @Delete
    suspend fun deleteChild(child: ChildEntity)

    @Query("SELECT * FROM session_logs WHERE childId = :childId ORDER BY timestamp DESC")
    fun getSessionLogsForChild(childId: Int): Flow<List<SessionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionLog(log: SessionLogEntity): Long

    @Delete
    suspend fun deleteSessionLog(log: SessionLogEntity)
}

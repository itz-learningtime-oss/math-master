package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {

    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PracticeSessionEntity>>

    @Query("SELECT * FROM practice_sessions WHERE mode = :mode ORDER BY timestamp DESC")
    fun getSessionsByMode(mode: String): Flow<List<PracticeSessionEntity>>

    @Query("SELECT MIN(totalTimeSec) FROM practice_sessions WHERE mode = :mode")
    fun getBestTime(mode: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSessionEntity): Long

    @Query("DELETE FROM practice_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM practice_sessions")
    suspend fun clearAllSessions()

    // Goals & streaks
    @Query("SELECT * FROM user_goal WHERE id = 1")
    fun getUserGoal(): Flow<UserGoalEntity?>

    @Query("SELECT * FROM user_goal WHERE id = 1")
    suspend fun getUserGoalOnce(): UserGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: UserGoalEntity)
}

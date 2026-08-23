package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MathRepository(private val dao: PracticeDao) {

    val allSessions: Flow<List<PracticeSessionEntity>> = dao.getAllSessions()
    val userGoal: Flow<UserGoalEntity?> = dao.getUserGoal()

    fun getSessionsForMode(mode: String): Flow<List<PracticeSessionEntity>> =
        dao.getSessionsByMode(mode)

    fun getBestTimeForMode(mode: String): Flow<Double?> =
        dao.getBestTime(mode)

    suspend fun recordPracticeSession(
        mode: String,
        totalTimeSec: Double,
        totalQuestions: Int,
        correctCount: Int,
        rangeInfo: String,
        detailsJson: String
    ): Long {
        val session = PracticeSessionEntity(
            mode = mode,
            totalTimeSec = totalTimeSec,
            timestamp = System.currentTimeMillis(),
            totalQuestions = totalQuestions,
            correctCount = correctCount,
            rangeInfo = rangeInfo,
            detailsJson = detailsJson
        )
        val id = dao.insertSession(session)
        updateStreakOnPractice()
        return id
    }

    suspend fun deleteSession(id: Long) = dao.deleteSessionById(id)

    suspend fun clearHistory() = dao.clearAllSessions()

    suspend fun updateUserName(userName: String) {
        val existing = dao.getUserGoalOnce() ?: UserGoalEntity()
        dao.insertOrUpdateGoal(existing.copy(userName = userName.trim()))
    }

    suspend fun updateDailyGoal(
        targetQuestions: Int,
        reminderHour: Int,
        reminderMinute: Int,
        reminderEnabled: Boolean
    ) {
        val existing = dao.getUserGoalOnce() ?: UserGoalEntity()
        dao.insertOrUpdateGoal(
            existing.copy(
                dailyTargetQuestions = targetQuestions,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                reminderEnabled = reminderEnabled
            )
        )
    }

    private suspend fun updateStreakOnPractice() {
        val now = Calendar.getInstance()
        val todayEpochDay = TimeUnit.MILLISECONDS.toDays(now.timeInMillis)
        val existing = dao.getUserGoalOnce() ?: UserGoalEntity()

        val lastDay = existing.lastPracticeDateEpochDay
        val newStreak = when {
            lastDay == 0L -> 1
            lastDay == todayEpochDay -> existing.currentStreak // already practiced today
            lastDay == todayEpochDay - 1L -> existing.currentStreak + 1 // consecutive day
            else -> 1 // streak broken
        }

        dao.insertOrUpdateGoal(
            existing.copy(
                currentStreak = newStreak,
                lastPracticeDateEpochDay = todayEpochDay
            )
        )
    }
}

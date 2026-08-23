package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String, // "addition", "subtraction", "multiplication", "tables", "division", "complex", "roots", "grid"
    val totalTimeSec: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val totalQuestions: Int = 1,
    val correctCount: Int = 1,
    val rangeInfo: String = "",
    val detailsJson: String = "" // serializable questions and individual times
)

@Entity(tableName = "user_goal")
data class UserGoalEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "",
    val dailyTargetQuestions: Int = 20,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val reminderEnabled: Boolean = true,
    val currentStreak: Int = 0,
    val lastPracticeDateEpochDay: Long = 0L
)

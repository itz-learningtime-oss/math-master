package com.example.model

enum class PracticeMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val symbol: String
) {
    ADDITION("addition", "Addition Practice", "Custom number range & operands", "+"),
    SUBTRACTION("subtraction", "Subtraction Practice", "Mental subtraction drills", "-"),
    MULTIPLICATION("multiplication", "Multiplication Practice", "Custom range, factors & count", "×"),
    TABLES("tables", "Tables Reverse Practice", "Identify factors from product (e.g. 48 -> 24*2)", "×?"),
    FACTORS("factors", "Factors Practice", "Identify factor pairs A × B = N (≤99)", "➗"),
    DIVISION("division", "Division Practice", "Dividend & divisor range (no 0s)", "÷"),
    COMPLEX("complex", "Complex Analysis", "Diff between Sum(x,y) & Avg(a,b)", "∑"),
    ROOTS("roots", "Roots Practice", "Square roots (≤100) & Cube roots (≤20)", "√"),
    GRID("grid", "Grid Addition Speed Run", "5x5 matrix speed addition + totals", "▦");

    companion object {
        fun fromId(id: String): PracticeMode {
            return entries.firstOrNull { it.id == id } ?: ADDITION
        }
    }
}

enum class TableSelectionMode {
    COMBINATIONS,
    COUNT
}

enum class FactorsQuestionMode {
    ENTER_PAIR,
    CHOOSE_OPTION
}

enum class RootMode {
    SQROOT,
    CBROOT,
    BOTH
}

enum class LearnTableViewMode {
    MULTIPLICATION,
    DIVISION
}

enum class ExponentPowerMode {
    POWER2,
    POWER3,
    BOTH
}

enum class ExponentDisplayType {
    SQUARES,
    CUBES,
    BOTH
}

enum class LearnRootType {
    SQUARE,
    CUBE
}

enum class RootDisplayType {
    SQROOT,
    CBROOT
}

data class MathQuestion(
    val index: Int,
    val prompt: String,
    val answer: String,
    val numericAnswer: Double? = null,
    val type: String = "standard",
    val targetNumber: Int? = null,
    val options: List<String> = emptyList(),
    val allValidAnswers: List<String> = emptyList(),
    val hint: String = ""
)

data class QuestionResult(
    val prompt: String,
    val userAnswer: String,
    val expectedAnswer: String,
    val isCorrect: Boolean,
    val timeTakenSec: Double
)

data class GridCellResult(
    val key: String,
    val label: String,
    val value: Int,
    val timeSec: Double
)

sealed interface ScreenDestination {
    data object Home : ScreenDestination
    data class Config(val mode: PracticeMode, val backDestination: ScreenDestination = Home) : ScreenDestination
    data object PracticeSession : ScreenDestination
    data object GridSession : ScreenDestination
    data class Result(val mode: PracticeMode) : ScreenDestination
    data class Analysis(
        val mode: PracticeMode,
        val lastCompletionTime: Double? = null,
        val backDestination: ScreenDestination = Home
    ) : ScreenDestination
    data object History : ScreenDestination
    data object Dashboard : ScreenDestination
    data class LearnTables(val backDestination: ScreenDestination = Home) : ScreenDestination
    data class LearnExponents(val backDestination: ScreenDestination = Home) : ScreenDestination
    data class LearnRoots(val backDestination: ScreenDestination = Home) : ScreenDestination
    data class LearnFactors(val backDestination: ScreenDestination = Home) : ScreenDestination
    data class PrivacyPolicy(val backDestination: ScreenDestination = Home) : ScreenDestination
}

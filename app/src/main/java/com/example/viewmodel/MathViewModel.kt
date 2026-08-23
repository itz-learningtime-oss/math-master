package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MathRepository
import com.example.data.PracticeSessionEntity
import com.example.data.UserGoalEntity
import com.example.model.ExponentPowerMode
import com.example.model.FactorsQuestionMode
import com.example.model.GridCellResult
import com.example.model.LearnRootType
import com.example.model.LearnTableViewMode
import com.example.model.MathQuestion
import com.example.model.PracticeMode
import com.example.model.QuestionResult
import com.example.model.RootMode
import com.example.model.ScreenDestination
import com.example.model.TableSelectionMode
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

data class PracticeConfig(
    val mode: PracticeMode = PracticeMode.ADDITION,
    val minRange: Int = 100,
    val maxRange: Int = 999,
    val numsPerQuestion: Int = 2,
    val totalQuestions: Int = 5,
    // Factors
    val factorsMin: Int = 100,
    val factorsMax: Int = 999,
    val factorsQuestionMode: FactorsQuestionMode = FactorsQuestionMode.ENTER_PAIR,
    // Division
    val dividendMin: Int = 100,
    val dividendMax: Int = 999,
    val divisorMin: Int = 2,
    val divisorMax: Int = 20,
    // Tables
    val selectedTables: Set<Int> = (12..37).toSet(),
    val tableSelectionMode: TableSelectionMode = TableSelectionMode.COMBINATIONS,
    // Roots
    val sqRootMin: Int = 1,
    val sqRootMax: Int = 100,
    val cbRootMin: Int = 1,
    val cbRootMax: Int = 20,
    val rootMode: RootMode = RootMode.SQROOT
)

data class GridPlayState(
    val rows: List<Int> = emptyList(),
    val cols: List<Int> = emptyList(),
    val currentStep: Int = 0, // 0..24 (cells), 25..29 (row sums), 30..34 (col sums), 35 (grand total)
    val userAnswers: Map<String, GridCellResult> = emptyMap(),
    val stepStartTime: Long = 0L,
    val isError: Boolean = false,
    val activePrompt: String = "",
    val expectedAnswer: Int = 0
)

data class StudyState(
    val learnTableNum: Int = 12,
    val learnTableViewMode: LearnTableViewMode = LearnTableViewMode.MULTIPLICATION,
    val learnTableHideAnswers: Boolean = false,
    val revealedTableAnswers: Set<String> = emptySet(),
    // Factors
    val learnFactorNumber: Int = 108,
    val learnFactorHideAnswers: Boolean = false,
    val revealedFactors: Set<String> = emptySet(),
    // Exponents
    val exponentPowerMode: ExponentPowerMode = ExponentPowerMode.POWER2,
    val exponentRangeFilter: String = "2-10",
    val exponentHideAnswers: Boolean = false,
    val revealedExponents: Set<String> = emptySet(),
    // Roots
    val learnRootType: LearnRootType = LearnRootType.SQUARE,
    val learnRootRangeFilter: String = "1-20",
    val learnRootHideAnswers: Boolean = false,
    val revealedRoots: Set<String> = emptySet()
)

data class MathUiState(
    val destination: ScreenDestination = ScreenDestination.Home,
    val previousDestination: ScreenDestination = ScreenDestination.Home,
    val config: PracticeConfig = PracticeConfig(),
    // Active practice state
    val questions: List<MathQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val currentInput: String = "",
    val isAnswerError: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Double = 0.0,
    val results: List<QuestionResult> = emptyList(),
    val finalCompletionTime: Double = 0.0,
    // Grid
    val grid: GridPlayState = GridPlayState(),
    // Study
    val study: StudyState = StudyState(),
    // Dashboard & Notification
    val userName: String = "",
    val showNamePromptDialog: Boolean = false,
    val dailyGoalTarget: Int = 20,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val reminderEnabled: Boolean = true,
    val currentStreak: Int = 0
)

class MathViewModel(
    application: Application,
    private val repository: MathRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MathUiState())
    val uiState: StateFlow<MathUiState> = _uiState.asStateFlow()

    val allSessions = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userGoal = repository.userGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private var timerJob: Job? = null
    private var sessionStartTime: Long = 0L
    private var accumulatedTimeMs: Long = 0L

    init {
        // Collect goal from repository
        viewModelScope.launch {
            repository.userGoal.collect { goal ->
                if (goal != null) {
                    _uiState.update {
                        it.copy(
                            userName = goal.userName,
                            showNamePromptDialog = goal.userName.isBlank(),
                            dailyGoalTarget = goal.dailyTargetQuestions,
                            reminderHour = goal.reminderHour,
                            reminderMinute = goal.reminderMinute,
                            reminderEnabled = goal.reminderEnabled,
                            currentStreak = goal.currentStreak
                        )
                    }
                } else {
                    _uiState.update { it.copy(showNamePromptDialog = true) }
                }
            }
        }
        // Initialize Notification channel
        NotificationHelper.createNotificationChannel(application)
    }

    fun setUserName(name: String) {
        val cleanName = name.trim()
        _uiState.update { it.copy(userName = cleanName, showNamePromptDialog = false) }
        viewModelScope.launch {
            repository.updateUserName(cleanName)
        }
    }

    fun openNamePromptDialog() {
        _uiState.update { it.copy(showNamePromptDialog = true) }
    }

    fun closeNamePromptDialog() {
        _uiState.update { it.copy(showNamePromptDialog = false) }
    }

    fun navigateTo(dest: ScreenDestination) {
        val current = _uiState.value.destination
        _uiState.update { it.copy(destination = dest, previousDestination = current) }
    }

    fun selectMode(mode: PracticeMode, backDest: ScreenDestination = ScreenDestination.Home) {
        val defaultConfig = when (mode) {
            PracticeMode.GRID -> PracticeConfig(mode = mode, minRange = 1, maxRange = 100)
            PracticeMode.FACTORS -> PracticeConfig(
                mode = mode,
                factorsMin = 100,
                factorsMax = 999,
                factorsQuestionMode = FactorsQuestionMode.ENTER_PAIR,
                totalQuestions = 5
            )
            PracticeMode.DIVISION -> PracticeConfig(
                mode = mode,
                dividendMin = 100,
                dividendMax = 999,
                divisorMin = 2,
                divisorMax = 20,
                totalQuestions = 5
            )
            PracticeMode.ADDITION, PracticeMode.SUBTRACTION -> PracticeConfig(
                mode = mode,
                minRange = 100,
                maxRange = 999,
                numsPerQuestion = 2,
                totalQuestions = 5
            )
            PracticeMode.MULTIPLICATION -> PracticeConfig(
                mode = mode,
                minRange = 2,
                maxRange = 20,
                numsPerQuestion = 2,
                totalQuestions = 5
            )
            PracticeMode.TABLES -> PracticeConfig(
                mode = mode,
                selectedTables = (12..37).toSet(),
                tableSelectionMode = TableSelectionMode.COMBINATIONS,
                totalQuestions = 5
            )
            PracticeMode.ROOTS -> PracticeConfig(
                mode = mode,
                sqRootMin = 1,
                sqRootMax = 100,
                cbRootMin = 1,
                cbRootMax = 20,
                rootMode = RootMode.SQROOT,
                totalQuestions = 10
            )
            PracticeMode.COMPLEX -> PracticeConfig(
                mode = mode,
                totalQuestions = 5
            )
        }
        _uiState.update {
            it.copy(
                config = defaultConfig,
                destination = ScreenDestination.Config(mode, backDest),
                previousDestination = backDest
            )
        }
    }

    fun updateConfig(update: (PracticeConfig) -> PracticeConfig) {
        _uiState.update { it.copy(config = update(it.config)) }
    }

    fun toggleTableSelection(table: Int) {
        _uiState.update { state ->
            val set = state.config.selectedTables.toMutableSet()
            if (set.contains(table)) set.remove(table) else set.add(table)
            state.copy(config = state.config.copy(selectedTables = set))
        }
    }

    fun startPractice() {
        val config = _uiState.value.config
        stopTimer()
        accumulatedTimeMs = 0L

        if (config.mode == PracticeMode.GRID) {
            startGridPractice(config.minRange, config.maxRange)
            return
        }

        val questions = generateQuestions(config)
        sessionStartTime = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                destination = ScreenDestination.PracticeSession,
                questions = questions,
                currentQuestionIndex = 0,
                currentInput = "",
                isAnswerError = false,
                isPaused = false,
                elapsedSeconds = 0.0,
                results = emptyList()
            )
        }
        startTimer()
    }

    private fun generateQuestions(config: PracticeConfig): List<MathQuestion> {
        val questions = mutableListOf<MathQuestion>()
        val count = config.totalQuestions

        when (config.mode) {
            PracticeMode.ADDITION -> {
                for (i in 0 until count) {
                    val nums = List(config.numsPerQuestion) {
                        getRandomUnique(config.minRange, config.maxRange, emptyList())
                    }
                    val sum = nums.sum()
                    val prompt = nums.joinToString(" + ")
                    questions.add(MathQuestion(i, prompt, sum.toString(), sum.toDouble()))
                }
            }
            PracticeMode.SUBTRACTION -> {
                for (i in 0 until count) {
                    val nums = List(config.numsPerQuestion) {
                        getRandomUnique(config.minRange, config.maxRange, emptyList())
                    }.sortedDescending()
                    var result = nums[0]
                    for (j in 1 until nums.size) {
                        result -= nums[j]
                    }
                    val prompt = nums.joinToString(" - ")
                    questions.add(MathQuestion(i, prompt, result.toString(), result.toDouble()))
                }
            }
            PracticeMode.MULTIPLICATION -> {
                for (i in 0 until count) {
                    val nums = List(config.numsPerQuestion) {
                        getRandomUnique(config.minRange, config.maxRange, emptyList())
                    }
                    var product = 1
                    nums.forEach { product *= it }
                    val prompt = nums.joinToString(" × ")
                    questions.add(MathQuestion(i, prompt, product.toString(), product.toDouble()))
                }
            }
            PracticeMode.TABLES -> {
                val tables = if (config.selectedTables.isEmpty()) listOf(12) else config.selectedTables.toList()
                if (config.tableSelectionMode == TableSelectionMode.COMBINATIONS) {
                    var idx = 0
                    tables.forEach { t ->
                        for (factor in 2..9) {
                            val prod = t * factor
                            questions.add(
                                MathQuestion(
                                    idx++,
                                    prod.toString(),
                                    "$t*$factor",
                                    type = "reverse-table"
                                )
                            )
                        }
                    }
                    questions.shuffle()
                } else {
                    for (i in 0 until count) {
                        val t = tables.random()
                        val factor = Random.nextInt(2, 10)
                        val prod = t * factor
                        questions.add(
                            MathQuestion(
                                i,
                                prod.toString(),
                                "$t*$factor",
                                type = "reverse-table"
                            )
                        )
                    }
                }
            }
            PracticeMode.DIVISION -> {
                for (i in 0 until count) {
                    val divisor = getRandomUnique(config.divisorMin, config.divisorMax, emptyList())
                    val maxQuotient = (config.dividendMax / divisor).coerceAtLeast(1)
                    val minQuotient = (config.dividendMin / divisor).coerceAtLeast(1)
                    val quotient = if (maxQuotient > minQuotient) {
                        Random.nextInt(minQuotient, maxQuotient + 1)
                    } else {
                        minQuotient
                    }
                    val dividend = divisor * quotient
                    questions.add(
                        MathQuestion(
                            i,
                            "$dividend ÷ $divisor",
                            quotient.toString(),
                            quotient.toDouble()
                        )
                    )
                }
            }
            PracticeMode.COMPLEX -> {
                for (i in 0 until count) {
                    val x = Random.nextInt(10, 100)
                    val y = Random.nextInt(10, 100)
                    val a = Random.nextInt(10, 100)
                    val b = Random.nextInt(10, 100)
                    val sum = x + y
                    val avg = (a + b) / 2.0
                    val diff = abs(sum - avg)
                    val formattedDiff = if (diff % 1.0 == 0.0) diff.toInt().toString() else String.format("%.1f", diff)
                    questions.add(
                        MathQuestion(
                            i,
                            "Difference between Sum($x, $y) and Average($a, $b)",
                            formattedDiff,
                            diff
                        )
                    )
                }
            }
            PracticeMode.ROOTS -> {
                for (i in 0 until count) {
                    val isSquare = when (config.rootMode) {
                        RootMode.SQROOT -> true
                        RootMode.CBROOT -> false
                        RootMode.BOTH -> Random.nextBoolean()
                    }
                    if (isSquare) {
                        val base = Random.nextInt(config.sqRootMin, config.sqRootMax + 1)
                        val square = base * base
                        questions.add(MathQuestion(i, "√$square", base.toString(), base.toDouble()))
                    } else {
                        val base = Random.nextInt(config.cbRootMin, config.cbRootMax + 1)
                        val cube = base * base * base
                        questions.add(MathQuestion(i, "∛$cube", base.toString(), base.toDouble()))
                    }
                }
            }
            PracticeMode.FACTORS -> {
                val existingNums = mutableSetOf<Int>()
                for (i in 0 until count) {
                    var targetNum = 0
                    var pairs: List<Pair<Int, Int>> = emptyList()
                    var attempts = 0
                    while (attempts < 500) {
                        attempts++
                        val candidate = Random.nextInt(config.factorsMin, config.factorsMax + 1)
                        if (existingNums.contains(candidate)) continue
                        val validPairs = calculateValidFactorPairs(candidate, 99)
                        if (validPairs.isNotEmpty()) {
                            targetNum = candidate
                            pairs = validPairs
                            existingNums.add(candidate)
                            break
                        }
                    }
                    if (targetNum == 0) {
                        targetNum = if (i % 2 == 0) 252 else 108
                        pairs = calculateValidFactorPairs(targetNum, 99)
                    }

                    val allValidList = pairs.flatMap { listOf("${it.second} × ${it.first}", "${it.first} × ${it.second}", "${it.second}*${it.first}", "${it.first}*${it.second}") }
                    val canonicalAnswer = "${pairs[0].second} × ${pairs[0].first}"
                    val pairsDisplay = pairs.joinToString(", ") { "${it.second} × ${it.first}" }

                    // Generate multiple choice options (1 correct, 3 plausible distractors)
                    val correctPair = pairs.random()
                    val correctOption = "${correctPair.second} × ${correctPair.first}"
                    val distractors = mutableSetOf<String>()
                    val offsetList = listOf(-3, -2, -1, 1, 2, 3, 4, -4, 5, -5)
                    var distAttempts = 0
                    while (distractors.size < 3 && distAttempts < 80) {
                        distAttempts++
                        val da = (correctPair.second + offsetList.random()).coerceIn(2, 99)
                        val db = (correctPair.first + offsetList.random()).coerceIn(2, 99)
                        if (da * db != targetNum && !distractors.contains("$da × $db") && !distractors.contains("$db × $da")) {
                            distractors.add("$da × $db")
                        }
                    }
                    if (distractors.size < 3) {
                        distractors.add("${(correctPair.second + 2).coerceIn(2, 99)} × ${correctPair.first}")
                        distractors.add("${correctPair.second} × ${(correctPair.first + 1).coerceIn(2, 99)}")
                        distractors.add("${(correctPair.second - 1).coerceIn(2, 99)} × ${(correctPair.first + 2).coerceIn(2, 99)}")
                    }
                    val options = (distractors.take(3) + correctOption).shuffled()

                    questions.add(
                        MathQuestion(
                            index = i,
                            prompt = targetNum.toString(),
                            answer = canonicalAnswer,
                            type = "factors",
                            targetNumber = targetNum,
                            options = options,
                            allValidAnswers = allValidList,
                            hint = "Factor pairs: $pairsDisplay"
                        )
                    )
                }
            }
            PracticeMode.GRID -> {}
        }
        return questions
    }

    fun calculateValidFactorPairs(n: Int, maxFactor: Int = 99): List<Pair<Int, Int>> {
        val pairs = mutableListOf<Pair<Int, Int>>()
        val limit = kotlin.math.min(maxFactor, kotlin.math.sqrt(n.toDouble()).toInt())
        for (a in 2..limit) {
            if (n % a == 0) {
                val b = n / a
                if (b <= maxFactor && b >= 2) {
                    pairs.add(Pair(a, b))
                }
            }
        }
        return pairs.sortedByDescending { it.second }
    }

    private fun getRandomUnique(min: Int, max: Int, existing: List<Int>): Int {
        var attempts = 0
        var num: Int
        do {
            num = Random.nextInt(min, max + 1)
            attempts++
            if (attempts > 500) break
        } while (existing.contains(num) || num % 10 == 0)
        return num
    }

    // Timer management
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(50)
                if (!_uiState.value.isPaused) {
                    val now = System.currentTimeMillis()
                    val totalMs = accumulatedTimeMs + (now - sessionStartTime)
                    val sec = totalMs / 1000.0
                    _uiState.update { it.copy(elapsedSeconds = sec) }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun togglePause() {
        val current = _uiState.value
        if (current.isPaused) {
            sessionStartTime = System.currentTimeMillis()
            _uiState.update { it.copy(isPaused = false) }
            startTimer()
        } else {
            val now = System.currentTimeMillis()
            accumulatedTimeMs += (now - sessionStartTime)
            _uiState.update { it.copy(isPaused = true) }
        }
    }

    fun updateCurrentInput(newInput: String) {
        _uiState.update { it.copy(currentInput = newInput, isAnswerError = false) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val questions = state.questions
        if (state.currentQuestionIndex >= questions.size) return

        val q = questions[state.currentQuestionIndex]
        val inputVal = state.currentInput.trim().replace(" ", "").replace("x", "*").replace("X", "*")

        var isCorrect = false
        if (q.type == "reverse-table") {
            // allows format like "12*3" or "3*12"
            val parts = q.answer.split("*")
            if (parts.size == 2) {
                val p1 = parts[0]
                val p2 = parts[1]
                isCorrect = (inputVal == "$p1*$p2" || inputVal == "$p2*$p1")
            } else {
                isCorrect = (inputVal == q.answer)
            }
        } else if (q.type == "factors") {
            // Dynamic factor pair validation: e.g. "36*7", "36x7", "36 7", "36,7", "36×7", "7*36", "7x36", "7 36", "7×36"
            val tokens = inputVal.split(Regex("[*xX×, ]+")).filter { it.isNotBlank() }
            if (tokens.size == 2) {
                val a = tokens[0].toIntOrNull()
                val b = tokens[1].toIntOrNull()
                val target = q.targetNumber ?: q.prompt.toIntOrNull() ?: 0
                if (a != null && b != null && a >= 2 && b >= 2 && a <= 99 && b <= 99 && (a * b == target)) {
                    isCorrect = true
                }
            } else {
                // Check against valid answer list
                isCorrect = q.allValidAnswers.any { it.replace(" ", "").equals(inputVal, ignoreCase = true) }
            }
        } else {
            val numericInput = inputVal.toDoubleOrNull()
            if (numericInput != null && q.numericAnswer != null) {
                isCorrect = abs(numericInput - q.numericAnswer) < 0.01
            } else {
                isCorrect = inputVal.equals(q.answer, ignoreCase = true)
            }
        }

        if (!isCorrect) {
            _uiState.update { it.copy(isAnswerError = true) }
            viewModelScope.launch {
                delay(500)
                _uiState.update { it.copy(isAnswerError = false) }
            }
            return
        }

        // Correct answer!
        val timeForQ = state.elapsedSeconds
        val formattedUserAnswer = if (q.type == "factors") {
            val tokens = inputVal.split(Regex("[*xX×, ]+")).filter { it.isNotBlank() }
            if (tokens.size == 2) "${tokens[0]} × ${tokens[1]}" else inputVal
        } else inputVal

        val qResult = QuestionResult(
            prompt = if (q.type == "factors") "Factors of ${q.targetNumber ?: q.prompt}" else q.prompt,
            userAnswer = formattedUserAnswer,
            expectedAnswer = if (q.type == "factors" && q.hint.isNotBlank()) q.hint else q.answer,
            isCorrect = true,
            timeTakenSec = timeForQ
        )
        val newResults = state.results + qResult

        if (state.currentQuestionIndex + 1 < questions.size) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    currentInput = "",
                    results = newResults
                )
            }
        } else {
            // Finish session!
            stopTimer()
            val totalTime = state.elapsedSeconds
            finishSession(state.config.mode, totalTime, newResults)
        }
    }

    private fun finishSession(mode: PracticeMode, totalTime: Double, results: List<QuestionResult>) {
        viewModelScope.launch {
            val jsonDetails = JSONArray().apply {
                results.forEach { r ->
                    put(JSONObject().apply {
                        put("prompt", r.prompt)
                        put("userAnswer", r.userAnswer)
                        put("expected", r.expectedAnswer)
                        put("time", r.timeTakenSec)
                    })
                }
            }.toString()

            val rangeInfo = when (mode) {
                PracticeMode.ADDITION, PracticeMode.SUBTRACTION, PracticeMode.MULTIPLICATION ->
                    "${_uiState.value.config.minRange}-${_uiState.value.config.maxRange}"
                PracticeMode.DIVISION ->
                    "${_uiState.value.config.dividendMin}..${_uiState.value.config.dividendMax} ÷ ${_uiState.value.config.divisorMin}..${_uiState.value.config.divisorMax}"
                PracticeMode.TABLES ->
                    "Tables: ${_uiState.value.config.selectedTables.sorted().joinToString(",")}"
                PracticeMode.ROOTS ->
                    "Roots: ${_uiState.value.config.rootMode.name}"
                PracticeMode.FACTORS ->
                    "Factors: ${_uiState.value.config.factorsMin}-${_uiState.value.config.factorsMax}"
                PracticeMode.GRID ->
                    "Grid: ${_uiState.value.config.minRange}-${_uiState.value.config.maxRange}"
                PracticeMode.COMPLEX -> "Complex"
            }

            repository.recordPracticeSession(
                mode = mode.id,
                totalTimeSec = totalTime,
                totalQuestions = results.size,
                correctCount = results.count { it.isCorrect },
                rangeInfo = rangeInfo,
                detailsJson = jsonDetails
            )

            _uiState.update {
                it.copy(
                    destination = ScreenDestination.Result(mode),
                    finalCompletionTime = totalTime,
                    results = results
                )
            }
        }
    }

    // Grid Practice Implementation
    private fun startGridPractice(min: Int, max: Int) {
        val existing = mutableListOf<Int>()
        val rows = mutableListOf<Int>()
        val cols = mutableListOf<Int>()
        for (i in 0 until 5) {
            val r = getRandomUnique(min, max, existing)
            rows.add(r)
            existing.add(r)
            val c = getRandomUnique(min, max, existing)
            cols.add(c)
            existing.add(c)
        }

        val prompt0 = "${rows[0]} + ${cols[0]}"
        val expected0 = rows[0] + cols[0]

        sessionStartTime = System.currentTimeMillis()
        val gridState = GridPlayState(
            rows = rows,
            cols = cols,
            currentStep = 0,
            userAnswers = emptyMap(),
            stepStartTime = System.currentTimeMillis(),
            isError = false,
            activePrompt = prompt0,
            expectedAnswer = expected0
        )

        _uiState.update {
            it.copy(
                destination = ScreenDestination.GridSession,
                grid = gridState,
                isPaused = false,
                elapsedSeconds = 0.0,
                currentInput = ""
            )
        }
        startTimer()
    }

    fun submitGridAnswer(input: String) {
        val state = _uiState.value
        val grid = state.grid
        val valInt = input.trim().toIntOrNull() ?: return

        if (valInt != grid.expectedAnswer) {
            _uiState.update { it.copy(grid = it.grid.copy(isError = true)) }
            viewModelScope.launch {
                delay(500)
                _uiState.update { it.copy(grid = it.grid.copy(isError = false)) }
            }
            return
        }

        val now = System.currentTimeMillis()
        val stepTime = (now - grid.stepStartTime) / 1000.0
        val currentStep = grid.currentStep

        val key = when {
            currentStep < 25 -> "r${currentStep / 5}c${currentStep % 5}"
            currentStep < 30 -> "rowSum${currentStep - 25}"
            currentStep < 35 -> "colSum${currentStep - 30}"
            else -> "grand"
        }
        val label = grid.activePrompt
        val newAnswers = grid.userAnswers + (key to GridCellResult(key, label, valInt, stepTime))

        if (currentStep < 35) {
            val nextStep = currentStep + 1
            var nextPrompt = ""
            var nextExpected = 0

            if (nextStep < 25) {
                val r = nextStep / 5
                val c = nextStep % 5
                nextPrompt = "${grid.rows[r]} + ${grid.cols[c]}"
                nextExpected = grid.rows[r] + grid.cols[c]
            } else if (nextStep < 30) {
                val r = nextStep - 25
                nextPrompt = "Row ${r + 1} Total"
                for (c in 0 until 5) {
                    nextExpected += (newAnswers["r${r}c$c"]?.value ?: 0)
                }
            } else if (nextStep < 35) {
                val c = nextStep - 30
                nextPrompt = "Col ${c + 1} Total"
                for (r in 0 until 5) {
                    nextExpected += (newAnswers["r${r}c$c"]?.value ?: 0)
                }
            } else {
                nextPrompt = "GRAND TOTAL"
                for (r in 0 until 5) {
                    nextExpected += (newAnswers["rowSum$r"]?.value ?: 0)
                }
            }

            _uiState.update {
                it.copy(
                    currentInput = "",
                    grid = grid.copy(
                        currentStep = nextStep,
                        userAnswers = newAnswers,
                        stepStartTime = System.currentTimeMillis(),
                        isError = false,
                        activePrompt = nextPrompt,
                        expectedAnswer = nextExpected
                    )
                )
            }
        } else {
            // Finished 5x5 Speed Grid!
            stopTimer()
            val totalTime = state.elapsedSeconds
            finishGridSession(totalTime, newAnswers)
        }
    }

    private fun finishGridSession(totalTime: Double, answers: Map<String, GridCellResult>) {
        viewModelScope.launch {
            val jsonDetails = JSONArray().apply {
                answers.values.forEach { a ->
                    put(JSONObject().apply {
                        put("key", a.key)
                        put("label", a.label)
                        put("val", a.value)
                        put("time", a.timeSec)
                    })
                }
            }.toString()

            repository.recordPracticeSession(
                mode = PracticeMode.GRID.id,
                totalTimeSec = totalTime,
                totalQuestions = 36,
                correctCount = 36,
                rangeInfo = "${_uiState.value.config.minRange}-${_uiState.value.config.maxRange}",
                detailsJson = jsonDetails
            )

            _uiState.update {
                it.copy(
                    destination = ScreenDestination.Result(PracticeMode.GRID),
                    finalCompletionTime = totalTime,
                    grid = it.grid.copy(userAnswers = answers)
                )
            }
        }
    }

    // Study state actions
    fun setLearnTableNum(num: Int) {
        _uiState.update { it.copy(study = it.study.copy(learnTableNum = num, revealedTableAnswers = emptySet())) }
    }

    fun setLearnTableViewMode(mode: LearnTableViewMode) {
        _uiState.update { it.copy(study = it.study.copy(learnTableViewMode = mode)) }
    }

    fun toggleLearnTableHideAnswers() {
        _uiState.update {
            it.copy(study = it.study.copy(learnTableHideAnswers = !it.study.learnTableHideAnswers, revealedTableAnswers = emptySet()))
        }
    }

    fun toggleTableAnswerReveal(key: String) {
        _uiState.update { state ->
            val set = state.study.revealedTableAnswers.toMutableSet()
            if (set.contains(key)) set.remove(key) else set.add(key)
            state.copy(study = state.study.copy(revealedTableAnswers = set))
        }
    }

    // Exponents study
    fun setExponentPowerMode(mode: ExponentPowerMode) {
        _uiState.update { it.copy(study = it.study.copy(exponentPowerMode = mode)) }
    }

    fun setExponentRangeFilter(range: String) {
        _uiState.update { it.copy(study = it.study.copy(exponentRangeFilter = range)) }
    }

    fun toggleExponentHideAnswers() {
        _uiState.update {
            it.copy(study = it.study.copy(exponentHideAnswers = !it.study.exponentHideAnswers, revealedExponents = emptySet()))
        }
    }

    fun toggleExponentReveal(key: String) {
        _uiState.update { state ->
            val set = state.study.revealedExponents.toMutableSet()
            if (set.contains(key)) set.remove(key) else set.add(key)
            state.copy(study = state.study.copy(revealedExponents = set))
        }
    }

    // Roots study
    fun setLearnRootType(type: LearnRootType) {
        val defaultRange = if (type == LearnRootType.SQUARE) "1-20" else "1-10"
        _uiState.update {
            it.copy(study = it.study.copy(learnRootType = type, learnRootRangeFilter = defaultRange, revealedRoots = emptySet()))
        }
    }

    fun setLearnRootRangeFilter(range: String) {
        _uiState.update { it.copy(study = it.study.copy(learnRootRangeFilter = range)) }
    }

    fun toggleLearnRootHideAnswers() {
        _uiState.update {
            it.copy(study = it.study.copy(learnRootHideAnswers = !it.study.learnRootHideAnswers, revealedRoots = emptySet()))
        }
    }

    fun toggleRootReveal(key: String) {
        _uiState.update { state ->
            val set = state.study.revealedRoots.toMutableSet()
            if (set.contains(key)) set.remove(key) else set.add(key)
            state.copy(study = state.study.copy(revealedRoots = set))
        }
    }

    // Factors study
    fun setLearnFactorNumber(num: Int) {
        _uiState.update {
            it.copy(study = it.study.copy(learnFactorNumber = num.coerceIn(10, 9999), revealedFactors = emptySet()))
        }
    }

    fun toggleLearnFactorHideAnswers() {
        _uiState.update {
            it.copy(study = it.study.copy(learnFactorHideAnswers = !it.study.learnFactorHideAnswers, revealedFactors = emptySet()))
        }
    }

    fun toggleFactorReveal(key: String) {
        _uiState.update { state ->
            val set = state.study.revealedFactors.toMutableSet()
            if (set.contains(key)) set.remove(key) else set.add(key)
            state.copy(study = state.study.copy(revealedFactors = set))
        }
    }

    // User Daily Goal & Notification Management
    fun saveDailyGoal(targetQuestions: Int, hour: Int, minute: Int, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDailyGoal(targetQuestions, hour, minute, enabled)
            NotificationHelper.scheduleDailyReminder(
                getApplication(),
                hour,
                minute,
                enabled
            )
            _uiState.update {
                it.copy(
                    dailyGoalTarget = targetQuestions,
                    reminderHour = hour,
                    reminderMinute = minute,
                    reminderEnabled = enabled
                )
            }
        }
    }

    fun sendTestNotification() {
        NotificationHelper.showPracticeNotification(
            getApplication(),
            title = "Math Master Daily Goal 🎯",
            message = "You're on a ${_uiState.value.currentStreak}-day streak! Complete your ${_uiState.value.dailyGoalTarget} daily problems today."
        )
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { repository.deleteSession(id) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }
}

class MathViewModelFactory(
    private val application: Application,
    private val repository: MathRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MathViewModel::class.java)) {
            return MathViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.MathRepository
import com.example.model.ExponentDisplayType
import com.example.model.ExponentPowerMode
import com.example.model.LearnRootType
import com.example.model.PracticeMode
import com.example.model.RootDisplayType
import com.example.model.ScreenDestination
import com.example.ui.components.UserNameDialog
import com.example.ui.screens.ConfigScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GridScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnExponentsScreen
import com.example.ui.screens.LearnFactorsScreen
import com.example.ui.screens.LearnRootsScreen
import com.example.ui.screens.LearnTablesScreen
import com.example.ui.screens.PerformanceAnalysisScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MathViewModel
import com.example.viewmodel.MathViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MathViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext)
        val repo = MathRepository(db.practiceDao())
        MathViewModelFactory(application, repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8FAFC))
                ) { innerPadding ->
                    MathMasterApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MathMasterApp(
    viewModel: MathViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()

    // Global Back Handler
    BackHandler(enabled = uiState.destination != ScreenDestination.Home) {
        when (val dest = uiState.destination) {
            is ScreenDestination.Config -> viewModel.navigateTo(dest.backDestination)
            is ScreenDestination.PracticeSession -> viewModel.navigateTo(ScreenDestination.Config(uiState.config.mode))
            is ScreenDestination.GridSession -> viewModel.navigateTo(ScreenDestination.Config(PracticeMode.GRID))
            is ScreenDestination.Result -> viewModel.navigateTo(ScreenDestination.Home)
            is ScreenDestination.Analysis -> viewModel.navigateTo(dest.backDestination)
            is ScreenDestination.History -> viewModel.navigateTo(ScreenDestination.Dashboard)
            is ScreenDestination.Dashboard -> viewModel.navigateTo(ScreenDestination.Home)
            is ScreenDestination.LearnTables -> viewModel.navigateTo(dest.backDestination)
            is ScreenDestination.LearnFactors -> viewModel.navigateTo(dest.backDestination)
            is ScreenDestination.LearnExponents -> viewModel.navigateTo(dest.backDestination)
            is ScreenDestination.LearnRoots -> viewModel.navigateTo(dest.backDestination)
            is ScreenDestination.PrivacyPolicy -> viewModel.navigateTo(dest.backDestination)
            ScreenDestination.Home -> {}
        }
    }

    // Name Prompt Dialog
    if (uiState.showNamePromptDialog) {
        UserNameDialog(
            currentName = uiState.userName,
            onSaveName = { newName -> viewModel.setUserName(newName) },
            onDismiss = { viewModel.closeNamePromptDialog() }
        )
    }

    when (val dest = uiState.destination) {
        is ScreenDestination.Home -> {
            HomeScreen(
                sessions = allSessions,
                currentStreak = uiState.currentStreak,
                dailyGoalTarget = uiState.dailyGoalTarget,
                userName = uiState.userName,
                onEditName = { viewModel.openNamePromptDialog() },
                onSelectMode = { mode -> viewModel.selectMode(mode, ScreenDestination.Home) },
                onNavigate = { target -> viewModel.navigateTo(target) },
                modifier = modifier
            )
        }

        is ScreenDestination.Config -> {
            ConfigScreen(
                config = uiState.config,
                onUpdateConfig = { update -> viewModel.updateConfig(update) },
                onToggleTable = { table -> viewModel.toggleTableSelection(table) },
                onStart = { viewModel.startPractice() },
                onBack = { viewModel.navigateTo(dest.backDestination) },
                onNavigateStudy = { studyDest -> viewModel.navigateTo(studyDest) },
                modifier = modifier
            )
        }

        is ScreenDestination.PracticeSession -> {
            PracticeScreen(
                questions = uiState.questions,
                currentIndex = uiState.currentQuestionIndex,
                currentInput = uiState.currentInput,
                elapsedSeconds = uiState.elapsedSeconds,
                isPaused = uiState.isPaused,
                isError = uiState.isAnswerError,
                mode = uiState.config.mode,
                onInputChange = { viewModel.updateCurrentInput(it) },
                onSubmit = { viewModel.submitAnswer() },
                onTogglePause = { viewModel.togglePause() },
                modifier = modifier
            )
        }

        is ScreenDestination.GridSession -> {
            GridScreen(
                grid = uiState.grid,
                currentInput = uiState.currentInput,
                elapsedSeconds = uiState.elapsedSeconds,
                isPaused = uiState.isPaused,
                onInputChange = { viewModel.updateCurrentInput(it) },
                onSubmit = { viewModel.submitGridAnswer(uiState.currentInput) },
                onTogglePause = { viewModel.togglePause() },
                modifier = modifier
            )
        }

        is ScreenDestination.Result -> {
            ResultScreen(
                mode = dest.mode,
                totalTimeSec = uiState.finalCompletionTime,
                results = uiState.results,
                grid = uiState.grid,
                onHome = { viewModel.navigateTo(ScreenDestination.Home) },
                onAnalysis = { viewModel.navigateTo(ScreenDestination.Analysis(dest.mode, uiState.finalCompletionTime, ScreenDestination.Home)) },
                modifier = modifier
            )
        }

        is ScreenDestination.Analysis -> {
            PerformanceAnalysisScreen(
                initialMode = dest.mode,
                allSessions = allSessions,
                lastCompletionTime = dest.lastCompletionTime,
                onBack = { viewModel.navigateTo(dest.backDestination) },
                onHome = { viewModel.navigateTo(ScreenDestination.Home) },
                modifier = modifier
            )
        }

        is ScreenDestination.Dashboard, ScreenDestination.History -> {
            DashboardScreen(
                sessions = allSessions,
                currentStreak = uiState.currentStreak,
                dailyGoalTarget = uiState.dailyGoalTarget,
                reminderHour = uiState.reminderHour,
                reminderMinute = uiState.reminderMinute,
                reminderEnabled = uiState.reminderEnabled,
                userName = uiState.userName,
                onEditName = { viewModel.openNamePromptDialog() },
                onNavigatePrivacy = { viewModel.navigateTo(ScreenDestination.PrivacyPolicy(ScreenDestination.Dashboard)) },
                onSaveGoal = { target, h, m, en -> viewModel.saveDailyGoal(target, h, m, en) },
                onSendTestNotification = { viewModel.sendTestNotification() },
                onDeleteSession = { id -> viewModel.deleteSession(id) },
                onClearHistory = { viewModel.clearAllHistory() },
                onBack = { viewModel.navigateTo(ScreenDestination.Home) },
                onSelectAnalysis = { mode -> viewModel.navigateTo(ScreenDestination.Analysis(mode)) },
                modifier = modifier
            )
        }

        is ScreenDestination.LearnTables -> {
            LearnTablesScreen(
                currentTableNum = uiState.study.learnTableNum,
                viewMode = uiState.study.learnTableViewMode,
                isFlashcardMode = uiState.study.learnTableHideAnswers,
                revealedKeys = uiState.study.revealedTableAnswers,
                onSelectTable = { viewModel.setLearnTableNum(it) },
                onToggleViewMode = { viewModel.setLearnTableViewMode(it) },
                onToggleFlashcardMode = { viewModel.toggleLearnTableHideAnswers() },
                onToggleReveal = { viewModel.toggleTableAnswerReveal(it) },
                onPracticeTable = { tableNum ->
                    viewModel.updateConfig {
                        it.copy(
                            mode = PracticeMode.TABLES,
                            selectedTables = setOf(tableNum),
                            totalQuestions = 5
                        )
                    }
                    viewModel.startPractice()
                },
                onBack = { viewModel.navigateTo(dest.backDestination) },
                modifier = modifier
            )
        }

        is ScreenDestination.LearnFactors -> {
            LearnFactorsScreen(
                targetNumber = uiState.study.learnFactorNumber,
                isFlashcardMode = uiState.study.learnFactorHideAnswers,
                revealedKeys = uiState.study.revealedFactors,
                onSelectNumber = { viewModel.setLearnFactorNumber(it) },
                onToggleFlashcardMode = { viewModel.toggleLearnFactorHideAnswers() },
                onToggleReveal = { viewModel.toggleFactorReveal(it) },
                onPractice = {
                    viewModel.updateConfig {
                        it.copy(
                            mode = PracticeMode.FACTORS,
                            factorsMin = 100,
                            factorsMax = 999,
                            totalQuestions = 5
                        )
                    }
                    viewModel.startPractice()
                },
                onBack = { viewModel.navigateTo(dest.backDestination) },
                modifier = modifier
            )
        }

        is ScreenDestination.LearnExponents -> {
            val displayType = when (uiState.study.exponentPowerMode) {
                ExponentPowerMode.POWER2 -> ExponentDisplayType.SQUARES
                ExponentPowerMode.POWER3 -> ExponentDisplayType.CUBES
                ExponentPowerMode.BOTH -> ExponentDisplayType.BOTH
            }

            LearnExponentsScreen(
                displayType = displayType,
                selectedRange = uiState.study.exponentRangeFilter,
                isFlashcardMode = uiState.study.exponentHideAnswers,
                revealedKeys = uiState.study.revealedExponents,
                onToggleType = { type ->
                    val mode = when (type) {
                        ExponentDisplayType.SQUARES -> ExponentPowerMode.POWER2
                        ExponentDisplayType.CUBES -> ExponentPowerMode.POWER3
                        ExponentDisplayType.BOTH -> ExponentPowerMode.BOTH
                    }
                    viewModel.setExponentPowerMode(mode)
                },
                onSelectRange = { viewModel.setExponentRangeFilter(it) },
                onToggleFlashcardMode = { viewModel.toggleExponentHideAnswers() },
                onToggleReveal = { viewModel.toggleExponentReveal(it) },
                onPractice = {
                    viewModel.updateConfig {
                        it.copy(
                            mode = PracticeMode.MULTIPLICATION,
                            minRange = 2,
                            maxRange = 20,
                            numsPerQuestion = 2,
                            totalQuestions = 5
                        )
                    }
                    viewModel.startPractice()
                },
                onBack = { viewModel.navigateTo(dest.backDestination) },
                modifier = modifier
            )
        }

        is ScreenDestination.LearnRoots -> {
            val rootDisplayType = when (uiState.study.learnRootType) {
                LearnRootType.SQUARE -> RootDisplayType.SQROOT
                LearnRootType.CUBE -> RootDisplayType.CBROOT
            }

            LearnRootsScreen(
                displayType = rootDisplayType,
                selectedRange = uiState.study.learnRootRangeFilter,
                isFlashcardMode = uiState.study.learnRootHideAnswers,
                revealedKeys = uiState.study.revealedRoots,
                onToggleType = { type ->
                    val m = when (type) {
                        RootDisplayType.SQROOT -> LearnRootType.SQUARE
                        RootDisplayType.CBROOT -> LearnRootType.CUBE
                    }
                    viewModel.setLearnRootType(m)
                },
                onSelectRange = { viewModel.setLearnRootRangeFilter(it) },
                onToggleFlashcardMode = { viewModel.toggleLearnRootHideAnswers() },
                onToggleReveal = { viewModel.toggleRootReveal(it) },
                onPractice = {
                    viewModel.updateConfig {
                        it.copy(
                            mode = PracticeMode.ROOTS,
                            sqRootMin = 1,
                            sqRootMax = 100,
                            cbRootMin = 1,
                            cbRootMax = 20,
                            totalQuestions = 10
                        )
                    }
                    viewModel.startPractice()
                },
                onBack = { viewModel.navigateTo(dest.backDestination) },
                modifier = modifier
            )
        }

        is ScreenDestination.PrivacyPolicy -> {
            PrivacyPolicyScreen(
                onBack = { viewModel.navigateTo(dest.backDestination) },
                modifier = modifier
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PracticeSessionEntity
import com.example.model.PracticeMode
import com.example.model.ScreenDestination
import com.example.ui.components.ShareAppDialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberBg
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueBg
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentCyanBg
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentEmeraldBg
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleBg
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentRoseBg
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    sessions: List<PracticeSessionEntity>,
    currentStreak: Int,
    dailyGoalTarget: Int,
    userName: String = "",
    onEditName: () -> Unit = {},
    onSelectMode: (PracticeMode) -> Unit,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate today's solved problems
    val todayEpochDay = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().timeInMillis)
    val todayQuestions = sessions.filter {
        TimeUnit.MILLISECONDS.toDays(it.timestamp) == todayEpochDay
    }.sumOf { it.totalQuestions }

    val gridSessions = sessions.filter { it.mode == PracticeMode.GRID.id }
    val bestGridTime = gridSessions.minOfOrNull { it.totalTimeSec }

    var showShareDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Line: Developer Attribution & Version requested by user
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigate(ScreenDestination.PrivacyPolicy(ScreenDestination.Home)) }
                    .testTag("banner_developer_top_line"),
                color = Color(0xFFEEF2FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "This app is developed by Vishesh Chaturvedi • Version 3.14",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryIndigo.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Policy 📜",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Hero Header & Quick Stats
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .testTag("home_hero_card"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // User greeting & profile chip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable(onClick = onEditName)
                                    .testTag("btn_user_profile_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Profile",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (userName.isNotBlank()) userName else "Tap to add your Name",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Name",
                                        tint = Slate400,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            if (userName.isNotBlank()) {
                                Text(
                                    text = "Ready to train! ⚡",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentEmerald
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PrimaryIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Math Master",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (userName.isNotBlank()) "Hi, $userName!" else "Math Master",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Speed & Accuracy Trainer",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate400
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Share App Button in Hero Header
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF334155),
                                    modifier = Modifier
                                        .clickable { showShareDialog = true }
                                        .testTag("btn_share_hero")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share App",
                                            tint = AccentCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Share",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Daily Streak Flame
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF334155),
                                    modifier = Modifier.clickable { onNavigate(ScreenDestination.Dashboard) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "Streak",
                                            tint = AccentAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "${currentStreak}d",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Daily Goal Progress Bar
                        val goalProgress = if (dailyGoalTarget > 0) {
                            (todayQuestions.toFloat() / dailyGoalTarget).coerceIn(0f, 1f)
                        } else 0f

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(ScreenDestination.Dashboard) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Today's Practice Goal",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate400
                                        )
                                        if (todayQuestions >= dailyGoalTarget && dailyGoalTarget > 0) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Goal Achieved",
                                                tint = AccentEmerald,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$todayQuestions / $dailyGoalTarget problems solved",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { goalProgress },
                                        modifier = Modifier.size(40.dp),
                                        color = if (goalProgress >= 1f) AccentEmerald else PrimaryIndigo,
                                        trackColor = Color(0xFF334155),
                                        strokeWidth = 4.dp
                                    )
                                    Text(
                                        text = "${(goalProgress * 100).toInt()}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Practice Drills Header
        item {
            Text(
                text = "PRACTICE MODES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Slate500,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // 1. Addition
        item {
            ModeRowCard(
                title = "Addition Practice",
                subtitle = "Multi-digit mental addition",
                symbol = "+",
                accentColor = AccentBlue,
                accentBg = AccentBlueBg,
                testTag = "mode_addition",
                onClick = { onSelectMode(PracticeMode.ADDITION) }
            )
        }

        // 2. Subtraction
        item {
            ModeRowCard(
                title = "Subtraction Practice",
                subtitle = "Mental difference drills",
                symbol = "-",
                accentColor = AccentRose,
                accentBg = AccentRoseBg,
                testTag = "mode_subtraction",
                onClick = { onSelectMode(PracticeMode.SUBTRACTION) }
            )
        }

        // 3. Multiplication
        item {
            ModeRowCard(
                title = "Multiplication Practice",
                subtitle = "Custom range, factors & count",
                symbol = "×",
                accentColor = AccentAmber,
                accentBg = AccentAmberBg,
                testTag = "mode_multiplication",
                onClick = { onSelectMode(PracticeMode.MULTIPLICATION) }
            )
        }

        // 4. Tables Reverse
        item {
            ModeRowCard(
                title = "Tables Reverse Practice",
                subtitle = "Find factor pairs (e.g. 48 -> 24×2)",
                symbol = "×?",
                accentColor = PrimaryIndigo,
                accentBg = PrimaryIndigoLight,
                testTag = "mode_tables",
                onClick = { onSelectMode(PracticeMode.TABLES) }
            )
        }

        // 5. Factors Practice (New!)
        item {
            ModeRowCard(
                title = "Factors Practice",
                subtitle = "Identify factor pairs A × B = N (A, B ≤ 99)",
                symbol = "➗",
                accentColor = AccentCyan,
                accentBg = AccentCyanBg,
                testTag = "mode_factors",
                onClick = { onSelectMode(PracticeMode.FACTORS) }
            )
        }

        // 6. Division
        item {
            ModeRowCard(
                title = "Division Practice",
                subtitle = "Clean integer division without zeroes",
                symbol = "÷",
                accentColor = AccentEmerald,
                accentBg = AccentEmeraldBg,
                testTag = "mode_division",
                onClick = { onSelectMode(PracticeMode.DIVISION) }
            )
        }

        // STUDY MODULES SECTION
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "STUDY GUIDES & FLASHCARDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate500,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Study Tables
                    StudyCard(
                        title = "Tables",
                        subtitle = "12 to 37",
                        badge = "Study 📖",
                        color = PrimaryIndigo,
                        bgColor = Color(0xFFEEF2FF),
                        borderColor = Color(0xFFC7D2FE),
                        modifier = Modifier.weight(1f),
                        testTag = "study_tables",
                        onClick = { onNavigate(ScreenDestination.LearnTables(ScreenDestination.Home)) }
                    )

                    // Study Factors
                    StudyCard(
                        title = "Factors",
                        subtitle = "A × B = N",
                        badge = "Study 🔍",
                        color = AccentCyan,
                        bgColor = Color(0xFFECFEFF),
                        borderColor = Color(0xFFA5F3FC),
                        modifier = Modifier.weight(1f),
                        testTag = "study_factors",
                        onClick = { onNavigate(ScreenDestination.LearnFactors(ScreenDestination.Home)) }
                    )

                    // Study Exponents
                    StudyCard(
                        title = "Exponents",
                        subtitle = "x² & x³",
                        badge = "Study ⚡",
                        color = AccentAmber,
                        bgColor = Color(0xFFFFFBEB),
                        borderColor = Color(0xFFFDE68A),
                        modifier = Modifier.weight(1f),
                        testTag = "study_exponents",
                        onClick = { onNavigate(ScreenDestination.LearnExponents(ScreenDestination.Home)) }
                    )

                    // Study Roots
                    StudyCard(
                        title = "Roots",
                        subtitle = "√100 & ∛20",
                        badge = "Study 🌱",
                        color = AccentEmerald,
                        bgColor = Color(0xFFECFDF5),
                        borderColor = Color(0xFFA7F3D0),
                        modifier = Modifier.weight(1f),
                        testTag = "study_roots",
                        onClick = { onNavigate(ScreenDestination.LearnRoots(ScreenDestination.Home)) }
                    )
                }
            }
        }

        // 6. Complex Analysis
        item {
            ModeRowCard(
                title = "Complex Analysis",
                subtitle = "Diff between Sum(x, y) & Avg(a, b)",
                symbol = "∑",
                accentColor = AccentPurple,
                accentBg = AccentPurpleBg,
                testTag = "mode_complex",
                onClick = { onSelectMode(PracticeMode.COMPLEX) }
            )
        }

        // 7. Roots Practice
        item {
            ModeRowCard(
                title = "Roots Practice",
                subtitle = "Square roots (≤100) & Cube roots (≤20)",
                symbol = "√",
                accentColor = AccentEmerald,
                accentBg = AccentEmeraldBg,
                testTag = "mode_roots",
                onClick = { onSelectMode(PracticeMode.ROOTS) }
            )
        }

        // 8. Grid Addition Speed Run
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onSelectMode(PracticeMode.GRID) }
                    .testTag("mode_grid"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Grid Addition Speed Run",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        if (bestGridTime != null) {
                            Text(
                                text = "BEST: ${String.format("%.2f", bestGridTime)}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = AccentRose
                            )
                        } else {
                            Text(
                                text = "5x5 matrix + row & col totals speed run",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Slate400
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▦",
                            fontSize = 24.sp,
                            color = AccentRose
                        )
                    }
                }
            }
        }

        // Student Dashboard Action Button
        item {
            Button(
                onClick = { onNavigate(ScreenDestination.Dashboard) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("btn_view_dashboard"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate100,
                    contentColor = Slate700
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Dashboard",
                        tint = Slate700,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "View Student Progress Dashboard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Share App Card (Direct APK & Invite)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showShareDialog = true }
                    .testTag("card_share_app"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Share App with Anyone",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                            Text(
                                text = "Send APK file directly or share invite with friends",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "APK 📦",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Privacy Policy Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigate(ScreenDestination.PrivacyPolicy(ScreenDestination.Home)) }
                    .testTag("card_home_privacy_policy"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Privacy Policy",
                                tint = AccentEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Privacy Policy & Developer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                            Text(
                                text = "By Vishesh Chaturvedi • 100% Free & Local Practice",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5)
                    ) {
                        Text(
                            text = "Read 📜",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Footer Attribution
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Math Master • Version 3.14",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500
                )
                Text(
                    text = "Developed by Vishesh Chaturvedi",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showShareDialog) {
        ShareAppDialog(onDismiss = { showShareDialog = false })
    }
}

@Composable
fun ModeRowCard(
    title: String,
    subtitle: String,
    symbol: String,
    accentColor: Color,
    accentBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Slate200, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate400
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun StudyCard(
    title: String,
    subtitle: String,
    badge: String,
    color: Color,
    bgColor: Color,
    borderColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = bgColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = badge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

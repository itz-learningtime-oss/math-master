package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.PracticeSessionEntity
import com.example.model.PracticeMode
import com.example.model.ScreenDestination
import com.example.ui.components.ShareAppDialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(
    sessions: List<PracticeSessionEntity>,
    currentStreak: Int,
    dailyGoalTarget: Int,
    reminderHour: Int,
    reminderMinute: Int,
    reminderEnabled: Boolean,
    userName: String = "",
    onEditName: () -> Unit = {},
    onNavigatePrivacy: () -> Unit = {},
    onSaveGoal: (Int, Int, Int, Boolean) -> Unit,
    onSendTestNotification: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
    onSelectAnalysis: (PracticeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var targetQuestions by remember(dailyGoalTarget) { mutableIntStateOf(dailyGoalTarget) }
    var hour by remember(reminderHour) { mutableIntStateOf(reminderHour) }
    var minute by remember(reminderMinute) { mutableIntStateOf(reminderMinute) }
    var isEnabled by remember(reminderEnabled) { mutableStateOf(reminderEnabled) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onSendTestNotification()
        }
    }

    // Calculations
    val totalSessions = sessions.size
    val totalQuestionsSolved = sessions.sumOf { it.totalQuestions }
    val totalTimeSec = sessions.sumOf { it.totalTimeSec }
    val avgSpeedPerQ = if (totalQuestionsSolved > 0) totalTimeSec / totalQuestionsSolved else 0.0

    val todayEpochDay = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().timeInMillis)
    val todayQuestions = sessions.filter {
        TimeUnit.MILLISECONDS.toDays(it.timestamp) == todayEpochDay
    }.sumOf { it.totalQuestions }

    val goalProgress = if (targetQuestions > 0) {
        (todayQuestions.toFloat() / targetQuestions).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Developer Attribution Line
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigatePrivacy() }
                    .testTag("dashboard_developer_top_line"),
                color = Color(0xFFEEF2FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE))
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
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "Developed by Vishesh Chaturvedi • Version 3.14",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryIndigo.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Privacy",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_dashboard_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate700
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (userName.isNotBlank()) "$userName's Dashboard" else "Student Dashboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate900
                        )
                        Text(
                            text = "Practice Analytics & Goal Tracker",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate100,
                        modifier = Modifier.clickable(onClick = onEditName)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (userName.isNotBlank()) userName else "Name",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showShareDialog = true },
                        modifier = Modifier.testTag("btn_dashboard_share_app")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share App",
                            tint = PrimaryIndigo
                        )
                    }
                }
            }
        }

        // Streak & Daily Target Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = AccentAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(text = "$currentStreak Day Streak", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text(text = "Practice daily to keep streak", fontSize = 11.sp, color = Slate400)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (goalProgress >= 1f) AccentEmerald.copy(alpha = 0.2f) else PrimaryIndigo.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = if (goalProgress >= 1f) "Goal Completed! 🎉" else "${(goalProgress * 100).toInt()}% Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (goalProgress >= 1f) Color(0xFF6EE7B7) else Color(0xFFA5B4FC),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Today's Progress: $todayQuestions / $targetQuestions problems",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (goalProgress >= 1f) AccentEmerald else PrimaryIndigo,
                        trackColor = Color(0xFF334155)
                    )
                }
            }
        }

        // Overall Aggregate Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "SOLVED",
                    value = "$totalQuestionsSolved",
                    subtitle = "problems",
                    color = AccentBlue,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "AVG SPEED",
                    value = String.format("%.2fs", avgSpeedPerQ),
                    subtitle = "per problem",
                    color = AccentPurple,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "SESSIONS",
                    value = "$totalSessions",
                    subtitle = "completed",
                    color = AccentEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Push Notifications & Daily Goal Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Daily Practice Reminder",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                isEnabled = checked
                                onSaveGoal(targetQuestions, hour, minute, checked)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryIndigo),
                            modifier = Modifier.testTag("switch_daily_reminder")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Target Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Daily Target", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(text = "Questions to solve per day", fontSize = 11.sp, color = Slate400)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate100,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(enabled = targetQuestions > 5) {
                                        targetQuestions -= 5
                                        onSaveGoal(targetQuestions, hour, minute, isEnabled)
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) { Text(text = "-", fontWeight = FontWeight.Bold) }
                            }
                            Text(text = "$targetQuestions", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Slate900)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate100,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(enabled = targetQuestions < 200) {
                                        targetQuestions += 5
                                        onSaveGoal(targetQuestions, hour, minute, isEnabled)
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) { Text(text = "+", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reminder Time Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Reminder Time", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(
                                text = String.format("%02d:%02d %s", if (hour % 12 == 0) 12 else hour % 12, minute, if (hour >= 12) "PM" else "AM"),
                                fontSize = 11.sp,
                                color = Slate500,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        OutlinedButton(
                            onClick = { showTimeDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Change Time", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Notification Button
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    onSendTestNotification()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                onSendTestNotification()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_test_notification"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Slate800)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Test Notification", modifier = Modifier.size(16.dp))
                            Text(text = "Send Test Notification 🔔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Mode Breakdown Cards (tap to view detailed graph)
        item {
            Text(
                text = "PERFORMANCE BY MODE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Slate500,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        val grouped = sessions.groupBy { it.mode }
        PracticeMode.entries.forEach { mode ->
            val modeItems = grouped[mode.id] ?: emptyList()
            val best = modeItems.minOfOrNull { it.totalTimeSec }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectAnalysis(mode) }
                        .testTag("dashboard_mode_${mode.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = mode.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                            Text(text = "${modeItems.size} sessions completed", fontSize = 11.sp, color = Slate400)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (best != null) String.format("%.2fs", best) else "--",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = PrimaryIndigo
                            )
                            Text(text = "Personal Best", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        }
                    }
                }
            }
        }

        // Full History Log & Clear Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT PRACTICE SESSIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
                if (sessions.isNotEmpty()) {
                    TextButton(onClick = { showClearHistoryDialog = true }) {
                        Text(text = "Clear All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentRose)
                    }
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No practice sessions recorded yet. Start training today!", fontSize = 12.sp, color = Slate400)
                    }
                }
            }
        } else {
            items(sessions.take(15)) { session ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = PracticeMode.fromId(session.mode).title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Text(
                                text = "${sdf.format(Date(session.timestamp))} • ${session.rangeInfo}",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = String.format("%.2fs", session.totalTimeSec),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Slate900
                            )
                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Share App Action Card in Dashboard
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showShareDialog = true }
                    .testTag("dashboard_share_card"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
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
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Share Math Master APK",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Send app file directly to friends, students & family",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }

                    Button(
                        onClick = { showShareDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Share",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }
            }
        }

        // Privacy Policy Card in Dashboard
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigatePrivacy() }
                    .testTag("dashboard_privacy_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
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
                                contentDescription = "Privacy",
                                tint = AccentEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Privacy Policy & Trust",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                            Text(
                                text = "Developer: Vishesh Chaturvedi • 100% Free & Local",
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
                            text = "View 📜",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Share Dialog
    if (showShareDialog) {
        ShareAppDialog(onDismiss = { showShareDialog = false })
    }

    // Time Picker Simple Dialog
    if (showTimeDialog) {
        var tempHour by remember { mutableIntStateOf(hour) }
        var tempMin by remember { mutableIntStateOf(minute) }

        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text(text = "Set Reminder Time", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Select daily notification time:", fontSize = 13.sp, color = Slate600)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Selector
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier.clickable { tempHour = (tempHour + 1) % 24 }
                        ) {
                            Text(
                                text = String.format("%02d", tempHour),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Text(text = " : ", fontSize = 28.sp, fontWeight = FontWeight.Black)
                        // Minute Selector
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier.clickable { tempMin = (tempMin + 15) % 60 }
                        ) {
                            Text(
                                text = String.format("%02d", tempMin),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hour = tempHour
                        minute = tempMin
                        onSaveGoal(targetQuestions, hour, minute, isEnabled)
                        showTimeDialog = false
                    }
                ) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(text = "Clear History?", fontWeight = FontWeight.Black) },
            text = { Text(text = "Are you sure you want to delete all saved practice sessions? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                ) {
                    Text(text = "Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Slate400, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color, fontFamily = FontFamily.Monospace)
            Text(text = subtitle, fontSize = 10.sp, color = Slate500)
        }
    }
}

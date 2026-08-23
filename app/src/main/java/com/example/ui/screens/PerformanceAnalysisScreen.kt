package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PracticeSessionEntity
import com.example.model.PracticeMode
import com.example.ui.components.PerformanceLineChart
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PerformanceAnalysisScreen(
    initialMode: PracticeMode,
    allSessions: List<PracticeSessionEntity>,
    lastCompletionTime: Double?,
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(initialMode) }

    val modeSessions = allSessions
        .filter { it.mode == selectedMode.id }
        .sortedBy { it.timestamp }

    val lastTime = if (selectedMode == initialMode && lastCompletionTime != null && lastCompletionTime > 0) {
        lastCompletionTime
    } else {
        modeSessions.lastOrNull()?.totalTimeSec
    }

    val bestTime = modeSessions.minOfOrNull { it.totalTimeSec } ?: lastTime

    val chartData = modeSessions.takeLast(15).map { it.timestamp to it.totalTimeSec }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_analysis_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate700
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Performance Analysis",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selector Pills
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PracticeMode.entries.forEach { m ->
                    val isSel = m == selectedMode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) PrimaryIndigo else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PrimaryIndigo else Slate200),
                        modifier = Modifier
                            .height(38.dp)
                            .clickable { selectedMode = m }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(
                                text = m.title.replace(" Practice", ""),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Slate700
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Last Time vs Personal Best Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LAST TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentBlue,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lastTime != null) String.format("%.2fs", lastTime) else "--",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = AccentBlue
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF1F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PERSONAL BEST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentRose,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (bestTime != null) String.format("%.2fs", bestTime) else "--",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = AccentRose
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Section
            Text(
                text = "SPEED & TIMING PROGRESSION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Slate500,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            PerformanceLineChart(
                times = chartData,
                lineColor = PrimaryIndigo
            )
        }

        // Action: Exit Analysis
        Button(
            onClick = onHome,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_exit_analysis")
        ) {
            Text(text = "Exit Analysis", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

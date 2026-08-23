package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PracticeMode
import com.example.model.QuestionResult
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.viewmodel.GridPlayState

@Composable
fun ResultScreen(
    mode: PracticeMode,
    totalTimeSec: Double,
    results: List<QuestionResult>,
    grid: GridPlayState,
    onHome: () -> Unit,
    onAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGrid = mode == PracticeMode.GRID
    val count = if (isGrid) 36 else results.size
    val avgPerQ = if (count > 0) totalTimeSec / count else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header & Celebration Time Card
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFECFDF5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Success",
                    tint = AccentEmerald,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Practice Complete!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format("%.2f s", totalTimeSec),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = PrimaryIndigo
            )

            Text(
                text = "Total Completion Time",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "PROBLEMS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Slate400)
                        Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Slate900)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "AVG SPEED", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Slate400)
                        Text(text = String.format("%.2f s/q", avgPerQ), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Slate900)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Breakdown Table
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "ITEMIZED TIME REPORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate500,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                if (isGrid) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val answersList = grid.userAnswers.values.toList()
                        itemsIndexed(answersList) { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800,
                                    fontFamily = FontFamily.Monospace
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "${item.value}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PrimaryIndigo,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = String.format("%.2fs", item.timeSec),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate500,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(results) { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.prompt,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate800,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "= ${item.userAnswer}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PrimaryIndigo,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = String.format("%.2fs", item.timeTakenSec),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons: Home & Analysis
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onHome,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Slate700),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_result_home")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(18.dp))
                    Text(text = "Home", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onAnalysis,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_result_analysis")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = "Analysis", modifier = Modifier.size(18.dp), tint = Color.White)
                    Text(text = "Analysis", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

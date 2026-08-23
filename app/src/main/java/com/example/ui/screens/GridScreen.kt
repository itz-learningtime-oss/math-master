package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MathKeypad
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.viewmodel.GridPlayState
import kotlin.math.roundToInt

@Composable
fun GridScreen(
    grid: GridPlayState,
    currentInput: String,
    elapsedSeconds: Double,
    isPaused: Boolean,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = grid.rows
    val cols = grid.cols
    if (rows.size < 5 || cols.size < 5) return

    val currentStep = grid.currentStep
    val userAnswers = grid.userAnswers
    val isError = grid.isError

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -8f at 250
                    8f at 300
                    0f at 400
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate900
                ) {
                    Text(
                        text = "GRID SPEED RUN (${currentStep + 1}/36)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onTogglePause,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Slate700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "Timer", tint = AccentAmber, modifier = Modifier.size(14.dp))
                            Text(
                                text = String.format("%.2f s", elapsedSeconds),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = AccentAmber
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7x7 Matrix Table inside horizontal scroller
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Slate200, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Header Row (Empty, 5 Cols, TOTAL)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        GridCell(text = "", bgColor = Slate100, textColor = Slate700)
                        cols.forEach { c ->
                            GridCell(text = "$c", bgColor = Color(0xFFFEF08A), textColor = Color(0xFF854D0E), isBold = true)
                        }
                        GridCell(text = "TOT", bgColor = Color(0xFFBBF7D0), textColor = Color(0xFF166534), isBold = true)
                    }

                    // 5 Data Rows
                    rows.forEachIndexed { rIdx, rVal ->
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            // Row Header
                            GridCell(text = "$rVal", bgColor = Color(0xFFFEF08A), textColor = Color(0xFF854D0E), isBold = true)

                            // 5 Cells in this row
                            for (cIdx in 0 until 5) {
                                val cellStep = rIdx * 5 + cIdx
                                val cellKey = "r${rIdx}c${cIdx}"
                                val cellData = userAnswers[cellKey]
                                val isActive = currentStep == cellStep

                                val cellBg = when {
                                    isActive && isError -> Color(0xFFFEE2E2)
                                    isActive -> Color(0xFFDBEAFE)
                                    cellData != null -> Color.White
                                    else -> Slate100.copy(alpha = 0.5f)
                                }
                                val cellBorder = if (isActive) AccentBlue else Slate200
                                val cellText = cellData?.value?.toString() ?: if (isActive) currentInput else ""

                                GridCell(
                                    text = cellText,
                                    bgColor = cellBg,
                                    textColor = if (isActive && isError) AccentRose else Slate900,
                                    borderColor = cellBorder,
                                    isBold = true
                                )
                            }

                            // Row Total Cell
                            val rowSumStep = 25 + rIdx
                            val rowSumData = userAnswers["rowSum$rIdx"]
                            val isRowSumActive = currentStep == rowSumStep
                            val rowSumBg = when {
                                isRowSumActive && isError -> Color(0xFFFEE2E2)
                                isRowSumActive -> Color(0xFFDBEAFE)
                                rowSumData != null -> Color(0xFFDCFCE7)
                                else -> Color(0xFFF0FDF4)
                            }
                            val rowSumText = rowSumData?.value?.toString() ?: if (isRowSumActive) currentInput else ""

                            GridCell(
                                text = rowSumText,
                                bgColor = rowSumBg,
                                textColor = Color(0xFF166534),
                                borderColor = if (isRowSumActive) AccentBlue else Color(0xFF86EFAC),
                                isBold = true
                            )
                        }
                    }

                    // Bottom Total Row (Col totals + Grand Total)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        GridCell(text = "TOT", bgColor = Color(0xFFBBF7D0), textColor = Color(0xFF166534), isBold = true)

                        for (cIdx in 0 until 5) {
                            val colSumStep = 30 + cIdx
                            val colSumData = userAnswers["colSum$cIdx"]
                            val isColSumActive = currentStep == colSumStep
                            val colSumBg = when {
                                isColSumActive && isError -> Color(0xFFFEE2E2)
                                isColSumActive -> Color(0xFFDBEAFE)
                                colSumData != null -> Color(0xFFDCFCE7)
                                else -> Color(0xFFF0FDF4)
                            }
                            val colSumText = colSumData?.value?.toString() ?: if (isColSumActive) currentInput else ""

                            GridCell(
                                text = colSumText,
                                bgColor = colSumBg,
                                textColor = Color(0xFF166534),
                                borderColor = if (isColSumActive) AccentBlue else Color(0xFF86EFAC),
                                isBold = true
                            )
                        }

                        // Grand Total Cell (Step 35)
                        val isGrandActive = currentStep == 35
                        val grandData = userAnswers["grand"]
                        val grandBg = when {
                            isGrandActive && isError -> Color(0xFFFEE2E2)
                            isGrandActive -> Color(0xFFDBEAFE)
                            grandData != null -> Color(0xFFDBEAFE)
                            else -> Color(0xFFEFF6FF)
                        }
                        val grandText = grandData?.value?.toString() ?: if (isGrandActive) currentInput else ""

                        GridCell(
                            text = grandText,
                            bgColor = grandBg,
                            textColor = PrimaryIndigo,
                            borderColor = if (isGrandActive) AccentBlue else Color(0xFF93C5FD),
                            isBold = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Target Prompt Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT TARGET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Slate400,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = grid.activePrompt,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Dedicated Keypad
            MathKeypad(
                onDigit = { onInputChange(currentInput + it) },
                onBackspace = { if (currentInput.isNotEmpty()) onInputChange(currentInput.dropLast(1)) },
                onClear = { onInputChange("") },
                onSubmit = onSubmit
            )
        }

        // Pause Overlay
        if (isPaused) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Grid Speed Run Paused", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onTogglePause,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("Resume Speed Run", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun GridCell(
    text: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color = Slate200,
    isBold: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = textColor
        )
    }
}

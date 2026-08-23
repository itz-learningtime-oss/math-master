package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.model.MathQuestion
import com.example.model.PracticeMode
import com.example.ui.components.MathKeypad
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlin.math.roundToInt

@Composable
fun PracticeScreen(
    questions: List<MathQuestion>,
    currentIndex: Int,
    currentInput: String,
    elapsedSeconds: Double,
    isPaused: Boolean,
    isError: Boolean,
    mode: PracticeMode,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (questions.isEmpty()) return

    val currentQ = questions.getOrNull(currentIndex) ?: questions.first()
    val isReverseTable = currentQ.type == "reverse-table"
    val isFactors = mode == PracticeMode.FACTORS || currentQ.type == "factors"
    val isComplex = mode == PracticeMode.COMPLEX

    // Shake animation on error
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
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Question index, Pause/Resume, and Timer badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate100
                ) {
                    Text(
                        text = "Q ${currentIndex + 1} / ${questions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                            .testTag("btn_practice_pause")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = AccentAmber,
                                modifier = Modifier.size(14.dp)
                            )
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

            Spacer(modifier = Modifier.height(16.dp))

            // Main Problem Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        isFactors -> "FIND ANY FACTOR PAIR (A × B = N, A,B ≤ 99)"
                        isReverseTable -> "ENTER FACTOR PAIR (e.g. 24*2)"
                        else -> "SOLVE THIS"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate400,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Prompt Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isError) AccentRose else Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isFactors) {
                            Text(
                                text = "Target Number (3-Digit Non-Prime)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = currentQ.prompt,
                            fontSize = if (isComplex) 22.sp else 38.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate900,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = if (isComplex) 28.sp else 44.sp
                        )
                        if (isFactors) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = "Enter factor pair: A × B = ${currentQ.prompt}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Answer Display Field
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isError) Color(0xFFFFF1F2) else Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (isError) AccentRose else PrimaryIndigo.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("practice_input_display")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentInput.isEmpty()) {
                            Text(
                                text = when {
                                    isFactors -> "Tap keypad (e.g. 36×7) or pick below"
                                    isReverseTable -> "Tap keypad (e.g. 12*2)"
                                    else -> "Enter answer..."
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400
                            )
                        } else {
                            Text(
                                text = currentInput,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (isError) AccentRose else PrimaryIndigo
                            )
                        }
                    }
                }

                // If Factors practice and options are available, show choice chips
                if (isFactors && currentQ.options.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "OR CHOOSE A VALID FACTOR PAIR:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentQ.options.forEach { opt ->
                            val isSelected = currentInput == opt
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryIndigo else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimaryIndigo else Slate200
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onInputChange(opt) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) Color.White else Slate800
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Dedicated On-Screen Speed Keypad
            MathKeypad(
                onDigit = { digit -> onInputChange(currentInput + digit) },
                onBackspace = {
                    if (currentInput.isNotEmpty()) {
                        onInputChange(currentInput.dropLast(1))
                    }
                },
                onClear = { onInputChange("") },
                onSubmit = onSubmit,
                showMultiplySymbol = isReverseTable || isFactors,
                showMinusSymbol = isComplex || mode == PracticeMode.SUBTRACTION,
                showDecimal = isComplex
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
                    Text(
                        text = "Session Paused",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onTogglePause,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier
                            .height(52.dp)
                            .padding(horizontal = 24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.White)
                            Text(text = "Resume Practice", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

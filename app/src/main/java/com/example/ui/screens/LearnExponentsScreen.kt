package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.model.ExponentDisplayType
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun LearnExponentsScreen(
    displayType: ExponentDisplayType,
    selectedRange: String,
    isFlashcardMode: Boolean,
    revealedKeys: Set<String>,
    onToggleType: (ExponentDisplayType) -> Unit,
    onSelectRange: (String) -> Unit,
    onToggleFlashcardMode: () -> Unit,
    onToggleReveal: (String) -> Unit,
    onPractice: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ranges = listOf("2-10", "11-20", "21-30", "31-40", "41-50", "All (2-50)")

    val filteredNumbers = when (selectedRange) {
        "2-10" -> (2..10).toList()
        "11-20" -> (11..20).toList()
        "21-30" -> (21..30).toList()
        "31-40" -> (31..40).toList()
        "41-50" -> (41..50).toList()
        else -> (2..50).toList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_learn_exponents_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate700
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Learn Exponents (x² & x³)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Type Toggle: Squares (x²), Cubes (x³), or Both
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExponentTypeButton(
                    label = "Squares (x²)",
                    isSelected = displayType == ExponentDisplayType.SQUARES,
                    modifier = Modifier.weight(1f)
                ) { onToggleType(ExponentDisplayType.SQUARES) }

                ExponentTypeButton(
                    label = "Cubes (x³)",
                    isSelected = displayType == ExponentDisplayType.CUBES,
                    modifier = Modifier.weight(1f)
                ) { onToggleType(ExponentDisplayType.CUBES) }

                ExponentTypeButton(
                    label = "Both",
                    isSelected = displayType == ExponentDisplayType.BOTH,
                    modifier = Modifier.weight(1f)
                ) { onToggleType(ExponentDisplayType.BOTH) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Range Selector horizontal pills
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ranges.forEach { r ->
                    val isSel = r == selectedRange
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Slate900 else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Slate900 else Slate200),
                        modifier = Modifier
                            .height(34.dp)
                            .clickable { onSelectRange(r) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                            Text(
                                text = r,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Slate700
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Flashcard Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredNumbers.size} NUMBERS IN LIST",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate400,
                    letterSpacing = 1.sp
                )

                OutlinedButton(
                    onClick = onToggleFlashcardMode,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlashcardMode) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Flashcard mode",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isFlashcardMode) "Show All" else "Flashcard Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exponents list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredNumbers) { n ->
                    val showSquare = displayType == ExponentDisplayType.SQUARES || displayType == ExponentDisplayType.BOTH
                    val showCube = displayType == ExponentDisplayType.CUBES || displayType == ExponentDisplayType.BOTH

                    val sqKey = "sq_$n"
                    val cbKey = "cb_$n"

                    val isSqRevealed = !isFlashcardMode || revealedKeys.contains(sqKey)
                    val isCbRevealed = !isFlashcardMode || revealedKeys.contains(cbKey)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "n = $n",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Slate900
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (showSquare) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSqRevealed) Color(0xFFFFFBEB) else Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSqRevealed) Color(0xFFFDE68A) else Slate200),
                                        modifier = Modifier.clickable { if (isFlashcardMode) onToggleReveal(sqKey) }
                                    ) {
                                        Text(
                                            text = if (isSqRevealed) "$n² = ${n * n}" else "$n² = ?",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSqRevealed) Color(0xFFB45309) else Slate500,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                if (showCube) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCbRevealed) Color(0xFFEEF2FF) else Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCbRevealed) Color(0xFFC7D2FE) else Slate200),
                                        modifier = Modifier.clickable { if (isFlashcardMode) onToggleReveal(cbKey) }
                                    ) {
                                        Text(
                                            text = if (isCbRevealed) "$n³ = ${n * n * n}" else "$n³ = ?",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isCbRevealed) PrimaryIndigo else Slate500,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Practice Multiplication Launcher
        Button(
            onClick = onPractice,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_practice_exponents")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = "Practice", tint = Color.White)
                Text(text = "Practice Multiplication Drills", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ExponentTypeButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryIndigo else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryIndigo else Slate200),
        modifier = modifier
            .height(40.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Slate700
            )
        }
    }
}

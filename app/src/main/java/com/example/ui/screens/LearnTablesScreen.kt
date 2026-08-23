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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LearnTableViewMode
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun LearnTablesScreen(
    currentTableNum: Int,
    viewMode: LearnTableViewMode,
    isFlashcardMode: Boolean,
    revealedKeys: Set<String>,
    onSelectTable: (Int) -> Unit,
    onToggleViewMode: (LearnTableViewMode) -> Unit,
    onToggleFlashcardMode: () -> Unit,
    onToggleReveal: (String) -> Unit,
    onPracticeTable: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tableNumbers = (12..37).toList()

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_learn_tables_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate700
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Table of $currentTableNum",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Select Table horizontal pills (12 to 37)
            Text(
                text = "SELECT TABLE (12 TO 37)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Slate400,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tableNumbers.forEach { n ->
                    val isSel = n == currentTableNum
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) PrimaryIndigo else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PrimaryIndigo else Slate200),
                        modifier = Modifier
                            .size(width = 44.dp, height = 38.dp)
                            .clickable { onSelectTable(n) }
                            .testTag("learn_table_pill_$n")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$n",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Slate700
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View Mode Controls: Multiplication vs Division + Flashcard toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate100,
                    modifier = Modifier.weight(1.3f)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        PillTab(
                            text = "Multiplication (×)",
                            isSelected = viewMode == LearnTableViewMode.MULTIPLICATION,
                            modifier = Modifier.weight(1f)
                        ) { onToggleViewMode(LearnTableViewMode.MULTIPLICATION) }
                        PillTab(
                            text = "Division (÷)",
                            isSelected = viewMode == LearnTableViewMode.DIVISION,
                            modifier = Modifier.weight(1f)
                        ) { onToggleViewMode(LearnTableViewMode.DIVISION) }
                    }
                }

                OutlinedButton(
                    onClick = onToggleFlashcardMode,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
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
                            text = if (isFlashcardMode) "Show All" else "Flashcard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1 to 10 Rows
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(10) { idx ->
                    val multiplier = idx + 1
                    val product = currentTableNum * multiplier
                    val key = "${currentTableNum}_$multiplier"
                    val isRevealed = !isFlashcardMode || revealedKeys.contains(key)

                    val leftExpression = if (viewMode == LearnTableViewMode.MULTIPLICATION) {
                        "$currentTableNum × $multiplier"
                    } else {
                        "$product ÷ $currentTableNum"
                    }

                    val rightAnswer = if (viewMode == LearnTableViewMode.MULTIPLICATION) {
                        "$product"
                    } else {
                        "$multiplier"
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isRevealed) Color.White else Color(0xFFEEF2FF),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isRevealed) Slate200 else Color(0xFFC7D2FE)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (isFlashcardMode) onToggleReveal(key) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Slate100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$multiplier",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
                                }
                                Text(
                                    text = leftExpression,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate900
                                )
                            }

                            if (isRevealed) {
                                Text(
                                    text = "= $rightAnswer",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = PrimaryIndigo
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFC7D2FE).copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "Tap to reveal",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Practice Table Button
        Button(
            onClick = { onPracticeTable(currentTableNum) },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_practice_current_table")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = "Practice", tint = Color.White)
                Text(text = "Practice Table $currentTableNum Drills", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun PillTab(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 1.dp else 0.dp,
        modifier = modifier
            .height(34.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryIndigo else Slate500
            )
        }
    }
}

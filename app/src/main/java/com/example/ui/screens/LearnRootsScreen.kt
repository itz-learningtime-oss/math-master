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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RootDisplayType
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun LearnRootsScreen(
    displayType: RootDisplayType,
    selectedRange: String,
    isFlashcardMode: Boolean,
    revealedKeys: Set<String>,
    onToggleType: (RootDisplayType) -> Unit,
    onSelectRange: (String) -> Unit,
    onToggleFlashcardMode: () -> Unit,
    onToggleReveal: (String) -> Unit,
    onPractice: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sqRanges = listOf("1-25", "26-50", "51-75", "76-100", "All (1-100)")
    val cbRanges = listOf("1-10", "11-20", "All (1-20)")

    val ranges = if (displayType == RootDisplayType.SQROOT) sqRanges else cbRanges

    val numbers = when {
        displayType == RootDisplayType.SQROOT && selectedRange == "1-25" -> (1..25).toList()
        displayType == RootDisplayType.SQROOT && selectedRange == "26-50" -> (26..50).toList()
        displayType == RootDisplayType.SQROOT && selectedRange == "51-75" -> (51..75).toList()
        displayType == RootDisplayType.SQROOT && selectedRange == "76-100" -> (76..100).toList()
        displayType == RootDisplayType.SQROOT -> (1..100).toList()
        displayType == RootDisplayType.CBROOT && selectedRange == "1-10" -> (1..10).toList()
        displayType == RootDisplayType.CBROOT && selectedRange == "11-20" -> (11..20).toList()
        else -> (1..20).toList()
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_learn_roots_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate700
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Learn Roots (√ & ∛)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Type Toggle: Square Roots (√) vs Cube Roots (∛)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (displayType == RootDisplayType.SQROOT) PrimaryIndigo else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (displayType == RootDisplayType.SQROOT) PrimaryIndigo else Slate200),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable {
                            onToggleType(RootDisplayType.SQROOT)
                            onSelectRange("1-25")
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Square Roots (√ ≤ 100)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (displayType == RootDisplayType.SQROOT) Color.White else Slate700
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (displayType == RootDisplayType.CBROOT) PrimaryIndigo else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (displayType == RootDisplayType.CBROOT) PrimaryIndigo else Slate200),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable {
                            onToggleType(RootDisplayType.CBROOT)
                            onSelectRange("1-10")
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Cube Roots (∛ ≤ 20)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (displayType == RootDisplayType.CBROOT) Color.White else Slate700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Range horizontal pills
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
                    text = "${numbers.size} ROOTS IN LIST",
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

            // Roots List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(numbers) { n ->
                    val isSq = displayType == RootDisplayType.SQROOT
                    val powerVal = if (isSq) n * n else n * n * n
                    val symbol = if (isSq) "√" else "∛"
                    val key = "${displayType.name}_$n"
                    val isRevealed = !isFlashcardMode || revealedKeys.contains(key)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
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
                            Text(
                                text = "$symbol$powerVal",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Slate900
                            )

                            if (isRevealed) {
                                Text(
                                    text = "= $n",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = AccentEmerald
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFD1FAE5)
                                ) {
                                    Text(
                                        text = "Tap to reveal",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46),
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

        // Practice Roots Launcher
        Button(
            onClick = onPractice,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_practice_roots")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = "Practice", tint = Color.White)
                Text(text = "Practice Roots Drills", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LearnFactorsScreen(
    targetNumber: Int,
    isFlashcardMode: Boolean,
    revealedKeys: Set<String>,
    onSelectNumber: (Int) -> Unit,
    onToggleFlashcardMode: () -> Unit,
    onToggleReveal: (String) -> Unit,
    onPractice: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchInput by remember(targetNumber) { mutableStateOf(targetNumber.toString()) }
    val focusManager = LocalFocusManager.current

    // Dynamically calculate factor pairs for the current target number (A × B = N with A, B <= 99 and A,B >= 2)
    val validFactorPairs = remember(targetNumber) {
        val pairs = mutableListOf<Pair<Int, Int>>()
        val limit = kotlin.math.min(99, sqrt(targetNumber.toDouble()).toInt())
        for (a in 2..limit) {
            if (targetNumber % a == 0) {
                val b = targetNumber / a
                if (b <= 99 && b >= 2) {
                    pairs.add(Pair(a, b))
                }
            }
        }
        pairs.sortedByDescending { it.second }
    }

    val sampleNumbers = listOf(108, 144, 180, 216, 252, 288, 360, 420, 504, 576, 720, 840)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Factors Explorer 🔍",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Slate900
                    )
                    Text(
                        text = "Dynamic factor pairs: A × B = N (≤99)",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate700
                    )
                }
            },
            actions = {
                Button(
                    onClick = onPractice,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Practice",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Number Picker & Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SELECT OR ENTER A 3-DIGIT NUMBER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Number Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchInput,
                                onValueChange = { str ->
                                    if (str.length <= 4 && (str.isEmpty() || str.all { it.isDigit() })) {
                                        searchInput = str
                                        val num = str.toIntOrNull()
                                        if (num != null && num in 10..9999) {
                                            onSelectNumber(num)
                                        }
                                    }
                                },
                                placeholder = { Text("e.g. 252 or 108", color = Slate400, fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchInput.isNotEmpty()) {
                                        IconButton(onClick = {
                                            searchInput = ""
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = Slate400,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    unfocusedBorderColor = Slate200,
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate800,
                                    cursorColor = PrimaryIndigo
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("factors_number_input")
                            )

                            // Random button
                            Button(
                                onClick = {
                                    val r = sampleNumbers.random()
                                    searchInput = r.toString()
                                    onSelectNumber(r)
                                    focusManager.clearFocus()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Slate700),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Random",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Presets
                        Text(
                            text = "Popular Non-Prime 3-Digit Numbers:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(sampleNumbers) { num ->
                                val isSelected = num == targetNumber
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PrimaryIndigo else Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable {
                                        searchInput = num.toString()
                                        onSelectNumber(num)
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Text(
                                        text = num.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Slate700,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Flashcard Toggle Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isFlashcardMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Flashcard mode",
                                tint = if (isFlashcardMode) PrimaryIndigo else Slate500,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Flashcard / Hide Mode",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = if (isFlashcardMode) "Tap cards to reveal factor pairs" else "All factor pairs visible",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }
                        Switch(
                            checked = isFlashcardMode,
                            onCheckedChange = { onToggleFlashcardMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryIndigo
                            )
                        )
                    }
                }
            }

            // Target Number Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FACTORS OF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate400,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = targetNumber.toString(),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = if (validFactorPairs.isNotEmpty()) "${validFactorPairs.size} valid factor pairs (≤99)" else "Prime or no 2-digit factor pairs",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (validFactorPairs.isNotEmpty()) AccentCyan else Color(0xFFFCA5A5),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Factor Pairs Grid
            if (validFactorPairs.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF1F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$targetNumber has no factor pair where both factors are between 2 and 99.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9F1239)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try selecting another composite number like 108, 144, or 252.",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            } else {
                items(validFactorPairs) { pair ->
                    val key = "factor_${targetNumber}_${pair.first}_${pair.second}"
                    val isRevealed = !isFlashcardMode || revealedKeys.contains(key)
                    val formula = "${pair.second} × ${pair.first}"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                if (isFlashcardMode) {
                                    onToggleReveal(key)
                                }
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRevealed) Color.White else Color(0xFFF8FAFC)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isRevealed) PrimaryIndigo.copy(alpha = 0.3f) else Slate300
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isRevealed) 2.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isRevealed) Color(0xFFEEF2FF) else Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${pair.first}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isRevealed) PrimaryIndigo else Slate500
                                    )
                                }

                                Column {
                                    if (isRevealed) {
                                        Text(
                                            text = formula,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "$formula = $targetNumber",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentEmerald
                                        )
                                    } else {
                                        Text(
                                            text = "Tap to reveal factor pair",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate500
                                        )
                                        Text(
                                            text = "A × ${pair.first} = $targetNumber",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = PrimaryIndigo
                                        )
                                    }
                                }
                            }

                            if (isRevealed) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Valid",
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE2E8F0)
                                ) {
                                    Text(
                                        text = "???",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate600,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

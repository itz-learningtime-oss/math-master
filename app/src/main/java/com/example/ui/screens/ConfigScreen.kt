package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PracticeMode
import com.example.model.RootMode
import com.example.model.ScreenDestination
import com.example.model.TableSelectionMode
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.viewmodel.PracticeConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfigScreen(
    config: PracticeConfig,
    onUpdateConfig: ((PracticeConfig) -> PracticeConfig) -> Unit,
    onToggleTable: (Int) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
    onNavigateStudy: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val mode = config.mode

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Back Button and Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_config_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Slate700
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${mode.title} Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Slate900
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrollable settings form
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode-specific configuration blocks
            when (mode) {
                PracticeMode.ADDITION, PracticeMode.SUBTRACTION, PracticeMode.MULTIPLICATION -> {
                    item {
                        ConfigRangeInput(
                            title = "Number Range",
                            minVal = config.minRange,
                            maxVal = config.maxRange,
                            onMinChange = { newMin -> onUpdateConfig { it.copy(minRange = newMin) } },
                            onMaxChange = { newMax -> onUpdateConfig { it.copy(maxRange = newMax) } },
                            presets = listOf(
                                "1-Digit" to (1 to 9),
                                "2-Digit" to (10 to 99),
                                "3-Digit" to (100 to 999),
                                "4-Digit" to (1000 to 9999)
                            )
                        )
                    }

                    item {
                        ConfigStepper(
                            title = "Numbers per Question",
                            value = config.numsPerQuestion,
                            min = 2,
                            max = 5,
                            onChange = { onUpdateConfig { c -> c.copy(numsPerQuestion = it) } }
                        )
                    }

                    item {
                        ConfigCountPills(
                            title = "Number of Questions",
                            selectedCount = config.totalQuestions,
                            counts = listOf(5, 10, 20, 30),
                            onSelect = { onUpdateConfig { c -> c.copy(totalQuestions = it) } }
                        )
                    }

                    if (mode == PracticeMode.MULTIPLICATION) {
                        item {
                            StudyBannerCard(
                                title = "Need to review exponents or tables?",
                                subtitle = "Study 12-37 tables or x² / x³ powers",
                                buttonText = "Exponents ⚡",
                                color = AccentAmber,
                                onClick = { onNavigateStudy(ScreenDestination.LearnExponents(ScreenDestination.Config(mode))) }
                            )
                        }
                    }
                }

                PracticeMode.DIVISION -> {
                    item {
                        StudyBannerCard(
                            title = "Review Division & Tables",
                            subtitle = "Study tables 12 to 37 before practicing",
                            buttonText = "Learn Tables 📖",
                            color = PrimaryIndigo,
                            onClick = { onNavigateStudy(ScreenDestination.LearnTables(ScreenDestination.Config(mode))) }
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFECFDF5),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Numbers ending in 0 are automatically excluded for clean calculation drills.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                            }
                        }
                    }

                    item {
                        ConfigRangeInput(
                            title = "Dividend Range (Greater Number)",
                            minVal = config.dividendMin,
                            maxVal = config.dividendMax,
                            onMinChange = { onUpdateConfig { c -> c.copy(dividendMin = it) } },
                            onMaxChange = { onUpdateConfig { c -> c.copy(dividendMax = it) } },
                            presets = listOf(
                                "2-Digit" to (10 to 99),
                                "3-Digit" to (100 to 999),
                                "4-Digit" to (1000 to 9999)
                            )
                        )
                    }

                    item {
                        ConfigRangeInput(
                            title = "Divisor Range (Smaller Number)",
                            minVal = config.divisorMin,
                            maxVal = config.divisorMax,
                            onMinChange = { onUpdateConfig { c -> c.copy(divisorMin = it) } },
                            onMaxChange = { onUpdateConfig { c -> c.copy(divisorMax = it) } },
                            presets = listOf(
                                "Single" to (2 to 9),
                                "2-Digit" to (10 to 99),
                                "12-37" to (12 to 37)
                            )
                        )
                    }

                    item {
                        ConfigCountPills(
                            title = "Number of Questions",
                            selectedCount = config.totalQuestions,
                            counts = listOf(5, 10, 20, 30),
                            onSelect = { onUpdateConfig { c -> c.copy(totalQuestions = it) } }
                        )
                    }
                }

                PracticeMode.TABLES -> {
                    item {
                        Column {
                            Text(
                                text = "Select Tables to Practice (12 to 37)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // 6-col grid of selectable numbers 2 to 37
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (tableNum in 2..37) {
                                    val isSelected = config.selectedTables.contains(tableNum)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFFEEF2FF) else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) PrimaryIndigo else Slate200
                                        ),
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clickable { onToggleTable(tableNum) }
                                            .testTag("table_select_$tableNum")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$tableNum",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) PrimaryIndigo else Slate700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column {
                            Text(
                                text = "Question Selection Mode",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PillOption(
                                    label = "All Combinations (2-9)",
                                    isSelected = config.tableSelectionMode == TableSelectionMode.COMBINATIONS,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onUpdateConfig { it.copy(tableSelectionMode = TableSelectionMode.COMBINATIONS) }
                                }
                                PillOption(
                                    label = "Specific Count",
                                    isSelected = config.tableSelectionMode == TableSelectionMode.COUNT,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onUpdateConfig { it.copy(tableSelectionMode = TableSelectionMode.COUNT) }
                                }
                            }
                        }
                    }

                    if (config.tableSelectionMode == TableSelectionMode.COUNT) {
                        item {
                            ConfigCountPills(
                                title = "Number of Questions",
                                selectedCount = config.totalQuestions,
                                counts = listOf(5, 10, 20, 30),
                                onSelect = { onUpdateConfig { c -> c.copy(totalQuestions = it) } }
                            )
                        }
                    }
                }

                PracticeMode.ROOTS -> {
                    item {
                        StudyBannerCard(
                            title = "Need to review roots first?",
                            subtitle = "Study √1-100 & ∛1-20 rules",
                            buttonText = "Learn Roots 🌱",
                            color = AccentEmerald,
                            onClick = { onNavigateStudy(ScreenDestination.LearnRoots(ScreenDestination.Config(mode))) }
                        )
                    }

                    item {
                        Column {
                            Text(
                                text = "Practice Type",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PillOption(
                                    label = "Square Root (√)",
                                    isSelected = config.rootMode == RootMode.SQROOT,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onUpdateConfig { it.copy(rootMode = RootMode.SQROOT) }
                                }
                                PillOption(
                                    label = "Cube Root (∛)",
                                    isSelected = config.rootMode == RootMode.CBROOT,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onUpdateConfig { it.copy(rootMode = RootMode.CBROOT) }
                                }
                                PillOption(
                                    label = "Mixed (√ & ∛)",
                                    isSelected = config.rootMode == RootMode.BOTH,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onUpdateConfig { it.copy(rootMode = RootMode.BOTH) }
                                }
                            }
                        }
                    }

                    if (config.rootMode == RootMode.SQROOT || config.rootMode == RootMode.BOTH) {
                        item {
                            ConfigRangeInput(
                                title = "Square Root Base Range (up to 100)",
                                minVal = config.sqRootMin,
                                maxVal = config.sqRootMax,
                                onMinChange = { onUpdateConfig { c -> c.copy(sqRootMin = it.coerceIn(1, 100)) } },
                                onMaxChange = { onUpdateConfig { c -> c.copy(sqRootMax = it.coerceIn(1, 100)) } },
                                presets = listOf(
                                    "1 - 25" to (1 to 25),
                                    "1 - 50" to (1 to 50),
                                    "1 - 100" to (1 to 100)
                                )
                            )
                        }
                    }

                    if (config.rootMode == RootMode.CBROOT || config.rootMode == RootMode.BOTH) {
                        item {
                            ConfigRangeInput(
                                title = "Cube Root Base Range (up to 20)",
                                minVal = config.cbRootMin,
                                maxVal = config.cbRootMax,
                                onMinChange = { onUpdateConfig { c -> c.copy(cbRootMin = it.coerceIn(1, 20)) } },
                                onMaxChange = { onUpdateConfig { c -> c.copy(cbRootMax = it.coerceIn(1, 20)) } },
                                presets = listOf(
                                    "1 - 10" to (1 to 10),
                                    "1 - 15" to (1 to 15),
                                    "1 - 20" to (1 to 20)
                                )
                            )
                        }
                    }

                    item {
                        ConfigCountPills(
                            title = "Number of Questions",
                            selectedCount = config.totalQuestions,
                            counts = listOf(5, 10, 20, 30),
                            onSelect = { onUpdateConfig { c -> c.copy(totalQuestions = it) } }
                        )
                    }
                }

                PracticeMode.FACTORS -> {
                    item {
                        StudyBannerCard(
                            title = "Need to explore factor pairs?",
                            subtitle = "Interactive factor explorer for any 3-digit number",
                            buttonText = "Factors Explorer 🔍",
                            color = PrimaryIndigo,
                            onClick = { onNavigateStudy(ScreenDestination.LearnFactors(ScreenDestination.Config(mode))) }
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "3-Digit Factors Rule (A × B = N, A, B ≤ 99)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Presents a random 3-digit non-prime number (e.g. 252 or 108).\n• Enter or select any valid factor pair where both factors are ≤ 99.\n• Example: 108 = 54×2, 36×3, 27×4, 18×6, 12×9\n• Example: 252 = 84×3, 63×4, 42×6, 36×7, 28×9, 21×12, 18×14",
                                    fontSize = 11.sp,
                                    color = Slate700,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    item {
                        ConfigRangeInput(
                            title = "3-Digit Number Range (Non-Prime)",
                            minVal = config.factorsMin,
                            maxVal = config.factorsMax,
                            onMinChange = { onUpdateConfig { c -> c.copy(factorsMin = it.coerceIn(100, 999)) } },
                            onMaxChange = { onUpdateConfig { c -> c.copy(factorsMax = it.coerceIn(100, 999)) } },
                            presets = listOf(
                                "100 - 300" to (100 to 300),
                                "100 - 500" to (100 to 500),
                                "100 - 999" to (100 to 999)
                            )
                        )
                    }

                    item {
                        ConfigCountPills(
                            title = "Number of Questions",
                            selectedCount = config.totalQuestions,
                            counts = listOf(5, 10, 15, 20),
                            onSelect = { onUpdateConfig { c -> c.copy(totalQuestions = it) } }
                        )
                    }
                }

                PracticeMode.COMPLEX -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF5F3FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Format: Difference between Sum(x, y) and Average(a, b)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPurple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tests dual-path mental arithmetic and instant mental subtraction.",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }

                    item {
                        ConfigCountPills(
                            title = "Number of Questions",
                            selectedCount = config.totalQuestions,
                            counts = listOf(5, 10, 20),
                            onSelect = { onUpdateConfig { c -> c.copy(totalQuestions = it) } }
                        )
                    }
                }

                PracticeMode.GRID -> {
                    item {
                        ConfigRangeInput(
                            title = "Matrix Number Range (Multiples of 10 Excluded)",
                            minVal = config.minRange,
                            maxVal = config.maxRange,
                            onMinChange = { onUpdateConfig { c -> c.copy(minRange = it) } },
                            onMaxChange = { onUpdateConfig { c -> c.copy(maxRange = it) } },
                            presets = listOf(
                                "1-Digit" to (1 to 9),
                                "2-Digit" to (10 to 99),
                                "3-Digit" to (100 to 999)
                            )
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Slate900,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "5×5 Speed Matrix Run",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Includes 25 cell additions + 5 row sums + 5 column sums + 1 grand total with per-step timing analysis.",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Start Practice Button
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("btn_start_practice"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    tint = Color.White
                )
                Text(
                    text = "Start Practice",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ConfigRangeInput(
    title: String,
    minVal: Int,
    maxVal: Int,
    onMinChange: (Int) -> Unit,
    onMaxChange: (Int) -> Unit,
    presets: List<Pair<String, Pair<Int, Int>>>? = null
) {
    var minStr by remember(minVal) { mutableStateOf(minVal.toString()) }
    var maxStr by remember(maxVal) { mutableStateOf(maxVal.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryIndigoLight
                ) {
                    Text(
                        text = "$minVal to $maxVal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (!presets.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (label, range) ->
                        val isSelected = minVal == range.first && maxVal == range.second
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryIndigo else Slate100,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryIndigo else Slate200
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clickable {
                                    onMinChange(range.first)
                                    onMaxChange(range.second)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Slate700
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minStr,
                    onValueChange = { str ->
                        val digits = str.filter { it.isDigit() }
                        minStr = digits
                        digits.toIntOrNull()?.let { onMinChange(it) }
                    },
                    label = {
                        Text(
                            text = "Min",
                            color = Slate600,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        disabledTextColor = Slate500,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Slate300,
                        focusedLabelColor = PrimaryIndigo,
                        unfocusedLabelColor = Slate600,
                        cursorColor = PrimaryIndigo
                    )
                )

                Text(
                    text = "to",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500
                )

                OutlinedTextField(
                    value = maxStr,
                    onValueChange = { str ->
                        val digits = str.filter { it.isDigit() }
                        maxStr = digits
                        digits.toIntOrNull()?.let { onMaxChange(it) }
                    },
                    label = {
                        Text(
                            text = "Max",
                            color = Slate600,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        disabledTextColor = Slate500,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Slate300,
                        focusedLabelColor = PrimaryIndigo,
                        unfocusedLabelColor = Slate600,
                        cursorColor = PrimaryIndigo
                    )
                )
            }
        }
    }
}

@Composable
fun ConfigStepper(
    title: String,
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate100,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(enabled = value > min) { onChange(value - 1) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    }
                }
                Text(
                    text = "$value",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate100,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(enabled = value < max) { onChange(value + 1) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigCountPills(
    title: String,
    selectedCount: Int,
    counts: List<Int>,
    onSelect: (Int) -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Slate700,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            counts.forEach { c ->
                val isSelected = c == selectedCount
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryIndigo else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) PrimaryIndigo else Slate200
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { onSelect(c) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$c",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Slate700
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PillOption(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryIndigo else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryIndigo else Slate200
        ),
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Slate700
            )
        }
    }
}

@Composable
fun StudyBannerCard(
    title: String,
    subtitle: String,
    buttonText: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate500
                )
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

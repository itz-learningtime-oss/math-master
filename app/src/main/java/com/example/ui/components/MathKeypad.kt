package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun MathKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    showMultiplySymbol: Boolean = false,
    showMinusSymbol: Boolean = false,
    showDecimal: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = Color(0xFFF1F5F9),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: 1, 2, 3, Clear / Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadButton("1", modifier = Modifier.weight(1f)) { onDigit("1") }
                KeypadButton("2", modifier = Modifier.weight(1f)) { onDigit("2") }
                KeypadButton("3", modifier = Modifier.weight(1f)) { onDigit("3") }
                KeypadIconButton(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Backspace",
                            tint = Slate700
                        )
                    },
                    backgroundColor = Slate200,
                    modifier = Modifier.weight(1f),
                    testTag = "keypad_backspace",
                    onClick = onBackspace
                )
            }

            // Row 2: 4, 5, 6, special symbol or clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadButton("4", modifier = Modifier.weight(1f)) { onDigit("4") }
                KeypadButton("5", modifier = Modifier.weight(1f)) { onDigit("5") }
                KeypadButton("6", modifier = Modifier.weight(1f)) { onDigit("6") }
                if (showMultiplySymbol) {
                    KeypadButton(
                        text = "×",
                        backgroundColor = Color(0xFFE0E7FF),
                        textColor = PrimaryIndigo,
                        modifier = Modifier.weight(1f)
                    ) { onDigit("*") }
                } else if (showMinusSymbol) {
                    KeypadButton(
                        text = "-",
                        backgroundColor = Color(0xFFFEE2E2),
                        textColor = Color(0xFFB91C1C),
                        modifier = Modifier.weight(1f)
                    ) { onDigit("-") }
                } else {
                    KeypadButton(
                        text = "C",
                        backgroundColor = Slate200,
                        textColor = Slate700,
                        modifier = Modifier.weight(1f),
                        onClick = onClear
                    )
                }
            }

            // Row 3: 7, 8, 9, Decimal / Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadButton("7", modifier = Modifier.weight(1f)) { onDigit("7") }
                KeypadButton("8", modifier = Modifier.weight(1f)) { onDigit("8") }
                KeypadButton("9", modifier = Modifier.weight(1f)) { onDigit("9") }
                if (showDecimal) {
                    KeypadButton(".", modifier = Modifier.weight(1f)) { onDigit(".") }
                } else if (showMultiplySymbol && showMinusSymbol) {
                    KeypadButton(
                        text = "-",
                        backgroundColor = Color(0xFFFEE2E2),
                        textColor = Color(0xFFB91C1C),
                        modifier = Modifier.weight(1f)
                    ) { onDigit("-") }
                } else {
                    KeypadButton(
                        text = "C",
                        backgroundColor = Slate200,
                        textColor = Slate700,
                        modifier = Modifier.weight(1f),
                        onClick = onClear
                    )
                }
            }

            // Row 4: 0, and SUBMIT / ENTER button spanning columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadButton("0", modifier = Modifier.weight(1f)) { onDigit("0") }

                if (showMultiplySymbol && !showDecimal) {
                    KeypadButton(
                        text = "C",
                        backgroundColor = Slate200,
                        textColor = Slate700,
                        modifier = Modifier.weight(1f),
                        onClick = onClear
                    )
                }

                // Submit button
                KeypadSubmitButton(
                    modifier = Modifier.weight(if (showMultiplySymbol && !showDecimal) 2f else 3f),
                    onClick = onSubmit
                )
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    textColor: Color = Slate800,
    testTag: String = "keypad_$text",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "scale")

    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun KeypadIconButton(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Slate200,
    testTag: String = "keypad_icon",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "scale")

    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun KeypadSubmitButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(PrimaryIndigo)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .testTag("keypad_submit"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "SUBMIT",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Submit",
                tint = Color.White
            )
        }
    }
}

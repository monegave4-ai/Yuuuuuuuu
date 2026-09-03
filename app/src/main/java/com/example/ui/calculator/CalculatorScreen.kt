package com.example.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var input by remember { mutableStateOf("0") }
    var previousInput by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf<String?>(null) }

    val format = DecimalFormat("#.########")

    fun onAction(action: String) {
        when (action) {
            "AC" -> {
                input = "0"
                previousInput = ""
                operation = null
            }
            "⌫" -> {
                if (input.length > 1) {
                    input = input.dropLast(1)
                } else {
                    input = "0"
                }
            }
            "÷", "×", "-", "+" -> {
                if (operation != null && input == "0") {
                    operation = action
                    return
                }
                if (previousInput.isNotEmpty()) {
                    // calculate previous before starting new
                    val prev = previousInput.toDoubleOrNull() ?: 0.0
                    val current = input.toDoubleOrNull() ?: 0.0
                    val result = when (operation) {
                        "+" -> prev + current
                        "-" -> prev - current
                        "×" -> prev * current
                        "÷" -> if (current != 0.0) prev / current else 0.0
                        else -> current
                    }
                    previousInput = format.format(result).replace(",", ".")
                } else {
                    previousInput = input
                }
                input = "0"
                operation = action
            }
            "=" -> {
                if (previousInput.isNotEmpty() && operation != null) {
                    val prev = previousInput.toDoubleOrNull() ?: 0.0
                    val current = input.toDoubleOrNull() ?: 0.0
                    val result = when (operation) {
                        "+" -> prev + current
                        "-" -> prev - current
                        "×" -> prev * current
                        "÷" -> if (current != 0.0) prev / current else 0.0
                        else -> current
                    }
                    input = format.format(result).replace(",", ".")
                    previousInput = ""
                    operation = null
                }
            }
            "." -> {
                if (!input.contains(".")) {
                    input += "."
                }
            }
            "%" -> {
                val current = input.toDoubleOrNull() ?: 0.0
                input = format.format(current / 100.0).replace(",", ".")
            }
            else -> {
                if (input == "0" && action != ".") {
                    input = action
                } else {
                    input += action
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (previousInput.isNotEmpty() || operation != null) {
                    Text(
                        text = "$previousInput ${operation ?: ""}",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 24.sp,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
                Text(
                    text = input,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad
            val buttons = listOf(
                listOf("AC", "⌫", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { btn ->
                        val isOperator = btn in listOf("÷", "×", "-", "+", "=")
                        val isAction = btn in listOf("AC", "⌫", "%")
                        val isZero = btn == "0"
                        
                        val bgColor = when {
                            isOperator -> MaterialTheme.colorScheme.primary
                            isAction -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        
                        val textColor = when {
                            isOperator -> MaterialTheme.colorScheme.onPrimary
                            isAction -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(if (isZero) 2.2f else 1f)
                                .aspectRatio(if (isZero) 2.2f else 1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(bgColor)
                                .clickable { onAction(btn) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = btn,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

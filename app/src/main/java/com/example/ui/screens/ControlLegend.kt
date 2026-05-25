package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayStationGlyph(
    symbol: String,
    modifier: Modifier = Modifier
) {
    val symbolColor = when (symbol) {
        "△" -> Color(0xFF4EEF74) // Green Triangle
        "○" -> Color(0xFFFF5252) // Red Circle
        "×" -> Color(0xFF42A5F5) // Blue Cross
        "□" -> Color(0xFFEC407A) // Pink Square
        else -> Color.White
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(20.dp)
            .border(1.dp, Color(0x66FFFFFF), CircleShape)
            .background(Color(0x33000000), CircleShape)
    ) {
        Text(
            text = symbol,
            color = symbolColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.offset(y = (-0.5).dp)
        )
    }
}

@Composable
fun ControlLegendItem(
    symbol: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        PlayStationGlyph(symbol = symbol)
        Text(
            text = label,
            color = Color(0xCCFFFFFF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun ControlLegendBar(
    actions: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        actions.forEach { (symbol, label) ->
            ControlLegendItem(symbol = symbol, label = label)
        }
    }
}

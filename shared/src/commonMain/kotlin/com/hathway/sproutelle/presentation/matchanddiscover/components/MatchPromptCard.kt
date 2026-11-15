package com.hathway.sproutelle.presentation.matchanddiscover.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MatchPromptCard(
    promptText: String,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left 3-Ray Yellow Burst
        SparkleBurstRays(isLeft = true, isTablet = isTablet)

        Surface(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 6.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFFFFDE7),
            border = BorderStroke(
                width = if (isTablet) 2.5.dp else 2.dp,
                color = Color(0xFFFFD54F)
            ),
            shadowElevation = 2.dp
        ) {
            Text(
                text = promptText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (isTablet) 19.sp else 15.sp,
                    lineHeight = if (isTablet) 23.sp else 19.sp
                ),
                color = Color(0xFF1B365D),
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        // Right 3-Ray Yellow Burst
        SparkleBurstRays(isLeft = false, isTablet = isTablet)
    }
}

@Composable
private fun SparkleBurstRays(isLeft: Boolean, isTablet: Boolean) {
    val size = if (isTablet) 26.dp else 20.dp
    Canvas(modifier = Modifier.size(size)) {
        val stroke = 3.dp.toPx()
        val dir = if (isLeft) -1f else 1f
        val startX = if (isLeft) this.size.width * 0.85f else this.size.width * 0.15f
        val midY = this.size.height * 0.5f

        // Top diagonal ray
        drawLine(
            color = Color(0xFFFFCA28),
            start = Offset(startX, midY - 4.dp.toPx()),
            end = Offset(startX + dir * 12.dp.toPx(), midY - 10.dp.toPx()),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Middle horizontal ray
        drawLine(
            color = Color(0xFFFFCA28),
            start = Offset(startX, midY),
            end = Offset(startX + dir * 14.dp.toPx(), midY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Bottom diagonal ray
        drawLine(
            color = Color(0xFFFFCA28),
            start = Offset(startX, midY + 4.dp.toPx()),
            end = Offset(startX + dir * 12.dp.toPx(), midY + 10.dp.toPx()),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

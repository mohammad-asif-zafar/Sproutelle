package com.hathway.sproutelle.presentation.matchanddiscover.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MatchBottomActionBar(
    isAllCorrect: Boolean,
    isLastQuestion: Boolean,
    stars: Int = 2,
    maxStars: Int = 3,
    onShuffleClick: () -> Unit,
    onHintClick: () -> Unit,
    onNextClick: () -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Button
        Surface(
            modifier = Modifier
                .height(if (isTablet) 54.dp else 46.dp)
                .clickable { onShuffleClick() },
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF7C4DFF),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🔀", fontSize = 15.sp)
                Text(
                    text = "Shuffle",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isTablet) 16.sp else 14.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Hint & Dynamic Stars Rating Pill Indicator
        Surface(
            modifier = Modifier
                .height(if (isTablet) 54.dp else 46.dp)
                .clickable { onHintClick() },
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            border = BorderStroke(2.dp, Color(0xFFFFB74D)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("💡", fontSize = 15.sp)
                repeat(maxStars) { index ->
                    val isEarned = index < stars
                    Text(
                        text = if (isEarned) "⭐" else "☆",
                        fontSize = 14.sp,
                        color = if (isEarned) Color.Unspecified else Color(0xFFB0BEC5)
                    )
                }
            }
        }

        // Next / Results Action Button
        Button(
            onClick = onNextClick,
            enabled = isAllCorrect,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                disabledContainerColor = Color(0xFFC8E6C9)
            ),
            shape = RoundedCornerShape(26.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .height(if (isTablet) 54.dp else 46.dp)
                .width(if (isTablet) 150.dp else 112.dp)
        ) {
            Text(
                text = if (isLastQuestion) "Results ➔" else "Next ➔",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (isTablet) 16.sp else 14.sp
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

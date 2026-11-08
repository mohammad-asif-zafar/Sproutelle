package com.hathway.sproutelle.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object QuestProgressBar {

    @Composable
    operator fun invoke(
        currentQuestionIndex: Int,
        totalQuestions: Int,
        completedIndices: Set<Int>,
        modifier: Modifier = Modifier
    ) {
        val progress = if (totalQuestions > 0) {
            completedIndices.size.toFloat() / totalQuestions.toFloat()
        } else 0f

        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUESTION ${currentQuestionIndex + 1} / $totalQuestions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF1B365D),
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "${completedIndices.size} Completed",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }
}

// ==========================================
// MULTIPLE PREVIEWS
// ==========================================

@Preview(name = "Progress - Game Start (0/10)")
@Composable
private fun ProgressStartPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestProgressBar(currentQuestionIndex = 0, totalQuestions = 10, completedIndices = emptySet())
        }
    }
}

@Preview(name = "Progress - Halfway Done (5/10)")
@Composable
private fun ProgressHalfwayPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestProgressBar(currentQuestionIndex = 5, totalQuestions = 10, completedIndices = setOf(0, 1, 2, 3, 4))
        }
    }
}

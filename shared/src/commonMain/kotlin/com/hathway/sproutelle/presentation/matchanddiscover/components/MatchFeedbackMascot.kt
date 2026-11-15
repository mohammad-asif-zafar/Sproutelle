package com.hathway.sproutelle.presentation.matchanddiscover.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Mascot 🦊 & Feedback Status Badges (✔ Right Answer! / ✖ Wrong Answer!) matching the reference asset.
 */
@Composable
fun MatchFeedbackMascot(
    pairResults: Map<String, Boolean>,
    isAllCorrect: Boolean,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val hasWrong = pairResults.values.any { !it }
    val hasRight = pairResults.values.any { it }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
    ) {
        // Fox Mascot 🦊
        Text(
            text = if (hasWrong) "🤔" else "🦊",
            fontSize = if (isTablet) 56.sp else 42.sp
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Right Answer Pill
            if (hasRight || isAllCorrect) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✔", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Right Answer!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isTablet) 15.sp else 13.sp
                            ),
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Wrong Answer Pill
            if (hasWrong) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFFFFEBEE),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE53935)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✖", color = Color(0xFFE53935), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Wrong Answer!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isTablet) 15.sp else 13.sp
                            ),
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

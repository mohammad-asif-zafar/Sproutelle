package com.hathway.sproutelle.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A highly visual, kid-friendly header component. Separates navigational buttons
 * from instruction texts into distinct vertical layers to eliminate overlap clipping bugs.
 */
object QuestHeader {

    @Composable
    operator fun invoke(
        difficulty: String,
        onBackClick: () -> Unit,
        onHomeClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp) // Soft vertical separation budget
        ) {
            // ==========================================
            // LAYER 1: NAVIGATION & LEVEL CONTROLS
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Navigation Actions Cluster
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tactile 3D Comic Back Arrow Disc Button
                    Box(modifier = Modifier.size(40.dp)) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { onBackClick() },
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Go Back",
                                    tint = Color(0xFF007AFF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Tactile Reusable Spring Home Button Component
                    SproutelleHomeButton(
                        onClick = onHomeClick,
                        contentPadding = PaddingValues(0.dp),
                        buttonSize = 40.dp
                    )
                }

                // Right Difficulty Level Badge Layer
                val badgeModifier = Modifier.padding(top = 2.dp)
                when (difficulty.trim().lowercase()) {
                    "easy" -> GameLevelBadge(
                        levelText = "Easy",
                        icon = "🌱",
                        modifier = badgeModifier,
                        backgroundColor = Color(0xFFE8F5E9),
                        borderColor = Color(0xFF81C784),
                        textColor = Color(0xFF2E7D32)
                    )
                    "medium" -> GameLevelBadge(
                        levelText = "Medium",
                        icon = "⚡",
                        modifier = badgeModifier,
                        backgroundColor = Color(0xFFFFE0B2),
                        borderColor = Color(0xFFFFB74D),
                        textColor = Color(0xFFE65100)
                    )
                    else -> GameLevelBadge(
                        levelText = "Hard",
                        icon = "⭐",
                        modifier = badgeModifier,
                        backgroundColor = Color(0xFFFFF493),
                        borderColor = Color(0xFFFBC02D),
                        textColor = Color(0xFF5D4037)
                    )
                }
            }


        }
    }
}

// ==========================================
// DYNAMIC COMPONENT PREVIEWS
// ==========================================

@Preview(name = "Header - Easy Level (Phone Blueprint)", widthDp = 360, showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun HeaderEasyPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(12.dp)) {
            QuestHeader(difficulty = "Easy", onBackClick = {}, onHomeClick = {})
        }
    }
}

@Preview(name = "Header - Hard Level (Tablet Blueprint)", widthDp = 600, showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun HeaderHardTabletPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestHeader(difficulty = "Hard", onBackClick = {}, onHomeClick = {})
        }
    }
}

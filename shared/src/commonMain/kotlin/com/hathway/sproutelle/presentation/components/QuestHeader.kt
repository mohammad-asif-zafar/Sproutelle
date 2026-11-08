package com.hathway.sproutelle.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.hathway.sproutelle.presentation.components.GameLevelBadge
import com.hathway.sproutelle.presentation.components.SproutelleHomeButton

object QuestHeader {

    @Composable
    operator fun invoke(
        difficulty: String,
        onBackClick: () -> Unit,
        onHomeClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onBackClick() },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp
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

                SproutelleHomeButton(
                    onClick = onHomeClick,
                    contentPadding = PaddingValues(0.dp),
                    buttonSize = 40.dp
                )
            }

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

// ==========================================
// MULTIPLE PREVIEWS
// ==========================================

@Preview(name = "Header - Easy Level (Phone)", device = "spec:width=411dp,height=100dp")
@Composable
private fun HeaderEasyPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestHeader(difficulty = "Easy", onBackClick = {}, onHomeClick = {})
        }
    }
}

@Preview(name = "Header - Hard Level (Tablet)", device = "spec:width=1280dp,height=100dp,orientation=landscape")
@Composable
private fun HeaderHardTabletPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            QuestHeader(difficulty = "Hard", onBackClick = {}, onHomeClick = {})
        }
    }
}
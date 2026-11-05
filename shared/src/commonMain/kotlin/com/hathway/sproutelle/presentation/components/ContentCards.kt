package com.hathway.sproutelle.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.Subcategory
import theme.SproutelleColors
import theme.SproutelleShapes

@Composable
fun CategoryCard(
    item: ExplorerItem,
    isTablet: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = parseColorHex(item.backgroundColor)

    if (isTablet) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(128.dp)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = SproutelleShapes.Large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardIllustration(imageUrl = item.imageUrl, title = item.titleName, size = 96.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.titleName,
                        style = MaterialTheme.typography.titleMedium,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    val description = getCategoryDescription(item.titleName)
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SproutelleColors.SecondaryText,
                            maxLines = 2,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = SproutelleShapes.Pill,
                        color = Color(0xFFFFEAD0)
                    ) {
                        Text(
                            text = "Ages 3–8",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB35900),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SproutelleColors.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "›",
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
            }
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(175.dp)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = SproutelleShapes.Large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                CardIllustration(imageUrl = item.imageUrl, title = item.titleName, size = 100.dp)

                Text(
                    text = item.titleName,
                    style = MaterialTheme.typography.titleSmall,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = SproutelleShapes.Pill,
                        color = Color(0xFFFFEAD0)
                    ) {
                        Text(
                            text = "Ages 3–8",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB35900),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(SproutelleColors.White, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            color = SproutelleColors.Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MobileSubcategoryCard(
    item: Subcategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = parseColorHex(item.backgroundColor)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
        shape = SproutelleShapes.Large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(bgColor, shape = SproutelleShapes.Medium),
                contentAlignment = Alignment.Center
            ) {
                CardIllustration(imageUrl = item.imageUrl, title = item.name, size = 52.dp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ages 3–8",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SproutelleColors.SecondaryText
                )
            }

            Text(
                text = "›",
                color = SproutelleColors.Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun SubcategoryCard(
    item: Subcategory,
    isTablet: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = parseColorHex(item.backgroundColor)

    if (isTablet) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(110.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
            shape = SproutelleShapes.Large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(bgColor, shape = SproutelleShapes.Medium),
                    contentAlignment = Alignment.Center
                ) {
                    CardIllustration(imageUrl = item.imageUrl, title = item.name, size = 60.dp)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ages 3–8",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SproutelleColors.SecondaryText
                    )
                }
            }
        }
    } else {
        MobileSubcategoryCard(
            item = item,
            onClick = onClick,
            modifier = modifier
        )
    }
}

@Composable
fun GameCard(
    game: GameItem,
    isTablet: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconEmoji = when {
        game.name.contains("Star", ignoreCase = true) || game.name.contains("Count", ignoreCase = true) -> "⭐"
        game.name.contains("Puzzle", ignoreCase = true) || game.name.contains("Match", ignoreCase = true) -> "🧩"
        game.gameType.lowercase() in listOf("garden_quiz", "quiz") -> "❓"
        game.gameType.lowercase() == "image_selection" -> "🖼️"
        game.gameType.lowercase() == "spelling_bee" -> "🐝"
        else -> "🎮"
    }

    val typeBadgeText = when (game.gameType.lowercase()) {
        "garden_quiz" -> "Garden Quiz"
        "image_selection" -> "Image Match"
        "spelling_bee" -> "Spelling Bee"
        else -> "Mini Game"
    }

    val thumbnailBgColor = when (iconEmoji) {
        "⭐" -> Color(0xFFE8DEF8)
        "🧩" -> Color(0xFFE1F5FE)
        "🐝" -> Color(0xFFFFF9C4)
        "❓" -> Color(0xFFFFE0B2)
        else -> Color(0xFFE8F5E9)
    }

    val badgeBgColor = when (typeBadgeText) {
        "Mini Game" -> Color(0xFFEDE7F6)
        "Full Game" -> Color(0xFFFFF3E0)
        else -> SproutelleColors.Peach
    }

    val badgeTextColor = when (typeBadgeText) {
        "Mini Game" -> Color(0xFF673AB7)
        "Full Game" -> Color(0xFFE65100)
        else -> SproutelleColors.Ink
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 18.dp else 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 72.dp else 64.dp)
                        .background(thumbnailBgColor, shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconEmoji, fontSize = if (isTablet) 36.sp else 30.sp)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = game.name,
                        style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = SproutelleShapes.Pill,
                            color = badgeBgColor
                        ) {
                            Text(
                                text = typeBadgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        if (game.minAge != null && game.maxAge != null) {
                            Surface(
                                shape = SproutelleShapes.Pill,
                                color = Color(0xFFFFEAD0)
                            ) {
                                Text(
                                    text = "Ages ${game.minAge}–${game.maxAge}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFB35900),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF4CAF50), shape = CircleShape)
                    .size(if (isTablet) 48.dp else 42.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 18.sp else 15.sp
                )
            }
        }
    }
}

@Composable
private fun CardIllustration(imageUrl: String?, title: String, size: Dp) {
    var isError by remember { mutableStateOf(false) }

    if (!imageUrl.isNullOrBlank() && !isError) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit,
            onError = { isError = true }
        )
    }

    if (imageUrl.isNullOrBlank() || isError) {
        Text(
            text = when (title.lowercase()) {
                "letters", "letters & reading" -> "ABC"
                "numbers", "numbers & maths" -> "123"
                "animals", "animals & nature" -> "🦁"
                "shapes", "shapes & colours" -> "🎨"
                "guess fruits", "fruits" -> "🍎"
                else -> "🌱"
            },
            fontSize = (size.value * 0.4f).sp
        )
    }
}

private fun getCategoryDescription(title: String): String {
    return when (title.lowercase()) {
        "numbers & maths" -> "Counting, number sense, early arithmetic and patterns"
        "letters & reading" -> "Alphabet, phonics, vocabulary and early reading"
        "animals & nature" -> "Animal knowledge, natural environments and living things"
        "shapes & colours" -> "Visual recognition, classification and patterns"
        "creative studio" -> "Drawing, colouring, creating and imaginative play"
        "everyday world" -> "Familiar objects, daily routines and the world around us"
        else -> "Fun interactive learning for kids"
    }
}

private fun parseColorHex(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorLong = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color(colorLong or 0xFF000000)
        } else {
            Color(colorLong)
        }
    } catch (e: Exception) {
        SproutelleColors.Mint
    }
}

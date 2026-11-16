/*
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

fun getPastelBgColor(index: Int): Color {
    return when (index % 4) {
        0 -> Color(0xFFFFF8E1) // Soft warm yellow
        1 -> Color(0xFFF3E5F5) // Soft lavender / purple
        2 -> Color(0xFFE1F5FE) // Soft sky blue
        else -> Color(0xFFFCE4EC) // Soft pink
    }
}

@Composable
fun GameCard(
    game: GameItem,
    isTablet: Boolean,
    cardIndex: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isTablet) {
        TabletGameCard(
            game = game,
            cardIndex = cardIndex,
            onClick = onClick,
            modifier = modifier
        )
    } else {
        MobileGameCard(
            game = game,
            cardIndex = cardIndex,
            onClick = onClick,
            modifier = modifier
        )
    }
}

@Composable
fun MobileGameCard(
    game: GameItem,
    cardIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBgColor = getPastelBgColor(cardIndex)

    val iconEmoji = when {
        game.name.contains("Star", ignoreCase = true) || game.name.contains("Count", ignoreCase = true) -> "⭐"
        game.name.contains("Puzzle", ignoreCase = true) || game.name.contains("Match", ignoreCase = true) -> "🍎"
        game.gameType.lowercase() in listOf("garden_quiz", "quiz") -> "❓"
        game.gameType.lowercase() == "image_selection" -> "🖼️"
        game.gameType.lowercase() == "spelling_bee" || game.name.contains("Bee", ignoreCase = true) -> "🐝"
        else -> "🎮"
    }

    val badgeText = if (game.minAge != null && game.maxAge != null) {
        "Ages ${game.minAge}–${game.maxAge}"
    } else when (game.gameType.lowercase()) {
        "image_selection" -> "Image Match"
        "spelling_bee" -> "Spelling Bee"
        "garden_quiz" -> "Garden Quiz"
        else -> "Mini Game"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        .size(56.dp)
                        .background(Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconEmoji, fontSize = 28.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1B365D),
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = SproutelleShapes.Pill,
                        color = Color.White.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF4CAF50), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun TabletGameCard(
    game: GameItem,
    cardIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBgColor = getPastelBgColor(cardIndex)

    val iconEmoji = when {
        game.name.contains("Star", ignoreCase = true) || game.name.contains("Count", ignoreCase = true) -> "⭐"
        game.name.contains("Puzzle", ignoreCase = true) || game.name.contains("Match", ignoreCase = true) -> "🍎"
        game.gameType.lowercase() in listOf("garden_quiz", "quiz") -> "❓"
        game.gameType.lowercase() == "image_selection" -> "🖼️"
        game.gameType.lowercase() == "spelling_bee" || game.name.contains("Bee", ignoreCase = true) -> "🐝"
        else -> "🎮"
    }

    val badgeText = if (game.minAge != null && game.maxAge != null) {
        "Ages ${game.minAge}–${game.maxAge}"
    } else when (game.gameType.lowercase()) {
        "image_selection" -> "Image Match"
        "spelling_bee" -> "Spelling Bee"
        "garden_quiz" -> "Garden Quiz"
        else -> "Mini Game"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 56.sp)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1B365D),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = SproutelleShapes.Pill,
                        color = Color.White.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF4CAF50), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White,
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
*/

package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.Subcategory
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.Subcategories_bg_mobile
import sproutelle.shared.generated.resources.Subcategories_bg_tablet
import sproutelle.shared.generated.resources.ic_cat_animals_nature
import sproutelle.shared.generated.resources.ic_cat_creative_studio
import sproutelle.shared.generated.resources.ic_cat_everyday_world
import sproutelle.shared.generated.resources.ic_cat_fruits
import sproutelle.shared.generated.resources.ic_cat_language
import sproutelle.shared.generated.resources.ic_cat_letters_reading
import sproutelle.shared.generated.resources.ic_cat_memory_logic
import sproutelle.shared.generated.resources.ic_cat_music_sounds
import sproutelle.shared.generated.resources.ic_cat_numbers_maths
import sproutelle.shared.generated.resources.ic_cat_puzzles_thinking
import sproutelle.shared.generated.resources.ic_cat_science_discovery
import sproutelle.shared.generated.resources.ic_cat_shapes_colours
import sproutelle.shared.generated.resources.ic_cat_social_emotional
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
    val rimColor = lerp(bgColor, Color.Black, 0.14f)
    val borderColor = lerp(bgColor, Color.Black, 0.08f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "CategoryCardScale"
    )

    val cardHeight = if (isTablet) 138.dp else 192.dp
    val cardShape = SproutelleShapes.Large
    val ageLabel = "Ages 3–8"

    Box(
        modifier = modifier.fillMaxWidth().height(cardHeight + 4.dp).graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource, indication = null, onClick = onClick
                )
            } else {
                Modifier
            }
        )
    ) {
        // 3D Bottom Rim Shadow
        Box(
            modifier = Modifier.fillMaxWidth().height(cardHeight).offset(y = 4.dp)
                .background(rimColor, shape = cardShape)
        )

        Surface(
            modifier = Modifier.fillMaxWidth().height(cardHeight),
            color = bgColor,
            shape = cardShape,
            border = BorderStroke(2.dp, borderColor),
            shadowElevation = 2.dp
        ) {
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CardIllustration(
                        imageUrl = item.imageUrl, title = item.titleName, size = 98.dp
                    )

                    Column(
                        modifier = Modifier.weight(1f).padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.titleName,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                            color = Color(0xFF1B365D),
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        val description = getCategoryDescription(item.titleName)
                        if (description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF546E7A),
                                maxLines = 2,
                                lineHeight = 15.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        AgePillBadge(text = ageLabel, fontSize = 11.sp)
                    }

                    ChevronCircleButton(size = 38.dp)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    CardIllustration(
                        imageUrl = item.imageUrl, title = item.titleName, size = 100.dp
                    )

                    Text(
                        text = item.titleName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            lineHeight = 19.sp
                        ),
                        color = Color(0xFF1B365D),
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AgePillBadge(text = ageLabel, fontSize = 10.sp)
                        ChevronCircleButton(size = 32.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgePillBadge(text: String = "Ages 3–8", fontSize: androidx.compose.ui.unit.TextUnit) {
    Surface(
        shape = SproutelleShapes.Pill,
        color = Color(0xFFFFEAD0),
        border = BorderStroke(1.dp, Color(0xFFFFCC80))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFB35900),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ChevronCircleButton(size: Dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFE0E0E0)),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open Category",
                tint = Color(0xFF1B365D),
                modifier = Modifier.size(size * 0.62f)
            )
        }
    }
}

@Composable
fun MobileSubcategoryCard(
    item: Subcategory, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val bgColor = parseColorHex(item.backgroundColor)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "MobileSubcategoryScale"
    )

    Surface(
        modifier = modifier.fillMaxWidth().height(88.dp).graphicsLayer {
            scaleX = scale

            scaleY = scale
        }.clickable(
            interactionSource = interactionSource, indication = null, onClick = onClick
        ),
        color = SproutelleColors.White,
        shape = SproutelleShapes.Large,
        border = BorderStroke(2.dp, Color(0xFFE1F5FE)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(68.dp)
                    .background(bgColor, shape = SproutelleShapes.Medium),
                contentAlignment = Alignment.Center
            ) {
                CardIllustration(imageUrl = item.imageUrl, title = item.name, size = 54.dp)
            }
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = Color(0xFF1B365D),
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                AgePillBadge(fontSize = 11.sp)
            }
            ChevronCircleButton(size = 34.dp)
        }
    }
}

@Composable
fun SubcategoryCard(
    item: Subcategory, isTablet: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val bgColor = parseColorHex(item.backgroundColor)
    if (isTablet) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "TabletSubcategoryScale"
        )
        Surface(
            modifier = modifier.fillMaxWidth().height(110.dp).graphicsLayer {
                scaleX = scale
                scaleY = scale
            }.clickable(
                interactionSource = interactionSource, indication = null, onClick = onClick
            ),
            color = SproutelleColors.White,
            shape = SproutelleShapes.Large,
            border = BorderStroke(2.dp, Color(0xFFE1F5FE)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(80.dp)
                        .background(bgColor, shape = SproutelleShapes.Medium),
                    contentAlignment = Alignment.Center
                ) {
                    CardIllustration(imageUrl = item.imageUrl, title = item.name, size = 62.dp)
                }
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                        color = Color(0xFF1B365D),
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        lineHeight = 22.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AgePillBadge(fontSize = 11.sp)
                }
                ChevronCircleButton(size = 38.dp)
            }
        }
    } else {
        MobileSubcategoryCard(
            item = item, onClick = onClick, modifier = modifier
        )
    }
}

fun getPastelBgColor(index: Int): Color {
    return when (index % 5) {
        0 -> Color(0xFFFFF8E7) // Warm cream
        1 -> Color(0xFFF3EEFF) // Soft lavender
        2 -> Color(0xFFE6F4FF) // Soft sky blue
        3 -> Color(0xFFFCEBF3) // Soft blush pink
        else -> Color(0xFFE8F7EB) // Soft mint green
    }
}

@Composable
fun GameCard(
    game: GameItem,
    isTablet: Boolean,
    cardIndex: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isTablet) {
        TabletGameCard(
            game = game, cardIndex = cardIndex, onClick = onClick, modifier = modifier
        )
    } else {
        MobileGameCard(
            game = game, cardIndex = cardIndex, onClick = onClick, modifier = modifier
        )
    }
}

@Composable
fun MobileGameCard(
    game: GameItem, cardIndex: Int, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val cardBgColor = getPastelBgColor(cardIndex)
    val rimColor = lerp(cardBgColor, Color.Black, 0.14f)
    val iconEmoji = when {
        game.name.contains("Star", ignoreCase = true) || game.name.contains(
            "Count", ignoreCase = true
        ) -> "⭐"

        game.name.contains("Puzzle", ignoreCase = true) || game.name.contains(
            "Match", ignoreCase = true
        ) -> "🍎"

        game.gameType.lowercase() in listOf("garden_quiz", "quiz") -> "❓"
        game.gameType.lowercase() == "image_selection" -> "🖼️"
        game.gameType.lowercase() == "spelling_bee" || game.name.contains(
            "Bee", ignoreCase = true
        ) -> "🐝"

        else -> "🎮"
    }
    val badgeText = if (game.minAge != null && game.maxAge != null) {
        "Ages ${game.minAge}–${game.maxAge}"
    } else when (game.gameType.lowercase()) {
        "image_selection" -> "Image Match"
        "spelling_bee" -> "Spelling Bee"
        "garden_quiz" -> "Garden Quiz"
        else -> "Mini Game"
    }
    Box(
        modifier = modifier.fillMaxWidth().clickable { onClick() }) {
        Box(
            modifier = Modifier.matchParentSize().offset(y = 3.dp)
                .background(rimColor, shape = RoundedCornerShape(26.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = cardBgColor,
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.5.dp, Color.White),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).background(Color.White, shape = CircleShape)
                            .border(1.5.dp, rimColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iconEmoji, fontSize = 28.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1B365D),
                            fontWeight = FontWeight.Black
                        )
                        Surface(
                            shape = SproutelleShapes.Pill, color = Color.White.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                GlossyPlayDisc(size = 44.dp)
            }
        }
    }
}

@Composable
fun TabletGameCard(
    game: GameItem, cardIndex: Int, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val cardBgColor = getPastelBgColor(cardIndex)
    val rimColor = lerp(cardBgColor, Color.Black, 0.14f)
    val iconEmoji = when {
        game.name.contains("Star", ignoreCase = true) || game.name.contains(
            "Count", ignoreCase = true
        ) -> "⭐"

        game.name.contains("Puzzle", ignoreCase = true) || game.name.contains(
            "Match", ignoreCase = true
        ) -> "🍎"

        game.gameType.lowercase() in listOf("garden_quiz", "quiz") -> "❓"
        game.gameType.lowercase() == "image_selection" -> "🖼️"
        game.gameType.lowercase() == "spelling_bee" || game.name.contains(
            "Bee", ignoreCase = true
        ) -> "🐝"

        else -> "🎮"
    }
    val badgeText = if (game.minAge != null && game.maxAge != null) {
        "Ages ${game.minAge}–${game.maxAge}"
    } else when (game.gameType.lowercase()) {
        "image_selection" -> "Image Match"
        "spelling_bee" -> "Spelling Bee"
        "garden_quiz" -> "Garden Quiz"
        else -> "Mini Game"
    }
    Box(
        modifier = modifier.fillMaxWidth().height(234.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier.fillMaxWidth().height(230.dp).offset(y = 4.dp)
                .background(rimColor, shape = RoundedCornerShape(32.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth().height(230.dp),
            color = cardBgColor,
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(2.dp, Color.White),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconEmoji, fontSize = 56.sp)
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = game.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1B365D),
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = SproutelleShapes.Pill, color = Color.White.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        GlossyPlayDisc(size = 44.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlossyPlayDisc(size: Dp) {
    Box(
        modifier = Modifier.size(size), contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize().offset(y = 2.5.dp)
                .background(Color(0xFF2E7D32), CircleShape)
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF81C784), Color(0xFF43A047))
                ), shape = CircleShape
            ).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

@Composable
private fun CardIllustration(imageUrl: String?, title: String, size: Dp) {
    var isError by remember(imageUrl) { mutableStateOf(false) }

    val localResource = remember(title) {
        val clean = title.lowercase()
        when {
            clean.contains("fruit") -> Res.drawable.ic_cat_fruits
            clean.contains("letter") || clean.contains("read") || clean.contains("alphabet") -> Res.drawable.ic_cat_letters_reading
            clean.contains("number") || clean.contains("math") || clean.contains("count") -> Res.drawable.ic_cat_numbers_maths
            clean.contains("shape") || clean.contains("colour") || clean.contains("color") -> Res.drawable.ic_cat_shapes_colours
            clean.contains("creative") || clean.contains("studio") || clean.contains("art") -> Res.drawable.ic_cat_creative_studio
            clean.contains("animal") || clean.contains("nature") -> Res.drawable.ic_cat_animals_nature
            clean.contains("everyday") || clean.contains("world") -> Res.drawable.ic_cat_everyday_world
            clean.contains("memory") || clean.contains("logic") -> Res.drawable.ic_cat_memory_logic
            clean.contains("musi") || clean.contains("sound") -> Res.drawable.ic_cat_music_sounds
            clean.contains("puzzle") || clean.contains("think") -> Res.drawable.ic_cat_puzzles_thinking
            clean.contains("science") || clean.contains("discover") -> Res.drawable.ic_cat_science_discovery
            clean.contains("social") || clean.contains("emotion") -> Res.drawable.ic_cat_social_emotional
            clean.contains("language") -> Res.drawable.ic_cat_language
            else -> null
        }
    }

    when {
        localResource != null -> {
            Image(
                painter = painterResource(localResource),
                contentDescription = title,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Fit
            )
        }
        !imageUrl.isNullOrBlank() && !isError -> {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Fit,
                onError = { isError = true }
            )
        }
        else -> {
            Text(
                text = "🌱",
                fontSize = (size.value * 0.44f).sp
            )
        }
    }
}

private fun getCategoryDescription(title: String): String {
    val clean = title.lowercase()
    return when {
        clean.contains("number") || clean.contains("math") -> "Counting, number sense, early arithmetic and patterns"
        clean.contains("letter") || clean.contains("read") -> "Alphabet, phonics, vocabulary and early reading"
        clean.contains("animal") || clean.contains("nature") -> "Animal knowledge, natural environments and living things"
        clean.contains("shape") || clean.contains("colour") -> "Visual recognition, classification and patterns"
        clean.contains("creative") || clean.contains("studio") -> "Drawing, colouring, creating and imaginative play"
        clean.contains("everyday") || clean.contains("world") -> "Familiar objects, daily routines and the world around us"
        clean.contains("memory") || clean.contains("logic") -> "Pattern matching, recall and visual memory games"
        clean.contains("music") || clean.contains("sound") -> "Rhythm, animal sounds and musical discovery"
        clean.contains("puzzle") || clean.contains("think") -> "Brain teasers, sorting and problem-solving fun"
        clean.contains("science") || clean.contains("discover") -> "Curiosity, nature experiments and how things work"
        clean.contains("social") || clean.contains("emotion") -> "Feelings, kindness, sharing and empathy"
        clean.contains("language") -> "First words, greetings and global vocabulary"
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

// ==========================================
// PREVIEWS
// ==========================================
@Preview(name = "Category Cards - Tablet", widthDp = 760, showBackground = true)
@Composable
private fun CategoryCardsTabletPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryCard(
                item = ExplorerItem("1", "Creative Studio", null, "#FCE4EC", "c1", 1, true),
                isTablet = true,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                item = ExplorerItem("2", "Memory & Logic", null, "#FFF8E1", "c2", 2, true),
                isTablet = true,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}
package com.hathway.sproutelle.presentation.wordbuilder.components

import ScrambledLetterTiles
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hathway.sproutelle.presentation.wordbuilder.WordLetterTile
import kotlinx.coroutines.delay

@Composable
fun LetterTileItem(
    tile: WordLetterTile,
    tileWidth: Dp,
    tileHeight: Dp,
    isTablet: Boolean,
    onClick: () -> Unit,
    entranceIndex: Int = 0
) {
    // Staggered spring entrance when a new question loads
    val entranceScale = remember(tile.id) { Animatable(0.6f) }
    LaunchedEffect(tile.id) {
        delay(entranceIndex * 40L)
        entranceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TilePressScale"
    )

    val tileBgColor = Color(tile.tileColorHex)
    val isWhiteTile = tile.tileColorHex == 0xFFFFFFFFL || tile.tileColorHex == 0xFFFFFFFF
    val textColor = if (isWhiteTile) Color(0xFF0D47A1) else Color.White
    val borderColor = if (isWhiteTile) Color(0xFF81D4FA) else Color.White.copy(alpha = 0.8f)

    Box(modifier = Modifier.size(tileWidth, tileHeight)) {
        // Keeps layout grid stable when tile is placed into a slot
        Spacer(modifier = Modifier.fillMaxSize())

        AnimatedVisibility(
            visible = !tile.isUsed,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = scaleOut(targetScale = 0.5f) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val combined = entranceScale.value * pressScale
                        scaleX = combined
                        scaleY = combined
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            ) {
                // 3D Shadow Plate Rim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = 3.dp)
                        .background(
                            color = Color(0xFF37474F).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp)
                        )
                )

                // Foreground 3D Tactile Tile
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
                    color = tileBgColor,
                    border = BorderStroke(2.dp, borderColor),
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tile.letter.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = if (isTablet) 26.sp else 20.sp
                            ),
                            color = textColor,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Scrambled Tiles", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun ScrambledLetterTilesPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ScrambledLetterTiles(
                availableTiles = listOf(
                    WordLetterTile("1", 'S', false, 0xFFFFFFFF),
                    WordLetterTile("2", 'N', false, 0xFFFF5252),
                    WordLetterTile("3", 'F', true, 0xFF4CAF50),
                    WordLetterTile("4", 'A', false, 0xFFFFFFFF),
                    WordLetterTile("5", 'O', false, 0xFF4CAF50),
                    WordLetterTile("6", 'X', false, 0xFFFFFFFF),
                    WordLetterTile("7", 'Z', false, 0xFFFFFFFF)
                ),
                isTablet = false,
                onTileClick = {}
            )
        }
    }
}
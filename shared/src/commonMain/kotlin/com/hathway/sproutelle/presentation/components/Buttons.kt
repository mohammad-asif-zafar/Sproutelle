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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleShapes

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = SproutelleColors.LeafGreen,
    contentColor: Color = SproutelleColors.OnPrimary,
    icon: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "PrimaryButtonScale"
    )
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed && enabled) 2.5f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "PrimaryButtonOffset"
    )

    val activeBg = if (enabled) containerColor else SproutelleColors.Disabled
    val rimColor = lerp(activeBg, Color.Black, 0.25f)
    val highlightColor = lerp(activeBg, Color.White, 0.22f)
    val buttonHeight = SproutelleDimens.ButtonHeight

    Box(
        modifier = modifier.fillMaxWidth().height(buttonHeight + 4.dp).graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        // 3D Bottom Rim Shadow
        Box(
            modifier = Modifier.fillMaxWidth().height(buttonHeight).offset(y = 4.dp)
                .background(rimColor, shape = SproutelleShapes.Pill)
        )

        // Foreground Tactile Pill
        Surface(
            modifier = Modifier.fillMaxWidth().height(buttonHeight).offset(y = pressOffsetY.dp),
            shape = SproutelleShapes.Pill,
            color = Color.Transparent,
            border = BorderStroke(2.dp, Color.White.copy(alpha = if (enabled) 0.85f else 0.4f)),
            shadowElevation = if (enabled) 3.dp else 0.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(highlightColor, activeBg)
                    )
                ), contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (icon != null) {
                        Text(text = icon, fontSize = 19.sp)
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = if (enabled) contentColor else SproutelleColors.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = SproutelleColors.Peach,
    contentColor: Color = SproutelleColors.Ink,
    icon: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "SecondaryButtonScale"
    )
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed && enabled) 2f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "SecondaryButtonOffset"
    )

    val activeBg = if (enabled) containerColor else SproutelleColors.Disabled
    val rimColor = lerp(activeBg, Color.Black, 0.2f)
    val buttonHeight = SproutelleDimens.CompactButtonHeight

    Box(
        modifier = modifier.height(buttonHeight + 3.dp).graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        // 3D Bottom Rim
        Box(
            modifier = Modifier.matchParentSize().padding(top = 3.dp)
                .background(rimColor, shape = SproutelleShapes.Pill)
        )

        Surface(
            modifier = Modifier.height(buttonHeight).offset(y = pressOffsetY.dp),
            shape = SproutelleShapes.Pill,
            color = activeBg,
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon != null) {
                    Text(text = icon, fontSize = 15.sp)
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                    color = if (enabled) contentColor else SproutelleColors.White,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}


@Composable
fun RoundedIconButton(
    icon: String = "←",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = if (icon == "←") Icons.AutoMirrored.Rounded.ArrowBack else null,
    backgroundColor: Color = SproutelleColors.White,
    contentColor: Color = Color(0xFF1B365D),
    buttonSize: Dp = SproutelleDimens.MinimumTouchTarget,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "IconButtonScale"
    )
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) 2f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "IconButtonOffset"
    )

    val rimColor = lerp(backgroundColor, Color(0xFF90A4AE), 0.35f)

    Box(modifier = modifier.size(buttonSize).graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.clickable(
            interactionSource = interactionSource, indication = null, onClick = onClick
        ), contentAlignment = Alignment.Center) {
        // Soft 3D Bottom Shadow Disc
        Box(
            modifier = Modifier.fillMaxSize().offset(y = 3.dp).background(rimColor, CircleShape)
        )

        // Foreground Crisp Circular Button
        Surface(
            modifier = Modifier.fillMaxSize().offset(y = pressOffsetY.dp),
            shape = CircleShape,
            color = backgroundColor,
            border = BorderStroke(2.dp, Color(0xFFE1F5FE)),
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (imageVector != null) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(buttonSize * 0.52f)
                    )
                } else {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                        color = contentColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ParentButton(
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "ParentButtonScale"
    )
    Surface(
        shape = SproutelleShapes.Pill,
        color = SproutelleColors.White.copy(alpha = 0.96f),
        border = BorderStroke(2.dp, Color(0xFFE1F5FE)),
        shadowElevation = 3.dp,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.clickable(
            interactionSource = interactionSource, indication = null, onClick = onClick
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(22.dp).background(Color(0xFFE1F5FE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 12.sp)
            }
            Text(
                text = "Parent",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = Color(0xFF1B365D),
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================
@Preview(name = "Sproutelle Tactile Buttons", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun SproutelleButtonsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundedIconButton(icon = "←", onClick = {})
                ParentButton(onClick = {})
                SecondaryButton(text = "Try Again", icon = "🔄", onClick = {})
            }
            PrimaryButton(
                text = "Continue Adventure", icon = "🚀", onClick = {})
            PrimaryButton(
                text = "Disabled State", enabled = false, onClick = {})
        }
    }
}

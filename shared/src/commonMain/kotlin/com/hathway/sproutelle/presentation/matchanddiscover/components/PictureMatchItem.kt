package com.hathway.sproutelle.presentation.matchanddiscover.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.hathway.sproutelle.presentation.matchanddiscover.PictureNode

@Composable
fun PictureMatchItem(
    node: PictureNode,
    accentColor: Color,
    isTablet: Boolean,
    onAnchorPositioned: (Offset) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else if (node.isSelected) 1.03f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "PictureScale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            node.isSelected -> accentColor
            node.isCorrect == true -> Color(0xFF4CAF50)
            node.isCorrect == false -> Color(0xFFE53935)
            else -> Color(0xFF90CAF9)
        },
        label = "PictureBorderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            node.isSelected -> accentColor.copy(alpha = 0.14f)
            node.isCorrect == true -> Color(0xFFE8F5E9)
            node.isCorrect == false -> Color(0xFFFFEBEE)
            else -> Color.White
        },
        label = "PictureContainerColor"
    )

    val nodeColor by animateColorAsState(
        targetValue = when {
            node.isCorrect == false -> Color(0xFFE53935)
            node.isMatched || node.isSelected -> accentColor
            else -> accentColor.copy(alpha = 0.85f)
        },
        label = "PictureNodeColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isTablet) 68.dp else 56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (node.isSelected || node.isMatched) 2.5.dp else 2.dp,
            color = borderColor
        ),
        shadowElevation = if (node.isSelected) 5.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Connection Node Circle ⊙
            Box(
                modifier = Modifier
                    .size(if (isTablet) 24.dp else 20.dp)
                    .onGloballyPositioned { coords ->
                        onAnchorPositioned(coords.boundsInRoot().center)
                    }
                    .background(Color.White, shape = CircleShape)
                    .border(2.dp, nodeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 11.dp else 9.dp)
                        .background(nodeColor, shape = CircleShape)
                )
            }

            // Picture Image or Emoji
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!node.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = node.imageUrl,
                        contentDescription = "Picture",
                        modifier = Modifier
                            .size(if (isTablet) 46.dp else 38.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = node.emoji,
                        fontSize = if (isTablet) 34.sp else 26.sp
                    )
                }
            }
        }
    }
}

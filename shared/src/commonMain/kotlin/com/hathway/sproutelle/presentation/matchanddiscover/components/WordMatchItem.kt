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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hathway.sproutelle.presentation.matchanddiscover.WordNode

@Composable
fun WordMatchItem(
    node: WordNode,
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
        label = "WordScale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            node.isSelected -> accentColor
            node.isCorrect == true -> Color(0xFF4CAF50)
            node.isCorrect == false -> Color(0xFFE53935)
            else -> Color(0xFF90CAF9)
        },
        label = "WordBorderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            node.isSelected -> accentColor.copy(alpha = 0.14f)
            node.isCorrect == true -> Color(0xFFE8F5E9)
            node.isCorrect == false -> Color(0xFFFFEBEE)
            else -> Color.White
        },
        label = "WordContainerColor"
    )

    val nodeColor by animateColorAsState(
        targetValue = when {
            node.isCorrect == false -> Color(0xFFE53935)
            node.isMatched || node.isSelected -> accentColor
            else -> accentColor.copy(alpha = 0.85f)
        },
        label = "WordNodeColor"
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
        // Inside WordMatchItem.kt Row:
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🔊",
                fontSize = if (isTablet) 16.sp else 13.sp,
                modifier = Modifier.padding(end = 4.dp)
            )

            Text(
                text = node.word,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (isTablet) 18.sp else 15.sp,
                    lineHeight = if (isTablet) 21.sp else 17.sp
                ),
                color = Color(0xFF0D47A1),
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Right Connection Node Circle ⊙
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
        }
    }
       /* Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = node.word,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (isTablet) 18.sp else 15.sp,
                    lineHeight = if (isTablet) 21.sp else 17.sp
                ),
                color = Color(0xFF0D47A1),
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Right Connection Node Circle ⊙
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
        }
    }*/
}
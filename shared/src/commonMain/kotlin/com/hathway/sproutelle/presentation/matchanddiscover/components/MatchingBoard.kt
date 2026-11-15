package com.hathway.sproutelle.presentation.matchanddiscover.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hathway.sproutelle.presentation.matchanddiscover.PictureNode
import com.hathway.sproutelle.presentation.matchanddiscover.WordNode
import kotlin.math.hypot
import kotlinx.coroutines.launch

val MatchRowColors = listOf(
    Color(0xFF4CAF50), Color(0xFF03A9F4), Color(0xFF9C27B0), Color(0xFFFF9800)
)

@Composable
fun MatchingBoard(
    wordNodes: List<WordNode>,
    pictureNodes: List<PictureNode>,
    matchedPairs: Map<String, String>,
    pairResults: Map<String, Boolean>,
    isTablet: Boolean,
    onWordClick: (String) -> Unit,
    onPictureClick: (String) -> Unit,
    onSpeakWord: (String) -> Unit = {},
    onDragLink: (wordId: String, pictureId: String) -> Unit = { wId, pId ->
        onWordClick(wId)
        onPictureClick(pId)
    },
    modifier: Modifier = Modifier
) {
    var boardOrigin by remember { mutableStateOf(Offset.Zero) }
    val wordAnchors = remember { mutableStateMapOf<String, Offset>() }
    val pictureAnchors = remember { mutableStateMapOf<String, Offset>() }

    // Per-link drawing progress (0f -> 1f) keyed by "wordId->pictureId"
    val linkAnimations =
        remember { mutableStateMapOf<String, Animatable<Float, AnimationVector1D>>() }

    LaunchedEffect(matchedPairs) {
        val activeKeys = matchedPairs.map { (wId, pId) -> "$wId->$pId" }.toSet()
        // Remove stale entries
        linkAnimations.keys.filter { it !in activeKeys }.forEach { linkAnimations.remove(it) }

        // Animate any newly added link from 0f to 1f
        activeKeys.forEach { key ->
            if (!linkAnimations.containsKey(key)) {
                val anim = Animatable(0f)
                linkAnimations[key] = anim
                launch {
                    anim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    // Live drag-to-connect state
    var draggingWordId by remember { mutableStateOf<String?>(null) }
    var dragCurrentPos by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.fillMaxWidth().onGloballyPositioned { coords ->
        boardOrigin = coords.boundsInRoot().topLeft
    }.pointerInput(wordNodes, pictureNodes, matchedPairs) {
        detectDragGestures(onDragStart = { startOffset ->
            val hitThreshold = if (isTablet) 56.dp.toPx() else 44.dp.toPx()
            val closestWord = wordAnchors.entries.minByOrNull { (_, anchor) ->
                hypot(anchor.x - startOffset.x, anchor.y - startOffset.y)
            }
            if (closestWord != null && hypot(
                    closestWord.value.x - startOffset.x, closestWord.value.y - startOffset.y
                ) <= hitThreshold
            ) {
                draggingWordId = closestWord.key
                dragCurrentPos = startOffset
                wordNodes.find { it.id == closestWord.key }?.let { onSpeakWord(it.word) }
            }
        }, onDrag = { change, _ ->
            if (draggingWordId != null) {
                change.consume()
                dragCurrentPos = change.position
            }
        }, onDragEnd = {
            val activeWordId = draggingWordId
            if (activeWordId != null) {
                val dropThreshold = if (isTablet) 64.dp.toPx() else 48.dp.toPx()
                val targetPicture = pictureAnchors.entries.minByOrNull { (_, anchor) ->
                    hypot(anchor.x - dragCurrentPos.x, anchor.y - dragCurrentPos.y)
                }
                if (targetPicture != null && hypot(
                        targetPicture.value.x - dragCurrentPos.x,
                        targetPicture.value.y - dragCurrentPos.y
                    ) <= dropThreshold
                ) {
                    onDragLink(activeWordId, targetPicture.key)
                }
            }
            draggingWordId = null
        }, onDragCancel = {
            draggingWordId = null
        })
    }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 72.dp else 48.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left Column: Word Pills
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 14.dp else 10.dp)
            ) {
                wordNodes.forEachIndexed { index, wordNode ->
                    val accentColor = MatchRowColors[index % MatchRowColors.size]
                    WordMatchItem(
                        node = wordNode,
                        accentColor = accentColor,
                        isTablet = isTablet,
                        onAnchorPositioned = { rootCenter ->
                            wordAnchors[wordNode.id] = rootCenter - boardOrigin
                        },
                        onClick = {
                            onSpeakWord(wordNode.word)
                            onWordClick(wordNode.id)
                        })
                }
            }

            // Right Column: Picture Pills
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 14.dp else 10.dp)
            ) {
                pictureNodes.forEachIndexed { index, pictureNode ->
                    val matchedWordIndex =
                        wordNodes.indexOfFirst { it.id == pictureNode.matchedWordId }
                    val accentColor = if (matchedWordIndex >= 0) {
                        MatchRowColors[matchedWordIndex % MatchRowColors.size]
                    } else {
                        MatchRowColors[index % MatchRowColors.size]
                    }
                    PictureMatchItem(
                        node = pictureNode,
                        accentColor = accentColor,
                        isTablet = isTablet,
                        onAnchorPositioned = { rootCenter ->
                            pictureAnchors[pictureNode.id] = rootCenter - boardOrigin
                        },
                        onClick = { onPictureClick(pictureNode.id) })
                }
            }
        }

        // Foreground Canvas Layer: Animated Curved Bezier Strings
        val pathMeasure = remember { PathMeasure() }

        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = if (isTablet) 6.dp.toPx() else 4.5.dp.toPx()
            val outlinePx = strokePx + 3.dp.toPx()

            // 1. Draw established links with animated length
            matchedPairs.forEach { (wordId, pictureId) ->
                val start = wordAnchors[wordId] ?: return@forEach
                val end = pictureAnchors[pictureId] ?: return@forEach
                val wordIndex = wordNodes.indexOfFirst { it.id == wordId }.coerceAtLeast(0)
                val isPairCorrect = pairResults[wordId]

                val lineColor = when (isPairCorrect) {
                    false -> Color(0xFFE53935)
                    else -> MatchRowColors[wordIndex % MatchRowColors.size]
                }

                val controlOffsetX = (end.x - start.x) * 0.5f
                val fullPath = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(
                        x1 = start.x + controlOffsetX,
                        y1 = start.y,
                        x2 = end.x - controlOffsetX,
                        y2 = end.y,
                        x3 = end.x,
                        y3 = end.y
                    )
                }

                val progress = linkAnimations["$wordId->$pictureId"]?.value ?: 1f
                val animatedPath = Path()
                pathMeasure.setPath(fullPath, false)
                val totalLength = pathMeasure.length
                pathMeasure.getSegment(0f, totalLength * progress, animatedPath, true)
                val currentTip = pathMeasure.getPosition(totalLength * progress)
// Soft white outer glow/border
                drawPath(
                    path = animatedPath,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = outlinePx, cap = StrokeCap.Round)
                )
// Main colored string
                drawPath(
                    path = animatedPath,
                    color = lineColor,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
// Start dot + moving tip dot
                drawCircle(color = lineColor, radius = strokePx * 1.15f, center = start)
                if (currentTip != Offset.Unspecified) {
                    drawCircle(color = lineColor, radius = strokePx * 1.15f, center = currentTip)
                }
            }
// 2. Draw live dragging string while user's finger moves
            val activeDragWordId = draggingWordId
            if (activeDragWordId != null) {
                val start = wordAnchors[activeDragWordId]
                if (start != null) {
                    val wordIndex =
                        wordNodes.indexOfFirst { it.id == activeDragWordId }.coerceAtLeast(0)
                    val dragColor = MatchRowColors[wordIndex % MatchRowColors.size]
                    val controlOffsetX = (dragCurrentPos.x - start.x) * 0.5f
                    val dragPath = Path().apply {
                        moveTo(start.x, start.y)
                        cubicTo(
                            x1 = start.x + controlOffsetX,
                            y1 = start.y,
                            x2 = dragCurrentPos.x - controlOffsetX,
                            y2 = dragCurrentPos.y,
                            x3 = dragCurrentPos.x,
                            y3 = dragCurrentPos.y
                        )
                    }
                    drawPath(
                        path = dragPath,
                        color = Color.White.copy(alpha = 0.85f),
                        style = Stroke(width = outlinePx, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = dragPath,
                        color = dragColor,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    drawCircle(
                        color = dragColor, radius = strokePx * 1.25f, center = dragCurrentPos
                    )
                }
            }
        }
    }
}

@Preview(name = "Matching Board with Strings", showBackground = true, backgroundColor = 0xFFE1F5FE)
@Composable
private fun MatchingBoardPreview() {
    MaterialTheme {
        MatchingBoard(
            wordNodes = listOf(
                WordNode(
                    "w_1",
                    "mp_1",
                    "Broccoli",
                    isMatched = true,
                    matchedPictureId = "p_1",
                    isCorrect = true
                ), WordNode(
                    "w_2",
                    "mp_2",
                    "Cucumber",
                    isMatched = true,
                    matchedPictureId = "p_2",
                    isCorrect = true
                ), WordNode(
                    "w_3",
                    "mp_3",
                    "Sweet Potato",
                    isMatched = true,
                    matchedPictureId = "p_3",
                    isCorrect = true
                ), WordNode(
                    "w_4",
                    "mp_4",
                    "Orange",
                    isMatched = true,
                    matchedPictureId = "p_4",
                    isCorrect = true
                )
            ),
            pictureNodes = listOf(
                PictureNode(
                    "p_3", "mp_3", "🍠", isMatched = true, matchedWordId = "w_3", isCorrect = true
                ), PictureNode(
                    "p_4", "mp_4", "🍊", isMatched = true, matchedWordId = "w_4", isCorrect = true
                ), PictureNode(
                    "p_1", "mp_1", "🥦", isMatched = true, matchedWordId = "w_1", isCorrect = true
                ), PictureNode(
                    "p_2", "mp_2", "🥒", isMatched = true, matchedWordId = "w_2", isCorrect = true
                )
            ),
            matchedPairs = mapOf("w_1" to "p_1", "w_2" to "p_2", "w_3" to "p_3", "w_4" to "p_4"),
            pairResults = mapOf("w_1" to true, "w_2" to true, "w_3" to true, "w_4" to true),
            isTablet = false,
            onWordClick = {},
            onPictureClick = {})
    }
}
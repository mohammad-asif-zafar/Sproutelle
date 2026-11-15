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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.hathway.sproutelle.presentation.wordbuilder.components.LetterTileItem
import kotlinx.coroutines.delay

@Composable
fun ScrambledLetterTiles(
    availableTiles: List<WordLetterTile>,
    isTablet: Boolean,
    onTileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tileWidth: Dp = if (isTablet) 64.dp else 50.dp
    val tileHeight: Dp = if (isTablet) 60.dp else 48.dp

    val firstRow = availableTiles.take((availableTiles.size + 1) / 2)
    val secondRow = availableTiles.drop(firstRow.size)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 14.dp else 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            firstRow.forEachIndexed { index, tile ->
                LetterTileItem(
                    tile = tile,
                    tileWidth = tileWidth,
                    tileHeight = tileHeight,
                    isTablet = isTablet,
                    entranceIndex = index,
                    onClick = { onTileClick(tile.id) }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            secondRow.forEachIndexed { index, tile ->
                LetterTileItem(
                    tile = tile,
                    tileWidth = tileWidth,
                    tileHeight = tileHeight,
                    isTablet = isTablet,
                    entranceIndex = firstRow.size + index,
                    onClick = { onTileClick(tile.id) }
                )
            }
        }
    }
}

package theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object SproutelleShapes {
    val Small = RoundedCornerShape(12.dp)
    val Medium = RoundedCornerShape(18.dp)
    val Large = RoundedCornerShape(24.dp)
    val ExtraLarge = RoundedCornerShape(32.dp)
    val Pill = RoundedCornerShape(50)
    val Circle = RoundedCornerShape(50)
}

val SproutelleMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = SproutelleShapes.Small,
    medium = SproutelleShapes.Medium,
    large = SproutelleShapes.Large,
    extraLarge = SproutelleShapes.ExtraLarge
)

package theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val SproutelleLightColorScheme = lightColorScheme(
    primary = SproutelleColors.LeafGreen,
    onPrimary = SproutelleColors.OnPrimary,

    secondary = SproutelleColors.CoralOrange,
    onSecondary = SproutelleColors.Ink,

    tertiary = SproutelleColors.SkyBlue,
    onTertiary = SproutelleColors.Ink,

    background = SproutelleColors.Cream,
    onBackground = SproutelleColors.Ink,

    surface = SproutelleColors.White,
    onSurface = SproutelleColors.Ink,

    surfaceVariant = SproutelleColors.Mint,
    onSurfaceVariant = SproutelleColors.SecondaryText,

    outline = SproutelleColors.Outline,
    error = SproutelleColors.Error
)

@Composable
fun SproutelleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SproutelleLightColorScheme,
        typography = SproutelleTypography,
        shapes = SproutelleMaterialShapes,
        content = content
    )
}


@Preview(showBackground = true, name = "Sproutelle Theme Preview")
@Composable
fun SproutelleThemePreview() {
    SproutelleTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sproutelle Typography",
                style = MaterialTheme.typography.titleLarge
            )

            Button(onClick = {}) {
                Text("Sproutelle Button")
            }

            Card {
                Text(
                    text = "This card showcases your shapes and color scheme.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

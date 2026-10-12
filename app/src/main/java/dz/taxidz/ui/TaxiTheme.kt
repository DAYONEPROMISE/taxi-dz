package dz.taxidz.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * TaxiDZ "Taxi Jaune" identity — amber for earnings, blue for fuel spend.
 * Tokens from Shared/OpenPencil/taxi-dz/design-v2.html (design source of truth).
 */
private val TaxiLight = lightColorScheme(
    primary = Color(0xFF7A5900),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDF9E),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF4A6357),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8DA),
    onSecondaryContainer = Color(0xFF072018),
    tertiary = Color(0xFF00629A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCFE5FF),
    onTertiaryContainer = Color(0xFF001D33),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFF8F2),
    onBackground = Color(0xFF1E1B16),
    surface = Color(0xFFFFF8F2),
    onSurface = Color(0xFF1E1B16),
    surfaceVariant = Color(0xFFEDE1CF),
    onSurfaceVariant = Color(0xFF4C4639),
    outline = Color(0xFF7F7767),
    outlineVariant = Color(0xFFD0C5B4)
)

private val TaxiDark = darkColorScheme(
    primary = Color(0xFFF5BD48),
    onPrimary = Color(0xFF412D00),
    primaryContainer = Color(0xFF5C4200),
    onPrimaryContainer = Color(0xFFFFDF9E),
    secondary = Color(0xFFB1CFBE),
    onSecondary = Color(0xFF1D3527),
    secondaryContainer = Color(0xFF334B3C),
    onSecondaryContainer = Color(0xFFCCE8DA),
    tertiary = Color(0xFF99CBFF),
    onTertiary = Color(0xFF003354),
    tertiaryContainer = Color(0xFF004A78),
    onTertiaryContainer = Color(0xFFCFE5FF),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF16130E),
    onBackground = Color(0xFFE9E1D8),
    surface = Color(0xFF16130E),
    onSurface = Color(0xFFE9E1D8),
    surfaceVariant = Color(0xFF4C4639),
    onSurfaceVariant = Color(0xFFD0C5B4),
    outline = Color(0xFF999080),
    outlineVariant = Color(0xFF4C4639)
)

@Composable
fun TaxiDzTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) TaxiDark else TaxiLight, content = content)
}

/**
 * Empty state with a tinted icon badge. The badge is decorative; the
 * icon itself is tinted, so screen readers only announce title and hint.
 */
@Composable
fun TaxiEmptyState(
    icon: ImageVector,
    title: String,
    hint: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(50),
            modifier = Modifier.size(96.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

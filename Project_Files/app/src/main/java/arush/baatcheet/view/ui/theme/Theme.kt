package arush.baatcheet.view.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7202AA),
    secondary = Color.White,
    tertiary = Color(0xFF438CE6),
    background = Color(0xFF2e0144),
    onTertiary = Color(0xFF61006e),
    onPrimary = Color(0xFF6EA541),
    onSecondary = Color(0xFF7A11E4),
    onTertiaryContainer = Color(0xFF7202AA)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFA900FF),
    secondary = Color.Black,
    tertiary = Color(0xFF00299B),
    background = Color.White,
    onPrimary = Color(0xFFCAFDA1),
    onSecondary = Color(0xFFBD8AF1),
    onTertiaryContainer = Color(0xFFC656FF)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun BaatcheetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if(darkTheme){
        DarkColorScheme
    }else{
        LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
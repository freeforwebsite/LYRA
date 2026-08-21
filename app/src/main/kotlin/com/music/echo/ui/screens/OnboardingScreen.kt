package iad1tya.echo.music.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iad1tya.echo.music.constants.MusicLanguageOptions
import iad1tya.echo.music.constants.MusicLanguagesKey
import iad1tya.echo.music.constants.OnboardingCompletedKey
import iad1tya.echo.music.utils.rememberPreference
import java.util.Locale

@Composable
fun OnboardingScreen(
    isSettings: Boolean = false,
    onFinished: () -> Unit
) {
    val (musicLanguages, onMusicLanguagesChange) = rememberPreference(MusicLanguagesKey, emptySet<String>())
    val (_, onOnboardingComplete) = rememberPreference(OnboardingCompletedKey, false)

    val selected = remember(musicLanguages) { musicLanguages.toMutableStateSet() }

    // Animate in and Auto-detect language
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { 
        visible = true 
        if (!isSettings && selected.isEmpty() && musicLanguages.isEmpty()) {
            val deviceLang = Locale.getDefault().language
            if (MusicLanguageOptions.containsKey(deviceLang)) {
                selected.add(deviceLang)
            } else if (deviceLang == "en") {
                selected.add("en")
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .windowInsetsPadding(WindowInsets.systemBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))

                if (!isSettings) {
                    // Header
                    Text(
                        text = "🎵",
                        fontSize = 56.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Welcome to Lyra",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Pick the languages you love listening to.\nYou can change this anytime in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Music Languages",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Select languages to filter music on Home and Search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Choose your music languages",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Language grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(MusicLanguageOptions.entries.toList()) { (code, name) ->
                        val isSelected = selected.contains(code)
                        LanguageChipCard(
                            name = name,
                            code = code,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) selected.remove(code)
                                else selected.add(code)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Hint
                AnimatedVisibility(visible = selected.isEmpty()) {
                    Text(
                        text = "Select at least one, or skip to see all music",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Continue button
                Button(
                    onClick = {
                        onMusicLanguagesChange(selected.toSet())
                        if (!isSettings) {
                            onOnboardingComplete(true)
                        }
                        onFinished()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isSettings) "Save" else if (selected.isEmpty()) "Skip & Continue" else "Continue  →",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LanguageChipCard(
    name: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        tween(200)
    )
    val bgColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else Color.Transparent,
        tween(200)
    )
    
    // Premium Upgrade: Micro-animation scaling
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper: mutableStateSet
fun <T> Collection<T>.toMutableStateSet() = mutableStateListOf<T>().also { it.addAll(this) }
// Wrap as set-like for contains/add/remove
private fun <T> SnapshotStateList<T>.toSet(): Set<T> = this.toHashSet()
private fun <T> SnapshotStateList<T>.contains(element: T) = this.any { it == element }

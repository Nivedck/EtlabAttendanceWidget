package io.github.shreyasskdev.tiledeck.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.shreyasskdev.tiledeck.BuildConfig
import io.github.shreyasskdev.tiledeck.data.UpdateStatus

@Composable
internal fun SettingsTab(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    loading: Boolean,
    onLoginSave: () -> Unit,
    refreshInterval: Long,
    onIntervalSelected: (Long) -> Unit,
    onOpenAbout: () -> Unit,
    updateStatus: UpdateStatus?,
    lastCheckedAt: Long?,
    onCheckForUpdates: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    val palette = expressiveBadgePalette()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Section("Account") {
                CredentialsCard(
                    username = username,
                    onUsernameChange = onUsernameChange,
                    password = password,
                    onPasswordChange = onPasswordChange,
                    loading = loading,
                    onSave = onLoginSave,
                )
            }
        }

        item {
            Section("Sync") {
                BackgroundRefreshCard(
                    currentMinutes = refreshInterval,
                    onIntervalSelected = onIntervalSelected,
                )
            }
        }

        // Version + About are related, so they touch.
        item {
            Section("App") {
                TileColumn(count = 2) { index, position ->
                    if (index == 0) {
                        VersionCard(
                            status = updateStatus,
                            lastCheckedAt = lastCheckedAt,
                            onCheckForUpdates = onCheckForUpdates,
                            onInstallUpdate = onInstallUpdate,
                            shape = groupShape(position),
                        )
                    } else {
                        GroupedRow(
                            position = position,
                            title = "About & privacy",
                            subtitle = "Developers, data policy and how credentials are handled",
                            badgeColor = palette[1].first,
                            badgeContent = {
                                Text(
                                    text = "i",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette[1].second,
                                )
                            },
                            onClick = onOpenAbout,
                            rowContentDescription = "Open About and privacy screen",
                            trailing = { ChevronIcon(tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────────── Credentials ─────────────────────────────

@Composable
private fun CredentialsCard(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    loading: Boolean,
    onSave: () -> Unit,
) {
    AppCard {
        Text(
            text = "Etlab credentials",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Encrypted and stored only on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Etlab username") },
            singleLine = true,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Etlab password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onSave,
            enabled = !loading && username.isNotBlank() && password.isNotBlank(),
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = if (loading) "Checking…" else "Save & fetch attendance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ───────────────────────────── Background refresh ─────────────────────────────

@Composable
private fun BackgroundRefreshCard(
    currentMinutes: Long,
    onIntervalSelected: (Long) -> Unit,
) {
    val options = listOf(
        15L to "15m", 30L to "30m", 60L to "1h", 120L to "2h",
        240L to "4h", 360L to "6h", 720L to "12h", 1440L to "24h",
    )

    AppCard {
        Text(
            text = "Background refresh",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Fetches new attendance in the background, even when the app is closed. Android enforces a 15-minute minimum.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        listOf(options.take(4), options.drop(4)).forEachIndexed { index, rowOptions ->
            if (index > 0) Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { (minutes, label) ->
                    FilterChip(
                        selected = currentMinutes == minutes,
                        onClick = { onIntervalSelected(minutes) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ───────────────────────────── Version ─────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VersionCard(
    status: UpdateStatus?,
    lastCheckedAt: Long?,
    onCheckForUpdates: () -> Unit,
    onInstallUpdate: () -> Unit,
    shape: Shape,
) {
    val currentVersion = remember {
        runCatching { BuildConfig.VERSION_NAME }.getOrDefault("unknown")
    }

    val headline = when (status) {
        null -> "Checking for updates…"
        is UpdateStatus.UpToDate -> "You're on the latest version"
        is UpdateStatus.Available -> "Update available — v${status.version}"
        is UpdateStatus.Error -> "Couldn't check for updates"
    }
    val chipText = when (status) {
        null -> "CHECKING"
        is UpdateStatus.UpToDate -> "UP TO DATE"
        is UpdateStatus.Available -> "UPDATE"
        is UpdateStatus.Error -> "OFFLINE"
    }
    val chipBg = when (status) {
        null -> MaterialTheme.colorScheme.surfaceContainerHighest
        is UpdateStatus.UpToDate -> MaterialTheme.colorScheme.tertiaryContainer
        is UpdateStatus.Available -> MaterialTheme.colorScheme.primaryContainer
        is UpdateStatus.Error -> MaterialTheme.colorScheme.errorContainer
    }
    val chipFg = when (status) {
        null -> MaterialTheme.colorScheme.onSurfaceVariant
        is UpdateStatus.UpToDate -> MaterialTheme.colorScheme.onTertiaryContainer
        is UpdateStatus.Available -> MaterialTheme.colorScheme.onPrimaryContainer
        is UpdateStatus.Error -> MaterialTheme.colorScheme.onErrorContainer
    }
    val supportingText = when (status) {
        null -> "Fetching the latest release info from GitHub."
        is UpdateStatus.UpToDate -> lastCheckedAt?.let { "Last checked ${relativeTime(it)}" }
            ?: "Auto-checks once per hour."
        is UpdateStatus.Available ->
            status.releaseNotes?.takeIf { it.isNotBlank() }?.take(180) ?: "Tap to download and install."
        is UpdateStatus.Error -> status.message
    }

    AppCard(shape = shape) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "App version",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Surface(shape = CircleShape, color = chipBg) {
                Text(
                    text = chipText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = chipFg,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "v$currentVersion",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = headline,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = supportingText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        val available = status as? UpdateStatus.Available
        if (available != null) {
            Button(
                onClick = onInstallUpdate,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = "Get v${available.version}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // A labelled button instead of a bare icon, so it's obvious what it does.
        FilledTonalButton(
            onClick = onCheckForUpdates,
            enabled = status != null,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (status == null) {
                LoadingIndicator(modifier = Modifier.size(28.dp))
            } else {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (status == null) "Checking…" else "Check for updates",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun relativeTime(epochMs: Long): String {
    val minutes = (System.currentTimeMillis() - epochMs) / 60_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        else -> "${minutes / (60 * 24)}d ago"
    }
}
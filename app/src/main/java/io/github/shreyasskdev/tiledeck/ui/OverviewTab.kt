package io.github.shreyasskdev.tiledeck.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.shreyasskdev.tiledeck.data.AttendanceResult
import io.github.shreyasskdev.tiledeck.data.SubjectAttendance

private const val SAFE_THRESHOLD = 75.0

@Composable
internal fun OverviewTab(
    result: AttendanceResult?,
    updatedText: String,
    loading: Boolean,
    nameOverrides: Map<String, String>,
    useCustomNames: Boolean,
    onRefresh: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroAttendanceCard(
                result = result,
                updatedText = updatedText,
                loading = loading,
                onRefresh = onRefresh,
            )
        }

        if (result != null && result.subjects.isNotEmpty()) {
            item { StatsRow(result.subjects) }

            item { GroupLabel("Subject breakdown") }

            items(result.subjects) { subject ->
                val override = nameOverrides[subject.code]
                val displayLabel = when {
                    useCustomNames && !override.isNullOrBlank() -> override
                    subject.name.isNotBlank() -> subject.name
                    else -> subject.code
                }
                SubjectCard(subject = subject, displayLabel = displayLabel)
            }
        } else {
            item { EmptyState() }
        }
    }
}

// ───────────────────────────── Hero ─────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeroAttendanceCard(
    result: AttendanceResult?,
    updatedText: String,
    loading: Boolean,
    onRefresh: () -> Unit,
) {
    val overall = result?.overallPercent ?: 0.0
    val isGood = overall >= SAFE_THRESHOLD

    val containerBg = when {
        result == null -> MaterialTheme.colorScheme.surfaceContainerHigh
        isGood -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val contentFg = when {
        result == null -> MaterialTheme.colorScheme.onSurface
        isGood -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    val progress by animateFloatAsState(
        targetValue = (overall / 100.0).toFloat().coerceIn(0f, 1f),
        label = "OverallProgress",
    )

    AppCard(containerColor = containerBg, contentPadding = 24.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .semantics { contentDescription = "Overall attendance %.1f percent".format(overall) },
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = contentFg,
                    trackColor = contentFg.copy(alpha = 0.18f),
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (result == null) "--" else "%.1f".format(overall),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentFg,
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentFg,
                        modifier = Modifier.padding(bottom = 4.dp, start = 1.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(shape = CircleShape, color = contentFg.copy(alpha = 0.15f)) {
                    Text(
                        text = when {
                            result == null -> "NOT SET UP"
                            isGood -> "ON TRACK"
                            else -> "LOW ATTENDANCE"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentFg,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Overall attendance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentFg,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = updatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentFg.copy(alpha = 0.8f),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onRefresh,
            enabled = !loading,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = contentFg,
                contentColor = containerBg,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = if (loading) "Refreshing…" else "Refresh attendance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ───────────────────────────── Stats ─────────────────────────────

@Composable
private fun StatsRow(subjects: List<SubjectAttendance>) {
    val safe = subjects.count { it.percent >= SAFE_THRESHOLD }
    val atRisk = subjects.size - safe

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            value = subjects.size.toString(),
            label = "Subjects",
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = safe.toString(),
            label = "Safe",
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = atRisk.toString(),
            label = "At risk",
            container = if (atRisk > 0) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            content = if (atRisk > 0) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier, containerColor = container, contentPadding = 16.dp) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = content,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content.copy(alpha = 0.85f),
        )
    }
}

// ───────────────────────────── Subjects ─────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SubjectCard(subject: SubjectAttendance, displayLabel: String) {
    val progress by animateFloatAsState(
        targetValue = (subject.percent / 100.0).toFloat().coerceIn(0f, 1f),
        label = "SubjectProgress",
    )

    val isGood = subject.percent >= 85.0
    val isFair = subject.percent >= SAFE_THRESHOLD

    val chipBg = when {
        isGood -> MaterialTheme.colorScheme.tertiaryContainer
        isFair -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val chipFg = when {
        isGood -> MaterialTheme.colorScheme.onTertiaryContainer
        isFair -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    AppCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subject.code,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(12.dp))
            Surface(shape = CircleShape, color = chipBg) {
                Text(
                    text = "%.0f%%".format(subject.percent),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = chipFg,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "${subject.percent.toInt()} percent attendance" },
            color = if (isFair) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "${subject.present} of ${subject.total} hours attended",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState() {
    AppCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, contentPadding = 28.dp) {
        Text(
            text = "No attendance data yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Open the Settings tab and save your Etlab credentials to fetch your attendance.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
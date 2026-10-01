package io.github.shreyasskdev.tiledeck.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ONE corner radius for every card/container in the app. Anything smaller than a
 * card (chips, badges, buttons) is a fully-round pill via [CircleShape].
 * Change it here and the whole app follows.
 */
internal val AppCardShape = RoundedCornerShape(28.dp)

/** The single card primitive used by Overview, Customize, Settings and About. */
@Composable
internal fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = AppCardShape,
        color = containerColor,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/**
 * Expressive circular icon button used in top app bars and headers across the app.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CircleIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
            .size(42.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                LoadingIndicator(
                    modifier = Modifier.size(28.dp),
                    color = contentColor,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Grouped list pattern. Every row now uses the same [AppCardShape]; the
 * [GroupPosition] parameter is kept so existing call sites don't break.
 */
internal enum class GroupPosition { Single, Top, Middle, Bottom }

internal fun groupPosition(index: Int, count: Int): GroupPosition = when {
    count <= 1 -> GroupPosition.Single
    index == 0 -> GroupPosition.Top
    index == count - 1 -> GroupPosition.Bottom
    else -> GroupPosition.Middle
}

@Composable
internal fun expressiveBadgePalette(): List<Pair<Color, Color>> {
    val scheme = MaterialTheme.colorScheme
    return listOf(
        scheme.primaryContainer to scheme.onPrimaryContainer,
        scheme.tertiaryContainer to scheme.onTertiaryContainer,
        scheme.secondaryContainer to scheme.onSecondaryContainer,
        scheme.errorContainer to scheme.onErrorContainer,
    )
}

/** Circular flat-color badge that leads a [GroupedRow]. */
@Composable
internal fun IconBadge(
    containerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = modifier.size(44.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/** One row inside a [GroupedList]: badge, title/subtitle, optional trailing slot. */
@Composable
internal fun GroupedRow(
    position: GroupPosition,
    title: String,
    badgeColor: Color,
    badgeContent: @Composable () -> Unit,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    rowContentDescription: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(containerColor = badgeColor) { badgeContent() }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = AppCardShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = rowContentDescription ?: title },
            content = rowContent,
        )
    } else {
        Surface(
            shape = AppCardShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
            content = rowContent,
        )
    }
}

/** Vertical run of [GroupedRow]s. */
@Composable
internal fun GroupedList(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row -> row() }
    }
}

/** Small section label sitting above a card or list. */
@Composable
internal fun GroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 8.dp, top = 4.dp),
    )
}
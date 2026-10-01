package io.github.shreyasskdev.tiledeck.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  Tile system (Android 16/17 Settings style)
//
//  • A tile on its own, or at the outside edge of a group  -> CornerLarge
//  • A tile edge that touches another tile                  -> CornerSmall
//  • Touching tiles sit 2dp apart so they read as one group.
// ─────────────────────────────────────────────────────────────────────────────

internal val CornerLarge = 28.dp
internal val CornerSmall = 6.dp
internal val TileGap = 2.dp

/** Shape for a standalone tile. */
internal val AppCardShape = RoundedCornerShape(CornerLarge)

/**
 * Position inside a group. For a vertical group Top = first, Bottom = last.
 * For a horizontal group Top = start, Bottom = end.
 */
internal enum class GroupPosition { Single, Top, Middle, Bottom }

internal fun groupPosition(index: Int, count: Int): GroupPosition = when {
    count <= 1 -> GroupPosition.Single
    index == 0 -> GroupPosition.Top
    index == count - 1 -> GroupPosition.Bottom
    else -> GroupPosition.Middle
}

internal fun groupShape(position: GroupPosition, horizontal: Boolean = false): RoundedCornerShape {
    val l = CornerLarge
    val s = CornerSmall
    return when (position) {
        GroupPosition.Single -> RoundedCornerShape(l)
        GroupPosition.Middle -> RoundedCornerShape(s)
        GroupPosition.Top ->
            if (horizontal) RoundedCornerShape(topStart = l, topEnd = s, bottomEnd = s, bottomStart = l)
            else RoundedCornerShape(topStart = l, topEnd = l, bottomEnd = s, bottomStart = s)
        GroupPosition.Bottom ->
            if (horizontal) RoundedCornerShape(topStart = s, topEnd = l, bottomEnd = l, bottomStart = s)
            else RoundedCornerShape(topStart = s, topEnd = s, bottomEnd = l, bottomStart = l)
    }
}

/** Vertical run of touching tiles. */
@Composable
internal fun TileColumn(
    count: Int,
    modifier: Modifier = Modifier,
    tile: @Composable (index: Int, position: GroupPosition) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(TileGap)) {
        repeat(count) { tile(it, groupPosition(it, count)) }
    }
}

/** Horizontal run of touching tiles (use Modifier.weight(1f) inside). */
@Composable
internal fun TileRow(
    count: Int,
    modifier: Modifier = Modifier,
    tile: @Composable RowScope.(index: Int, position: GroupPosition) -> Unit,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TileGap)) {
        repeat(count) { this@Row.tile(it, groupPosition(it, count)) }
    }
}

/** Label + related content, with the label tucked close to its group. */
@Composable
internal fun Section(
    label: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) GroupLabel(label)
        content()
    }
}

/** The single card primitive used by every screen. */
@Composable
internal fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentPadding: Dp = 20.dp,
    shape: Shape = AppCardShape,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = shape,
        color = containerColor,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Buttons, badges, rows
// ─────────────────────────────────────────────────────────────────────────────

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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (loading) {
                LoadingIndicator(modifier = Modifier.size(28.dp), color = contentColor)
            } else {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}

/** One tile row: badge, title/subtitle, optional trailing. Shape follows [position]. */
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

    val shape = groupShape(position)
    val color = MaterialTheme.colorScheme.surfaceContainerHigh

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = rowContentDescription ?: title },
            content = rowContent,
        )
    } else {
        Surface(shape = shape, color = color, modifier = Modifier.fillMaxWidth(), content = rowContent)
    }
}

@Composable
internal fun GroupedList(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TileGap)) {
        rows.forEach { row -> row() }
    }
}

@Composable
internal fun GroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 8.dp),
    )
}
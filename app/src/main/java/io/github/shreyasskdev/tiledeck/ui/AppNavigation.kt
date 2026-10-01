package io.github.shreyasskdev.tiledeck.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  Floating Navigation Bar Motion Configuration
//  Tweak these variables to customize the spring bounciness, speed & icon scale!
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Controls bounciness / overshoot of the pill expansion and icon pop.
 * Lower value = more overshoot & bounciness. Higher value = stiffer & less overshoot.
 *
 * Preset values:
 * - Spring.DampingRatioHighBouncy (0.2f)   -> High bounciness / dramatic overshoot
 * - Spring.DampingRatioMediumBouncy (0.5f) -> Medium bounciness / natural overshoot
 * - Spring.DampingRatioLowBouncy (0.75f)  -> Low bounciness / subtle overshoot
 * - Spring.DampingRatioNoBouncy (1.0f)     -> No bounciness / smooth slide
 */
internal var TOOLBAR_DAMPING_RATIO: Float = Spring.DampingRatioMediumBouncy

/**
 * Controls speed / snappiness of the spring expansion motion.
 * Lower value = slower & more fluid. Higher value = faster & snappier.
 *
 * Preset values:
 * - Spring.StiffnessVeryLow (50f)    -> Slow & fluid
 * - Spring.StiffnessLow (200f)       -> Gentle
 * - Spring.StiffnessMediumLow (400f) -> Balanced & responsive
 * - Spring.StiffnessMedium (1500f)   -> Fast & snappy
 */
internal var TOOLBAR_STIFFNESS: Float = Spring.StiffnessLow

/**
 * Scale factor of the active icon when selected (e.g. 1.12f = 12% larger).
 */
internal var TOOLBAR_ICON_SELECTED_SCALE: Float = 1.12f

// ──────────────────────────────────────────────────────────────────────────ternal var TOOLBAR_ICON_SELECTED───
//  Floating Navigation Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FloatingTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Official Material 3 Expressive HorizontalFloatingToolbar.
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier
            .padding(horizontal = 20.dp)
            .semantics {
                contentDescription = "Main navigation floating toolbar"
            },
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            toolbarContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = CircleShape,
        expandedShadowElevation = 8.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            ExpressiveToolbarNavItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                icon = Icons.Outlined.PieChart,
                label = "Overview",
            )
            ExpressiveToolbarNavItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                icon = Icons.Outlined.Edit,
                label = "Customize",
            )
            ExpressiveToolbarNavItem(
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                icon = Icons.Outlined.Settings,
                label = "Settings",
            )
        }
    }
}

@Composable
private fun ExpressiveToolbarNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) TOOLBAR_ICON_SELECTED_SCALE else 1.0f,
        animationSpec = spring(
            dampingRatio = TOOLBAR_DAMPING_RATIO,
            stiffness = TOOLBAR_STIFFNESS,
        ),
        label = "IconScaleSpring",
    )

    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ContainerColorAnim",
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ContentColorAnim",
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor, shape = CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics {
                role = Role.Tab
                contentDescription = label
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale),
            )

            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = TOOLBAR_DAMPING_RATIO,
                        stiffness = TOOLBAR_STIFFNESS,
                    ),
                ) + fadeIn(spring(stiffness = Spring.StiffnessLow)),
                exit = shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = TOOLBAR_DAMPING_RATIO,
                        stiffness = TOOLBAR_STIFFNESS,
                    ),
                ) + fadeOut(spring(stiffness = Spring.StiffnessLow)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal var TOOLBAR_DAMPING_RATIO: Float = Spring.DampingRatioMediumBouncy
internal var TOOLBAR_STIFFNESS: Float = Spring.StiffnessLow
internal var TOOLBAR_ICON_SELECTED_SCALE: Float = 1.12f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FloatingTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier
//            .padding(horizontal = 20.dp)
            .semantics {
                contentDescription = "Main navigation floating toolbar"
            },
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.primary,
            toolbarContentColor = MaterialTheme.colorScheme.background,
        ),
        shape = CircleShape,
        expandedShadowElevation = 8.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 4.dp,
            ),
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
        targetValue = if (selected) {
            TOOLBAR_ICON_SELECTED_SCALE
        } else {
            1.0f
        },
        animationSpec = spring(
            dampingRatio = TOOLBAR_DAMPING_RATIO,
            stiffness = TOOLBAR_STIFFNESS,
        ),
        label = "IconScaleSpring",
    )

    /*
     * Container colors:
     *
     * Selected:
     *     background
     *
     * Unselected:
     *     transparent
     */
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.background
        } else {
            Color.Transparent
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
        ),
        label = "ContainerColorAnim",
    )

    /*
     * Content colors are intentionally CROSS-WIRED:
     *
     * Selected pill:
     *     background container
     *     primary content
     *
     * Outside pill:
     *     primary container
     *     background content
     */
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.background
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
        ),
        label = "ContentColorAnim",
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                color = containerColor,
                shape = CircleShape,
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(
                horizontal = 14.dp,
                vertical = 10.dp,
            )
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
                ) + fadeIn(
                    spring(
                        stiffness = Spring.StiffnessLow,
                    ),
                ),
                exit = shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = TOOLBAR_DAMPING_RATIO,
                        stiffness = TOOLBAR_STIFFNESS,
                    ),
                ) + fadeOut(
                    spring(
                        stiffness = Spring.StiffnessLow,
                    ),
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(
                        modifier = Modifier.width(8.dp),
                    )

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


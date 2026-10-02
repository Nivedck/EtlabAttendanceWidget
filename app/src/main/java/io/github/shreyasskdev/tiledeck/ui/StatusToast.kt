package io.github.shreyasskdev.tiledeck.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Fires a native Android Toast whenever [status] changes to a non-null value,
 * then notifies the caller via [onStatusShown] so it can clear the state and
 * avoid showing the same toast twice.
 *
 * The Toast is the platform component — bottom-anchored, auto-dismissing
 * after ~2 seconds (LENGTH_SHORT), and consistently styled by the OS. No
 * custom background, no in-app layout to fight with scroll tiers.
 */
@Composable
internal fun StatusToastEffect(
    status: String?,
    onStatusShown: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(status) {
        if (status != null) {
            Toast.makeText(
                context.applicationContext,
                status,
                Toast.LENGTH_SHORT,
            ).show()
            onStatusShown()
        }
    }
}
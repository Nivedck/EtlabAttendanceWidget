package io.github.shreyasskdev.tiledeck.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val FETCHING_MESSAGE = "Fetching attendance…"

/**
 * Drives a normal Material snackbar:
 *  - while [loading] is true -> "Fetching attendance…" stays up (with a spinner)
 *  - when loading ends       -> it's replaced by the result in [status], which
 *                               disappears on its own after a few seconds.
 */
@Composable
internal fun StatusSnackbarEffects(
    hostState: SnackbarHostState,
    loading: Boolean,
    status: String?,
    onStatusShown: () -> Unit,
) {
    LaunchedEffect(loading) {
        if (loading) {
            // Cancelled automatically (and the snackbar removed) when loading flips to false.
            hostState.showSnackbar(FETCHING_MESSAGE, duration = SnackbarDuration.Indefinite)
        } else {
            hostState.currentSnackbarData?.dismiss()
        }
    }

    LaunchedEffect(status) {
        if (status != null) {
            hostState.showSnackbar(status, duration = SnackbarDuration.Short)
            onStatusShown()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun StatusSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        val message = data.visuals.message
        val isError = message.startsWith("Invalid") || message.startsWith("Couldn't")
        val inProgress = data.visuals.duration == SnackbarDuration.Indefinite

        Snackbar(
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.inverseSurface,
            contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.inverseOnSurface,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (inProgress) {
                    LoadingIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.inversePrimary,
                    )
                }
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
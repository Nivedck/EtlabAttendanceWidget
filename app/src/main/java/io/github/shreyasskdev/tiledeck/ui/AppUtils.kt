package io.github.shreyasskdev.tiledeck.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Returns true if the app is allowed to launch the package installer.
 * On Android 8.0+ the user must grant this manually in Settings.
 */
internal fun canInstallPackages(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.packageManager.canRequestPackageInstalls()
    } else {
        true
    }
}

/**
 * Sends the user to Settings > Apps > Special access > Install unknown apps
 * so they can flip the toggle for this app.
 */
internal fun openInstallPermissionSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shorthand generator
// ─────────────────────────────────────────────────────────────────────────────

private val SHORTHAND_STOPWORDS = setOf(
    "and", "of", "the", "for", "io", "to", "a", "an", "&",
)

private val LAB_REGEX = Regex("(?i)\\b(lab|laboratory)\\b")

internal fun toShorthand(rawName: String): String {
    if (rawName.isBlank()) return ""

    val isLab = LAB_REGEX.containsMatchIn(rawName)
    val cleaned = LAB_REGEX.replace(rawName, " ").trim()

    val words = cleaned
        .split(Regex("\\s+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterIndexed { index, word ->
            index == 0 || word.lowercase() !in SHORTHAND_STOPWORDS
        }

    if (words.isEmpty()) return rawName.uppercase()

    val acronym: String = if (words.size == 1) {
        words.first().take(3).uppercase()
    } else {
        words.mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .take(5)
    }

    return if (isLab) "$acronym LAB" else acronym
}
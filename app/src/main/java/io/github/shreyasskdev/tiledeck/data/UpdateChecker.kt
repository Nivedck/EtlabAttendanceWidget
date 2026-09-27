package io.github.shreyasskdev.tiledeck.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateChecker {

    private const val TAG = "UpdateChecker"

    // Throttle config
    private const val PREFS = "update_check_prefs"
    private const val KEY_LAST_CHECK = "last_check"
    private const val MIN_INTERVAL_MS = 60 * 60 * 1000L   // 1 hour

    /**
     * Public entry point. Throttles checks so we don't hammer the GitHub API.
     * Pass `force = true` to bypass the throttle.
     */
    suspend fun check(context: Context, force: Boolean = false): UpdateStatus {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val last = sp.getLong(KEY_LAST_CHECK, 0L)

        if (!force && System.currentTimeMillis() - last < MIN_INTERVAL_MS) {
            Log.d(TAG, "Skipping check — last check was ${(System.currentTimeMillis() - last) / 1000}s ago")
            return UpdateStatus.UpToDate
        }

        val result = performCheck(context)
        sp.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
        return result
    }

    private suspend fun performCheck(context: Context): UpdateStatus = withContext(Dispatchers.IO) {
        try {
            val api = UpdateApi.create()

            Log.d(TAG, "Fetching pointer: ${UpdateApi.pointerUrl()}")
            val pointer = api.fetchPointer(UpdateApi.pointerUrl())
            Log.d(TAG, "Pointer resolved to: ${pointer.releasesUrl}")

            val releaseUrl = "${pointer.releasesUrl.trimEnd('/')}/releases/latest"
            val release = api.fetchLatestRelease(releaseUrl)

            val remoteVersion = release.tagName.removePrefix("v").trim()
            val currentVersion = currentVersionName(context)

            Log.d(TAG, "current=$currentVersion remote=$remoteVersion")

            if (!isNewer(remoteVersion, currentVersion)) {
                return@withContext UpdateStatus.UpToDate
            }

            val apk = pickApkAsset(release.assets, pointer.apkAssetName)
                ?: return@withContext UpdateStatus.Error(
                    "Release ${release.tagName} has no APK asset."
                )

            // Prefer the release body (markdown) but fall back to the release
            // name if the body is empty. Then convert markdown → plain text
            // so the UI doesn't have to deal with ** ** and ## ## and - dashes.
            val rawNotes = release.body?.takeIf { it.isNotBlank() }
                ?: release.name?.takeIf { it.isNotBlank() }

            UpdateStatus.Available(
                version = remoteVersion,
                releaseNotes = rawNotes?.let { stripMarkdown(it) },
                downloadUrl = apk.downloadUrl,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Update check failed", e)
            UpdateStatus.Error(e.message ?: "Unknown error")
        }
    }

    private fun currentVersionName(context: Context): String {
        return runCatching {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0L),
                ).versionName
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, 0).versionName
            }
        }.getOrNull() ?: "0.0.0"
    }

    fun isNewer(remote: String, current: String): Boolean {
        val r = remote.substringBefore('-').split('.').mapNotNull { it.toIntOrNull() }
        val c = current.substringBefore('-').split('.').mapNotNull { it.toIntOrNull() }

        val len = maxOf(r.size, c.size)
        for (i in 0 until len) {
            val rv = r.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (rv > cv) return true
            if (rv < cv) return false
        }
        return false
    }

    private fun pickApkAsset(
        assets: List<GitHubAsset>,
        preferredName: String?,
    ): GitHubAsset? {
        if (assets.isEmpty()) return null

        if (!preferredName.isNullOrBlank()) {
            assets.firstOrNull { it.name.equals(preferredName, ignoreCase = true) }
                ?.let { return it }
        }

        val apks = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apks.isEmpty()) return null

        val abi = preferredAbi()
        return apks.firstOrNull { it.name.contains(abi, ignoreCase = true) }
            ?: apks.first()
    }

    private fun preferredAbi(): String {
        val supported = Build.SUPPORTED_ABIS
        return when {
            supported.any { it == "arm64-v8a" } -> "arm64"
            supported.any { it == "armeabi-v7a" } -> "arm"
            supported.any { it == "x86_64" } -> "x86_64"
            supported.any { it == "x86" } -> "x86"
            else -> "universal"
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Markdown → plain text
    //
    //  GitHub release bodies are markdown. The UI just wants clean text.
    //  We do a lightweight pass that handles the constructs you're actually
    //  likely to see in release notes:
    //    ## Header         → Header
    //    **bold** / *ital* → bold / ital
    //    `code`            → code
    //    - bullet          → • bullet
    //    1. numbered       → • numbered
    //    [text](url)       → text
    //    ![alt](img)       → (removed)
    //    > quote           → quote
    //    ``` blocks ```    → (removed)
    //    ---               → (removed)
    // ─────────────────────────────────────────────────────────────────────────

    private val CODE_BLOCK = Regex("```[\\s\\S]*?```")
    private val INLINE_CODE = Regex("`([^`]*)`")
    private val IMAGE = Regex("!\\[[^\\]]*]\\([^)]*\\)")
    private val LINK = Regex("\\[([^\\]]+)]\\([^)]*\\)")
    private val BOLD = Regex("(\\*\\*|__)(.*?)\\1")
    private val ITALIC = Regex("(\\*|_)(.*?)\\1")
    private val STRIKE = Regex("~~(.*?)~~")
    private val HEADER = Regex("(?m)^#{1,6}\\s*")
    private val BULLET = Regex("(?m)^\\s*[-*+]\\s+")
    private val NUMBERED = Regex("(?m)^\\s*\\d+\\.\\s+")
    private val QUOTE = Regex("(?m)^>\\s*")
    private val HR = Regex("(?m)^-{3,}$")
    private val MULTI_BLANK = Regex("\n{3,}")

    private fun stripMarkdown(md: String): String {
        return md
            .replace(CODE_BLOCK, "")
            .replace(INLINE_CODE, "$1")
            .replace(IMAGE, "")
            .replace(LINK, "$1")
            .replace(BOLD, "$2")
            .replace(ITALIC, "$2")
            .replace(STRIKE, "$1")
            .replace(HEADER, "")
            .replace(BULLET, "• ")
            .replace(NUMBERED, "• ")
            .replace(QUOTE, "")
            .replace(HR, "")
            .replace(MULTI_BLANK, "\n\n")
            .trim()
    }
}
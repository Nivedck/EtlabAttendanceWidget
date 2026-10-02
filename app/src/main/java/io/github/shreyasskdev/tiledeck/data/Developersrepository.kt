package io.github.shreyasskdev.tiledeck.data

import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/** GitHub /contributors item (only the fields we need). */
data class GitHubContributor(
    @SerializedName("login") val login: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("html_url") val htmlUrl: String?,
    @SerializedName("url") val apiUrl: String?,
    @SerializedName("contributions") val contributions: Int = 0,
    @SerializedName("type") val type: String? = null,
)

/** GitHub /users/{login} response (only the name). */
data class GitHubUser(
    @SerializedName("login") val login: String?,
    @SerializedName("name") val name: String?,
)

/** What the UI shows. */
data class Developer(
    val login: String,
    val name: String,          // real name, or login if the profile has none
    val avatarUrl: String?,
    val profileUrl: String,
    val contributions: Int,
)

object DevelopersRepository {

    private const val TAG = "DevelopersRepository"

    /** Keeps us well under GitHub's 60 unauthenticated requests/hour. */
    private const val MAX_NAME_LOOKUPS = 30

    @Volatile
    private var cache: List<Developer>? = null

    /**
     * Same route as [UpdateChecker]: pointer file -> releases_url -> /contributors.
     */
    suspend fun load(force: Boolean = false): Result<List<Developer>> =
        withContext(Dispatchers.IO) {
            if (!force) cache?.let { return@withContext Result.success(it) }

            runCatching {
                val api = UpdateApi.create()
                val pointer = api.fetchPointer(UpdateApi.pointerUrl())
                val base = pointer.releasesUrl.trimEnd('/')

                val contributors = api.fetchContributors("$base/contributors?per_page=100")
                    .filterNot { it.type.equals("Bot", ignoreCase = true) || it.login.endsWith("[bot]") }
                    .take(MAX_NAME_LOOKUPS)

                coroutineScope {
                    contributors.map { c ->
                        async {
                            val realName = c.apiUrl?.let { url ->
                                runCatching { api.fetchUser(url).name }
                                    .onFailure { Log.w(TAG, "Name lookup failed for ${c.login}", it) }
                                    .getOrNull()
                            }?.takeIf { it.isNotBlank() }

                            Developer(
                                login = c.login,
                                name = realName ?: c.login,
                                avatarUrl = c.avatarUrl,
                                profileUrl = c.htmlUrl ?: "https://github.com/${c.login}",
                                contributions = c.contributions,
                            )
                        }
                    }.awaitAll()
                }.sortedByDescending { it.contributions }
            }.onSuccess { cache = it }
                .onFailure { Log.e(TAG, "Failed to load developers", it) }
        }
}
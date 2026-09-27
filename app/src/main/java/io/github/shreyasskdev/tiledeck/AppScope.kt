package io.github.shreyasskdev.tiledeck

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
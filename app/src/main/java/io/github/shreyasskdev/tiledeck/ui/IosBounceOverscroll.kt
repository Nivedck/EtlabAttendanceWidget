package io.github.shreyasskdev.tiledeck.ui

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * Master switch for the iOS-style rubber-band overscroll.
 *
 * When `false` (default), `rememberIosBounceOverscroll()` returns the
 * **platform default** overscroll effect — the Android 12+ stretch on
 * gesture nav, or the classic glow on older setups. No custom spring math
 * runs, and the app bar handoff is disabled.
 *
 * When `true`, every scrollable that calls `rememberIosBounceOverscroll()`
 * gets the custom rubber-band + bounce + top-bar handoff.
 *
 * Flip this to `true` to re-enable app-wide.
 */
const val ENABLE_IOS_BOUNCE_OVERSCROLL: Boolean = false

class IosBounceOverscrollEffect(
    /**
     * Called after a bounce at the TOP edge has swung back to rest once.
     * Receives the bounce velocity (px/s) at that exact crossing so the
     * caller can hand it off to another spring and stay continuous.
     */
    private val onTopBounceReturned: suspend (Float) -> Unit = {},
) : OverscrollEffect {

    private val offset = mutableFloatStateOf(0f)
    private var rawOverscroll = 0f

    private var flinging = false
    private var edgeHit = false
    private var edgeVelocity = 0f
    private var lastFlingFrameNanos = 0L

    private var bounceGen = 0
    private var bounceReturned = false
    private var bounceReturnedVelocity = 0f

    // --- tuning ---
    private val maxStretchPx = 400f
    private val minLeftoverScroll = 0.5f
    private val minBounceVelocity = 120f
    private val bounceVelocityGain = 1.0f
    private val maxBounceVelocity = 7000f

    private val stiffness = 200f
    private val bounceDamping = 0.80f
    private val releaseDamping = 0.80f

    override val node: DelegatableNode = object : Modifier.Node(), LayoutModifierNode {
        override fun MeasureScope.measure(
            measurable: Measurable,
            constraints: Constraints,
        ): MeasureResult {
            val placeable = measurable.measure(constraints)
            return layout(placeable.width, placeable.height) {
                placeable.placeRelativeWithLayer(0, 0) {
                    translationY = offset.floatValue
                }
            }
        }
    }

    override val isInProgress: Boolean
        get() = rawOverscroll != 0f || offset.floatValue != 0f

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        if (source != NestedScrollSource.UserInput) {
            val consumed = performScroll(delta)
            if (flinging) {
                val now = System.nanoTime()
                val leftover = delta.y - consumed.y
                if (!edgeHit && abs(leftover) > minLeftoverScroll) {
                    edgeHit = true
                    val dt = ((now - lastFlingFrameNanos) / 1e9f).coerceIn(0.008f, 0.05f)
                    edgeVelocity = delta.y / dt
                }
                lastFlingFrameNanos = now
            }
            return consumed
        }

        bounceGen++
        if (rawOverscroll == 0f && abs(offset.floatValue) > 0.5f) {
            rawOverscroll = inverseRubberBand(offset.floatValue)
        }

        var remaining = delta.y

        if (rawOverscroll != 0f && remaining != 0f && sign(remaining) != sign(rawOverscroll)) {
            val newRaw = if (rawOverscroll > 0f) maxOf(0f, rawOverscroll + remaining)
            else minOf(0f, rawOverscroll + remaining)
            remaining -= (newRaw - rawOverscroll)
            rawOverscroll = newRaw
        }

        val consumed = performScroll(Offset(delta.x, remaining))
        val leftover = remaining - consumed.y
        if (abs(leftover) > minLeftoverScroll) rawOverscroll += leftover

        offset.floatValue = rubberBand(rawOverscroll)
        return delta
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        val stretched = rawOverscroll != 0f || abs(offset.floatValue) > 1f

        if (stretched) {
            val raw = if (rawOverscroll != 0f) rawOverscroll else inverseRubberBand(offset.floatValue)
            rawOverscroll = 0f
            val slope = maxStretchPx / (abs(raw) + maxStretchPx)
            val v = (velocity.y * slope * slope).coerceIn(-maxBounceVelocity, maxBounceVelocity)
            val gen = ++bounceGen
            coroutineScope {
                launch { runSpring(gen, v, releaseDamping) }
                performFling(velocity)
            }
            return
        }

        rawOverscroll = 0f
        edgeHit = false
        edgeVelocity = 0f
        lastFlingFrameNanos = System.nanoTime()
        flinging = true
        try {
            performFling(velocity)
        } finally {
            flinging = false
        }

        if (!edgeHit || abs(edgeVelocity) < minBounceVelocity) return

        val v = (edgeVelocity * bounceVelocityGain).coerceIn(-maxBounceVelocity, maxBounceVelocity)
        val gen = ++bounceGen
        coroutineScope {
            val bounce = launch { runSpring(gen, v, bounceDamping) }
            if (v > 0f) {
                // Top edge: wait until the content has swung back to rest once,
                // then hand the crossing velocity off so the app bar can start
                // its expansion with the same momentum — no visible reset.
                while (bounce.isActive && !bounceReturned && gen == bounceGen) {
                    withFrameNanos { }
                }
                if (bounceReturned && gen == bounceGen) {
                    onTopBounceReturned(bounceReturnedVelocity)
                }
            }
        }
    }

    private suspend fun runSpring(gen: Int, v0: Float, dampingRatio: Float) {
        val k = stiffness
        val c = 2f * dampingRatio * sqrt(k)
        var x = offset.floatValue
        var v = v0
        val dir = sign(v0)
        var wasOut = false
        bounceReturned = false
        var last = withFrameNanos { it }
        while (gen == bounceGen) {
            val now = withFrameNanos { it }
            var remaining = ((now - last) / 1e9f).coerceAtMost(0.032f)
            last = now
            while (remaining > 0f) {
                val h = min(remaining, 0.004f)
                v += (-k * x - c * v) * h
                x += v * h
                remaining -= h
            }
            if (gen != bounceGen) return
            offset.floatValue = x
            if (x * dir > 1f) wasOut = true
            if (wasOut && x * dir <= 0f && !bounceReturned) {
                bounceReturned = true
                bounceReturnedVelocity = v
            }
            if (abs(x) < 0.5f && abs(v) < 10f) break
        }
        if (gen == bounceGen) offset.floatValue = 0f
    }

    private fun rubberBand(raw: Float): Float {
        val a = abs(raw)
        if (a < 0.01f) return 0f
        val visible = a * maxStretchPx / (a + maxStretchPx)
        return if (raw > 0f) visible else -visible
    }

    private fun inverseRubberBand(visible: Float): Float {
        val v = min(abs(visible), maxStretchPx - 1f)
        val raw = v * maxStretchPx / (maxStretchPx - v)
        return if (visible > 0f) raw else -raw
    }
}

/**
 * Returns an [IosBounceOverscrollEffect] when [ENABLE_IOS_BOUNCE_OVERSCROLL]
 * is `true`, or the **platform default** overscroll effect when it's `false`.
 *
 * The fallback is `rememberOverscrollEffect()` — the same effect Compose
 * would install if you never passed an `overscrollEffect` at all. On
 * Android 12+ that's the stretch you'd see with the system gesture nav
 * enabled; on older versions it's the classic glow.
 *
 * Because this returns the platform default instead of `null`, every
 * `LazyColumn` that does `overscrollEffect = rememberIosBounceOverscroll()`
 * keeps working in both modes — no call-site changes needed.
 */
@Composable
fun rememberIosBounceOverscroll(
    onTopBounceReturned: suspend (Float) -> Unit = {},
): OverscrollEffect? {
    // Always call rememberOverscrollEffect() unconditionally so the
    // composition structure stays stable across flag changes. It reads
    // from LocalOverscrollFactory and is cheap to call.
    val platformDefault = rememberOverscrollEffect()

    if (!ENABLE_IOS_BOUNCE_OVERSCROLL) return platformDefault

    val latest by rememberUpdatedState(onTopBounceReturned)
    return remember { IosBounceOverscrollEffect { v -> latest(v) } }
}
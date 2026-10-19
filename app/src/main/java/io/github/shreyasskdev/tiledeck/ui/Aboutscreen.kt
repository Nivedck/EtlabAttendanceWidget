package io.github.shreyasskdev.tiledeck.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Typeface
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.shreyasskdev.tiledeck.R
import io.github.shreyasskdev.tiledeck.data.Developer
import io.github.shreyasskdev.tiledeck.data.DevelopersRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private sealed interface DevState {
    data object Loading : DevState
    data object Failed : DevState
    data class Ready(val developers: List<Developer>) : DevState
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val palette = expressiveBadgePalette()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var devState by remember { mutableStateOf<DevState>(DevState.Loading) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(reloadKey) {
        devState = DevState.Loading
        devState = DevelopersRepository.load(force = reloadKey > 0).fold(
            onSuccess = { DevState.Ready(it) },
            onFailure = { DevState.Failed },
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            val collapsedFraction = scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f)
            val titleWeight = lerp(850, 500, collapsedFraction)
            val titleFontSize = lerp(24.5f, 18f, collapsedFraction).sp
            val titleLineHeight = lerp(28.5f, 21f, collapsedFraction).sp
            val titleWdth = lerp(128f, 110f, collapsedFraction)
            val titleOpsz = lerp(24.5f, 18f, collapsedFraction)

            val subtitleFontSize = lerp(11f, 10f, collapsedFraction).sp
            val subtitleLineHeight = lerp(14f, 12f, collapsedFraction).sp
            val subtitleWeight = lerp(500, 450, collapsedFraction)
            val subtitleWdth = lerp(115f, 105f, collapsedFraction)
            val spacerHeight = lerp(1f, 0f, collapsedFraction).dp

            LargeTopAppBar(
                title = {
                    Column {
                        StaticFlexText(
                            text = "About & privacy",
                            weight = titleWeight,
                            grad = 50f,
                            wdth = titleWdth,
                            opsz = titleOpsz,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = titleFontSize,
                                lineHeight = titleLineHeight,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(spacerHeight))
                        StaticFlexText(
                            text = "Tiledeck Widgets",
                            weight = subtitleWeight,
                            grad = 20f,
                            wdth = subtitleWdth,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = subtitleFontSize,
                                lineHeight = subtitleLineHeight,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp)) {
                        CircleIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to settings",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 12.dp + navBarBottom),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { AboutHero() }

            item { GroupLabel("Made by") }
            item {
                DevelopersCard(
                    state = devState,
                    onClick = {
                        when (devState) {
                            is DevState.Ready -> showSheet = true
                            DevState.Failed -> reloadKey++
                            DevState.Loading -> Unit
                        }
                    },
                )
            }

            item { GroupLabel("App details") }
            item {
                GroupedList(
                    rows = listOf(
                        {
                            GroupedRow(
                                position = GroupPosition.Top,
                                title = "Endpoint",
                                subtitle = "lbscek.etlab.app",
                                badgeColor = palette[1].first,
                                badgeContent = {
                                    Text("E", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = palette[1].second)
                                },
                            )
                        },
                        {
                            GroupedRow(
                                position = GroupPosition.Bottom,
                                title = "Data policy",
                                subtitle = "On-device, encrypted only",
                                badgeColor = palette[2].first,
                                badgeContent = { LockDotIcon(tint = palette[2].second) },
                            )
                        },
                    ),
                )
            }

            item { GroupLabel("Privacy") }
            item { PrivacyCard() }
        }
    }

    val ready = devState as? DevState.Ready
    if (showSheet && ready != null) {
        DevelopersSheet(
            developers = ready.developers,
            onDismiss = { showSheet = false },
        )
    }
}

// ───────────────────────────── Hero ─────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutHero() {
    val context = LocalContext.current
    val versionName = remember { appVersionName(context) }
    val reduced = rememberReducedMotion()
    val scope = rememberCoroutineScope()

    // The icon shape only rotates: a slow endless spin, plus an extra spin kick on tap.
    val spin by rememberInfiniteTransition(label = "logoSpin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Restart),
        label = "logoSpinAngle",
    )
    val kick = remember { Animatable(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Rotating shape (the text is NOT inside this layer, so it stays upright).
            Surface(
                onClick = {
                    if (!reduced) {
                        scope.launch {
                            kick.animateTo(
                                kick.value + 360f,
                                spring<Float>(dampingRatio = 0.8f, stiffness = 15f),
                            )
                        }
                    }
                },
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = if (reduced) 0f else spin + kick.value },
            ) {}

            // The real app icon: the launcher's monochrome layer (the one Android uses for
            // themed icons), tinted with the system / dynamic color.
            Icon(
                painter = painterResource(R.mipmap.ic_launcher_monochrome),
                contentDescription = "Tiledeck logo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(190.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        // App name: the ONLY animated text. Plays once. Every letter animates its own
        // weight, width, roundness, grade, slant and optical size, starting a little after
        // the letter before it, so the motion travels left to right like a wave.
        AnimatedFlexText(
            text = "Tiledeck Widgets",
            targetWeight = 900,
            initialWeight = 100,
            targetRond = 100f,
            initialRond = 0f,
            targetGrad = 100f,
            initialGrad = -100f,
            targetWdth = 112f,
            initialWdth = 45f,
            targetSlnt = 0f,
            initialSlnt = -10f,
            targetOpsz = 40f,
            initialOpsz = 8f,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 23.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            startDelayMs = 500,
            letterGapMs = 110,
            timeScale = 1.8f,
        )

        Spacer(Modifier.height(10.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
            StaticFlexText(
                text = "Version $versionName",
                weight = 750,
                grad = 40f,
                wdth = 110f,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        Spacer(Modifier.height(12.dp))

        StaticFlexText(
            text = "Your widgets, always one glance away.",
            weight = 550,
            wdth = 105f,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ─────────────────────────── Developers ───────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DevelopersCard(state: DevState, onClick: () -> Unit) {
    val shape = AppCardShape
    val container = MaterialTheme.colorScheme.secondaryContainer
    val onContainer = MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        onClick = onClick,
        enabled = state !is DevState.Loading,
        shape = shape,
        color = container,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.width(96.dp), contentAlignment = Alignment.CenterStart) {
                when (state) {
                    DevState.Loading -> LoadingIndicator(modifier = Modifier.size(44.dp))
                    DevState.Failed -> Text("⚠", style = MaterialTheme.typography.headlineMedium)
                    is DevState.Ready -> AvatarStack(state.developers.take(4), ringColor = container)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                StaticFlexText(
                    text = "Developers",
                    weight = 850,
                    grad = 50f,
                    wdth = 110f,
                    style = MaterialTheme.typography.titleLarge,
                    color = onContainer,
                )
                when (state) {
                    DevState.Loading -> Text(
                        text = "Loading from GitHub…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainer.copy(alpha = 0.8f),
                    )
                    DevState.Failed -> Text(
                        text = "Couldn't load — tap to retry",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainer.copy(alpha = 0.8f),
                    )
                    is DevState.Ready -> {
                        val n = state.developers.size
                        val label = if (n == 1) "1 person built this" else "$n people built this"
                        StaticFlexText(
                            text = label,
                            weight = 650,
                            grad = 40f,
                            wdth = 108f,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onContainer.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            if (state is DevState.Ready) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = onContainer,
                )
            }
        }
    }
}

@Composable
private fun AvatarStack(developers: List<Developer>, ringColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
        developers.forEach { dev ->
            DeveloperAvatar(
                dev = dev,
                size = 44.dp,
                modifier = Modifier.border(2.dp, ringColor, CircleShape),
            )
        }
    }
}

@Composable
private fun DeveloperAvatar(dev: Developer, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dev.name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        AsyncImage(
            model = dev.avatarUrl,
            contentDescription = "${dev.name}'s profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevelopersSheet(developers: List<Developer>, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                StaticFlexText(
                    text = "Developers",
                    weight = 850,
                    grad = 60f,
                    wdth = 115f,
                    style = MaterialTheme.typography.headlineSmall,
                )
                StaticFlexText(
                    text = "Tap a profile to open it on GitHub",
                    weight = 450,
                    grad = 10f,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                itemsIndexed(developers, key = { _, dev -> dev.login }) { index, dev ->
                    DeveloperRow(
                        dev = dev,
                        position = groupPosition(index, developers.size),
                        onClick = { uriHandler.openUri(dev.profileUrl) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DeveloperRow(dev: Developer, position: GroupPosition, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = groupShape(position),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DeveloperAvatar(dev = dev, size = 56.dp)

            Column(modifier = Modifier.weight(1f)) {
                StaticFlexText(
                    text = dev.name,
                    weight = 750,
                    grad = 40f,
                    wdth = 108f,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "@${dev.login}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    text = "${dev.contributions} commits",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

// ───────────────────────────── Privacy ─────────────────────────────

@Composable
private fun PrivacyCard() {
    Surface(
        shape = AppCardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                LockDotIcon(tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "This app fetches your current semester's attendance automatically. " +
                        "Credentials are encrypted at rest on your device and are only ever " +
                        "transmitted to lbscek.etlab.app — never to any third party or " +
                        "analytics service.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LockDotIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.2f, size.height * 0.45f),
            size = Size(size.width * 0.6f, size.height * 0.45f),
            cornerRadius = CornerRadius(3f, 3f),
        )
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.3f, size.height * 0.15f),
            size = Size(size.width * 0.4f, size.height * 0.45f),
            style = Stroke(width = 2.5f),
        )
    }
}

private fun appVersionName(context: Context): String = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.PackageInfoFlags.of(0L),
        ).versionName
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }
}.getOrNull() ?: "1.0.0"

// ═════════════════════════════════════════════════════════════════════════════
//  Variable-font text animation (private to this file)
//
//  Google Sans Flex has six axes. Each one gets its own animation:
//    wght weight · wdth width · ROND roundness · GRAD grade · slnt slant · opsz optical size
//
//  AnimatedFlexText  plays ONCE: every axis has its own spring/tween, start delay and
//                    letter-by-letter stagger (words for long text)
//  StaticFlexText    fixed axis values, no animation (cheap)
//
//  Needs app/src/main/res/font/google_sans_flex.ttf (the variable TTF).
// ═════════════════════════════════════════════════════════════════════════════

private data class AxisKey(
    val w: Int, val wd: Int, val ro: Int, val g: Int, val s: Int, val o: Int,
)

private fun axisKey(
    w: Float, wd: Float, ro: Float, g: Float, s: Float, o: Float,
    fine: Boolean = false,
): AxisKey {
    // Animated text snaps to small steps so the typeface cache gets hits.
    fun q(v: Float, step: Int): Int =
        if (fine) v.roundToInt() else (v / step).roundToInt() * step
    return AxisKey(
        w = q(w, 10).coerceIn(1, 1000),
        wd = q(wd, 2).coerceAtLeast(1),
        ro = q(ro, 5).coerceIn(0, 100),
        g = q(g, 5),
        s = q(s, 1),
        o = q(o, 2).coerceAtLeast(1),
    )
}

private val familyCache = HashMap<AxisKey, FontFamily>()

@OptIn(ExperimentalTextApi::class)
private fun familyFor(k: AxisKey): FontFamily {
    if (familyCache.size > 800) familyCache.clear()
    return familyCache.getOrPut(k) {
        FontFamily(
            Font(
                resId = R.font.google_sans_flex,
                weight = FontWeight(k.w),
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(k.w),
                    FontVariation.width(k.wd.toFloat()),
                    FontVariation.opticalSizing(k.o.sp),
                    FontVariation.slant(k.s.toFloat().coerceIn(-90f, 90f)),
                    FontVariation.grade(k.g),
                    FontVariation.Setting("ROND", k.ro.toFloat()),
                ),
            )
        )
    }
}

private fun autoOpsz(style: TextStyle): Float =
    if (style.fontSize.isSp) style.fontSize.value else 16f

private fun mix(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)

private fun lerp(start: Int, stop: Int, fraction: Float): Int =
    (start + (stop - start) * fraction.coerceIn(0f, 1f)).roundToInt()

/** Short text staggers per letter, long text per word. */
private fun unitMap(text: String): Pair<IntArray, Int> {
    if (text.length <= 18) return IntArray(text.length) { it } to text.length.coerceAtLeast(1)
    var u = 0
    val arr = IntArray(text.length)
    text.forEachIndexed { i, c ->
        arr[i] = u
        if (c == ' ') u++
    }
    return arr to (u + 1)
}

/** Static text with explicit axis values (no animation). */
@Composable
private fun StaticFlexText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    weight: Int = 400,
    rond: Float = 100f,
    grad: Float = 0f,
    wdth: Float = 100f,
    slnt: Float = 0f,
    opsz: Float = Float.NaN,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign = TextAlign.Unspecified,
) {
    val key = axisKey(
        weight.toFloat(), wdth, rond, grad, slnt,
        if (opsz.isNaN()) autoOpsz(style) else opsz,
        fine = true,
    )
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style.copy(fontFamily = familyFor(key), fontWeight = FontWeight(key.w)),
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
    )
}

private const val IDX_WGHT = 0
private const val IDX_WDTH = 1
private const val IDX_ROND = 2
private const val IDX_GRAD = 3
private const val IDX_SLNT = 4
private const val IDX_OPSZ = 5

/**
 * One axis' motion curve. Either a damped spring ([zeta] > 0, can overshoot) or an
 * eased tween. [letterFactor] makes each axis ripple across the letters at its own pace.
 */
private class AxisSpec(
    val zeta: Float,
    val stiffness: Float,
    val tweenMs: Float,
    val baseDelayMs: Int,
    val letterFactor: Float,
) {
    /** Progress at [tMs] after this axis/letter starts: 0 before, ~1 after. */
    fun at(tMs: Float): Float {
        if (tMs <= 0f) return 0f
        return if (zeta > 0f) {
            val t = tMs / 1000f
            val w = sqrt(stiffness)
            val wd = w * sqrt(1f - zeta * zeta)
            val e = exp(-zeta * w * t)
            1f - e * (cos(wd * t) + (zeta * w / wd) * sin(wd * t))
        } else {
            FastOutSlowInEasing.transform((tMs / tweenMs).coerceIn(0f, 1f))
        }
    }

    fun settleMs(): Float =
        if (zeta > 0f) 4500f / (zeta * sqrt(stiffness)) else tweenMs
}

// ───────────────────────── Smooth-animation plumbing ─────────────────────────

/**
 * Builds the animated typefaces OFF the main thread and keeps them in our own cache.
 *
 * Why: Compose's own font cache only holds ~16 typefaces, so every animation frame (16 letters,
 * each with different axis values) used to create brand-new typefaces on the main thread.
 * That is what made the animation feel like 60fps while the rest of the UI ran at 120.
 */
private object FlexTypefaces {
    private const val MAX_ENTRIES = 2400
    private val lock = Any()
    private var fontFile: File? = null

    private val cache = object : LinkedHashMap<AxisKey, FontFamily>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<AxisKey, FontFamily>?): Boolean =
            size > MAX_ENTRIES
    }

    /** Copies the variable TTF out of res/font once, so typefaces can be built from a file. */
    private fun fontFile(context: Context): File? = synchronized(lock) {
        fontFile ?: runCatching {
            val out = File(context.cacheDir, "google_sans_flex_anim.ttf")
            if (!out.exists() || out.length() == 0L) {
                val tmp = File(context.cacheDir, "google_sans_flex_anim.ttf.tmp")
                context.resources.openRawResource(R.font.google_sans_flex).use { input ->
                    tmp.outputStream().use { input.copyTo(it) }
                }
                tmp.renameTo(out)
            }
            out
        }.getOrNull().also { fontFile = it }
    }

    fun prepare(context: Context) {
        fontFile(context)
    }

    fun get(context: Context, k: AxisKey): FontFamily {
        synchronized(lock) { cache[k] }?.let { return it }
        val built = build(context, k)          // built outside the lock
        synchronized(lock) { cache[k] = built }
        return built
    }

    private fun build(context: Context, k: AxisKey): FontFamily {
        val file = fontFile(context) ?: return familyFor(k)
        val typeface = runCatching {
            Typeface.Builder(file)
                .setFontVariationSettings(
                    "'wght' ${k.w}, 'wdth' ${k.wd}, 'opsz' ${k.o}, " +
                            "'slnt' ${k.s}, 'GRAD' ${k.g}, 'ROND' ${k.ro}"
                )
                .build()
        }.getOrNull() ?: return familyFor(k)
        return FontFamily(typeface)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Android's adaptive refresh rate drops idle animations to 60Hz unless the app asks for more.
 * While [active], ask for the highest refresh rate so the text animation runs at 120Hz.
 */
@Composable
private fun HighFrameRate(active: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, active) {
        if (!active) return@DisposableEffect onDispose { }

        val window = view.context.findActivity()?.window
        val oldPreferred = window?.attributes?.preferredRefreshRate ?: 0f

        if (Build.VERSION.SDK_INT >= 35) {
            view.requestedFrameRate = View.REQUESTED_FRAME_RATE_CATEGORY_HIGH
        }
        val maxRate = view.display?.supportedModes?.maxOfOrNull { it.refreshRate }
        if (window != null && maxRate != null) {
            window.attributes = window.attributes.apply { preferredRefreshRate = maxRate }
        }

        onDispose {
            if (Build.VERSION.SDK_INT >= 35) {
                view.requestedFrameRate = View.REQUESTED_FRAME_RATE_CATEGORY_DEFAULT
            }
            if (window != null) {
                window.attributes = window.attributes.apply { preferredRefreshRate = oldPreferred }
            }
        }
    }
}

/**
 * Plays once. EVERY LETTER gets its own delayed animation on EVERY axis: one shared
 * clock drives all of them, and each letter starts [letterGapMs] after the previous
 * one, with each axis rippling at its own speed (weight, width, roundness, grade,
 * slant, optical size). Replays only if [text] or [triggerKey] changes.
 *
 *  timeScale     3f = everything plays 3x slower
 *  axisGapMs     > 0: axes start one after another, this many ms apart
 *  startDelayMs  pause before anything starts
 *  letterGapMs   delay between one letter and the next (words for long text)
 *
 * Smoothness: typefaces for the next ~0.9s are built on a background thread while the
 * animation plays (see [FlexTypefaces]), and the screen is asked to run at its top refresh rate.
 */
@Composable
private fun AnimatedFlexText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    targetWeight: Int = 500,
    initialWeight: Int = 100,
    targetRond: Float = 100f,
    initialRond: Float = 0f,
    targetGrad: Float = 0f,
    initialGrad: Float = -50f,
    targetWdth: Float = 100f,
    initialWdth: Float = 80f,
    targetSlnt: Float = 0f,
    initialSlnt: Float = -8f,
    targetOpsz: Float = Float.NaN,
    initialOpsz: Float = Float.NaN,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign = TextAlign.Unspecified,
    triggerKey: Any? = null,
    timeScale: Float = 1f,
    axisGapMs: Int = 0,
    startDelayMs: Int = 0,
    letterGapMs: Int = 60,
) {
    val reduced = rememberReducedMotion()
    val appContext = LocalContext.current.applicationContext
    val ts = timeScale.coerceAtLeast(0.1f)
    val specs = remember(ts) {
        listOf(
            AxisSpec(0.42f, 70f / (ts * ts), 0f, 0, 1.0f),     // weight: bouncy
            AxisSpec(0.60f, 55f / (ts * ts), 0f, 90, 0.8f),    // width: softer
            AxisSpec(0.75f, 35f / (ts * ts), 0f, 180, 1.3f),   // roundness: silky
            AxisSpec(0f, 0f, 900f * ts, 120, 0.6f),            // grade: eased
            AxisSpec(0.30f, 110f / (ts * ts), 0f, 240, 1.6f),  // slant: wobbly
            AxisSpec(0f, 0f, 1100f * ts, 60, 0.5f),            // optical size: long ease
        )
    }
    val (unitOf, units) = remember(text) { unitMap(text) }

    fun axisDelay(i: Int): Float =
        (startDelayMs + if (axisGapMs > 0) i * axisGapMs else (specs[i].baseDelayMs * ts).toInt()).toFloat()

    val totalMs = remember(specs, units, startDelayMs, axisGapMs, letterGapMs) {
        specs.indices.maxOf { i ->
            axisDelay(i) + (units - 1) * letterGapMs * specs[i].letterFactor + specs[i].settleMs()
        } + 100f
    }

    val tOpsz = if (targetOpsz.isNaN()) autoOpsz(style) else targetOpsz
    val iOpsz = if (initialOpsz.isNaN()) (tOpsz * 0.4f).coerceAtLeast(6f) else initialOpsz

    /** Axis values of letter [index] at time [tMs] on the animation clock. */
    fun keyAt(tMs: Float, index: Int): AxisKey {
        val u = unitOf[index]
        fun p(i: Int): Float =
            if (reduced) 1f
            else specs[i].at(tMs - axisDelay(i) - u * letterGapMs * specs[i].letterFactor)
        return axisKey(
            w = mix(initialWeight.toFloat(), targetWeight.toFloat(), p(IDX_WGHT)),
            wd = mix(initialWdth, targetWdth, p(IDX_WDTH)),
            ro = mix(initialRond, targetRond, p(IDX_ROND)),
            g = mix(initialGrad, targetGrad, p(IDX_GRAD)),
            s = mix(initialSlnt, targetSlnt, p(IDX_SLNT)),
            o = mix(iOpsz, tOpsz, p(IDX_OPSZ)),
        )
    }

    val clock = remember { Animatable(0f) }
    LaunchedEffect(text, triggerKey, reduced, totalMs) {
        if (reduced) {
            clock.snapTo(totalMs)
            return@LaunchedEffect
        }
        clock.snapTo(0f)

        // Build typefaces ahead of the clock on a background thread, ~0.9s of animation ahead.
        val lookAheadMs = 900f
        val firstWindowReady = CompletableDeferred<Unit>()
        val prewarm = launch(Dispatchers.Default) {
            FlexTypefaces.prepare(appContext)
            var t = 0f
            while (t <= totalMs) {
                if (t >= lookAheadMs) firstWindowReady.complete(Unit)
                while (t > clock.value + lookAheadMs) delay(16)
                for (index in text.indices) FlexTypefaces.get(appContext, keyAt(t, index))
                t += 8f
            }
            firstWindowReady.complete(Unit)
        }
        // Start once the first window is ready (or after 2.5s at most, building keeps going).
        withTimeoutOrNull(2500) { firstWindowReady.await() }

        clock.animateTo(totalMs, tween(totalMs.toInt(), easing = LinearEasing))
        prewarm.cancel()
    }
    val now = clock.value

    HighFrameRate(active = !reduced && now < totalMs)

    val annotated = buildAnnotatedString {
        text.forEachIndexed { index, ch ->
            val family = FlexTypefaces.get(appContext, keyAt(now, index))
            withStyle(
                SpanStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.Normal,
                    fontSynthesis = FontSynthesis.None,
                )
            ) {
                append(ch)
            }
        }
    }

    Text(
        text = annotated,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
    )
}
package io.github.shreyasskdev.tiledeck.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import io.github.shreyasskdev.tiledeck.R

/**
 * Creates a Google Sans Flex [Font] with specific variable font axes:
 * - weight ('wght'): 100..1000
 * - rond ('ROND'): 0..100 (corner roundness)
 * - grad ('GRAD'): -200..150 (font grade / contrast)
 * - opsz ('opsz'): 6..144 (optical size)
 * - wdth ('wdth'): 25..150 (font width / expansion)
 * - slnt ('slnt'): -10..0 (slant / italic)
 */
@OptIn(ExperimentalTextApi::class)
fun flexFont(
    weight: FontWeight,
    rond: Float = 100f,
    grad: Float = 0f,
    opsz: Float = 16f,
    wdth: Float = 100f,
    slnt: Float = 0f,
) = Font(
    resId = R.font.google_sans_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("ROND", rond.coerceIn(0f, 100f)),
        FontVariation.Setting("GRAD", grad.coerceIn(-200f, 150f)),
        FontVariation.Setting("opsz", opsz.coerceIn(6f, 144f)),
        FontVariation.Setting("wdth", wdth.coerceIn(25f, 150f)),
        FontVariation.Setting("slnt", slnt.coerceIn(-10f, 0f)),
    )
)

/**
 * Full-spectrum Google Sans Flex FontFamily with default fully rounded ('ROND' = 100f) settings
 * adhering to Material 3 Expressive design standards.
 */
val AppFontFamily = FontFamily(
    flexFont(FontWeight.Thin),
    flexFont(FontWeight.ExtraLight),
    flexFont(FontWeight.Light),
    flexFont(FontWeight.Normal),
    flexFont(FontWeight.Medium),
    flexFont(FontWeight.SemiBold),
    flexFont(FontWeight.Bold),
    flexFont(FontWeight.ExtraBold),
    flexFont(FontWeight.Black),
)

/**
 * Generates a dynamic [FontFamily] using Google Sans Flex variable axes:
 * - [weight]: 100..1000 ('wght')
 * - [rond]: 0..100 ('ROND' corner roundness)
 * - [grad]: -200..150 ('GRAD' grade for contrast / visual weight tuning)
 * - [opsz]: 6..144 ('opsz' optical sizing)
 * - [wdth]: 25..150 ('wdth' font width / expansion)
 * - [slnt]: -10..0 ('slnt' font slant / angle)
 */
@OptIn(ExperimentalTextApi::class)
fun flexFontFamily(
    weight: Int = 400,
    rond: Float = 100f,
    grad: Float = 0f,
    opsz: Float = 16f,
    wdth: Float = 100f,
    slnt: Float = 0f,
): FontFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight(weight.coerceIn(100, 1000)),
        variationSettings = FontVariation.Settings(
            FontVariation.weight(weight.coerceIn(100, 1000)),
            FontVariation.Setting("ROND", rond.coerceIn(0f, 100f)),
            FontVariation.Setting("GRAD", grad.coerceIn(-200f, 150f)),
            FontVariation.Setting("opsz", opsz.coerceIn(6f, 144f)),
            FontVariation.Setting("wdth", wdth.coerceIn(25f, 150f)),
            FontVariation.Setting("slnt", slnt.coerceIn(-10f, 0f)),
        )
    )
)

/**
 * Remembers a dynamic Google Sans Flex [FontFamily] with quantized values for smooth composition.
 */
@Composable
fun rememberFlexFontFamily(
    weight: Int = 400,
    rond: Float = 100f,
    grad: Float = 0f,
    opsz: Float = 16f,
    wdth: Float = 100f,
    slnt: Float = 0f,
): FontFamily {
    val qWeight = weight.coerceIn(100, 1000)
    val qRond = (rond.coerceIn(0f, 100f) * 2f).toInt() / 2f
    val qGrad = (grad.coerceIn(-200f, 150f) * 2f).toInt() / 2f
    val qOpsz = (opsz.coerceIn(6f, 144f) * 2f).toInt() / 2f
    val qWdth = (wdth.coerceIn(25f, 150f) * 2f).toInt() / 2f
    val qSlnt = (slnt.coerceIn(-10f, 0f) * 2f).toInt() / 2f

    return remember(qWeight, qRond, qGrad, qOpsz, qWdth, qSlnt) {
        flexFontFamily(qWeight, qRond, qGrad, qOpsz, qWdth, qSlnt)
    }
}

/**
 * A Material 3 Expressive text component that animates all Google Sans Flex variable font axes
 * (weight, rond, grad, opsz, wdth, slnt) simultaneously using spring physics.
 */
@Composable
fun AnimatedFlexText(
    text: String,
    modifier: Modifier = Modifier,
    targetWeight: Int = 700,
    initialWeight: Int = 300,
    targetRond: Float = 100f,
    initialRond: Float = 0f,
    targetGrad: Float = 0f,
    initialGrad: Float = 0f,
    targetOpsz: Float = 16f,
    initialOpsz: Float = 16f,
    targetWdth: Float = 100f,
    initialWdth: Float = 100f,
    targetSlnt: Float = 0f,
    initialSlnt: Float = 0f,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    triggerKey: Any? = Unit,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    ),
) {
    var animate by remember(triggerKey) { mutableStateOf(false) }

    LaunchedEffect(triggerKey) {
        animate = true
    }

    val animatedWeight by animateFloatAsState(
        targetValue = if (animate) targetWeight.toFloat() else initialWeight.toFloat(),
        animationSpec = animationSpec,
        label = "WeightAnim",
    )
    val animatedRond by animateFloatAsState(
        targetValue = if (animate) targetRond else initialRond,
        animationSpec = animationSpec,
        label = "RondAnim",
    )
    val animatedGrad by animateFloatAsState(
        targetValue = if (animate) targetGrad else initialGrad,
        animationSpec = animationSpec,
        label = "GradAnim",
    )
    val animatedOpsz by animateFloatAsState(
        targetValue = if (animate) targetOpsz else initialOpsz,
        animationSpec = animationSpec,
        label = "OpszAnim",
    )
    val animatedWdth by animateFloatAsState(
        targetValue = if (animate) targetWdth else initialWdth,
        animationSpec = animationSpec,
        label = "WdthAnim",
    )
    val animatedSlnt by animateFloatAsState(
        targetValue = if (animate) targetSlnt else initialSlnt,
        animationSpec = animationSpec,
        label = "SlntAnim",
    )

    val fontFamily = rememberFlexFontFamily(
        weight = animatedWeight.toInt(),
        rond = animatedRond,
        grad = animatedGrad,
        opsz = animatedOpsz,
        wdth = animatedWdth,
        slnt = animatedSlnt,
    )

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontFamily = fontFamily),
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}

/**
 * A Material 3 Expressive text component that continuously breathes/pulses across all variable font axes
 * (weight, rond, grad, wdth, slnt) during dynamic or indeterminate states.
 */
@Composable
fun PulsingFlexText(
    text: String,
    modifier: Modifier = Modifier,
    minWeight: Int = 300,
    maxWeight: Int = 850,
    minRond: Float = 0f,
    maxRond: Float = 100f,
    minGrad: Float = -50f,
    maxGrad: Float = 100f,
    minOpsz: Float = 12f,
    maxOpsz: Float = 24f,
    minWdth: Float = 85f,
    maxWdth: Float = 120f,
    minSlnt: Float = -8f,
    maxSlnt: Float = 0f,
    pulsePeriodMs: Int = 1200,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FlexPulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulsePeriodMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseProgress",
    )

    val currentWeight = (minWeight + (maxWeight - minWeight) * pulseProgress).toInt()
    val currentRond = minRond + (maxRond - minRond) * pulseProgress
    val currentGrad = minGrad + (maxGrad - minGrad) * pulseProgress
    val currentOpsz = minOpsz + (maxOpsz - minOpsz) * pulseProgress
    val currentWdth = minWdth + (maxWdth - minWdth) * pulseProgress
    val currentSlnt = minSlnt + (maxSlnt - minSlnt) * pulseProgress

    val fontFamily = rememberFlexFontFamily(
        weight = currentWeight,
        rond = currentRond,
        grad = currentGrad,
        opsz = currentOpsz,
        wdth = currentWdth,
        slnt = currentSlnt,
    )

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontFamily = fontFamily),
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}

/**
 * A convenience Text composable configured with custom static variable font parameters across all axes.
 */
@Composable
fun FlexText(
    text: String,
    modifier: Modifier = Modifier,
    weight: Int = 400,
    rond: Float = 100f,
    grad: Float = 0f,
    opsz: Float = 16f,
    wdth: Float = 100f,
    slnt: Float = 0f,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
) {
    val fontFamily = rememberFlexFontFamily(
        weight = weight,
        rond = rond,
        grad = grad,
        opsz = opsz,
        wdth = wdth,
        slnt = slnt,
    )

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontFamily = fontFamily),
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}

private val base = Typography()

private fun TextStyle.scaled(factor: Float = 0.88f): TextStyle {
    val newFontSize = if (fontSize.isSp) (fontSize.value * factor).sp else fontSize
    val newLineHeight = if (lineHeight.isSp) (lineHeight.value * factor).sp else lineHeight
    return copy(fontFamily = AppFontFamily, fontSize = newFontSize, lineHeight = newLineHeight)
}

val AppTypography = Typography(
    displayLarge = base.displayLarge.scaled(),
    displayMedium = base.displayMedium.scaled(),
    displaySmall = base.displaySmall.scaled(),
    headlineLarge = base.headlineLarge.scaled(),
    headlineMedium = base.headlineMedium.scaled(),
    headlineSmall = base.headlineSmall.scaled(),
    titleLarge = base.titleLarge.scaled(),
    titleMedium = base.titleMedium.scaled(),
    titleSmall = base.titleSmall.scaled(),
    bodyLarge = base.bodyLarge.scaled(),
    bodyMedium = base.bodyMedium.scaled(),
    bodySmall = base.bodySmall.scaled(),
    labelLarge = base.labelLarge.scaled(),
    labelMedium = base.labelMedium.scaled(),
    labelSmall = base.labelSmall.scaled(),

    // Expressive emphasized styles.
    displayLargeEmphasized = base.displayLargeEmphasized.scaled(),
    displayMediumEmphasized = base.displayMediumEmphasized.scaled(),
    displaySmallEmphasized = base.displaySmallEmphasized.scaled(),
    headlineLargeEmphasized = base.headlineLargeEmphasized.scaled(),
    headlineMediumEmphasized = base.headlineMediumEmphasized.scaled(),
    headlineSmallEmphasized = base.headlineSmallEmphasized.scaled(),
    titleLargeEmphasized = base.titleLargeEmphasized.scaled(),
    titleMediumEmphasized = base.titleMediumEmphasized.scaled(),
    titleSmallEmphasized = base.titleSmallEmphasized.scaled(),
    bodyLargeEmphasized = base.bodyLargeEmphasized.scaled(),
    bodyMediumEmphasized = base.bodyMediumEmphasized.scaled(),
    bodySmallEmphasized = base.bodySmallEmphasized.scaled(),
    labelLargeEmphasized = base.labelLargeEmphasized.scaled(),
    labelMediumEmphasized = base.labelMediumEmphasized.scaled(),
    labelSmallEmphasized = base.labelSmallEmphasized.scaled(),
)
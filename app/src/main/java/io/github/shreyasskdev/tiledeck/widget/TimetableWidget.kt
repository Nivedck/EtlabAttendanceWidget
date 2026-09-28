package io.github.shreyasskdev.tiledeck.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.ColorFilter
import androidx.glance.ImageProvider
import io.github.shreyasskdev.tiledeck.R
import io.github.shreyasskdev.tiledeck.data.AttendancePrefs
import io.github.shreyasskdev.tiledeck.data.TimetableDayResult
import io.github.shreyasskdev.tiledeck.data.TimetableResult
import io.github.shreyasskdev.tiledeck.ui.MainActivity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val TIMETABLE_WIDGET_UPDATE_KEY = longPreferencesKey("timetable_widget_update_timestamp")

// LBSCEK standard period start times (hour index 1–6 → hour, minute)
private val PERIOD_START_TIMES = mapOf(
    1 to Pair(9, 30),
    2 to Pair(10, 30),
    3 to Pair(11, 30),
    4 to Pair(13, 30),
    5 to Pair(14, 30),
    6 to Pair(15, 30)
)

// LBSCEK standard period end times
private val PERIOD_END_TIMES = mapOf(
    1 to Pair(10, 20),
    2 to Pair(11, 20),
    3 to Pair(12, 20),
    4 to Pair(14, 20),
    5 to Pair(15, 20),
    6 to Pair(16, 20)
)

class TimetableWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<Preferences> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext

        provideContent {
            val glanceState = currentState<Preferences>()
            val updateTime = glanceState[TIMETABLE_WIDGET_UPDATE_KEY] ?: 0L

            val prefs = remember(updateTime) { AttendancePrefs(appContext) }
            val timetable = prefs.getLastTimetable()
            val subjectNameOverrides = prefs.getSubjectNames()
            val hasCredentials = prefs.hasCredentials()

            GlanceTheme {
                WidgetContent(hasCredentials, timetable, subjectNameOverrides)
            }
        }
    }

    @Composable
    private fun WidgetContent(
        hasCredentials: Boolean,
        timetable: TimetableResult?,
        subjectNameOverrides: Map<String, String>
    ) {
        val context = LocalContext.current
        val today = LocalDate.now()
        val todayKey = today.dayOfWeek.name.take(1).let { first ->
            // Map Java DayOfWeek to Etlab day keys: M, T, W, Th, F
            when (today.dayOfWeek.value) {
                1 -> "M"
                2 -> "T"
                3 -> "W"
                4 -> "Th"
                5 -> "F"
                else -> null // Weekend
            }
        }

        val todaySchedule = todayKey?.let { key ->
            timetable?.timetable?.find { it.day == key }
        }

        val dayLabel = today.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)).uppercase()
        val dateLabel = today.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)).uppercase()
        val headerText = "$dayLabel • $dateLabel"

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(24.dp)
                .padding(12.dp)
        ) {
            if (!hasCredentials || timetable == null) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap to set up TileDeck",
                        style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp)
                    )
                }
            } else if (todayKey == null) {
                // Weekend
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            headerText,
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(6.dp))
                        Text(
                            "No classes today",
                            style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 11.sp)
                        )
                    }
                }
            } else if (todaySchedule == null || todaySchedule.subjects.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            headerText,
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(6.dp))
                        Text(
                            "Timetable unavailable",
                            style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 11.sp)
                        )
                    }
                }
            } else {
                TimetableGrid(
                    headerText = headerText,
                    schedule = todaySchedule,
                    subjectNameOverrides = subjectNameOverrides,
                    context = context
                )
            }
        }
    }

    @Composable
    private fun TimetableGrid(
        headerText: String,
        schedule: TimetableDayResult,
        subjectNameOverrides: Map<String, String>,
        context: Context
    ) {
        val now = java.time.LocalTime.now()
        val nowHour = now.hour
        val nowMinute = now.minute

        // Filter to only hours 1-6 and sort
        val periods = schedule.subjects
            .filter { it.hour in 1..6 }
            .sortedBy { it.hour }

        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Header row: day • date
            Text(
                text = headerText,
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // 2×3 grid of period pills
            val rows = periods.chunked(3)
            rows.forEachIndexed { rowIndex, rowPeriods ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .padding(bottom = if (rowIndex < rows.size - 1) 6.dp else 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowPeriods.forEachIndexed { colIndex, subject ->
                        val isCurrentPeriod = isCurrentPeriod(subject.hour, nowHour, nowMinute)
                        val shortName = resolveShortName(subject.subject, subjectNameOverrides)

                        PeriodPill(
                            label = shortName,
                            hour = subject.hour,
                            isCurrent = isCurrentPeriod,
                            context = context,
                            modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                        )

                        if (colIndex < rowPeriods.size - 1) {
                            Spacer(modifier = GlanceModifier.width(6.dp))
                        }
                        // Fill remaining space if row has fewer than 3 items
                        if (colIndex == rowPeriods.size - 1 && rowPeriods.size < 3) {
                            repeat(3 - rowPeriods.size) {
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {}
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PeriodPill(
        label: String,
        hour: Int,
        isCurrent: Boolean,
        context: Context,
        modifier: GlanceModifier
    ) {
        val bgColor = if (isCurrent)
            GlanceTheme.colors.primaryContainer
        else
            GlanceTheme.colors.secondaryContainer

        val textColor = if (isCurrent)
            GlanceTheme.colors.onPrimaryContainer
        else
            GlanceTheme.colors.onSecondaryContainer

        val timeStr = PERIOD_START_TIMES[hour]?.let { (h, m) ->
            "%d:%02d".format(h, m)
        } ?: ""

        Box(
            modifier = modifier
                .background(
                    imageProvider = ImageProvider(R.drawable.bg_card_inner),
                    colorFilter = ColorFilter.tint(bgColor)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (timeStr.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.width(3.dp))
                    Text(
                        text = timeStr,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }

    /**
     * Resolves the display name for a period subject.
     *
     * Priority:
     * 1. User-defined override keyed by COURSE CODE (e.g., "PBITT504" -> "WAD").
     *    Keying by period number is WRONG because the same period slot holds different
     *    subjects on different days.
     * 2. Cleaned subject name parsed from the raw API string.
     */
    private fun resolveShortName(
        rawSubject: String,
        overrides: Map<String, String>
    ): String {
        // Extract the course code from the raw subject (e.g., "PCITT501")
        // to check if the user has a custom shorthand override for it.
        val courseCode = extractCourseCode(rawSubject)
        if (courseCode != null) {
            overrides[courseCode]?.takeIf { it.isNotBlank() }?.let { return it }
        }

        return parseSubjectName(rawSubject)
    }

    /**
     * Extracts the course code prefix from a raw Etlab subject string.
     *
     * Example: "PCITT501 - MACHINE LEARNING [ Theory ]RICHU NORMAN" → "PCITT501"
     *          "CLOUD COMPUTING" → null (no code prefix)
     */
    private fun extractCourseCode(raw: String): String? {
        val dashIdx = raw.indexOf(" - ")
        if (dashIdx <= 0) return null
        val potentialCode = raw.substring(0, dashIdx).trim()
        return if (potentialCode.length <= 12 && potentialCode.matches(Regex("[A-Z0-9]+"))) {
            potentialCode
        } else null
    }

    /**
     * Parses Etlab's verbose subject strings into a clean display name.
     *
     * The API returns strings like:
     *   "PCITT501 - MACHINE LEARNING [ Theory ]RICHU NORMAN"
     *   "PCITT502 - ALGORITHM ANALYSIS AND DESIGN[ Theory ]Mr. BINOY D M PANICKER"
     *   "CLOUD COMPUTING"  (no code, no faculty)
     *
     * Output keeps the original casing from the API after stripping noise:
     *   → "MACHINE LEARNING"
     *   → "ALGORITHM ANALYSIS AND DESIGN"
     *   → "CLOUD COMPUTING"
     */
    private fun parseSubjectName(raw: String): String {
        var name = raw.trim()

        // Strip everything from "[" onwards (type tag + faculty name)
        // e.g., "MACHINE LEARNING [ Theory ]RICHU NORMAN" → "MACHINE LEARNING"
        val bracketIdx = name.indexOf('[')
        if (bracketIdx > 0) {
            name = name.substring(0, bracketIdx).trim()
        }

        // Strip leading course code: "PCITT501 - MACHINE LEARNING" → "MACHINE LEARNING"
        val dashIdx = name.indexOf(" - ")
        if (dashIdx > 0) {
            val potentialCode = name.substring(0, dashIdx).trim()
            if (potentialCode.length <= 12 && potentialCode.matches(Regex("[A-Z0-9]+"))) {
                name = name.substring(dashIdx + 3).trim()
            }
        }

        return name
    }

    private fun isCurrentPeriod(hour: Int, nowHour: Int, nowMinute: Int): Boolean {
        val start = PERIOD_START_TIMES[hour] ?: return false
        val end = PERIOD_END_TIMES[hour] ?: return false
        val nowTotal = nowHour * 60 + nowMinute
        val startTotal = start.first * 60 + start.second
        val endTotal = end.first * 60 + end.second
        return nowTotal in startTotal..endTotal
    }
}

package com.listo.app.ui.screens.spending

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.domain.spending.PeriodCalendar
import com.listo.app.domain.spending.SpendingCsvColumns
import com.listo.app.domain.spending.SpendingExport
import com.listo.app.domain.spending.SpendingPeriod
import com.listo.app.domain.spending.SpendingShare
import com.listo.app.domain.spending.SpendingTextLabels
import com.listo.app.ui.components.EmptyState
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.screens.stores.StoreBadge
import com.listo.app.ui.viewmodel.SpendingUiState
import com.listo.app.ui.viewmodel.SpendingViewModel
import com.listo.app.utils.CategoryLabels
import com.listo.app.utils.GroceryCatalog
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.emojiForCategory
import com.listo.app.utils.formatPrice
import com.listo.app.utils.toComposeColor
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

/** Colour for spending with no category or no store. */
private const val OTHER_COLOR = "#9CA3AF"

/** Wider than this, the summary and the breakdown sit side by side. */
private val TWO_COLUMN_WIDTH = 600.dp

private enum class Grouping { CATEGORY, STORE }

/** How much was spent over a week, a month or chosen days, by category or store. */
@Composable
fun SpendingScreen(
    navController: NavController,
    viewModel: SpendingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var grouping by rememberSaveable { mutableStateOf(Grouping.CATEGORY) }
    var shareMenu by remember { mutableStateOf(false) }
    var pickDates by remember { mutableStateOf(false) }
    val labels = rememberTextLabels(state)
    val columns = SpendingCsvColumns(
        stringResource(R.string.csv_date), stringResource(R.string.csv_item), stringResource(R.string.csv_category),
        stringResource(R.string.csv_store), stringResource(R.string.csv_amount)
    )
    val periodText = periodLabel(state.period)

    Scaffold(
        topBar = {
            LisToTopBar(
                title = stringResource(R.string.spending_title),
                navController = navController,
                actions = {
                    Box {
                        IconButton(onClick = { shareMenu = true }, enabled = state.summary.boughtCount > 0) {
                            Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.spending_share))
                        }
                        DropdownMenu(expanded = shareMenu, onDismissRequest = { shareMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.spending_share_text)) },
                                leadingIcon = { Icon(Icons.Rounded.TextSnippet, contentDescription = null) },
                                onClick = {
                                    shareMenu = false
                                    shareText(context, SpendingExport.text(state.summary, periodText, labels))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.spending_share_csv)) },
                                leadingIcon = { Icon(Icons.Rounded.TableChart, contentDescription = null) },
                                onClick = {
                                    shareMenu = false
                                    val csv = SpendingExport.csv(state.itemsInPeriod, columns, labels, Locale.getDefault(), TimeZone.getDefault())
                                    shareCsv(context, csv)
                                }
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val wide = maxWidth >= TWO_COLUMN_WIDTH
            val controls: @Composable ColumnScope.() -> Unit = {
                PeriodPicker(
                    state = state,
                    periodText = periodText,
                    onWeeks = viewModel::showWeeks,
                    onMonths = viewModel::showMonths,
                    onCustom = { pickDates = true },
                    onPrevious = viewModel::previous,
                    onNext = viewModel::next
                )
                TotalCard(state)
            }
            val breakdown: @Composable ColumnScope.() -> Unit = {
                if (state.summary.boughtCount == 0 && !state.isLoading) {
                    EmptyState(
                        title = stringResource(R.string.spending_empty_title),
                        subtitle = stringResource(R.string.spending_empty_subtitle)
                    )
                } else {
                    GroupingChips(grouping) { grouping = it }
                    if (grouping == Grouping.CATEGORY) {
                        state.summary.byCategory.forEach { share ->
                            ShareRow(
                                share = share,
                                name = labels.category(share.key),
                                color = share.color ?: OTHER_COLOR,
                                leading = { EmojiWell(emojiForCategory(share.key), share.color) }
                            )
                        }
                    } else {
                        state.summary.byStore.forEach { share ->
                            ShareRow(
                                share = share,
                                name = labels.store(share.key),
                                color = storeColor(state, share.key),
                                leading = { StoreBadge(storeColor(state, share.key), size = 36) }
                            )
                        }
                    }
                    if (state.summary.unpricedCount > 0) {
                        Text(
                            labels.unpriced(state.summary.unpricedCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            if (wide) {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), content = controls)
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 8.dp), content = breakdown)
                }
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
                ) {
                    controls()
                    breakdown()
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (pickDates) {
        DateRangeDialog(
            onPick = { from, to -> pickDates = false; viewModel.showCustom(from, to) },
            onDismiss = { pickDates = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodPicker(
    state: SpendingUiState,
    periodText: String,
    onWeeks: () -> Unit,
    onMonths: () -> Unit,
    onCustom: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val options = listOf(
        stringResource(R.string.spending_week) to onWeeks,
        stringResource(R.string.spending_month) to onMonths,
        stringResource(R.string.spending_custom) to onCustom
    )
    val selected = when (state.period) {
        is SpendingPeriod.Week -> 0
        is SpendingPeriod.Month -> 1
        is SpendingPeriod.Custom -> 2
    }
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        options.forEachIndexed { index, (label, action) ->
            SegmentedButton(
                selected = index == selected,
                onClick = action,
                shape = SegmentedButtonDefaults.itemShape(index, options.size)
            ) { Text(label, maxLines = 1) }
        }
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.cd_previous_period))
        }
        Text(
            periodText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onNext, enabled = state.canGoForward) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = stringResource(R.string.cd_next_period))
        }
    }
}

@Composable
private fun TotalCard(state: SpendingUiState) {
    val summary = state.summary
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                stringResource(R.string.spending_total).uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Text(
                formatPrice(summary.total),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val change = summary.change
            val (text, color) = when {
                abs(change) < 0.005 -> stringResource(R.string.spending_same) to MaterialTheme.colorScheme.onSurfaceVariant
                change > 0 -> stringResource(R.string.spending_more, formatPrice(change)) to MaterialTheme.colorScheme.error
                else -> stringResource(R.string.spending_less, formatPrice(-change)) to MaterialTheme.colorScheme.primary
            }
            Text((if (change > 0.005) "▲ " else if (change < -0.005) "▼ " else "") + text, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            val calendar = remember { PeriodCalendar() }
            val days = (state.period as? SpendingPeriod.Custom)?.days(calendar)
            Text(
                listOfNotNull(
                    days?.let { stringResource(R.string.spending_days, it) },
                    stringResource(R.string.spending_bought, summary.boughtCount)
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun GroupingChips(selected: Grouping, onSelect: (Grouping) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        FilterChip(
            selected = selected == Grouping.CATEGORY,
            onClick = { onSelect(Grouping.CATEGORY) },
            label = { Text(stringResource(R.string.spending_by_category)) }
        )
        FilterChip(
            selected = selected == Grouping.STORE,
            onClick = { onSelect(Grouping.STORE) },
            label = { Text(stringResource(R.string.spending_by_store)) }
        )
    }
}

@Composable
private fun <K> ShareRow(share: SpendingShare<K>, name: String, color: String, leading: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.5.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Spacer(Modifier.width(10.dp))
                Text(name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatPrice(share.amount), fontWeight = FontWeight.ExtraBold)
                Text(
                    "${(share.share * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(min = 40.dp).padding(start = 6.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
            LinearProgressIndicator(
                progress = { share.share.toFloat() },
                color = color.toComposeColor(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(7.dp)
            )
        }
    }
}

@Composable
private fun EmojiWell(emoji: String, color: String?) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background((color ?: OTHER_COLOR).toComposeColor().copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) { Text(emoji, fontSize = 18.sp) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(onPick: (Long, Long) -> Unit, onDismiss: () -> Unit) {
    val picker = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val from = picker.selectedStartDateMillis
                    val to = picker.selectedEndDateMillis ?: from
                    if (from != null && to != null) onPick(utcDayToLocal(from), utcDayToLocal(to))
                },
                enabled = picker.selectedStartDateMillis != null
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    ) {
        DateRangePicker(
            state = picker,
            title = { Text(stringResource(R.string.spending_pick_dates), modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
            modifier = Modifier.weight(1f)
        )
    }
}

/** The date picker hands back UTC midnight; turn it into noon of that day locally. */
private fun utcDayToLocal(utcMillis: Long): Long {
    val utc = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return java.util.Calendar.getInstance().apply {
        clear()
        set(utc.get(java.util.Calendar.YEAR), utc.get(java.util.Calendar.MONTH), utc.get(java.util.Calendar.DAY_OF_MONTH), 12, 0)
    }.timeInMillis
}

private fun storeColor(state: SpendingUiState, storeId: Long?): String =
    storeId?.let { state.storeColors[it] } ?: OTHER_COLOR

/** "November 2026", "23 – 29 Nov" or "10 Nov – 27 Nov 2026". */
@Composable
private fun periodLabel(period: SpendingPeriod): String {
    val calendar = remember { PeriodCalendar() }
    val locale = Locale.getDefault()
    return when (period) {
        is SpendingPeriod.Month -> SimpleDateFormat("LLLL yyyy", locale)
            .format(Date(period.range(calendar).start)).replaceFirstChar { it.titlecase(locale) }
        else -> {
            val range = period.range(calendar)
            val format = SimpleDateFormat("d MMM", locale)
            val last = range.endExclusive - 1
            "${format.format(Date(range.start))} – ${SimpleDateFormat("d MMM yyyy", locale).format(Date(last))}"
        }
    }
}

@Composable
private fun rememberTextLabels(state: SpendingUiState): SpendingTextLabels {
    val title = stringResource(R.string.spending_title)
    val total = stringResource(R.string.spending_total)
    val byCategory = stringResource(R.string.spending_by_category)
    val byStore = stringResource(R.string.spending_by_store)
    val noStore = stringResource(R.string.spending_no_store)
    val other = categoryLabel(GroceryCatalog.UNCATEGORIZED)
    val context = LocalContext.current
    return SpendingTextLabels(
        title = title,
        total = total,
        byCategory = byCategory,
        byStore = byStore,
        unpriced = { n -> context.getString(R.string.spending_unpriced, n) },
        category = { name -> name?.let { CategoryLabels.labelRes(it)?.let(context::getString) ?: it } ?: other },
        store = { id -> id?.let { state.storeNames[it] } ?: noStore },
        money = { formatPrice(it) }
    )
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

/** Writes the CSV to the cache and hands it to another app; nothing is kept elsewhere. */
private fun shareCsv(context: Context, csv: String) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "listo-spending.csv").apply { writeText(csv) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

package com.listo.app.ui.screens.calendar

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.data.models.ShoppingList
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.components.ThemedMeshBackground
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.DisplayFontFamily
import com.listo.app.ui.viewmodel.CalendarViewModel
import com.listo.app.utils.DateUtils
import com.listo.app.utils.ListReminderScheduler
import com.listo.app.utils.formatPrice
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun firstOfMonth(millis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun shiftMonth(anchor: Long, months: Int): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = anchor; add(Calendar.MONTH, months) }
    return firstOfMonth(cal.timeInMillis)
}

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val now = remember { System.currentTimeMillis() }

    var anchor by remember { mutableStateOf(firstOfMonth(now)) }
    var selectedDay by remember { mutableStateOf(DateUtils.startOfDay(now)) }
    var showCreate by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val dayLists = uiState.listsOn(selectedDay)

    ThemedMeshBackground {
        Scaffold(
            topBar = { LisToTopBar(title = stringResource(R.string.calendar_title), navController = navController) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                MonthHeader(
                    anchor = anchor,
                    onPrev = { anchor = shiftMonth(anchor, -1) },
                    onNext = { anchor = shiftMonth(anchor, 1) }
                )

                MonthGrid(
                    anchor = anchor,
                    selectedDay = selectedDay,
                    today = DateUtils.startOfDay(now),
                    scheduledDays = uiState.scheduledDays,
                    onSelect = { selectedDay = it }
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = remember(selectedDay) { DateUtils.formatDate(selectedDay) },
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))

                if (dayLists.isEmpty()) {
                    Text(
                        text = stringResource(R.string.calendar_no_lists),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    dayLists.forEach { list ->
                        DayListCard(list = list) {
                            navController.navigate(Screen.ListDetail.route(list.id))
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showCreate = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.calendar_create_for_day), fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showCreate) {
        CreateForDayDialog(
            onConfirm = { name, reminderHour, reminderMinute, remind ->
                val reminderAt = if (remind) reminderMillis(selectedDay, reminderHour, reminderMinute) else null
                viewModel.createListForDay(name, selectedDay, reminderAt) { id, rem ->
                    if (rem != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        ListReminderScheduler.schedule(context, id, rem)
                    }
                }
                showCreate = false
            },
            onDismiss = { showCreate = false }
        )
    }
}

private fun reminderMillis(dayStart: Long, hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = dayStart
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

@Composable
private fun MonthHeader(anchor: Long, onPrev: () -> Unit, onNext: () -> Unit) {
    val label = remember(anchor) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(anchor)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = label,
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun MonthGrid(
    anchor: Long,
    selectedDay: Long,
    today: Long,
    scheduledDays: Set<Long>,
    onSelect: (Long) -> Unit
) {
    val monthCal = remember(anchor) { Calendar.getInstance().apply { timeInMillis = anchor } }
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = monthCal.firstDayOfWeek
    val firstDow = monthCal.get(Calendar.DAY_OF_WEEK)
    val leading = ((firstDow - firstDayOfWeek) + 7) % 7
    val totalCells = leading + daysInMonth
    val rows = (totalCells + 6) / 7

    val weekdayLabels = remember {
        val short = DateFormatSymbols(Locale.getDefault()).shortWeekdays // [1]=Sun..[7]=Sat
        (0..6).map { short[(((firstDayOfWeek - 1) + it) % 7) + 1] }
    }

    Column {
        Row(Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label.take(2),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - leading + 1
                    if (dayNumber in 1..daysInMonth) {
                        val dayStart = remember(anchor, dayNumber) {
                            val c = Calendar.getInstance().apply {
                                timeInMillis = anchor
                                set(Calendar.DAY_OF_MONTH, dayNumber)
                            }
                            DateUtils.startOfDay(c.timeInMillis)
                        }
                        DayCell(
                            day = dayNumber,
                            isSelected = dayStart == selectedDay,
                            isToday = dayStart == today,
                            hasLists = scheduledDays.contains(dayStart),
                            modifier = Modifier.weight(1f),
                            onClick = { onSelect(dayStart) }
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasLists: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .background(if (isSelected) primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$day",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> primary
                    else -> MaterialTheme.colorScheme.onBackground
                }
            )
            if (hasLists) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else primary)
                )
            }
        }
    }
}

@Composable
private fun DayListCard(list: ShoppingList, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.lists_items_count, list.checkedCount, list.itemCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (list.reminderAt != null) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                }
            }
            if (list.totalPrice > 0.0) {
                Text(formatPrice(list.totalPrice), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CreateForDayDialog(
    onConfirm: (name: String, reminderHour: Int, reminderMinute: Int, remind: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var remind by remember { mutableStateOf(false) }
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(stringResource(R.string.lists_new_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(stringResource(R.string.lists_new_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.reminder_add), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = remind, onCheckedChange = { remind = it })
                }
                if (remind) {
                    val timeLabel = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                    TextButton(onClick = {
                        TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, true).show()
                    }) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(timeLabel)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, hour, minute, remind) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_create), fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

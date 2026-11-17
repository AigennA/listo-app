package com.listo.app.ui.screens.listdetail

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.data.models.Category
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.ShoppingList
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.viewmodel.ListDetailUiState
import com.listo.app.ui.viewmodel.ListDetailViewModel
import com.listo.app.utils.AppDefaults
import com.listo.app.utils.DateUtils
import com.listo.app.utils.ItemCatalog
import com.listo.app.utils.ListReminderScheduler
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.formatPrice
import com.listo.app.utils.iconForItem
import com.listo.app.utils.rememberItemCatalog
import com.listo.app.utils.toComposeColor
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    navController: NavController,
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val list = uiState.list
    val context = LocalContext.current
    val catalog = rememberItemCatalog()

    var showRename by remember { mutableStateOf(false) }
    var showDeleteList by remember { mutableStateOf(false) }
    var showBudget by remember { mutableStateOf(false) }
    var showSchedule by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<ShoppingItem?>(null) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    Scaffold(
        topBar = {
            LisToTopBar(
                title = list?.name ?: stringResource(R.string.detail_default_title),
                navController = navController,
                actions = {
                    if (list != null) {
                        IconButton(onClick = { shareList(context, list, uiState.items) }) {
                            Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.cd_share))
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = null)
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.detail_schedule)) },
                                    leadingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
                                    onClick = { showMenu = false; showSchedule = true }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.detail_rename)) },
                                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                    onClick = { showMenu = false; showRename = true }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.budget_title)) },
                                    leadingIcon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null) },
                                    onClick = { showMenu = false; showBudget = true }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.detail_delete)) },
                                    leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
                                    onClick = { showMenu = false; showDeleteList = true }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (list != null && !list.isArchived) {
                AddItemBar(
                    categories = uiState.categories,
                    catalog = catalog,
                    onAdd = { name, qty, catId, price -> viewModel.addItem(name, qty, catId, price) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }
        if (list == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.detail_not_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SummaryHeader(uiState)
            }

            if (uiState.items.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.AddShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.detail_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.detail_empty_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                uiState.grouped.forEach { (categoryName, groupItems) ->
                    item(key = "header_$categoryName") {
                        CategoryHeader(
                            name = categoryName,
                            color = groupItems.firstOrNull()?.categoryColor?.toComposeColor()
                                ?: MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(groupItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            iconKey = catalog.keyFor(item.name) ?: item.name,
                            onToggle = { viewModel.toggleItem(item) },
                            onClick = { editItem = item },
                            onDelete = { viewModel.deleteItem(item.id) }
                        )
                    }
                }
            }

            if (!list.isArchived && uiState.items.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.archiveList { navController.navigateUp() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.detail_finish), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showRename && list != null) {
        RenameDialog(
            initial = list.name,
            onConfirm = { viewModel.renameList(it); showRename = false },
            onDismiss = { showRename = false }
        )
    }

    if (showSchedule && list != null) {
        ScheduleDialog(
            currentDate = list.scheduledDate,
            currentReminder = list.reminderAt,
            onConfirm = { scheduledDate, reminderAt ->
                viewModel.setSchedule(scheduledDate, reminderAt)
                if (reminderAt != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    ListReminderScheduler.schedule(context, list.id, reminderAt)
                } else {
                    ListReminderScheduler.cancel(context, list.id)
                }
                showSchedule = false
            },
            onDismiss = { showSchedule = false }
        )
    }

    if (showDeleteList && list != null) {
        AlertDialog(
            onDismissRequest = { showDeleteList = false },
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_msg, list.name)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteList = false
                    viewModel.deleteList { navController.navigateUp() }
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteList = false }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }

    if (showBudget && list != null) {
        BudgetDialog(
            current = list.budget,
            onConfirm = { budget -> viewModel.setBudget(budget); showBudget = false },
            onDismiss = { showBudget = false }
        )
    }

    editItem?.let { item ->
        EditItemDialog(
            item = item,
            categories = uiState.categories,
            onConfirm = { updated -> viewModel.updateItem(updated); editItem = null },
            onDismiss = { editItem = null }
        )
    }
}

@Composable
private fun SummaryHeader(state: ListDetailUiState) {
    val budget = state.list?.budget
    val overBudget = budget != null && state.total > budget
    val scheduledDate = state.list?.scheduledDate
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        if (scheduledDate != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = DateUtils.formatDate(scheduledDate),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                state.list?.reminderAt?.let { reminder ->
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = DateUtils.formatTime(reminder),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.detail_done_count, state.checkedCount, state.itemCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.total > 0.0) {
                Text(
                    text = stringResource(R.string.detail_total, formatPrice(state.total)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }

        if (budget != null) {
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.budget_title)}: ${formatPrice(budget)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (overBudget) {
                        stringResource(R.string.budget_over, formatPrice(state.total - budget))
                    } else {
                        stringResource(R.string.budget_remaining, formatPrice(budget - state.total))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        )
    }
}

@Composable
private fun CategoryHeader(name: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = categoryLabel(name),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    iconKey: String,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.cd_delete), tint = Color.White, modifier = Modifier.padding(end = 20.dp))
            }
        }
    ) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (item.isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (item.isChecked) stringResource(R.string.cd_uncheck) else stringResource(R.string.cd_check),
                        tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                // Item icon avatar (chosen from the item name)
                val avatarColor = item.categoryColor?.toComposeColor() ?: MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForItem(iconKey),
                        contentDescription = null,
                        tint = avatarColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                    )
                    if (item.quantity.isNotBlank()) {
                        Text(
                            text = item.quantity,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item.price?.let { p ->
                    Text(
                        text = formatPrice(p),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemBar(
    categories: List<Category>,
    catalog: ItemCatalog,
    onAdd: (name: String, quantity: String, categoryId: Long?, price: Double?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(0.0) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun categoryIdFor(catName: String?): Long? =
        catName?.let { n -> categories.firstOrNull { it.name == n }?.id }

    fun submit() {
        if (name.isBlank()) return
        val resolvedCategory = categoryId ?: categoryIdFor(catalog.categoryFor(name))
        onAdd(name, quantity, resolvedCategory, if (price > 0.0) price else null)
        name = ""; quantity = ""; price = 0.0; categoryId = null
        // keep focus so the user can keep adding items quickly
        focusRequester.requestFocus()
    }

    val suggestions = remember(name, catalog) { catalog.suggest(name) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Type-ahead suggestions
            AnimatedVisibility(
                visible = name.isNotBlank() && suggestions.isNotEmpty() &&
                    suggestions.none { it.name.equals(name.trim(), ignoreCase = true) }
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { s ->
                        SuggestionChip(
                            onClick = {
                                name = s.name
                                categoryId = categoryIdFor(s.category)
                            },
                            label = { Text(s.name) }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        placeholder = { Text(stringResource(R.string.qty_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    PriceStepper(value = price, onChange = { price = it })
                    if (categories.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryChoice(stringResource(R.string.category_none), null, categoryId == null) { categoryId = null }
                            categories.forEach { c ->
                                CategoryChoice(categoryLabel(c.name), c.color.toComposeColor(), categoryId == c.id) { categoryId = c.id }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Rounded.ExpandMore else Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.cd_details),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(stringResource(R.string.add_item_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { submit() },
                    enabled = name.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                }
            }
        }
    }
}

@Composable
private fun CategoryChoice(label: String, color: Color?, selected: Boolean, onClick: () -> Unit) {
    val accent = color ?: MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) accent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(1.5.dp, if (selected) accent else Color.Transparent, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RenameDialog(initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_rename), fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun PriceStepper(
    value: Double,
    onChange: (Double) -> Unit,
    step: Double = AppDefaults.PRICE_STEP
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.price_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        FilledTonalIconButton(
            onClick = { onChange((value - step).coerceAtLeast(0.0)) },
            modifier = Modifier.size(36.dp)
        ) { Icon(Icons.Rounded.Remove, contentDescription = "−", modifier = Modifier.size(18.dp)) }
        Text(
            text = if (value > 0.0) formatPrice(value) else "—",
            modifier = Modifier.widthIn(min = 64.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FilledTonalIconButton(
            onClick = { onChange(value + step) },
            modifier = Modifier.size(36.dp)
        ) { Icon(Icons.Rounded.Add, contentDescription = "+", modifier = Modifier.size(18.dp)) }
    }
}

@Composable
private fun BudgetDialog(
    current: Double?,
    onConfirm: (Double?) -> Unit,
    onDismiss: () -> Unit
) {
    val maxBudget = AppDefaults.BUDGET_MAX
    val step = AppDefaults.BUDGET_STEP
    var amount by remember { mutableStateOf(current ?: 0.0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(stringResource(R.string.budget_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (amount > 0.0) formatPrice(amount) else "—",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { amount = (amount - step).coerceAtLeast(0.0) }) {
                        Icon(Icons.Rounded.Remove, contentDescription = "−")
                    }
                    Slider(
                        value = amount.toFloat().coerceIn(0f, maxBudget.toFloat()),
                        onValueChange = { amount = it.toDouble() },
                        valueRange = 0f..maxBudget.toFloat(),
                        steps = (maxBudget / step).toInt() - 1,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    FilledTonalIconButton(onClick = { amount = (amount + step).coerceAtMost(maxBudget) }) {
                        Icon(Icons.Rounded.Add, contentDescription = "+")
                    }
                }
                if (current != null) {
                    TextButton(onClick = { onConfirm(null) }) {
                        Text(stringResource(R.string.budget_remove), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(if (amount > 0.0) amount else null) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun EditItemDialog(
    item: ShoppingItem,
    categories: List<Category>,
    onConfirm: (ShoppingItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var quantity by remember { mutableStateOf(item.quantity) }
    var price by remember { mutableStateOf(item.price ?: 0.0) }
    var categoryId by remember { mutableStateOf(item.categoryId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_item_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_label)) }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity, onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.qty_label)) }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                )
                PriceStepper(value = price, onChange = { price = it })
                if (categories.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryChoice(stringResource(R.string.category_none), null, categoryId == null) { categoryId = null }
                        categories.forEach { c ->
                            CategoryChoice(categoryLabel(c.name), c.color.toComposeColor(), categoryId == c.id) { categoryId = c.id }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        item.copy(
                            name = name.trim(),
                            quantity = quantity.trim(),
                            price = if (price > 0.0) price else null,
                            categoryId = categoryId
                        )
                    )
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun ScheduleDialog(
    currentDate: Long?,
    currentReminder: Long?,
    onConfirm: (scheduledDate: Long?, reminderAt: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(currentDate) }
    var remind by remember { mutableStateOf(currentReminder != null) }
    val initial = remember { Calendar.getInstance().apply { timeInMillis = currentReminder ?: System.currentTimeMillis() } }
    var hour by remember { mutableStateOf(if (currentReminder != null) initial.get(Calendar.HOUR_OF_DAY) else 9) }
    var minute by remember { mutableStateOf(if (currentReminder != null) initial.get(Calendar.MINUTE) else 0) }

    fun openDatePicker() {
        val c = Calendar.getInstance().apply { timeInMillis = date ?: System.currentTimeMillis() }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply {
                    set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                date = DateUtils.startOfDay(picked)
            },
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(stringResource(R.string.detail_schedule), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { openDatePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(date?.let { DateUtils.formatDate(it) } ?: stringResource(R.string.schedule_pick_date))
                }
                if (date != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.reminder_add), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = remind, onCheckedChange = { remind = it })
                    }
                    if (remind) {
                        TextButton(onClick = {
                            TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, true).show()
                        }) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
                        }
                    }
                    TextButton(onClick = { date = null; remind = false }) {
                        Text(stringResource(R.string.schedule_remove), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = date
                    val reminderAt = if (d != null && remind) {
                        Calendar.getInstance().apply {
                            timeInMillis = d
                            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    } else null
                    onConfirm(d, reminderAt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

/** Builds a plain-text version of the list and opens the Android share sheet. */
private fun shareList(context: Context, list: ShoppingList, items: List<ShoppingItem>) {
    val builder = StringBuilder()
    builder.append(list.name).append("\n\n")
    items.forEach { item ->
        val box = if (item.isChecked) "[x]" else "[ ]"
        val qty = if (item.quantity.isNotBlank()) " (${item.quantity})" else ""
        builder.append(box).append(' ').append(item.name).append(qty).append('\n')
    }
    val total = items.sumOf { it.price ?: 0.0 }
    if (total > 0.0) builder.append('\n').append(formatPrice(total))

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, list.name)
        putExtra(Intent.EXTRA_TEXT, builder.toString().trim())
    }
    context.startActivity(
        Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

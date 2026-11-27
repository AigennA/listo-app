package com.listo.app.ui.screens.lists

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront as OutlinedStorefront
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.data.models.ShoppingList
import com.listo.app.data.models.Store
import com.listo.app.ui.screens.stores.StoreBadge
import com.listo.app.ui.screens.stores.StorePicker
import com.listo.app.ui.components.AdBanner
import com.listo.app.ui.components.DeleteListDialog
import com.listo.app.ui.components.EmptyState
import com.listo.app.ui.components.RenameListDialog
import com.listo.app.ui.components.ThemedMeshBackground
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.PoppinsFontFamily
import com.listo.app.ui.viewmodel.ListsViewModel
import com.listo.app.ui.viewmodel.StoreArgs
import com.listo.app.utils.DateUtils
import com.listo.app.utils.formatPrice
import com.listo.app.utils.shareList
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ListsScreen(
    navController: NavController,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // The list whose actions sheet is open, and the one being renamed / deleted.
    var actionTarget by remember { mutableStateOf<ShoppingList?>(null) }
    var renameTarget by remember { mutableStateOf<ShoppingList?>(null) }
    var deleteTarget by remember { mutableStateOf<ShoppingList?>(null) }

    ThemedMeshBackground {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = viewModel::showCreateDialog,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.lists_new), fontWeight = FontWeight.SemiBold) }
                )
            },
            // Sits directly above the bottom navigation bar, never over it.
            bottomBar = { AdBanner() },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { ListsHeader(onStores = { navController.navigate(Screen.Stores.route(StoreArgs.NO_LIST)) }) }

                if (uiState.lists.isEmpty() && !uiState.isLoading) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.lists_empty_title),
                            subtitle = stringResource(R.string.lists_empty_subtitle),
                            icon = {
                                Icon(
                                    Icons.Rounded.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        )
                    }
                } else {
                    items(uiState.lists, key = { it.id }) { list ->
                        SwipeForActions(
                            onSwiped = { actionTarget = list },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            ShoppingListCard(
                                list = list,
                                store = uiState.storeOf(list),
                                onClick = { navController.navigate(Screen.ListDetail.route(list.id)) },
                                onMore = { actionTarget = list }
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateListDialog(
            name = uiState.newListName,
            onNameChange = viewModel::updateNewName,
            stores = uiState.stores,
            storeName = uiState.newStoreName,
            onStoreChange = viewModel::updateNewStore,
            onConfirm = {
                viewModel.createList { id ->
                    navController.navigate(Screen.ListDetail.route(id))
                }
            },
            onDismiss = viewModel::hideCreateDialog
        )
    }

    actionTarget?.let { list ->
        val copyName = stringResource(R.string.list_copy_name, list.name)
        ListActionsSheet(
            listName = list.name,
            onRename = { actionTarget = null; renameTarget = list },
            onShare = {
                actionTarget = null
                viewModel.withItems(list) { items -> shareList(context, list, items) }
            },
            onCopy = { actionTarget = null; viewModel.duplicateList(list, copyName) },
            onDelete = { actionTarget = null; deleteTarget = list },
            onDismiss = { actionTarget = null }
        )
    }

    renameTarget?.let { list ->
        RenameListDialog(
            initial = list.name,
            onConfirm = { name -> viewModel.renameList(list.id, name); renameTarget = null },
            onDismiss = { renameTarget = null }
        )
    }

    deleteTarget?.let { list ->
        DeleteListDialog(
            name = list.name,
            onConfirm = { viewModel.deleteList(list.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

/**
 * Swiping the card to the left opens its actions instead of dismissing it:
 * the card springs back and [onSwiped] fires, like tapping its ⋮ button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeForActions(
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onSwiped()
            false
        }
    )
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Rounded.MoreHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    ) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListActionsSheet(
    listName: String,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
            Text(
                text = listName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            ListActionRow(Icons.Rounded.Edit, stringResource(R.string.list_action_rename), onRename)
            ListActionRow(Icons.Rounded.Share, stringResource(R.string.list_action_share), onShare)
            ListActionRow(Icons.Rounded.ContentCopy, stringResource(R.string.list_action_copy), onCopy)
            ListActionRow(
                Icons.Rounded.DeleteOutline,
                stringResource(R.string.action_delete),
                onDelete,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ListActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    ListItem(
        headlineContent = { Text(label, color = color) },
        leadingContent = { Icon(icon, contentDescription = null, tint = color) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/** Date and title only: the other screens are in the bottom navigation bar. */
@Composable
private fun ListsHeader(onStores: () -> Unit) {
    val today = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 18.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = today,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.lists_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            // My stores, one tap from home.
            AssistChip(
                onClick = onStores,
                label = { Text(stringResource(R.string.stores_title)) },
                leadingIcon = { AnimatedStoreIcon() }
            )
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingList,
    store: Store?,
    onClick: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = list.progress,
        animationSpec = tween(600),
        label = "listProgress"
    )
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (list.isComplete) Icons.Rounded.CheckCircle else Icons.Rounded.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = list.name,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.lists_items_count, list.checkedCount, list.itemCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (store != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StoreBadge(store.color, size = 16)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = store.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (list.scheduledDate != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = DateUtils.formatShortDate(list.scheduledDate),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            list.reminderAt?.let { reminder ->
                                Icon(
                                    Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = DateUtils.formatTime(reminder),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                if (list.totalPrice > 0.0) {
                    Text(
                        text = formatPrice(list.totalPrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (list.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onMore, modifier = Modifier.offset(x = 8.dp)) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.cd_list_actions),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun CreateListDialog(
    name: String,
    onNameChange: (String) -> Unit,
    stores: List<Store>,
    storeName: String,
    onStoreChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = {
            Text(
                stringResource(R.string.lists_new_dialog_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text(stringResource(R.string.lists_new_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.height(12.dp))
                StoreField(stores = stores, value = storeName, onValueChange = onStoreChange)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_create), fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

/**
 * Optional store for a new list: pick one from My stores, or type a name that
 * is not there yet and it becomes a new store.
 */
@Composable
private fun StoreField(stores: List<Store>, value: String, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val matches = StorePicker.matches(stores, value)
    val canAdd = StorePicker.canAdd(stores, value)
    ExposedDropdownMenuBox(expanded = expanded && (matches.isNotEmpty() || canAdd), onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = true },
            label = { Text(stringResource(R.string.lists_store_label)) },
            leadingIcon = { Icon(Icons.Rounded.Storefront, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        // Not focusable, so the keyboard keeps typing into the field while suggestions show.
        DropdownMenu(
            expanded = expanded && (matches.isNotEmpty() || canAdd),
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier.exposedDropdownSize()
        ) {
            matches.forEach { store ->
                DropdownMenuItem(
                    text = { Text(store.name) },
                    leadingIcon = { StoreBadge(store.color, size = 24) },
                    onClick = { onValueChange(store.name); expanded = false }
                )
            }
            if (canAdd) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.lists_store_new, value.trim())) },
                    leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    onClick = { expanded = false }
                )
            }
        }
    }
}

/** Diagonal stripes on the My stores icon: yellow, red, blue and a soft silver (pure white vanished on the light chip). */
private val storeIconColors = listOf(Color(0xFFF5B800), Color(0xFFE5383B), Color(0xFF1E6FD9), Color(0xFFA7B0BE))
private const val STORE_ICON_SLIDE_MS = 4500

/**
 * The My stores icon striped in a few colours, the stripes slowly sliding
 * across it, so the feature catches the eye without the icon jumping about.
 */
@Composable
private fun AnimatedStoreIcon() {
    val transition = rememberInfiniteTransition(label = "storeIcon")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(STORE_ICON_SLIDE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "storeIconShift"
    )
    Box(contentAlignment = Alignment.Center) {
        StripedStoreIcon(shift)
        // A thin dark outline on top keeps the shape clear whatever colour passes under it.
        Icon(
            Icons.Outlined.OutlinedStorefront,
            contentDescription = null,
            tint = STORE_ICON_OUTLINE,
            modifier = Modifier.size(20.dp)
        )
    }
}

private val STORE_ICON_OUTLINE = Color(0xCC1F2937)

@Composable
private fun StripedStoreIcon(shift: Float) {
    Icon(
        Icons.Rounded.Storefront,
        contentDescription = null,
        modifier = Modifier
            .size(20.dp)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                // One run of the four colours spans the icon diagonally; it repeats so the stripes keep flowing.
                val run = size.width
                onDrawWithContent {
                    drawContent()
                    val offset = run * 2 * shift
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = storeIconColors + storeIconColors.first(),
                            start = Offset(offset, offset),
                            end = Offset(offset + run, offset + run),
                            tileMode = TileMode.Repeated
                        ),
                        blendMode = BlendMode.SrcIn
                    )
                }
            }
    )
}

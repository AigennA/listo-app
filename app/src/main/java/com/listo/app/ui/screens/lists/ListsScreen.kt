package com.listo.app.ui.screens.lists

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.data.models.ShoppingList
import com.listo.app.ui.components.AdBanner
import com.listo.app.ui.components.DeleteListDialog
import com.listo.app.ui.components.EmptyState
import com.listo.app.ui.components.RenameListDialog
import com.listo.app.ui.components.ThemedMeshBackground
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.PoppinsFontFamily
import com.listo.app.ui.viewmodel.ListsViewModel
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
                item { ListsHeader() }

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
private fun ListsHeader() {
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
        Text(
            text = stringResource(R.string.lists_title),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingList,
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

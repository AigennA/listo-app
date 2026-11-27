package com.listo.app.ui.screens.stores

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.data.models.Store
import com.listo.app.domain.store.StoreColors
import com.listo.app.ui.components.EmptyState
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.components.RenameListDialog
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.viewmodel.StoreArgs
import com.listo.app.ui.viewmodel.StoresViewModel
import com.listo.app.utils.toComposeColor

/**
 * My stores. Opened from a list ([listId] set) it asks which store you are in
 * and opens shopping mode; opened from Settings it manages stores and maps.
 */
@Composable
fun StoresScreen(
    navController: NavController,
    listId: Long,
    viewModel: StoresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val picking = listId != StoreArgs.NO_LIST

    var showAdd by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Store?>(null) }
    var recoloring by remember { mutableStateOf<Store?>(null) }
    var deleting by remember { mutableStateOf<Store?>(null) }

    fun openMap(id: Long) = navController.navigate(Screen.StoreMapEditor.route(id))

    Scaffold(
        topBar = { LisToTopBar(title = stringResource(R.string.stores_title), navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.stores_add)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.stores.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.stores_empty_title),
                    subtitle = stringResource(R.string.stores_empty_subtitle),
                    icon = {
                        Icon(
                            Icons.Rounded.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (picking) {
                    item {
                        Text(
                            stringResource(R.string.stores_pick_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(uiState.stores, key = { it.id }) { store ->
                    StoreCard(
                        store = store,
                        onClick = {
                            if (picking) {
                                // Remember the choice; back from the map returns to the list.
                                viewModel.linkList(listId, store.id)
                                navController.navigate(Screen.StoreShopping.route(listId, store.id)) {
                                    popUpTo(Screen.Stores.route) { inclusive = true }
                                }
                            } else {
                                openMap(store.id)
                            }
                        },
                        onEditMap = { openMap(store.id) },
                        onRename = { renaming = store },
                        onRecolor = { recoloring = store },
                        onDelete = { deleting = store }
                    )
                }
            }
        }
    }

    if (showAdd) {
        StoreNameDialog(
            onConfirm = { name ->
                showAdd = false
                viewModel.add(name) { id ->
                    if (picking) viewModel.linkList(listId, id)
                    openMap(id)
                }
            },
            onDismiss = { showAdd = false }
        )
    }
    renaming?.let { store ->
        RenameListDialog(
            initial = store.name,
            onConfirm = { viewModel.rename(store.id, it); renaming = null },
            onDismiss = { renaming = null }
        )
    }
    recoloring?.let { store ->
        StoreColorDialog(
            current = store.color,
            onPick = { viewModel.setColor(store.id, it); recoloring = null },
            onDismiss = { recoloring = null }
        )
    }
    deleting?.let { store ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.store_delete)) },
            text = { Text(stringResource(R.string.store_delete_msg, store.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(store.id); deleting = null }) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }
}

@Composable
private fun StoreCard(
    store: Store,
    onClick: () -> Unit,
    onEditMap: () -> Unit,
    onRename: () -> Unit,
    onRecolor: () -> Unit,
    onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            StoreBadge(store.color)
            Spacer(Modifier.width(14.dp))
            Text(
                store.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = null) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.store_edit_map)) },
                        leadingIcon = { Icon(Icons.Rounded.GridView, contentDescription = null) },
                        onClick = { menu = false; onEditMap() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.store_rename)) },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                        onClick = { menu = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.store_color)) },
                        leadingIcon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                        onClick = { menu = false; onRecolor() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.store_delete)) },
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
                        onClick = { menu = false; onDelete() }
                    )
                }
            }
        }
    }
}

/** The store's icon on its own color. */
@Composable
fun StoreBadge(color: String, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.toComposeColor()),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size((size * 0.55f).dp))
    }
}

@Composable
private fun StoreNameDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.stores_add), fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.stores_name_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoreColorDialog(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.store_color), fontWeight = FontWeight.Bold) },
        text = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StoreColors.palette.forEach { color ->
                    val selected = color.equals(current, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color.toComposeColor())
                            .then(
                                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { onPick(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

package com.listo.app.ui.screens.stores

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.data.models.ShoppingItem
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.components.RepeatingStepButton
import com.listo.app.ui.components.quantityText
import com.listo.app.ui.navigation.Screen
import com.listo.app.domain.store.entranceDirection
import com.listo.app.ui.viewmodel.StoreShoppingViewModel
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.formatPrice
import com.listo.app.utils.iconForCategory
import com.listo.app.utils.rememberItemCatalog
import com.listo.app.utils.toComposeColor

/** What the bottom sheet shows: one aisle, or the items not on the map. */
private sealed interface OpenAisle {
    data class Aisle(val categoryId: Long) : OpenAisle
    data object Other : OpenAisle
}

/** Shop a list on a store map: tap an aisle, tick what you take there, move on. */
@Composable
fun StoreShoppingScreen(
    navController: NavController,
    viewModel: StoreShoppingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val map = uiState.map
    val catalog = rememberItemCatalog()
    var open by remember { mutableStateOf<OpenAisle?>(null) }

    Scaffold(
        topBar = {
            LisToTopBar(
                title = uiState.store?.name.orEmpty(),
                navController = navController,
                actions = {
                    uiState.store?.let { store ->
                        IconButton(onClick = { navController.navigate(Screen.StoreMapEditor.route(store.id)) }) {
                            Icon(Icons.Rounded.GridView, contentDescription = stringResource(R.string.store_edit_map))
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            uiState.store?.let { store ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StoreBadge(store.color, size = 28)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.shop_progress, map.checkedCount, map.itemCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LinearProgressIndicator(
                progress = { if (map.itemCount == 0) 0f else map.checkedCount.toFloat() / map.itemCount },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(6.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Text(
                stringResource(R.string.shop_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            StoreMapGrid { cell ->
                map.tiles.forEach { tile ->
                    AisleTileView(
                        categoryName = tile.name,
                        color = tile.color,
                        cell = cell,
                        badge = tile.remaining,
                        done = tile.isDone,
                        quiet = !tile.hasItems,
                        modifier = Modifier
                            .offset(cell * tile.x, cell * tile.y)
                            .clickable { open = OpenAisle.Aisle(tile.categoryId) }
                    )
                }
                // Signs only help finding the way; tapping them does nothing.
                uiState.store?.let { store ->
                    LandmarkTileView(
                        Landmark.ENTRANCE,
                        cell,
                        Modifier.offset(cell * store.entrance.x, cell * store.entrance.y),
                        entranceDirection(store.entrance)
                    )
                    LandmarkTileView(
                        Landmark.CHECKOUT,
                        cell,
                        Modifier.offset(cell * store.checkout.x, cell * store.checkout.y)
                    )
                }
            }

            if (map.otherItems.isNotEmpty()) {
                val left = map.otherItems.count { !it.isChecked }
                Card(
                    onClick = { open = OpenAisle.Other },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ShoppingBasket, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.shop_other), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        if (left > 0) Text(left.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    open?.let { which ->
        val (title, color, icon, items) = when (which) {
            is OpenAisle.Aisle -> {
                val tile = map.tiles.firstOrNull { it.categoryId == which.categoryId }
                SheetContent(
                    title = tile?.let { categoryLabel(it.name) }.orEmpty(),
                    color = tile?.color,
                    icon = tile?.let { iconForCategory(it.name) },
                    items = map.itemsFor(which.categoryId)
                )
            }
            OpenAisle.Other -> SheetContent(stringResource(R.string.shop_other), null, null, map.otherItems)
        }
        AisleSheet(
            title = title,
            color = color,
            icon = icon,
            items = items,
            onToggle = viewModel::toggle,
            onStep = viewModel::step,
            onAdd = { name ->
                val aisle = (which as? OpenAisle.Aisle)?.categoryId
                viewModel.addItem(name, aisle, if (aisle == null) catalog.categoryFor(name) else null)
            },
            onDismiss = { open = null }
        )
    }
}

private data class SheetContent(
    val title: String,
    val color: String?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val items: List<ShoppingItem>
)

@Composable
private fun AisleSheet(
    title: String,
    color: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    items: List<ShoppingItem>,
    onToggle: (ShoppingItem) -> Unit,
    onStep: (ShoppingItem, Boolean) -> Unit,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newItem by remember { mutableStateOf("") }
    fun submit() {
        if (newItem.isNotBlank()) onAdd(newItem)
        newItem = ""
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = color?.toComposeColor() ?: MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                }
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) {
                Text(
                    stringResource(R.string.shop_aisle_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(item) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (item.isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                            color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        val quantity = quantityText(item)
                        if (quantity.isNotBlank()) {
                            Text(quantity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.price != null) {
                            Text(
                                formatPrice(item.lineTotal),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    RepeatingStepButton(Icons.Rounded.Remove, "−") { onStep(item, false) }
                    Spacer(Modifier.width(8.dp))
                    RepeatingStepButton(Icons.Rounded.Add, "+") { onStep(item, true) }
                }
                HorizontalDivider()
            }
            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    placeholder = { Text(stringResource(R.string.shop_add_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = { submit() }, enabled = newItem.isNotBlank()) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.shop_add_hint))
                }
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text(stringResource(R.string.shop_next_aisle)) }
        }
    }
}

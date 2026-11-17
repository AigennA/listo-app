package com.listo.app.ui.screens.categoryitems

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
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
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.viewmodel.CategoryItemsViewModel
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.iconForItem
import com.listo.app.utils.rememberItemCatalog
import com.listo.app.utils.toComposeColor

@Composable
fun CategoryItemsScreen(
    navController: NavController,
    viewModel: CategoryItemsViewModel = hiltViewModel()
) {
    val category by viewModel.category.collectAsStateWithLifecycle()
    val accent = category?.color?.toComposeColor() ?: MaterialTheme.colorScheme.primary
    val catalog = rememberItemCatalog()
    val catalogItems = remember(category?.name, catalog) {
        category?.name?.let { catalog.itemsForCategory(it) } ?: emptyList()
    }
    val title = category?.name?.let { categoryLabel(it) } ?: ""

    val selections = remember { mutableStateMapOf<String, Int>() }
    var listName by remember { mutableStateOf("") }
    LaunchedEffect(title) {
        if (listName.isBlank()) listName = title
    }
    val anyChosen = selections.values.any { it > 0 }

    Scaffold(
        topBar = { LisToTopBar(title = title, navController = navController) },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        viewModel.createList(listName.ifBlank { title.ifBlank { "List" } }, selections.toMap()) {
                            // Return to My Lists so the new (active) list is visible there.
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        }
                    },
                    enabled = anyChosen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    val total = selections.values.sum()
                    Text(
                        text = stringResource(R.string.cat_create_list) + if (total > 0) " ($total)" else "",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (catalogItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.cat_items_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    label = { Text(stringResource(R.string.name_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent)
                )
            }
            items(catalogItems, key = { it.key }) { entry ->
                val name = entry.name
                CatalogItemRow(
                    name = name,
                    iconKey = entry.key,
                    accent = accent,
                    count = selections[name] ?: 0,
                    onCountChange = { c -> if (c <= 0) selections.remove(name) else selections[name] = c }
                )
            }
        }
    }
}

@Composable
private fun CatalogItemRow(
    name: String,
    iconKey: String,
    accent: Color,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    val selected = count > 0
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForItem(iconKey), contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            QuantityStepper(count = count, accent = accent, onChange = onCountChange)
        }
    }
}

@Composable
private fun QuantityStepper(count: Int, accent: Color, onChange: (Int) -> Unit) {
    if (count == 0) {
        FilledTonalIconButton(
            onClick = { onChange(1) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = accent.copy(alpha = 0.15f),
                contentColor = accent
            )
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add")
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = { onChange(count - 1) },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = accent.copy(alpha = 0.15f),
                    contentColor = accent
                )
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
            }
            Text(
                text = "$count",
                modifier = Modifier.widthIn(min = 28.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            FilledTonalIconButton(
                onClick = { onChange(count + 1) },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = accent.copy(alpha = 0.15f),
                    contentColor = accent
                )
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
            }
        }
    }
}

package com.listo.app.ui.screens.stores

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.data.models.Category
import com.listo.app.data.models.StoreAisle
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.viewmodel.StoreMapEditorViewModel
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.toComposeColor
import kotlin.math.roundToInt

/** Lay out a store: drag aisles where they really are, add or remove them. Changes save at once. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoreMapEditorScreen(
    navController: NavController,
    viewModel: StoreMapEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LisToTopBar(
                title = state.store?.name ?: stringResource(R.string.store_edit_map),
                navController = navController
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text(stringResource(R.string.map_save)) }
            }
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
            Text(
                stringResource(R.string.map_edit_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            StoreMapGrid { cell ->
                state.placed.forEach { (category, aisle) ->
                    key(category.id) {
                        DraggableAisle(
                            category = category,
                            aisle = aisle,
                            cell = cell,
                            onDrop = { x, y -> viewModel.move(category.id, x, y) },
                            onRemove = { viewModel.remove(category.id) }
                        )
                    }
                }
            }

            Text(
                stringResource(R.string.map_add_aisle),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            if (state.available.isEmpty()) {
                Text(
                    stringResource(R.string.map_all_placed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.available.forEach { category ->
                        AssistChip(
                            onClick = { viewModel.add(category.id) },
                            label = { Text(categoryLabel(category.name)) },
                            leadingIcon = {
                                Box(
                                    Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(category.color.toComposeColor())
                                )
                            },
                            trailingIcon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** An aisle that follows the finger and snaps to the nearest cell when let go. */
@Composable
private fun DraggableAisle(
    category: Category,
    aisle: StoreAisle,
    cell: Dp,
    onDrop: (x: Int, y: Int) -> Unit,
    onRemove: () -> Unit
) {
    val cellPx = with(LocalDensity.current) { cell.toPx() }
    var drag by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    val current by rememberUpdatedState(aisle)

    Box(
        modifier = Modifier
            .offset { IntOffset((current.x * cellPx + drag.x).roundToInt(), (current.y * cellPx + drag.y).roundToInt()) }
            .zIndex(if (dragging) 1f else 0f)
            .then(if (dragging) Modifier.shadow(8.dp, RoundedCornerShape(12.dp)) else Modifier)
            .pointerInput(category.id) {
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = {
                        val x = ((current.x * cellPx + drag.x) / cellPx).roundToInt()
                        val y = ((current.y * cellPx + drag.y) / cellPx).roundToInt()
                        drag = Offset.Zero
                        dragging = false
                        onDrop(x, y)
                    },
                    onDragCancel = { drag = Offset.Zero; dragging = false },
                    onDrag = { change, amount ->
                        change.consume()
                        drag += amount
                    }
                )
            }
    ) {
        AisleTileView(categoryName = category.name, color = category.color, cell = cell)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = stringResource(R.string.cd_remove_aisle, categoryLabel(category.name)),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

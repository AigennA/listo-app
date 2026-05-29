package com.listo.app.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.data.models.ShoppingList
import com.listo.app.ui.components.EmptyState
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.viewmodel.HistoryViewModel
import com.listo.app.utils.DateUtils
import com.listo.app.utils.formatPrice

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<ShoppingList?>(null) }

    Scaffold(
        topBar = { LisToTopBar(title = "History", navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.lists.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "No past lists",
                    subtitle = "Finished shopping lists will appear here",
                    icon = {
                        Icon(
                            Icons.Rounded.History,
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
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.lists, key = { it.id }) { list ->
                    HistoryCard(
                        list = list,
                        onOpen = { navController.navigate(Screen.ListDetail.route(list.id)) },
                        onDelete = { pendingDelete = list }
                    )
                }
            }
        }
    }

    pendingDelete?.let { list ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete from history?") },
            text = { Text("\"${list.name}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteList(list.id)
                    pendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HistoryCard(
    list: ShoppingList,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val date = list.archivedAt ?: list.createdAt
                Text(
                    text = "${DateUtils.formatDate(date)}  ·  ${list.itemCount} items" +
                        if (list.totalPrice > 0.0) "  ·  ${formatPrice(list.totalPrice)}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

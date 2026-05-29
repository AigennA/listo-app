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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.data.models.ShoppingList
import com.listo.app.ui.components.AdBanner
import com.listo.app.ui.components.EmptyState
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.viewmodel.ListsViewModel
import com.listo.app.utils.formatPrice
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ListsScreen(
    navController: NavController,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        bottomBar = {
            AdBanner(modifier = Modifier.navigationBarsPadding())
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { ListsHeader(navController) }

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
                    ShoppingListCard(
                        list = list,
                        onClick = { navController.navigate(Screen.ListDetail.route(list.id)) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(12.dp))
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
}

@Composable
private fun ListsHeader(navController: NavController) {
    val today = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = today,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.lists_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HeaderIcon(Icons.Rounded.History, stringResource(R.string.cd_history)) { navController.navigate(Screen.History.route) }
            HeaderIcon(Icons.Rounded.Category, stringResource(R.string.cd_categories)) { navController.navigate(Screen.Categories.route) }
            HeaderIcon(Icons.Rounded.Settings, stringResource(R.string.cd_settings)) { navController.navigate(Screen.Settings.route) }
        }
    }
}

@Composable
private fun HeaderIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = desc,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingList,
    onClick: () -> Unit,
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (list.isComplete) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "Complete",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.lists_items_count, list.checkedCount, list.itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (list.totalPrice > 0.0) {
                    Text(
                        text = formatPrice(list.totalPrice),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (list.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
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
        title = { Text(stringResource(R.string.lists_new_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text(stringResource(R.string.lists_new_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

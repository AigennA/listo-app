package com.listo.app.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.components.EmptyState
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.components.PrimaryButton
import com.listo.app.ui.theme.*
import com.listo.app.ui.viewmodel.CategoriesViewModel
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.toComposeColor

private val categoryColors = listOf(
    "#2ECC71", "#6C5CE7", "#E17055", "#FDCB6E",
    "#00B894", "#0984E3", "#E84393", "#A29BFE"
)

@Composable
fun CategoriesScreen(
    navController: NavController,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { LisToTopBar(title = stringResource(R.string.categories_title), navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showDialog,
                containerColor = LisPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.cd_add_category))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.categories.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.categories_empty_title),
                    subtitle = stringResource(R.string.categories_empty_subtitle),
                    icon = {
                        Icon(
                            Icons.Rounded.Category,
                            contentDescription = null,
                            tint = LisPurple,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.categories, key = { it.id }) { category ->
                    val catColor = category.color.toComposeColor()
                    Card(
                        onClick = { navController.navigate(Screen.CategoryItems.route(category.id)) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = catColor.copy(alpha = 0.1f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(catColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Folder,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteCategory(category.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = categoryLabel(category.name),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = catColor
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateCategoryDialog(
            name = uiState.newCategoryName,
            selectedColor = uiState.newCategoryColor,
            onNameChange = viewModel::updateName,
            onColorSelect = viewModel::updateColor,
            onConfirm = viewModel::createCategory,
            onDismiss = viewModel::hideDialog
        )
    }
}

@Composable
private fun CreateCategoryDialog(
    name: String,
    selectedColor: String,
    onNameChange: (String) -> Unit,
    onColorSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.category_new_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.category_name_label)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LisPurple)
                )

                Text(stringResource(R.string.category_pick_color), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryColors.forEach { colorHex ->
                        val color = colorHex.toComposeColor()
                        IconButton(
                            onClick = { onColorSelect(colorHex) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (selectedColor == colorHex) 28.dp else 24.dp)
                                    .clip(CircleShape)
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == colorHex) {
                                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = LisPurple),
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.action_create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

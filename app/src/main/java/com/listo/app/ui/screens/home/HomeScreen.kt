package com.listo.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.data.models.Priority
import com.listo.app.ui.components.*
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.*
import com.listo.app.ui.viewmodel.HomeViewModel
import com.listo.app.ui.viewmodel.StatusFilter
import com.listo.app.utils.toComposeColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val openTask: (Long) -> Unit = { id ->
        navController.navigate(Screen.TaskDetail.route(id))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateTask.route()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                HomeHeader(
                    dateLabel = today,
                    navController = navController
                )
            }

            // Search + filters
            item {
                SearchBar(
                    query = uiState.query,
                    onQueryChange = viewModel::onQueryChange
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilterRow(
                    state = uiState,
                    onStatusChange = viewModel::onStatusChange,
                    onPriorityChange = viewModel::onPriorityChange,
                    onCategoryChange = viewModel::onCategoryChange
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (uiState.isFiltering) {
                searchResults(uiState, viewModel, openTask)
            } else {
                homeContent(uiState, viewModel, navController, openTask)
            }
        }
    }
}

/** Default home layout: progress, quick-nav, today's tasks, all tasks. */
private fun androidx.compose.foundation.lazy.LazyListScope.homeContent(
    uiState: com.listo.app.ui.viewmodel.HomeUiState,
    viewModel: HomeViewModel,
    navController: NavController,
    openTask: (Long) -> Unit
) {
    // Progress card
    if (uiState.todayTasks.isNotEmpty()) {
        item {
            ProgressCard(
                completedCount = uiState.completedToday,
                totalCount = uiState.totalToday,
                progress = uiState.progressToday,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Navigation chips row
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickNavChip(
                label = "Categories",
                icon = Icons.Rounded.Category,
                color = LisPurple,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Categories.route) }
            )
            QuickNavChip(
                label = "Statistics",
                icon = Icons.Rounded.BarChart,
                color = Color(0xFF00B894),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Statistics.route) }
            )
            QuickNavChip(
                label = "Premium",
                icon = Icons.Rounded.Star,
                color = Color(0xFFFDCB6E),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Premium.route) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Today's tasks header
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Tasks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (uiState.todayTasks.isNotEmpty()) {
                Text(
                    text = "${uiState.todayTasks.size} tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (uiState.todayTasks.isEmpty()) {
        item {
            EmptyState(
                title = "No tasks for today",
                subtitle = "Tap + to add your first task",
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            )
        }
    } else {
        items(
            items = uiState.todayTasks,
            key = { it.id }
        ) { task ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(tween(300)) + fadeIn(tween(300))
            ) {
                TaskItem(
                    task = task,
                    onToggleComplete = viewModel::toggleTaskCompletion,
                    onDelete = viewModel::deleteTask,
                    onClick = { openTask(task.id) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // All other tasks section
    if (uiState.allTasks.isNotEmpty()) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Tasks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = uiState.allTasks.filter { task ->
                uiState.todayTasks.none { it.id == task.id }
            },
            key = { "all_${it.id}" }
        ) { task ->
            TaskItem(
                task = task,
                onToggleComplete = viewModel::toggleTaskCompletion,
                onDelete = viewModel::deleteTask,
                onClick = { openTask(task.id) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/** Flat search/filter results list. */
private fun androidx.compose.foundation.lazy.LazyListScope.searchResults(
    uiState: com.listo.app.ui.viewmodel.HomeUiState,
    viewModel: HomeViewModel,
    openTask: (Long) -> Unit
) {
    val results = uiState.filteredTasks

    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${results.size} found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (results.isEmpty()) {
        item {
            EmptyState(
                title = "No matches",
                subtitle = "Try a different search or filter",
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                }
            )
        }
    } else {
        items(items = results, key = { "result_${it.id}" }) { task ->
            TaskItem(
                task = task,
                onToggleComplete = viewModel::toggleTaskCompletion,
                onDelete = viewModel::deleteTask,
                onClick = { openTask(task.id) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        placeholder = { Text("Search tasks…") },
        leadingIcon = {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun FilterRow(
    state: com.listo.app.ui.viewmodel.HomeUiState,
    onStatusChange: (StatusFilter) -> Unit,
    onPriorityChange: (Priority?) -> Unit,
    onCategoryChange: (Long?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status filters (single-select)
        items(StatusFilter.entries) { status ->
            FilterPill(
                label = status.label,
                selected = state.statusFilter == status,
                accent = MaterialTheme.colorScheme.primary,
                onClick = { onStatusChange(status) }
            )
        }

        // Priority filters (toggle)
        items(Priority.entries) { priority ->
            val color = when (priority) {
                Priority.LOW    -> PriorityLow
                Priority.MEDIUM -> PriorityMedium
                Priority.HIGH   -> PriorityHigh
            }
            FilterPill(
                label = priority.label,
                selected = state.priorityFilter == priority,
                accent = color,
                onClick = {
                    onPriorityChange(if (state.priorityFilter == priority) null else priority)
                }
            )
        }

        // Category filters (toggle)
        items(state.categories) { category ->
            val color = category.color.toComposeColor()
            FilterPill(
                label = category.name,
                selected = state.categoryFilter == category.id,
                accent = color,
                onClick = {
                    onCategoryChange(if (state.categoryFilter == category.id) null else category.id)
                }
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) accent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.5.dp,
                color = if (selected) accent else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HomeHeader(
    dateLabel: String,
    navController: NavController
) {
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
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "My Tasks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = { navController.navigate(Screen.Settings.route) }
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickNavChip(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

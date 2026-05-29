package com.listo.app.ui.screens.taskdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.data.models.Priority
import com.listo.app.data.models.Task
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.components.PrimaryButton
import com.listo.app.ui.components.SecondaryButton
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.*
import com.listo.app.ui.viewmodel.TaskDetailViewModel
import com.listo.app.utils.DateUtils
import com.listo.app.utils.toComposeColor

@Composable
fun TaskDetailScreen(
    navController: NavController,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val task = uiState.task
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LisToTopBar(
                title = "Task Details",
                navController = navController,
                actions = {
                    if (task != null) {
                        IconButton(onClick = {
                            navController.navigate(Screen.CreateTask.route(task.id))
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit task")
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            task == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Task not found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                TaskDetailContent(
                    task = task,
                    padding = padding,
                    onToggleComplete = viewModel::toggleCompletion,
                    onDeleteClick = { showDeleteDialog = true }
                )
            }
        }
    }

    if (showDeleteDialog && task != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = LisError) },
            title = { Text("Delete task?") },
            text = { Text("\"${task.title}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTask { navController.navigateUp() }
                }) {
                    Text("Delete", color = LisError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TaskDetailContent(
    task: Task,
    padding: PaddingValues,
    onToggleComplete: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val priorityColor = when (task.priority) {
        Priority.LOW    -> PriorityLow
        Priority.MEDIUM -> PriorityMedium
        Priority.HIGH   -> PriorityHigh
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(8.dp))

        // Hero card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(60.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    StatusBadge(isCompleted = task.isCompleted)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Description
        if (task.description.isNotBlank()) {
            SectionLabel("Description")
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
        }

        // Details
        SectionLabel("Details")
        DetailRow(
            icon = Icons.Rounded.Flag,
            label = "Priority",
            value = task.priority.label,
            valueColor = priorityColor
        )
        task.dueDate?.let { due ->
            val overdue = DateUtils.isOverdue(due) && !task.isCompleted
            DetailRow(
                icon = Icons.Rounded.CalendarMonth,
                label = "Due date",
                value = DateUtils.formatDate(due) + if (overdue) "  ·  Overdue" else "",
                valueColor = if (overdue) LisError else MaterialTheme.colorScheme.onSurface
            )
        }
        task.categoryName?.let { cat ->
            DetailRow(
                icon = Icons.Rounded.Category,
                label = "Category",
                value = cat,
                valueColor = task.categoryColor?.toComposeColor() ?: MaterialTheme.colorScheme.primary
            )
        }
        DetailRow(
            icon = Icons.Rounded.Schedule,
            label = "Created",
            value = DateUtils.formatDate(task.createdAt),
            valueColor = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(28.dp))

        PrimaryButton(
            text = if (task.isCompleted) "Mark as Active" else "Mark as Complete",
            onClick = onToggleComplete,
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                             else MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        SecondaryButton(
            text = "Delete Task",
            onClick = onDeleteClick
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatusBadge(isCompleted: Boolean) {
    val (label, color, icon) = if (isCompleted) {
        Triple("Completed", LisSuccess, Icons.Rounded.CheckCircle)
    } else {
        Triple("In Progress", MaterialTheme.colorScheme.primary, Icons.Rounded.Schedule)
    }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

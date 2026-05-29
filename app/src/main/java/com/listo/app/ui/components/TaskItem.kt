package com.listo.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.listo.app.data.models.Priority
import com.listo.app.data.models.Task
import com.listo.app.ui.theme.*
import com.listo.app.utils.DateUtils
import com.listo.app.utils.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    onToggleComplete: (Task) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(task.id)
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LisError),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        TaskItemContent(task = task, onToggleComplete = onToggleComplete, onClick = onClick)
    }
}

@Composable
private fun TaskItemContent(
    task: Task,
    onToggleComplete: (Task) -> Unit,
    onClick: (() -> Unit)? = null
) {
    val priorityColor = when (task.priority) {
        Priority.LOW    -> PriorityLow
        Priority.MEDIUM -> PriorityMedium
        Priority.HIGH   -> PriorityHigh
    }
    val priorityBgColor = when (task.priority) {
        Priority.LOW    -> PriorityLowBg
        Priority.MEDIUM -> PriorityMediumBg
        Priority.HIGH   -> PriorityHighBg
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Checkbox
            IconButton(
                onClick = { onToggleComplete(task) },
                modifier = Modifier.size(32.dp)
            ) {
                Crossfade(
                    targetState = task.isCompleted,
                    animationSpec = tween(200),
                    label = "check"
                ) { completed ->
                    Icon(
                        imageVector = if (completed) Icons.Rounded.CheckCircle
                                      else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (completed) "Mark incomplete" else "Mark complete",
                        tint = if (completed) LisGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Due date chip
                    task.dueDate?.let { date ->
                        val isOverdue = DateUtils.isOverdue(date) && !task.isCompleted
                        MiniChip(
                            text = DateUtils.formatShortDate(date),
                            color = if (isOverdue) LisError else MaterialTheme.colorScheme.onSurfaceVariant,
                            bgColor = if (isOverdue) Color(0xFFFFE5E5) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }

                    // Category chip
                    task.categoryName?.let { catName ->
                        val catColor = task.categoryColor?.toComposeColor() ?: LisGreen
                        MiniChip(
                            text = catName,
                            color = catColor,
                            bgColor = catColor.copy(alpha = 0.12f)
                        )
                    }

                    // Priority chip
                    MiniChip(
                        text = task.priority.label,
                        color = priorityColor,
                        bgColor = priorityBgColor
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniChip(text: String, color: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

package com.listo.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.listo.app.data.models.Priority
import com.listo.app.data.models.Task

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    val priority: String = Priority.LOW.name,
    val dueDate: Long? = null,
    val categoryId: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun TaskEntity.toDomain(
    categoryName: String? = null,
    categoryColor: String? = null
) = Task(
    id = id,
    title = title,
    description = description,
    priority = Priority.fromName(priority),
    dueDate = dueDate,
    categoryId = categoryId,
    categoryName = categoryName,
    categoryColor = categoryColor,
    isCompleted = isCompleted,
    createdAt = createdAt
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    priority = priority.name,
    dueDate = dueDate,
    categoryId = categoryId,
    isCompleted = isCompleted,
    createdAt = createdAt
)

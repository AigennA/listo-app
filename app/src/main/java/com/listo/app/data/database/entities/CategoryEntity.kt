package com.listo.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.listo.app.data.models.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val color: String = "#2ECC71"
)

fun CategoryEntity.toDomain(taskCount: Int = 0) = Category(
    id = id,
    name = name,
    color = color,
    taskCount = taskCount
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    color = color
)

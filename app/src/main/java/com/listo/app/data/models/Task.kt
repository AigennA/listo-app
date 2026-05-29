package com.listo.app.data.models

data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.LOW,
    val dueDate: Long? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColor: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

package com.listo.app.data.models

data class Category(
    val id: Long = 0L,
    val name: String,
    val color: String = "#2ECC71",
    val taskCount: Int = 0
)

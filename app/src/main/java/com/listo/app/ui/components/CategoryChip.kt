package com.listo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.listo.app.data.models.Category
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.toComposeColor

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val catColor = category.color.toComposeColor()

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (isSelected) catColor.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .border(
                width = 1.5.dp,
                color = if (isSelected) catColor else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(catColor)
        )
        Text(
            text = categoryLabel(category.name),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

package com.listo.app.ui.screens.stores

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listo.app.R
import com.listo.app.domain.store.StoreGrid
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.iconForCategory
import com.listo.app.utils.toComposeColor

private val TILE_GAP = 3.dp

/**
 * The store floor seen from above: a [StoreGrid] of cells, entrance at the top
 * left. [content] places tiles; it gets the side length of one cell.
 */
@Composable
fun StoreMapGrid(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(cell: Dp) -> Unit
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
            Icon(
                Icons.Rounded.DoorFront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                stringResource(R.string.map_entrance),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            val cell = maxWidth / StoreGrid.COLUMNS
            // Clear cell edges, so every spot an aisle can go is easy to see.
            val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            Box(Modifier.width(maxWidth).height(cell * StoreGrid.ROWS)) {
                Canvas(Modifier.matchParentSize()) {
                    val step = size.width / StoreGrid.COLUMNS
                    val stroke = 1.5.dp.toPx()
                    for (i in 1 until StoreGrid.COLUMNS) {
                        drawLine(lineColor, Offset(i * step, 0f), Offset(i * step, size.height), stroke)
                    }
                    for (j in 1 until StoreGrid.ROWS) {
                        drawLine(lineColor, Offset(0f, j * step), Offset(size.width, j * step), stroke)
                    }
                }
                content(cell)
            }
        }
    }
}

/**
 * One aisle: its category's color, icon and name. [badge] shows how many items
 * are left there; a [dimmed] aisle has nothing left to get.
 */
@Composable
fun AisleTileView(
    categoryName: String,
    color: String,
    cell: Dp,
    modifier: Modifier = Modifier,
    badge: Int? = null,
    dimmed: Boolean = false
) {
    Box(modifier.size(cell)) {
        Column(
            modifier = Modifier
                .padding(TILE_GAP)
                .fillMaxSize()
                .alpha(if (dimmed) 0.3f else 1f)
                .clip(RoundedCornerShape(12.dp))
                .background(color.toComposeColor()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                iconForCategory(categoryName),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(cell * 0.36f)
            )
            Text(
                text = categoryLabel(categoryName),
                color = Color.White,
                fontSize = 8.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
        if (badge != null && badge > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badge.toString(),
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.listo.app.ui.screens.stores

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listo.app.R
import com.listo.app.domain.store.Direction
import com.listo.app.domain.store.StoreGrid
import com.listo.app.utils.categoryLabel
import com.listo.app.utils.emojiForCategory
import com.listo.app.utils.toComposeColor
import java.util.Locale

/** Store floor, wall and the two signs: fixed colours so the plan reads the same in any theme. */
private object MapColors {
    val floor = Color(0xFFF3F1EC)
    val wall = Color(0xFF4B5563)
    val floorTile = Color(0x1A1F2937)
    val ink = Color(0xFF1F2937)
    val entrance = Color(0xFF1E8A4C)
    val checkout = Color(0xFF334155)
    val done = Color(0xFF1E8A4C)
}

private val CELL_GAP = 2.dp

/**
 * The store floor seen from above: an outer wall and a [StoreGrid] of floor
 * tiles, each a place an aisle, the entrance or the checkout can stand.
 * [content] places them; it gets the side length of one cell.
 */
@Composable
fun StoreMapGrid(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(cell: Dp) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MapColors.floor)
            .border(3.dp, MapColors.wall, RoundedCornerShape(14.dp))
            .padding(5.dp)
    ) {
        val cell = maxWidth / StoreGrid.COLUMNS
        Box(Modifier.width(maxWidth).height(cell * StoreGrid.ROWS)) {
            Canvas(Modifier.matchParentSize()) {
                val step = size.width / StoreGrid.COLUMNS
                val inset = 3.dp.toPx()
                val dashes = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                for (y in 0 until StoreGrid.ROWS) {
                    for (x in 0 until StoreGrid.COLUMNS) {
                        drawRoundRect(
                            color = MapColors.floorTile,
                            topLeft = Offset(x * step + inset, y * step + inset),
                            size = Size(step - 2 * inset, step - 2 * inset),
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = dashes)
                        )
                    }
                }
            }
            content(cell)
        }
    }
}

/**
 * One aisle: a white card with the category's emoji in a tinted well, its
 * name below and a stripe in the category colour. [badge] shows how many
 * items are left there; [done] marks an aisle with everything bought.
 */
@Composable
fun AisleTileView(
    categoryName: String,
    color: String,
    cell: Dp,
    modifier: Modifier = Modifier,
    badge: Int? = null,
    done: Boolean = false,
    quiet: Boolean = false
) {
    val accent = color.toComposeColor()
    Box(modifier.size(cell)) {
        Column(
            modifier = Modifier
                .padding(CELL_GAP)
                .fillMaxSize()
                .alpha(if (done || quiet) 0.38f else 1f)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(cell * 0.08f))
            Box(
                modifier = Modifier
                    .size(cell * 0.5f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emojiForCategory(categoryName), fontSize = (cell.value * 0.3f).sp)
            }
            Text(
                text = categoryLabel(categoryName),
                color = MapColors.ink,
                fontSize = 8.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
            )
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .background(accent)
            )
        }
        when {
            done -> CornerMark(MapColors.done) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
            badge != null && badge > 0 -> CornerMark(MapColors.ink) {
                Text(badge.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BoxScope.CornerMark(background: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(20.dp)
            .clip(CircleShape)
            .background(background)
            .border(2.dp, MapColors.floor, CircleShape),
        contentAlignment = Alignment.Center
    ) { content() }
}

/** What a sign on the map stands for. */
enum class Landmark { ENTRANCE, CHECKOUT }

/**
 * The entrance or checkout: a square sign, not an aisle card, so it is told
 * apart at a glance. The entrance arrow points the way in ([direction]).
 */
@Composable
fun LandmarkTileView(
    landmark: Landmark,
    cell: Dp,
    modifier: Modifier = Modifier,
    direction: Direction = Direction.RIGHT
) {
    val background = if (landmark == Landmark.ENTRANCE) MapColors.entrance else MapColors.checkout
    val label = stringResource(if (landmark == Landmark.ENTRANCE) R.string.map_entrance else R.string.map_checkout)
    Box(modifier.size(cell)) {
        Column(
            modifier = Modifier
                .padding(CELL_GAP)
                .fillMaxSize()
                .shadow(3.dp, RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp))
                .background(background)
                .padding(3.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(4.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (landmark == Landmark.ENTRANCE) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(cell * 0.4f).rotate(direction.degrees)
                )
            } else {
                Icon(Icons.Rounded.PointOfSale, contentDescription = null, tint = Color.White, modifier = Modifier.size(cell * 0.38f))
            }
            Text(
                label.uppercase(Locale.getDefault()),
                color = Color.White,
                fontSize = 8.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

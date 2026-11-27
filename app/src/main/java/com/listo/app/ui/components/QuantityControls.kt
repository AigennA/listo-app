package com.listo.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.listo.app.R
import com.listo.app.data.models.QuantityUnit
import com.listo.app.data.models.ShoppingItem
import com.listo.app.utils.PriceInput
import com.listo.app.utils.QuantityStepper
import com.listo.app.utils.RepeatSchedule
import com.listo.app.utils.formatAmount
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** String resource of a unit's short label, e.g. "pcs" / "st". */
fun unitLabelRes(unit: QuantityUnit): Int =
    when (unit) {
        QuantityUnit.PIECE -> R.string.unit_piece
        QuantityUnit.PACK -> R.string.unit_pack
        QuantityUnit.KILO -> R.string.unit_kilo
        QuantityUnit.GRAM -> R.string.unit_gram
        QuantityUnit.LITRE -> R.string.unit_litre
        QuantityUnit.MILLILITRE -> R.string.unit_millilitre
    }

/** Short label of a unit in the current language. */
@Composable
fun unitLabel(unit: QuantityUnit): String = stringResource(unitLabelRes(unit))

/** What a price is for: grams are priced per kilo and millilitres per litre. */
fun priceBasis(unit: QuantityUnit?): QuantityUnit = when (unit) {
    QuantityUnit.GRAM -> QuantityUnit.KILO
    QuantityUnit.MILLILITRE -> QuantityUnit.LITRE
    null -> QuantityUnit.PIECE
    else -> unit
}

/** "2 pcs", "500 g" — or the old free-text quantity of items made before units. */
@Composable
fun quantityText(item: ShoppingItem): String {
    val unit = item.unit
    val amount = item.amount
    return if (amount != null && unit != null) "${formatAmount(amount)} ${unitLabel(unit)}" else item.quantity
}

/** Plain version of [quantityText] for share text, with labels resolved by [label]. */
fun quantityText(item: ShoppingItem, label: (QuantityUnit) -> String): String {
    val unit = item.unit
    val amount = item.amount
    return if (amount != null && unit != null) "${formatAmount(amount)} ${label(unit)}" else item.quantity
}

/**
 * A round −/+ button that steps once on tap and keeps stepping, faster and
 * faster, while held down.
 */
@Composable
fun RepeatingStepButton(icon: ImageVector, contentDescription: String, onStep: () -> Unit) {
    val step by rememberUpdatedState(onStep)
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .pointerInput(Unit) {
                coroutineScope {
                    awaitEachGesture {
                        awaitFirstDown()
                        step()
                        val repeating = launch {
                            var index = 0
                            while (true) {
                                delay(RepeatSchedule.delayMillis(index++))
                                step()
                            }
                        }
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                        repeating.cancel()
                    }
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(18.dp))
        }
    }
}

/** A number box between − and +; the number can also be typed. */
@Composable
private fun StepperRow(
    text: String,
    onTextChange: (String) -> Unit,
    onDown: () -> Unit,
    onUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        RepeatingStepButton(Icons.Rounded.Remove, "−", onDown)
        OutlinedTextField(
            value = text,
            onValueChange = { onTextChange(PriceInput.clean(it)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .width(92.dp)
        )
        RepeatingStepButton(Icons.Rounded.Add, "+", onUp)
    }
}

/**
 * Amount and unit of an item: a typed or stepped number, plus a choice of
 * unit. Changing the unit starts from that unit's usual amount.
 */
@Composable
fun AmountEditor(
    amount: Double?,
    unit: QuantityUnit,
    onChange: (amount: Double?, unit: QuantityUnit) -> Unit
) {
    var text by remember { mutableStateOf(amount?.let { formatAmount(it) } ?: "") }
    // Follow −/+ and unit changes, but leave half-typed input like "1," alone.
    LaunchedEffect(amount, unit) {
        if (PriceInput.parse(text) != amount) text = amount?.let { formatAmount(it) } ?: ""
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.qty_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            StepperRow(
                text = text,
                onTextChange = { typed -> text = typed; onChange(PriceInput.parse(typed), unit) },
                onDown = { onChange(QuantityStepper.down(amount, unit), unit) },
                onUp = { onChange(QuantityStepper.up(amount, unit), unit) }
            )
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuantityUnit.entries.forEach { u ->
                FilterChip(
                    selected = u == unit,
                    onClick = { if (u != unit) onChange(u.defaultAmount, u) },
                    label = { Text(unitLabel(u)) }
                )
            }
        }
    }
}

/** Price per piece, pack, kilo or litre, typed or stepped; the label says which. */
@Composable
fun PriceEditor(
    price: Double?,
    unit: QuantityUnit?,
    step: Double,
    onChange: (Double?) -> Unit
) {
    var text by remember { mutableStateOf(price?.let { formatAmount(it) } ?: "") }
    LaunchedEffect(price) {
        if (PriceInput.parse(text) != price) text = price?.let { formatAmount(it) } ?: ""
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.price_per_unit, unitLabel(priceBasis(unit))),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        StepperRow(
            text = text,
            onTextChange = { typed -> text = typed; onChange(PriceInput.parse(typed)) },
            onDown = { onChange(((price ?: 0.0) - step).coerceAtLeast(0.0).takeIf { it > 0.0 }) },
            onUp = { onChange((price ?: 0.0) + step) }
        )
    }
}

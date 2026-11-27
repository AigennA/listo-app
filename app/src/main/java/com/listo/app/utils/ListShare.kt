package com.listo.app.utils

import android.content.Context
import android.content.Intent
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.ShoppingList
import com.listo.app.ui.components.quantityText
import com.listo.app.ui.components.unitLabelRes

/** Plain-text version of a list for sharing: name, one "[x]/[ ]" line per item, then the total. */
fun buildShareText(
    list: ShoppingList,
    items: List<ShoppingItem>,
    priceFormatter: (Double) -> String = { formatPrice(it) },
    quantityLabel: (ShoppingItem) -> String = { it.quantity }
): String {
    val builder = StringBuilder()
    builder.append(list.name).append("\n\n")
    items.forEach { item ->
        val box = if (item.isChecked) "[x]" else "[ ]"
        val label = quantityLabel(item)
        val qty = if (label.isNotBlank()) " ($label)" else ""
        builder.append(box).append(' ').append(item.name).append(qty).append('\n')
    }
    val total = items.sumOf { it.lineTotal }
    if (total > 0.0) builder.append('\n').append(priceFormatter(total))
    return builder.toString().trim()
}

/** Opens the system share sheet with [list] as text. */
fun shareList(context: Context, list: ShoppingList, items: List<ShoppingItem>) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, list.name)
        val label: (ShoppingItem) -> String = { item -> quantityText(item) { context.getString(unitLabelRes(it)) } }
        putExtra(Intent.EXTRA_TEXT, buildShareText(list, items, quantityLabel = label))
    }
    context.startActivity(
        Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

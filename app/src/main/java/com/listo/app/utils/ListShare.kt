package com.listo.app.utils

import android.content.Context
import android.content.Intent
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.ShoppingList

/** Plain-text version of a list for sharing: name, one "[x]/[ ]" line per item, then the total. */
fun buildShareText(
    list: ShoppingList,
    items: List<ShoppingItem>,
    priceFormatter: (Double) -> String = { formatPrice(it) }
): String {
    val builder = StringBuilder()
    builder.append(list.name).append("\n\n")
    items.forEach { item ->
        val box = if (item.isChecked) "[x]" else "[ ]"
        val qty = if (item.quantity.isNotBlank()) " (${item.quantity})" else ""
        builder.append(box).append(' ').append(item.name).append(qty).append('\n')
    }
    val total = items.sumOf { it.price ?: 0.0 }
    if (total > 0.0) builder.append('\n').append(priceFormatter(total))
    return builder.toString().trim()
}

/** Opens the system share sheet with [list] as text. */
fun shareList(context: Context, list: ShoppingList, items: List<ShoppingItem>) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, list.name)
        putExtra(Intent.EXTRA_TEXT, buildShareText(list, items))
    }
    context.startActivity(
        Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

package com.listo.app.data.models

/**
 * How an item's amount is measured. [code] is what is stored. Prices are per
 * piece, pack, kilo or litre, as on shelf labels; for grams and millilitres
 * the price is per kilo or litre, so [priceFactor] scales the amount to that.
 */
enum class QuantityUnit(
    val code: String,
    val defaultAmount: Double,
    val step: Double,
    val priceFactor: Double
) {
    PIECE("pcs", defaultAmount = 1.0, step = 1.0, priceFactor = 1.0),
    PACK("pack", defaultAmount = 1.0, step = 1.0, priceFactor = 1.0),
    KILO("kg", defaultAmount = 1.0, step = 0.5, priceFactor = 1.0),
    GRAM("g", defaultAmount = 500.0, step = 100.0, priceFactor = 0.001),
    LITRE("l", defaultAmount = 1.0, step = 0.5, priceFactor = 1.0),
    MILLILITRE("ml", defaultAmount = 500.0, step = 100.0, priceFactor = 0.001);

    /** The smallest amount the stepper goes down to. */
    val min: Double get() = step

    companion object {
        fun fromCode(code: String?): QuantityUnit? = entries.firstOrNull { it.code == code }
    }
}

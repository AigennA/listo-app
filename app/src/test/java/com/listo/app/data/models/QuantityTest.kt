package com.listo.app.data.models

import com.listo.app.utils.PriceInput
import com.listo.app.utils.QuantityStepper
import com.listo.app.utils.RepeatSchedule
import com.listo.app.utils.formatAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class QuantityUnitTest {

    @Test
    fun `units are stored by a stable code`() {
        QuantityUnit.entries.forEach { assertEquals(it, QuantityUnit.fromCode(it.code)) }
        assertNull(QuantityUnit.fromCode("bushel"))
        assertNull(QuantityUnit.fromCode(null))
    }

    @Test
    fun `every unit has a sensible starting amount`() {
        assertEquals(1.0, QuantityUnit.PIECE.defaultAmount, 0.0)
        assertEquals(500.0, QuantityUnit.GRAM.defaultAmount, 0.0)
        assertTrue(QuantityUnit.entries.all { it.defaultAmount > 0 })
    }
}

class LineTotalTest {

    private fun item(price: Double?, amount: Double?, unit: QuantityUnit?) =
        ShoppingItem(listId = 1, name = "x", price = price, amount = amount, unit = unit)

    @Test
    fun `no price means nothing to add up`() {
        assertEquals(0.0, item(null, 3.0, QuantityUnit.PIECE).lineTotal, 0.0)
    }

    @Test
    fun `price is per piece, so more pieces cost more`() {
        assertEquals(45.0, item(15.0, 3.0, QuantityUnit.PIECE).lineTotal, 0.001)
        assertEquals(50.0, item(25.0, 2.0, QuantityUnit.PACK).lineTotal, 0.001)
    }

    @Test
    fun `price is per kilo or litre`() {
        assertEquals(60.0, item(40.0, 1.5, QuantityUnit.KILO).lineTotal, 0.001)
        assertEquals(30.0, item(20.0, 1.5, QuantityUnit.LITRE).lineTotal, 0.001)
    }

    @Test
    fun `grams and millilitres use the price per kilo or litre`() {
        assertEquals(20.0, item(40.0, 500.0, QuantityUnit.GRAM).lineTotal, 0.001)
        assertEquals(5.0, item(20.0, 250.0, QuantityUnit.MILLILITRE).lineTotal, 0.001)
    }

    @Test
    fun `an item without an amount counts once`() {
        assertEquals(15.0, item(15.0, null, null).lineTotal, 0.001)
    }
}

class QuantityStepperTest {

    @Test
    fun `pieces go up and down by one, never below one`() {
        assertEquals(3.0, QuantityStepper.up(2.0, QuantityUnit.PIECE), 0.0)
        assertEquals(1.0, QuantityStepper.down(2.0, QuantityUnit.PIECE), 0.0)
        assertEquals(1.0, QuantityStepper.down(1.0, QuantityUnit.PIECE), 0.0)
    }

    @Test
    fun `an item with no amount yet counts as one`() {
        assertEquals(2.0, QuantityStepper.up(null, QuantityUnit.PIECE), 0.0)
    }

    @Test
    fun `kilos and litres step by half`() {
        assertEquals(1.5, QuantityStepper.up(1.0, QuantityUnit.KILO), 0.0)
        assertEquals(0.5, QuantityStepper.down(0.5, QuantityUnit.LITRE), 0.0)
    }

    @Test
    fun `grams and millilitres step by a hundred`() {
        assertEquals(600.0, QuantityStepper.up(500.0, QuantityUnit.GRAM), 0.0)
        assertEquals(100.0, QuantityStepper.down(100.0, QuantityUnit.MILLILITRE), 0.0)
    }
}

class ItemStepTest {

    @Test
    fun `stepping an item without an amount starts counting pieces`() {
        val item = ShoppingItem(listId = 1, name = "Milk")
        val up = item.stepped(up = true)
        assertEquals(2.0, up.amount!!, 0.0)
        assertEquals(QuantityUnit.PIECE, up.unit)
    }

    @Test
    fun `stepping keeps the item's unit`() {
        val item = ShoppingItem(listId = 1, name = "Rice", amount = 1.0, unit = QuantityUnit.KILO)
        assertEquals(1.5, item.stepped(up = true).amount!!, 0.0)
        assertEquals(QuantityUnit.KILO, item.stepped(up = false).unit)
    }

    @Test
    fun `an amount replaces the old free-text quantity`() {
        val item = ShoppingItem(listId = 1, name = "Eggs", quantity = "a dozen")
        assertEquals("", item.stepped(up = true).quantity)
    }
}

class PriceInputTest {

    @Test
    fun `accepts a comma or a dot as decimal sign`() {
        assertEquals(12.5, PriceInput.parse("12,50")!!, 0.0)
        assertEquals(12.5, PriceInput.parse(" 12.5 ")!!, 0.0)
        assertEquals(7.0, PriceInput.parse("7")!!, 0.0)
    }

    @Test
    fun `rejects empty, negative or non-numeric input`() {
        assertNull(PriceInput.parse(""))
        assertNull(PriceInput.parse("abc"))
        assertNull(PriceInput.parse("-3"))
    }

    @Test
    fun `typing keeps only digits and one decimal sign`() {
        assertEquals("12,5", PriceInput.clean("12,5"))
        assertEquals("125", PriceInput.clean("1a2b5"))
        assertEquals("1.25", PriceInput.clean("1.2.5"))
    }
}

class AmountFormatTest {

    @Test
    fun `whole amounts have no decimals and halves use the locale's sign`() {
        assertEquals("2", formatAmount(2.0, Locale.US))
        assertEquals("1.5", formatAmount(1.5, Locale.US))
        assertEquals("1,5", formatAmount(1.5, Locale.forLanguageTag("tr")))
    }
}

class RepeatScheduleTest {

    @Test
    fun `holding a button waits, then repeats faster and faster`() {
        val first = RepeatSchedule.delayMillis(0)
        val early = RepeatSchedule.delayMillis(3)
        val late = RepeatSchedule.delayMillis(20)
        assertTrue(first > early)
        assertTrue(early > late)
        assertTrue(late > 0)
    }
}

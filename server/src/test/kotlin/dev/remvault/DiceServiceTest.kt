package dev.remvault

import dev.remvault.server.services.DiceService
import kotlin.test.*

class DiceServiceTest {

    // ── roll() ─────────────────────────────────────────────────────────────

    @Test
    fun `roll is within valid range`() {
        repeat(100) {
            val result = DiceService.roll(6)
            assertTrue(result in 1..6, "Expected 1..6 but got $result")
        }
    }

    @Test
    fun `roll d20 is within valid range`() {
        repeat(100) {
            val result = DiceService.roll(20)
            assertTrue(result in 1..20, "Expected 1..20 but got $result")
        }
    }

    // ── rollMultiple() ─────────────────────────────────────────────────────

    @Test
    fun `rollMultiple returns correct count`() {
        val results = DiceService.rollMultiple(4, 6)
        assertEquals(4, results.size)
    }

    @Test
    fun `rollMultiple all values in range`() {
        val results = DiceService.rollMultiple(10, 8)
        results.forEach { assertTrue(it in 1..8, "Expected 1..8 but got $it") }
    }

    // ── rollStat() ─────────────────────────────────────────────────────────

    @Test
    fun `rollStat is between 3 and 18`() {
        repeat(200) {
            val result = DiceService.rollStat()
            assertTrue(result in 3..18, "Expected 3..18 but got $result")
        }
    }

    // ── rollStatBlock() ────────────────────────────────────────────────────

    @Test
    fun `rollStatBlock all scores in valid range`() {
        val block = DiceService.rollStatBlock()
        listOf(
            block.strength, block.dexterity, block.constitution,
            block.intelligence, block.wisdom, block.charisma
        )
            .forEach { assertTrue(it in 3..18, "Expected 3..18 but got $it") }
    }

    // ── rollExpression() ───────────────────────────────────────────────────

    @Test
    fun `1d6 result is in range`() {
        repeat(100) {
            val result = DiceService.rollExpression("1d6")
            assertTrue(result.total in 1..6)
        }
    }

    @Test
    fun `2d6 result is in range`() {
        repeat(100) {
            val result = DiceService.rollExpression("2d6")
            assertTrue(result.total in 2..12)
        }
    }

    @Test
    fun `2d6+3 result is in range`() {
        repeat(100) {
            val result = DiceService.rollExpression("2d6+3")
            assertTrue(result.total in 5..15)
            assertEquals(3, result.modifier)
        }
    }

    @Test
    fun `1d20-2 result is in range`() {
        repeat(100) {
            val result = DiceService.rollExpression("1d20-2")
            assertTrue(result.total in -1..18)
            assertEquals(-2, result.modifier)
        }
    }

    @Test
    fun `expression rolls count matches dice count`() {
        val result = DiceService.rollExpression("3d8")
        assertEquals(3, result.rolls.size)
    }

    @Test
    fun `expression stores original expression string`() {
        val result = DiceService.rollExpression("2d6+3")
        assertEquals("2d6+3", result.expression)
    }

    @Test
    fun `invalid expression throws`() {
        assertFailsWith<IllegalArgumentException> { DiceService.rollExpression("invalid") }
        assertFailsWith<IllegalArgumentException> { DiceService.rollExpression("abc") }
        assertFailsWith<IllegalArgumentException> { DiceService.rollExpression("d20") }
    }

    @Test
    fun `too many dice throws`() {
        assertFailsWith<IllegalArgumentException> { DiceService.rollExpression("101d6") }
    }

    @Test
    fun `too many sides throws`() {
        assertFailsWith<IllegalArgumentException> { DiceService.rollExpression("1d101") }
    }

    @Test
    fun `zero dice throws`() {
        assertFailsWith<IllegalArgumentException> { DiceService.rollExpression("0d6") }
    }
}

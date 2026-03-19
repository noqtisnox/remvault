package dev.remvault.shared.rules

import dev.remvault.shared.enums.Proficiency
import dev.remvault.shared.models.Stats
import kotlin.test.*

class RulesEngineTest {

    // ── modifier() ─────────────────────────────────────────────────────────

    @Test
    fun `modifier of 10 is 0`() = assertEquals(0, RulesEngine.modifier(10))
    @Test
    fun `modifier of 11 is 0`() = assertEquals(0, RulesEngine.modifier(11))
    @Test
    fun `modifier of 12 is 1`() = assertEquals(1, RulesEngine.modifier(12))
    @Test
    fun `modifier of 8 is -1`() = assertEquals(-1, RulesEngine.modifier(8))
    @Test
    fun `modifier of 20 is 5`() = assertEquals(5, RulesEngine.modifier(20))
    @Test
    fun `modifier of 1 is -5`() = assertEquals(-5, RulesEngine.modifier(1))

    // ── proficiencyBonus() ─────────────────────────────────────────────────

    @Test
    fun `proficiency bonus at level 1 is 2`() = assertEquals(2, RulesEngine.proficiencyBonus(1))
    @Test
    fun `proficiency bonus at level 4 is 2`() = assertEquals(2, RulesEngine.proficiencyBonus(4))
    @Test
    fun `proficiency bonus at level 5 is 3`() = assertEquals(3, RulesEngine.proficiencyBonus(5))
    @Test
    fun `proficiency bonus at level 9 is 4`() = assertEquals(4, RulesEngine.proficiencyBonus(9))
    @Test
    fun `proficiency bonus at level 17 is 6`() = assertEquals(6, RulesEngine.proficiencyBonus(17))
    @Test
    fun `proficiency bonus at level 20 is 6`() = assertEquals(6, RulesEngine.proficiencyBonus(20))

    @Test
    fun `proficiency bonus below level 1 throws`() {
        assertFailsWith<IllegalArgumentException> { RulesEngine.proficiencyBonus(0) }
    }

    // ── skillBonus() ───────────────────────────────────────────────────────

    @Test
    fun `skill bonus with no proficiency is just modifier`() {
        assertEquals(1, RulesEngine.skillBonus(12, 1, Proficiency.NONE))
    }

    @Test
    fun `skill bonus with proficiency adds proficiency bonus`() {
        // modifier(12) = 1, proficiencyBonus(1) = 2 → 3
        assertEquals(3, RulesEngine.skillBonus(12, 1, Proficiency.PROFICIENT))
    }

    @Test
    fun `skill bonus with expertise doubles proficiency bonus`() {
        // modifier(12) = 1, proficiencyBonus(1) * 2 = 4 → 5
        assertEquals(5, RulesEngine.skillBonus(12, 1, Proficiency.EXPERT))
    }

    // ── savingThrowBonus() ─────────────────────────────────────────────────

    @Test
    fun `saving throw without proficiency is just modifier`() {
        assertEquals(-1, RulesEngine.savingThrowBonus(8, 1, false))
    }

    @Test
    fun `saving throw with proficiency adds proficiency bonus`() {
        // modifier(8) = -1, proficiencyBonus(1) = 2 → 1
        assertEquals(1, RulesEngine.savingThrowBonus(8, 1, true))
    }

    // ── maxHitPoints() ─────────────────────────────────────────────────────

    @Test
    fun `wizard level 1 with 10 con has 6 hp`() {
        // hitDie=6, modifier(10)=0 → 6
        assertEquals(6, RulesEngine.maxHitPoints(1, 6, 10))
    }

    @Test
    fun `barbarian level 1 with 16 con has 15 hp`() {
        // hitDie=12, modifier(16)=3 → 15
        assertEquals(15, RulesEngine.maxHitPoints(1, 12, 16))
    }

    @Test
    fun `fighter level 2 with 10 con has correct hp`() {
        // level 1: 10+0=10, level 2: +(10/2+1)+0=6 → 16
        assertEquals(16, RulesEngine.maxHitPoints(2, 10, 10))
    }

    @Test
    fun `max hp below level 1 throws`() {
        assertFailsWith<IllegalArgumentException> { RulesEngine.maxHitPoints(0, 8, 10) }
    }

    // ── spellSaveDC() ──────────────────────────────────────────────────────

    @Test
    fun `spell save DC for level 1 wizard with 16 int is 13`() {
        // 8 + profBonus(1)=2 + modifier(16)=3 → 13
        assertEquals(13, RulesEngine.spellSaveDC(16, 1))
    }

    // ── carryingCapacity() ─────────────────────────────────────────────────

    @Test
    fun `carrying capacity is strength times 15`() {
        assertEquals(150, RulesEngine.carryingCapacity(10))
        assertEquals(225, RulesEngine.carryingCapacity(15))
    }

    // ── attunement ─────────────────────────────────────────────────────────

    @Test
    fun `can attune when under limit`() = assertTrue(RulesEngine.canAttune(2))
    @Test
    fun `cannot attune when at limit`() = assertFalse(RulesEngine.canAttune(3))
    @Test
    fun `cannot attune when over limit`() = assertFalse(RulesEngine.canAttune(4))

    // ── hitDie() ───────────────────────────────────────────────────────────

    @Test
    fun `barbarian has d12`() = assertEquals(12, RulesEngine.hitDie("barbarian"))
    @Test
    fun `wizard has d6`() = assertEquals(6, RulesEngine.hitDie("wizard"))
    @Test
    fun `fighter has d10`() = assertEquals(10, RulesEngine.hitDie("fighter"))
    @Test
    fun `unknown class has d8`() = assertEquals(8, RulesEngine.hitDie("homebrew"))

    // ── levelFromXp() ──────────────────────────────────────────────────────

    @Test
    fun `0 xp is level 1`() = assertEquals(1, RulesEngine.levelFromXp(0))
    @Test
    fun `300 xp is level 2`() = assertEquals(2, RulesEngine.levelFromXp(300))
    @Test
    fun `355000 xp is level 20`() = assertEquals(20, RulesEngine.levelFromXp(355000))

    // ── passivePerception() ────────────────────────────────────────────────

    @Test
    fun `passive perception without proficiency is 10 plus wis modifier`() {
        // 10 + modifier(14)=2 → 12
        assertEquals(12, RulesEngine.passivePerception(14, 1, false))
    }

    @Test
    fun `passive perception with proficiency adds proficiency bonus`() {
        // 10 + modifier(14)=2 + profBonus(1)=2 → 14
        assertEquals(14, RulesEngine.passivePerception(14, 1, true))
    }
}

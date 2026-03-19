package dev.remvault.shared.rules

import dev.remvault.shared.models.Stats
import dev.remvault.shared.enums.Proficiency

object RulesEngine {

    // ── Ability Modifiers ──────────────────────────────────────────────────

    fun modifier(score: Int): Int = Math.floorDiv(score - 10, 2)

    fun modifiers(stats: Stats) = object {
        val strength     = modifier(stats.strength)
        val dexterity    = modifier(stats.dexterity)
        val constitution = modifier(stats.constitution)
        val intelligence = modifier(stats.intelligence)
        val wisdom       = modifier(stats.wisdom)
        val charisma     = modifier(stats.charisma)
    }

    // ── Proficiency Bonus ──────────────────────────────────────────────────

    fun proficiencyBonus(level: Int): Int = when {
        level < 1  -> throw IllegalArgumentException("Level must be at least 1")
        level <= 4  -> 2
        level <= 8  -> 3
        level <= 12 -> 4
        level <= 16 -> 5
        else        -> 6
    }

    // ── Skills ─────────────────────────────────────────────────────────────

    fun skillBonus(
        statScore: Int,
        level: Int,
        proficiency: Proficiency
    ): Int {
        val base  = modifier(statScore)
        val prof  = proficiencyBonus(level)
        return base + when (proficiency) {
            Proficiency.NONE       -> 0
            Proficiency.PROFICIENT -> prof
            Proficiency.EXPERT     -> prof * 2
        }
    }

    // ── Saving Throws ──────────────────────────────────────────────────────

    fun savingThrowBonus(
        statScore: Int,
        level: Int,
        proficient: Boolean
    ): Int {
        val base = modifier(statScore)
        return if (proficient) base + proficiencyBonus(level) else base
    }

    // ── Hit Points ─────────────────────────────────────────────────────────

    // Returns max HP at level 1 (max hit die + CON modifier)
    fun baseHitPoints(hitDie: Int, constitutionScore: Int): Int =
        hitDie + modifier(constitutionScore)

    // Average HP gain per level after 1 (average hit die roll + CON modifier)
    fun hpPerLevel(hitDie: Int, constitutionScore: Int): Int =
        (hitDie / 2 + 1) + modifier(constitutionScore)

    fun maxHitPoints(level: Int, hitDie: Int, constitutionScore: Int): Int {
        if (level < 1) throw IllegalArgumentException("Level must be at least 1")
        val base = baseHitPoints(hitDie, constitutionScore)
        if (level == 1) return base
        return base + (hpPerLevel(hitDie, constitutionScore) * (level - 1))
    }

    // ── Spellcasting ───────────────────────────────────────────────────────

    fun spellSaveDC(spellcastingStatScore: Int, level: Int): Int =
        8 + proficiencyBonus(level) + modifier(spellcastingStatScore)

    fun spellAttackBonus(spellcastingStatScore: Int, level: Int): Int =
        proficiencyBonus(level) + modifier(spellcastingStatScore)

    // ── Carrying Capacity ──────────────────────────────────────────────────

    fun carryingCapacity(strengthScore: Int): Int = strengthScore * 15

    // ── Attunement ─────────────────────────────────────────────────────────

    const val MAX_ATTUNED_ITEMS = 3

    fun canAttune(currentAttunedCount: Int): Boolean =
        currentAttunedCount < MAX_ATTUNED_ITEMS

    // ── Hit Dice by Class ──────────────────────────────────────────────────

    fun hitDie(characterClass: String): Int = when (characterClass.lowercase()) {
        "barbarian"                         -> 12
        "fighter", "paladin", "ranger"      -> 10
        "bard", "cleric", "druid",
        "monk", "rogue", "warlock"          -> 8
        "sorcerer", "wizard"                -> 6
        else                                -> 8  // sensible default for homebrew
    }

    // ── Passive Perception ─────────────────────────────────────────────────

    fun passivePerception(wisdomScore: Int, level: Int, proficient: Boolean): Int =
        10 + savingThrowBonus(wisdomScore, level, proficient)

    // ── Experience & Level ─────────────────────────────────────────────────

    fun levelFromXp(xp: Int): Int = when {
        xp < 300    -> 1
        xp < 900    -> 2
        xp < 2700   -> 3
        xp < 6500   -> 4
        xp < 14000  -> 5
        xp < 23000  -> 6
        xp < 34000  -> 7
        xp < 48000  -> 8
        xp < 64000  -> 9
        xp < 85000  -> 10
        xp < 100000 -> 11
        xp < 120000 -> 12
        xp < 140000 -> 13
        xp < 165000 -> 14
        xp < 195000 -> 15
        xp < 225000 -> 16
        xp < 265000 -> 17
        xp < 305000 -> 18
        xp < 355000 -> 19
        else        -> 20
    }

    fun xpToNextLevel(currentLevel: Int): Int = when (currentLevel) {
        1  -> 300
        2  -> 900
        3  -> 2700
        4  -> 6500
        5  -> 14000
        6  -> 23000
        7  -> 34000
        8  -> 48000
        9  -> 64000
        10 -> 85000
        11 -> 100000
        12 -> 120000
        13 -> 140000
        14 -> 165000
        15 -> 195000
        16 -> 225000
        17 -> 265000
        18 -> 305000
        19 -> 355000
        else -> 0  // level 20, max
    }
}
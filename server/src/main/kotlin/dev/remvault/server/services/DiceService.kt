package dev.remvault.server.services

object DiceService {

    // ── Core Rolling ───────────────────────────────────────────────────────

    fun roll(sides: Int): Int = (1..sides).random()

    fun rollMultiple(count: Int, sides: Int): List<Int> =
        List(count) { roll(sides) }

    // ── 4d6 drop lowest ────────────────────────────────────────────────────

    fun rollStat(): Int {
        val rolls = rollMultiple(4, 6)
        return rolls.sum() - rolls.min()
    }

    // Rolls a full set of 6 stats
    fun rollStatBlock(): StatBlock {
        val scores = List(6) { rollStat() }
        return StatBlock(
            strength     = scores[0],
            dexterity    = scores[1],
            constitution = scores[2],
            intelligence = scores[3],
            wisdom       = scores[4],
            charisma     = scores[5]
        )
    }

    // ── Expression Parser ──────────────────────────────────────────────────
    // Supports: "2d6", "1d20+5", "3d8-2"

    fun rollExpression(expression: String): RollResult {
        val cleaned = expression.trim().lowercase()

        val regex = Regex("""^(\d+)d(\d+)([+-]\d+)?$""")
        val match = regex.matchEntire(cleaned)
            ?: throw IllegalArgumentException("Invalid dice expression: $expression")

        val count    = match.groupValues[1].toInt()
        val sides    = match.groupValues[2].toInt()
        val modifier = match.groupValues[3].toIntOrNull() ?: 0

        if (count < 1 || count > 100)
            throw IllegalArgumentException("Dice count must be between 1 and 100")
        if (sides < 2 || sides > 100)
            throw IllegalArgumentException("Dice sides must be between 2 and 100")

        val rolls = rollMultiple(count, sides)
        val total = rolls.sum() + modifier

        return RollResult(
            expression = expression,
            rolls      = rolls,
            modifier   = modifier,
            total      = total
        )
    }

    // ── Data classes ───────────────────────────────────────────────────────

    data class StatBlock(
        val strength: Int,
        val dexterity: Int,
        val constitution: Int,
        val intelligence: Int,
        val wisdom: Int,
        val charisma: Int
    )

    data class RollResult(
        val expression: String,
        val rolls: List<Int>,
        val modifier: Int,
        val total: Int
    )
}
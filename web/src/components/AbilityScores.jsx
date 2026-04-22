function StatBox({ label, score, modifier, onRoll }) {
  return (
    <div
      className="card"
      onClick={() => onRoll(`${label} Check`, modifier)}
      style={{
        textAlign: "center",
        padding: "0.75rem",
        cursor: "pointer",
        transition: "border-color 0.15s",
      }}
      onMouseEnter={(e) =>
        (e.currentTarget.style.borderColor = "var(--primary)")
      }
      onMouseLeave={(e) =>
        (e.currentTarget.style.borderColor = "var(--border)")
      }
    >
      <p
        className="muted"
        style={{
          fontSize: "0.7rem",
          textTransform: "uppercase",
          letterSpacing: "0.05em",
          marginBottom: "0.25rem",
        }}
      >
        {label}
      </p>
      <p style={{ fontSize: "1.5rem", fontWeight: 700 }}>{score}</p>
      <p style={{ fontSize: "0.9rem", color: "var(--primary)" }}>
        {modifier >= 0 ? `+${modifier}` : modifier}
      </p>
    </div>
  );
}

export default function AbilityScores({ stats, sheet, onRoll }) {
  return (
    <div>
      <h2 style={{ marginBottom: "0.75rem" }}>Ability Scores</h2>
      <div className="grid-3">
        <StatBox
          label="STR"
          score={stats.strength}
          modifier={sheet.strModifier}
          onRoll={onRoll}
        />
        <StatBox
          label="DEX"
          score={stats.dexterity}
          modifier={sheet.dexModifier}
          onRoll={onRoll}
        />
        <StatBox
          label="CON"
          score={stats.constitution}
          modifier={sheet.conModifier}
          onRoll={onRoll}
        />
        <StatBox
          label="INT"
          score={stats.intelligence}
          modifier={sheet.intModifier}
          onRoll={onRoll}
        />
        <StatBox
          label="WIS"
          score={stats.wisdom}
          modifier={sheet.wisModifier}
          onRoll={onRoll}
        />
        <StatBox
          label="CHA"
          score={stats.charisma}
          modifier={sheet.chaModifier}
          onRoll={onRoll}
        />
      </div>
    </div>
  );
}

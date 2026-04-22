function SkillRow({ skill, modifier, onRoll }) {
  const isProf = skill.proficiency === "PROFICIENT";
  const isExpert = skill.proficiency === "EXPERT";

  const dotStyle = {
    width: 14,
    height: 14,
    borderRadius: "50%",
    border: `2px solid ${
      isExpert ? "var(--primary)" : isProf ? "var(--success)" : "var(--muted)"
    }`,
    backgroundColor: isExpert
      ? "var(--primary)"
      : isProf
        ? "var(--success)"
        : "transparent",
    boxShadow: isExpert ? "inset 0 0 0 2px var(--bg)" : "none",
    display: "inline-block",
    flexShrink: 0,
  };

  return (
    <div
      className="flex-between"
      onClick={() => onRoll(`${skill.skillName} Check`, modifier)}
      style={{
        padding: "0.4rem 0.5rem",
        borderBottom: "1px solid var(--border)",
        cursor: "pointer",
        borderRadius: "4px",
      }}
      onMouseEnter={(e) =>
        (e.currentTarget.style.backgroundColor = "var(--bg-card)")
      }
      onMouseLeave={(e) =>
        (e.currentTarget.style.backgroundColor = "transparent")
      }
    >
      <div className="flex" style={{ gap: "0.6rem", alignItems: "center" }}>
        <span style={dotStyle} />
        <span style={{ fontSize: "0.9rem" }}>{skill.skillName}</span>
        <span className="muted" style={{ fontSize: "0.75rem" }}>
          ({skill.stat.slice(0, 3)})
        </span>
      </div>
      <span
        style={{
          fontSize: "0.9rem",
          fontWeight: 600,
          color: "var(--primary)",
        }}
      >
        {modifier >= 0 ? `+${modifier}` : modifier}
      </span>
    </div>
  );
}

export function skillModifier(skill, sheet) {
  const statMap = {
    strength: sheet.strModifier,
    dexterity: sheet.dexModifier,
    constitution: sheet.conModifier,
    intelligence: sheet.intModifier,
    wisdom: sheet.wisModifier,
    charisma: sheet.chaModifier,
  };
  const base = statMap[skill.stat] ?? 0;
  const prof = sheet.proficiencyBonus;
  switch (skill.proficiency) {
    case "EXPERT":
      return base + prof * 2;
    case "PROFICIENT":
      return base + prof;
    default:
      return base;
  }
}

export default function SkillsSection({ skills, sheet, onRoll }) {
  return (
    <div>
      <h2 style={{ marginBottom: "0.75rem" }}>Skills</h2>
      <div className="card" style={{ padding: "0.5rem 0.75rem" }}>
        {skills.map((skill) => (
          <SkillRow
            key={skill.skillName}
            skill={skill}
            modifier={skillModifier(skill, sheet)}
            onRoll={onRoll}
          />
        ))}
      </div>
    </div>
  );
}

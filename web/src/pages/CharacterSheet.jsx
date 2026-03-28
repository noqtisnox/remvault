import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import { useAuth } from "../store/AuthContext.jsx";

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

function SkillRow({ skill, modifier, onRoll }) {
  const isProf = skill.proficiency === "PROFICIENT";
  const isExpert = skill.proficiency === "EXPERT";

  // Hollow circle for NONE, filled for PROFICIENT, double-ring for EXPERT
  const dotStyle = {
    width: 14,
    height: 14,
    borderRadius: "50%",
    border: `2px solid ${isExpert ? "var(--primary)" : isProf ? "var(--success)" : "var(--muted)"}`,
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
        style={{ fontSize: "0.9rem", fontWeight: 600, color: "var(--primary)" }}
      >
        {modifier >= 0 ? `+${modifier}` : modifier}
      </span>
    </div>
  );
}

export default function CharacterSheet() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [sheet, setSheet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // HP editor
  const [editingHp, setEditingHp] = useState(false);
  const [hpValue, setHpValue] = useState("");
  const [hpLoading, setHpLoading] = useState(false);

  // XP editor
  const [editingXp, setEditingXp] = useState(false);
  const [xpValue, setXpValue] = useState("");
  const [xpLoading, setXpLoading] = useState(false);

  // Dice Roller State
  const [rollResult, setRollResult] = useState(null);
  const [isRolling, setIsRolling] = useState(false);

  useEffect(() => {
    api.characters
      .get(id)
      .then((s) => {
        setSheet(s);
        setHpValue(s.hitPoints.current);
        setXpValue(s.character.experiencePoints);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  const saveHp = async () => {
    setHpLoading(true);
    try {
      const hp = await api.characters.updateHp(id, {
        current: parseInt(hpValue),
      });
      setSheet((s) => ({ ...s, hitPoints: hp }));
      setEditingHp(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setHpLoading(false);
    }
  };

  const saveXp = async () => {
    setXpLoading(true);
    try {
      const updated = await api.characters.update(id, {
        experiencePoints: parseInt(xpValue),
      });
      setSheet(updated);
      setEditingXp(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setXpLoading(false);
    }
  };

  const deleteCharacter = async () => {
    if (!confirm(`Delete ${sheet.character.name}? This cannot be undone.`))
      return;
    try {
      await api.characters.delete(id);
      navigate("/dashboard");
    } catch (err) {
      setError(err.message);
    }
  };

  // ── Dice Rolling Handler ───────────────────────────────────────────────
  const handleRoll = async (rollName, modifier) => {
    setIsRolling(true);
    setRollResult(null); // Clear previous roll
    try {
      const expression = modifier >= 0 ? `1d20+${modifier}` : `1d20${modifier}`;
      const response = await api.dice.roll({ expression });

      setRollResult({
        name: rollName,
        ...response,
      });
    } catch (err) {
      alert(`Roll failed: ${err.message}`);
    } finally {
      setIsRolling(false);
    }
  };

  if (loading) return <div className="page muted">Loading…</div>;
  if (error) return <div className="page error">{error}</div>;
  if (!sheet) return <div className="page muted">Character not found.</div>;

  const {
    character,
    stats,
    hitPoints,
    proficiencyBonus,
    passivePerception,
    carryingCapacity,
    skills,
  } = sheet;
  const isOwner = character.userId === user?.id;

  return (
    <div className="page">
      {/* ── Header & Quick Stats ─────────────────────────────────────── */}
      <div className="flex-between" style={{ marginBottom: "1.5rem" }}>
        <div>
          <div
            className="flex"
            style={{ gap: "0.75rem", alignItems: "center" }}
          >
            <h1>{character.name}</h1>
            <span
              className={`badge ${character.status === "ALIVE" ? "badge-green" : "badge-red"}`}
            >
              {character.status}
            </span>
          </div>
          <p className="muted" style={{ marginTop: "0.25rem" }}>
            Level {character.level} {character.race} {character.characterClass}
            {character.background && ` · ${character.background}`}
            {character.alignment && ` · ${character.alignment}`}
          </p>
        </div>
        <div className="flex">
          <button className="btn-ghost" onClick={() => navigate("/dashboard")}>
            ← Back
          </button>
          {isOwner && (
            <button className="btn-danger" onClick={deleteCharacter}>
              Delete
            </button>
          )}
        </div>
      </div>

      <div
        className="grid-3"
        style={{
          marginBottom: "1.5rem",
          gridTemplateColumns: "repeat(4, 1fr)",
        }}
      >
        <div className="card" style={{ textAlign: "center" }}>
          <p
            className="muted"
            style={{ fontSize: "0.75rem", textTransform: "uppercase" }}
          >
            Hit Points
          </p>
          {editingHp ? (
            <div style={{ marginTop: "0.5rem" }}>
              <input
                type="number"
                value={hpValue}
                onChange={(e) => setHpValue(e.target.value)}
                min={0}
                max={hitPoints.maximum}
                style={{ textAlign: "center", marginBottom: "0.5rem" }}
              />
              <div className="flex" style={{ justifyContent: "center" }}>
                <button
                  className="btn-primary"
                  style={{ fontSize: "0.8rem", padding: "0.3rem 0.75rem" }}
                  onClick={saveHp}
                  disabled={hpLoading}
                >
                  {hpLoading ? "…" : "Save"}
                </button>
                <button
                  className="btn-ghost"
                  style={{ fontSize: "0.8rem", padding: "0.3rem 0.75rem" }}
                  onClick={() => setEditingHp(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <>
              <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>
                {hitPoints.current}
                <span className="muted">/{hitPoints.maximum}</span>
              </p>
              {hitPoints.temporary > 0 && (
                <p className="muted" style={{ fontSize: "0.8rem" }}>
                  +{hitPoints.temporary} temp
                </p>
              )}
              {isOwner && (
                <button
                  className="btn-ghost"
                  style={{
                    fontSize: "0.75rem",
                    padding: "0.2rem 0.5rem",
                    marginTop: "0.4rem",
                  }}
                  onClick={() => setEditingHp(true)}
                >
                  Edit
                </button>
              )}
            </>
          )}
        </div>

        <div className="card" style={{ textAlign: "center" }}>
          <p
            className="muted"
            style={{ fontSize: "0.75rem", textTransform: "uppercase" }}
          >
            Proficiency
          </p>
          <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>
            +{proficiencyBonus}
          </p>
        </div>
        <div className="card" style={{ textAlign: "center" }}>
          <p
            className="muted"
            style={{ fontSize: "0.75rem", textTransform: "uppercase" }}
          >
            Passive Perception
          </p>
          <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>
            {passivePerception}
          </p>
        </div>
        <div className="card" style={{ textAlign: "center" }}>
          <p
            className="muted"
            style={{ fontSize: "0.75rem", textTransform: "uppercase" }}
          >
            Carry Capacity
          </p>
          <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>
            {carryingCapacity} lb
          </p>
        </div>
      </div>

      {/* ── XP Editor ────────────────────────────────────── */}
      <div className="card" style={{ marginBottom: "1.5rem" }}>
        <div className="flex-between">
          <div className="flex">
            <span style={{ fontSize: "0.9rem" }}>Experience Points</span>
            <span className="badge badge-purple">
              {character.experiencePoints} XP
            </span>
            <span className="muted" style={{ fontSize: "0.85rem" }}>
              → Level {character.level}
            </span>
          </div>
          {isOwner && !editingXp && (
            <button
              className="btn-ghost"
              style={{ fontSize: "0.8rem", padding: "0.3rem 0.75rem" }}
              onClick={() => setEditingXp(true)}
            >
              Add XP
            </button>
          )}
        </div>
        {editingXp && (
          <div className="flex" style={{ marginTop: "0.75rem" }}>
            <input
              type="number"
              value={xpValue}
              onChange={(e) => setXpValue(e.target.value)}
              min={0}
              style={{ maxWidth: 140 }}
            />
            <button
              className="btn-primary"
              style={{ fontSize: "0.85rem" }}
              onClick={saveXp}
              disabled={xpLoading}
            >
              {xpLoading ? "…" : "Save"}
            </button>
            <button
              className="btn-ghost"
              style={{ fontSize: "0.85rem" }}
              onClick={() => setEditingXp(false)}
            >
              Cancel
            </button>
          </div>
        )}
      </div>

      {/* ── Main Layout: Left Sheet & Right Panel ───────────────────── */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "2fr 1fr",
          gap: "1.5rem",
          alignItems: "start",
        }}
      >
        {/* LEFT COLUMN: Stats, Skills, etc. */}
        <div
          style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}
        >
          {/* ── Ability Scores ───────────────────────────────────────── */}
          <div>
            <h2 style={{ marginBottom: "0.75rem" }}>Ability Scores</h2>
            <div className="grid-3">
              <StatBox
                label="STR"
                score={stats.strength}
                modifier={sheet.strModifier}
                onRoll={handleRoll}
              />
              <StatBox
                label="DEX"
                score={stats.dexterity}
                modifier={sheet.dexModifier}
                onRoll={handleRoll}
              />
              <StatBox
                label="CON"
                score={stats.constitution}
                modifier={sheet.conModifier}
                onRoll={handleRoll}
              />
              <StatBox
                label="INT"
                score={stats.intelligence}
                modifier={sheet.intModifier}
                onRoll={handleRoll}
              />
              <StatBox
                label="WIS"
                score={stats.wisdom}
                modifier={sheet.wisModifier}
                onRoll={handleRoll}
              />
              <StatBox
                label="CHA"
                score={stats.charisma}
                modifier={sheet.chaModifier}
                onRoll={handleRoll}
              />
            </div>
          </div>

          {/* ── Skills ───────────────────────────────────────────────── */}
          <div>
            <h2 style={{ marginBottom: "0.75rem" }}>Skills</h2>
            <div className="card" style={{ padding: "0.5rem 0.75rem" }}>
              {skills.map((skill) => (
                <SkillRow
                  key={skill.skillName}
                  skill={skill}
                  modifier={skillModifier(skill, sheet)}
                  onRoll={handleRoll}
                />
              ))}
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN: Dice Tray & Actions */}
        <div
          style={{
            position: "sticky",
            top: "1.5rem",
            display: "flex",
            flexDirection: "column",
            gap: "1.5rem",
          }}
        >
          <div>
            <h2 style={{ marginBottom: "0.75rem" }}>Dice Tray</h2>
            <div
              className="card"
              style={{
                minHeight: "220px",
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
                alignItems: "center",
                textAlign: "center",
                backgroundColor: "var(--bg-card)",
                border:
                  rollResult || isRolling
                    ? "2px solid var(--primary)"
                    : "1px solid var(--border)",
              }}
            >
              {isRolling ? (
                <h3 style={{ margin: 0, color: "var(--text)" }}>
                  Rolling... 🎲
                </h3>
              ) : rollResult ? (
                <>
                  <p
                    className="muted"
                    style={{
                      margin: 0,
                      fontSize: "0.9rem",
                      textTransform: "uppercase",
                      letterSpacing: "0.05em",
                    }}
                  >
                    {rollResult.name} ({rollResult.expression})
                  </p>
                  <div
                    className="flex"
                    style={{
                      justifyContent: "center",
                      alignItems: "center",
                      gap: "1rem",
                      marginTop: "0.5rem",
                    }}
                  >
                    <span style={{ fontSize: "1.2rem", color: "var(--muted)" }}>
                      d20: {rollResult.rolls[0]}
                    </span>
                    <span
                      style={{
                        fontSize: "2.5rem",
                        fontWeight: 800,
                        color: "var(--primary)",
                      }}
                    >
                      {rollResult.total}
                    </span>
                  </div>
                  {/* Optional: Show Natural 1 / Natural 20 highlights */}
                  {rollResult.rolls[0] === 20 && (
                    <p
                      style={{
                        color: "var(--success)",
                        margin: "0.5rem 0 0",
                        fontWeight: "bold",
                      }}
                    >
                      Critical Success!
                    </p>
                  )}
                  {rollResult.rolls[0] === 1 && (
                    <p
                      style={{
                        color: "var(--error)",
                        margin: "0.5rem 0 0",
                        fontWeight: "bold",
                      }}
                    >
                      Critical Failure!
                    </p>
                  )}

                  <button
                    className="btn-ghost"
                    style={{ marginTop: "1.5rem", fontSize: "0.8rem" }}
                    onClick={() => setRollResult(null)}
                  >
                    Clear Tray
                  </button>
                </>
              ) : (
                <p className="muted" style={{ margin: 0 }}>
                  Click any stat or skill
                  <br />
                  to roll a check.
                </p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function skillModifier(skill, sheet) {
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

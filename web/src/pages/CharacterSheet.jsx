import { useState, useEffect, useRef } from "react";
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

  // HP & XP editors
  const [editingHp, setEditingHp] = useState(false);
  const [hpValue, setHpValue] = useState("");
  const [hpLoading, setHpLoading] = useState(false);

  const [editingXp, setEditingXp] = useState(false);
  const [xpValue, setXpValue] = useState("");
  const [xpLoading, setXpLoading] = useState(false);

  // Dice Roller State
  const [rollResult, setRollResult] = useState(null);
  const [isRolling, setIsRolling] = useState(false);

  // ── AI Chat State ──────────────────────────────────────────────────
  const [chatMessages, setChatMessages] = useState([
    { role: "ai", text: "I'm ready. Need lore advice or rule clarifications?" }
  ]);
  const [chatInput, setChatInput] = useState("");
  const [isChatLoading, setIsChatLoading] = useState(false);
  const chatEndRef = useRef(null);

  // Auto-scroll chat to bottom
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatMessages]);

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

  const saveHp = async () => { /* ... existing code ... */ };
  const saveXp = async () => { /* ... existing code ... */ };
  const deleteCharacter = async () => { /* ... existing code ... */ };

  const handleRoll = async (rollName, modifier) => {
    setIsRolling(true);
    setRollResult(null);
    try {
      const expression = modifier >= 0 ? `1d20+${modifier}` : `1d20${modifier}`;
      const response = await api.dice.roll({ expression });
      setRollResult({ name: rollName, ...response });
    } catch (err) {
      alert(`Roll failed: ${err.message}`);
    } finally {
      setIsRolling(false);
    }
  };

  // ── AI Chat Handler ────────────────────────────────────────────────
  const handleChatSubmit = async (e) => {
    e.preventDefault();
    if (!chatInput.trim()) return;

    const userText = chatInput.trim();
    setChatMessages((prev) => [...prev, { role: "user", text: userText }]);
    setChatInput("");
    setIsChatLoading(true);

    try {
      const response = await api.ai.ask({
        message: userText,
        // Send character context so the LLM knows who it's advising
        context: {
          name: sheet.character.name,
          class: sheet.character.characterClass,
          level: sheet.character.level,
        }
      });
      
      setChatMessages((prev) => [...prev, { role: "ai", text: response.reply }]);
    } catch (err) {
      setChatMessages((prev) => [...prev, { role: "ai", text: `Error: ${err.message}` }]);
    } finally {
      setIsChatLoading(false);
    }
  };

  if (loading) return <div className="page muted">Loading…</div>;
  if (error) return <div className="page error">{error}</div>;
  if (!sheet) return <div className="page muted">Character not found.</div>;

  const { character, stats, hitPoints, proficiencyBonus, passivePerception, carryingCapacity, skills } = sheet;
  const isOwner = character.userId === user?.id;

  return (
    <div className="page">
      {/* ── Header & Quick Stats (Unchanged) ───────────────────────── */}
      <div className="flex-between" style={{ marginBottom: "1.5rem" }}>
        <div>
          <div className="flex" style={{ gap: "0.75rem", alignItems: "center" }}>
            <h1>{character.name}</h1>
            <span className={`badge ${character.status === "ALIVE" ? "badge-green" : "badge-red"}`}>
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
          <button className="btn-ghost" onClick={() => navigate("/dashboard")}>← Back</button>
          {isOwner && (
            <button className="btn-danger" onClick={deleteCharacter}>Delete</button>
          )}
        </div>
      </div>

      <div className="grid-3" style={{ marginBottom: "1.5rem", gridTemplateColumns: "repeat(4, 1fr)" }}>
         {/* ... Hit Points, Prof, Passive Perception, Carry Capacity (Unchanged) ... */}
         <div className="card" style={{ textAlign: "center" }}>
          <p className="muted" style={{ fontSize: "0.75rem", textTransform: "uppercase" }}>Hit Points</p>
          <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>
            {hitPoints.current}<span className="muted">/{hitPoints.maximum}</span>
          </p>
        </div>
        <div className="card" style={{ textAlign: "center" }}>
          <p className="muted" style={{ fontSize: "0.75rem", textTransform: "uppercase" }}>Proficiency</p>
          <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>+{proficiencyBonus}</p>
        </div>
        <div className="card" style={{ textAlign: "center" }}>
          <p className="muted" style={{ fontSize: "0.75rem", textTransform: "uppercase" }}>Passive Perception</p>
          <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>{passivePerception}</p>
        </div>
        <div className="card" style={{ textAlign: "center" }}>
          <p className="muted" style={{ fontSize: "0.75rem", textTransform: "uppercase" }}>Carry Capacity</p>
          <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>{carryingCapacity} lb</p>
        </div>
      </div>

      {/* ── Main Layout: Left Sheet & Right Panel ───────────────────── */}
      <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gap: "1.5rem", alignItems: "start" }}>
        
        {/* LEFT COLUMN: Stats, Skills */}
        <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
          <div>
            <h2 style={{ marginBottom: "0.75rem" }}>Ability Scores</h2>
            <div className="grid-3">
              <StatBox label="STR" score={stats.strength} modifier={sheet.strModifier} onRoll={handleRoll} />
              <StatBox label="DEX" score={stats.dexterity} modifier={sheet.dexModifier} onRoll={handleRoll} />
              <StatBox label="CON" score={stats.constitution} modifier={sheet.conModifier} onRoll={handleRoll} />
              <StatBox label="INT" score={stats.intelligence} modifier={sheet.intModifier} onRoll={handleRoll} />
              <StatBox label="WIS" score={stats.wisdom} modifier={sheet.wisModifier} onRoll={handleRoll} />
              <StatBox label="CHA" score={stats.charisma} modifier={sheet.chaModifier} onRoll={handleRoll} />
            </div>
          </div>

          <div>
            <h2 style={{ marginBottom: "0.75rem" }}>Skills</h2>
            <div className="card" style={{ padding: "0.5rem 0.75rem" }}>
              {skills.map((skill) => (
                <SkillRow key={skill.skillName} skill={skill} modifier={skillModifier(skill, sheet)} onRoll={handleRoll} />
              ))}
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN: Dice Tray & AI Chat */}
        <div style={{ position: "sticky", top: "1.5rem", display: "flex", flexDirection: "column", gap: "1.5rem" }}>
          
          {/* Dice Tray */}
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
                border: rollResult || isRolling ? "2px solid var(--primary)" : "1px solid var(--border)",
              }}
            >
              {isRolling ? (
                <h3 style={{ margin: 0, color: "var(--text)" }}>Rolling... 🎲</h3>
              ) : rollResult ? (
                <>
                  <p className="muted" style={{ margin: 0, fontSize: "0.9rem", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                    {rollResult.name} ({rollResult.expression})
                  </p>
                  <div className="flex" style={{ justifyContent: "center", alignItems: "center", gap: "1rem", marginTop: "0.5rem" }}>
                    <span style={{ fontSize: "1.2rem", color: "var(--muted)" }}>d20: {rollResult.rolls[0]}</span>
                    <span style={{ fontSize: "2.5rem", fontWeight: 800, color: "var(--primary)" }}>{rollResult.total}</span>
                  </div>
                  <button className="btn-ghost" style={{ marginTop: "1.5rem", fontSize: "0.8rem" }} onClick={() => setRollResult(null)}>
                    Clear Tray
                  </button>
                </>
              ) : (
                <p className="muted" style={{ margin: 0 }}>Click any stat or skill<br />to roll a check.</p>
              )}
            </div>
          </div>

          {/* ── AI Assistant Chat Window ──────────────────────────────── */}
          <div>
            <h2 style={{ marginBottom: "0.75rem" }}>Companion AI</h2>
            <div className="card" style={{ display: "flex", flexDirection: "column", height: "350px", padding: 0, overflow: "hidden" }}>
              
              {/* Message History */}
              <div style={{ flex: 1, overflowY: "auto", padding: "1rem", display: "flex", flexDirection: "column", gap: "0.75rem" }}>
                {chatMessages.map((msg, idx) => (
                  <div 
                    key={idx} 
                    style={{ 
                      alignSelf: msg.role === "user" ? "flex-end" : "flex-start",
                      backgroundColor: msg.role === "user" ? "var(--primary)" : "var(--bg)",
                      color: msg.role === "user" ? "#fff" : "var(--text)",
                      padding: "0.6rem 0.8rem",
                      borderRadius: "8px",
                      maxWidth: "85%",
                      fontSize: "0.85rem",
                      border: msg.role === "ai" ? "1px solid var(--border)" : "none"
                    }}
                  >
                    {msg.text}
                  </div>
                ))}
                {isChatLoading && (
                  <div style={{ alignSelf: "flex-start", fontSize: "0.8rem", color: "var(--muted)", padding: "0.5rem" }}>
                    Thinking...
                  </div>
                )}
                <div ref={chatEndRef} />
              </div>

              {/* Input Area */}
              <form 
                onSubmit={handleChatSubmit} 
                style={{ 
                  display: "flex", 
                  padding: "0.75rem", 
                  borderTop: "1px solid var(--border)",
                  backgroundColor: "var(--bg-card)",
                  gap: "0.5rem" 
                }}
              >
                <input
                  type="text"
                  placeholder="Ask the DM..."
                  value={chatInput}
                  onChange={(e) => setChatInput(e.target.value)}
                  style={{ flex: 1, padding: "0.5rem", fontSize: "0.85rem", borderRadius: "4px", border: "1px solid var(--border)", backgroundColor: "var(--bg)" }}
                  disabled={isChatLoading}
                />
                <button 
                  type="submit" 
                  className="btn-primary" 
                  style={{ padding: "0 0.75rem", fontSize: "0.85rem" }}
                  disabled={isChatLoading || !chatInput.trim()}
                >
                  Send
                </button>
              </form>

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
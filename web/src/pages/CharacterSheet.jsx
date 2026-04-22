import { useNavigate } from "react-router-dom";
import { useAuth } from "../store/AuthContext.jsx";
import { api } from "../api/client.js";
import { useCharacterSheet, useRoller } from "../hooks/useCharacterSheet.js";
import CharacterHeader from "../components/CharacterHeader.jsx";
import QuickStats from "../components/QuickStats.jsx";
import AbilityScores from "../components/AbilityScores.jsx";
import SkillsSection from "../components/SkillsSection.jsx";
import DiceTray from "../components/DiceTray.jsx";
import AIChat from "../components/AIChat.jsx";

export default function CharacterSheet() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { sheet, loading, error } = useCharacterSheet();
  const { rollResult, isRolling, handleRoll, setRollResult } = useRoller();

  const deleteCharacter = async () => {
    if (
      !window.confirm(
        `Delete "${sheet.character.name}"? This cannot be undone.`
      )
    )
      return;
    try {
      await api.characters.delete(sheet.id);
      navigate("/dashboard");
    } catch (err) {
      alert(`Failed to delete: ${err.message}`);
    }
  };

  if (loading) return <div className="page muted">Loading…</div>;
  if (error) return <div className="page error">{error}</div>;
  if (!sheet) return <div className="page muted">Character not found.</div>;

  const { character, stats, hitPoints, proficiencyBonus, passivePerception, carryingCapacity, skills } = sheet;
  const isOwner = character.userId === user?.id;

  return (
    <div className="page">
      <CharacterHeader
        character={character}
        isOwner={isOwner}
        onDelete={deleteCharacter}
      />

      <QuickStats
        hitPoints={hitPoints}
        proficiencyBonus={proficiencyBonus}
        passivePerception={passivePerception}
        carryingCapacity={carryingCapacity}
      />

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "2fr 1fr",
          gap: "1.5rem",
          alignItems: "start",
        }}
      >
        <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
          <AbilityScores stats={stats} sheet={sheet} onRoll={handleRoll} />
          <SkillsSection skills={skills} sheet={sheet} onRoll={handleRoll} />
        </div>

        <div
          style={{
            position: "sticky",
            top: "1.5rem",
            display: "flex",
            flexDirection: "column",
            gap: "1.5rem",
          }}
        >
          <DiceTray
            rollResult={rollResult}
            isRolling={isRolling}
            onClear={() => setRollResult(null)}
          />
          <AIChat sheet={sheet} />
        </div>
      </div>
    </div>
  );
}
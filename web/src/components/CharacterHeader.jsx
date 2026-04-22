import { useNavigate } from "react-router-dom";

export default function CharacterHeader({ character, isOwner, onDelete }) {
  const navigate = useNavigate();

  return (
    <div className="flex-between" style={{ marginBottom: "1.5rem" }}>
      <div>
        <div className="flex" style={{ gap: "0.75rem", alignItems: "center" }}>
          <h1>{character.name}</h1>
          <span
            className={`badge ${
              character.status === "ALIVE" ? "badge-green" : "badge-red"
            }`}
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
          <button className="btn-danger" onClick={onDelete}>
            Delete
          </button>
        )}
      </div>
    </div>
  );
}

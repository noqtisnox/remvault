import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import { useAuth } from "../store/AuthContext.jsx";

import { SRD } from "../data/srd.js";

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const isMaster = user?.role === "MASTER";

  const [characters, setCharacters] = useState([]);
  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // ── Create character form ──────────────────────────────────────────────
  const [showCharForm, setShowCharForm] = useState(false);
  const [charForm, setCharForm] = useState({
    name: "",
    race: "",
    characterClass: "",
    background: "",
    alignment: "",
  });
  const [charError, setCharError] = useState(null);
  const [charLoading, setCharLoading] = useState(false);

  // ── Create campaign form ───────────────────────────────────────────────
  const [showCampForm, setShowCampForm] = useState(false);
  const [campForm, setCampForm] = useState({
    name: "",
    description: "",
    setting: "",
  });
  const [campError, setCampError] = useState(null);
  const [campLoading, setCampLoading] = useState(false);

  useEffect(() => {
    Promise.all([api.characters.list(), api.campaigns.list()])
      .then(([chars, camps]) => {
        setCharacters(chars);
        setCampaigns(camps);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const setChar = (f) => (e) =>
    setCharForm((p) => ({ ...p, [f]: e.target.value }));
  const setCamp = (f) => (e) =>
    setCampForm((p) => ({ ...p, [f]: e.target.value }));

  const createCharacter = async (e) => {
    e.preventDefault();
    setCharError(null);
    setCharLoading(true);
    try {
      const sheet = await api.characters.create(charForm);
      setCharacters((prev) => [...prev, sheet]);
      setShowCharForm(false);
      setCharForm({
        name: "",
        race: "",
        characterClass: "",
        background: "",
        alignment: "",
      });
    } catch (err) {
      setCharError(err.message);
    } finally {
      setCharLoading(false);
    }
  };

  const createCampaign = async (e) => {
    e.preventDefault();
    setCampError(null);
    setCampLoading(true);
    try {
      const camp = await api.campaigns.create(campForm);
      setCampaigns((prev) => [...prev, camp]);
      setShowCampForm(false);
      setCampForm({ name: "", description: "", setting: "" });
    } catch (err) {
      setCampError(err.message);
    } finally {
      setCampLoading(false);
    }
  };

  if (loading) return <div className="page muted">Loading…</div>;
  if (error) return <div className="page error">{error}</div>;

  return (
    <div className="page">
      <div className="flex-between" style={{ marginBottom: "2rem" }}>
        <div>
          <h1>Welcome back, {user?.username}</h1>
          <p className="muted" style={{ marginTop: "0.25rem" }}>
            {isMaster
              ? "Managing your campaigns and characters"
              : "Your characters and campaigns"}
          </p>
        </div>
      </div>

      {/* ── Characters ────────────────────────────────────────────────── */}
      <section style={{ marginBottom: "2.5rem" }}>
        <div className="flex-between" style={{ marginBottom: "1rem" }}>
          <h2>Characters</h2>
          <button
            className="btn-primary"
            onClick={() => setShowCharForm((v) => !v)}
          >
            {showCharForm ? "Cancel" : "+ New Character"}
          </button>
        </div>

        {showCharForm && (
          <div className="card" style={{ marginBottom: "1rem" }}>
            <h3 style={{ marginBottom: "1rem" }}>Create Character</h3>
            <form onSubmit={createCharacter}>
              <div
                className="grid-2"
                style={{ gap: "0.75rem", marginBottom: "0.75rem" }}
              >
                <label
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.35rem",
                    fontSize: "0.9rem",
                    color: "var(--muted)",
                  }}
                >
                  Name *
                  <input
                    value={charForm.name}
                    onChange={setChar("name")}
                    placeholder="Character Name"
                    required
                  />
                </label>

                <label
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.35rem",
                    fontSize: "0.9rem",
                    color: "var(--muted)",
                  }}
                >
                  Race *
                  <select
                    value={charForm.race}
                    onChange={setChar("race")}
                    required
                  >
                    <option value="" disabled>
                      Select a race...
                    </option>
                    {SRD.races.map((r) => (
                      <option key={r} value={r}>
                        {r}
                      </option>
                    ))}
                  </select>
                </label>

                <label
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.35rem",
                    fontSize: "0.9rem",
                    color: "var(--muted)",
                  }}
                >
                  Class *
                  <select
                    value={charForm.characterClass}
                    onChange={setChar("characterClass")}
                    required
                  >
                    <option value="" disabled>
                      Select a class...
                    </option>
                    {SRD.classes.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </label>

                <label
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.35rem",
                    fontSize: "0.9rem",
                    color: "var(--muted)",
                  }}
                >
                  Background *
                  <select
                    value={charForm.background}
                    onChange={setChar("background")}
                    required
                  >
                    <option value="" disabled>
                      Select a background...
                    </option>
                    {SRD.backgrounds.map((b) => (
                      <option key={b} value={b}>
                        {b}
                      </option>
                    ))}
                  </select>
                </label>

                <label
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.35rem",
                    fontSize: "0.9rem",
                    color: "var(--muted)",
                  }}
                >
                  Alignment
                  <select
                    value={charForm.alignment}
                    onChange={setChar("alignment")}
                  >
                    <option value="">Select an alignment...</option>
                    {SRD.alignments.map((a) => (
                      <option key={a} value={a}>
                        {a}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
              {charError && (
                <p className="error" style={{ marginBottom: "0.75rem" }}>
                  {charError}
                </p>
              )}
              <button
                className="btn-primary"
                type="submit"
                disabled={charLoading}
              >
                {charLoading ? "Rolling stats…" : "Create & Roll Stats"}
              </button>
            </form>
          </div>
        )}

        {characters.length === 0 ? (
          <p className="muted">No characters yet.</p>
        ) : (
          <div className="grid-3">
            {characters.map((sheet) => (
              <div
                key={sheet.character.id}
                className="card"
                onClick={() => navigate(`/characters/${sheet.character.id}`)}
                style={{ cursor: "pointer", transition: "border-color 0.15s" }}
                onMouseEnter={(e) =>
                  (e.currentTarget.style.borderColor = "var(--primary)")
                }
                onMouseLeave={(e) =>
                  (e.currentTarget.style.borderColor = "var(--border)")
                }
              >
                <div
                  className="flex-between"
                  style={{ marginBottom: "0.5rem" }}
                >
                  <h3>{sheet.character.name}</h3>
                  <span
                    className={`badge ${sheet.character.status === "ALIVE" ? "badge-green" : "badge-red"}`}
                  >
                    {sheet.character.status}
                  </span>
                </div>
                <p className="muted" style={{ fontSize: "0.9rem" }}>
                  {sheet.character.race} · {sheet.character.characterClass}
                </p>
                <p
                  className="muted"
                  style={{ fontSize: "0.85rem", marginTop: "0.25rem" }}
                >
                  Level {sheet.character.level}
                </p>
                <div
                  style={{
                    marginTop: "0.75rem",
                    display: "flex",
                    gap: "0.5rem",
                    flexWrap: "wrap",
                  }}
                >
                  <span className="badge">
                    HP {sheet.hitPoints.current}/{sheet.hitPoints.maximum}
                  </span>
                  <span className="badge">+{sheet.proficiencyBonus} Prof</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* ── Campaigns ─────────────────────────────────────────────────── */}
      <section>
        <div className="flex-between" style={{ marginBottom: "1rem" }}>
          <h2>Campaigns</h2>
          {isMaster && (
            <button
              className="btn-primary"
              onClick={() => setShowCampForm((v) => !v)}
            >
              {showCampForm ? "Cancel" : "+ New Campaign"}
            </button>
          )}
        </div>

        {showCampForm && (
          <div className="card" style={{ marginBottom: "1rem" }}>
            <h3 style={{ marginBottom: "1rem" }}>Create Campaign</h3>
            <form onSubmit={createCampaign}>
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "0.75rem",
                  marginBottom: "0.75rem",
                }}
              >
                <label
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.35rem",
                    fontSize: "0.9rem",
                    color: "var(--muted)",
                  }}
                >
                  Name *
                  <input
                    value={campForm.name}
                    onChange={setCamp("name")}
                    required
                  />
                </label>
                <label
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.35rem",
                    fontSize: "0.9rem",
                    color: "var(--muted)",
                  }}
                >
                  Setting
                  <input
                    value={campForm.setting}
                    onChange={setCamp("setting")}
                    placeholder="Faerun, Eberron, Homebrew world…"
                  />
                </label>
                <label
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.35rem",
                    fontSize: "0.9rem",
                    color: "var(--muted)",
                  }}
                >
                  Description
                  <textarea
                    value={campForm.description}
                    onChange={setCamp("description")}
                    rows={3}
                  />
                </label>
              </div>
              {campError && (
                <p className="error" style={{ marginBottom: "0.75rem" }}>
                  {campError}
                </p>
              )}
              <button
                className="btn-primary"
                type="submit"
                disabled={campLoading}
              >
                {campLoading ? "Creating…" : "Create Campaign"}
              </button>
            </form>
          </div>
        )}

        {campaigns.length === 0 ? (
          <p className="muted">
            {isMaster
              ? "No campaigns yet."
              : "You haven't joined any campaigns yet."}
          </p>
        ) : (
          <div className="grid-3">
            {campaigns.map((camp) => (
              <div
                key={camp.id}
                className="card"
                onClick={() => navigate(`/campaigns/${camp.id}`)}
                style={{ cursor: "pointer", transition: "border-color 0.15s" }}
                onMouseEnter={(e) =>
                  (e.currentTarget.style.borderColor = "var(--primary)")
                }
                onMouseLeave={(e) =>
                  (e.currentTarget.style.borderColor = "var(--border)")
                }
              >
                <div
                  className="flex-between"
                  style={{ marginBottom: "0.5rem" }}
                >
                  <h3>{camp.name}</h3>
                  <span
                    className={`badge ${camp.status === "ACTIVE" ? "badge-green" : ""}`}
                  >
                    {camp.status}
                  </span>
                </div>
                {camp.setting && (
                  <p className="muted" style={{ fontSize: "0.85rem" }}>
                    {camp.setting}
                  </p>
                )}
                {camp.description && (
                  <p
                    style={{
                      fontSize: "0.85rem",
                      marginTop: "0.5rem",
                      color: "var(--text)",
                      opacity: 0.7,
                    }}
                  >
                    {camp.description}
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

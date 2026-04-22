export default function DiceTray({ rollResult, isRolling, onClear }) {
  return (
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
          <h3 style={{ margin: 0, color: "var(--text)" }}>Rolling... 🎲</h3>
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
            <button
              className="btn-ghost"
              style={{ marginTop: "1.5rem", fontSize: "0.8rem" }}
              onClick={onClear}
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
  );
}

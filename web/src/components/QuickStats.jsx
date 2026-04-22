export default function QuickStats({
  hitPoints,
  proficiencyBonus,
  passivePerception,
  carryingCapacity,
}) {
  const StatCard = ({ label, value }) => (
    <div className="card" style={{ textAlign: "center" }}>
      <p className="muted" style={{ fontSize: "0.75rem", textTransform: "uppercase" }}>
        {label}
      </p>
      <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>{value}</p>
    </div>
  );

  return (
    <div
      className="grid-3"
      style={{
        marginBottom: "1.5rem",
        gridTemplateColumns: "repeat(4, 1fr)",
      }}
    >
      <StatCard
        label="Hit Points"
        value={
          <>
            {hitPoints.current}
            <span className="muted">/{hitPoints.maximum}</span>
          </>
        }
      />
      <StatCard label="Proficiency" value={`+${proficiencyBonus}`} />
      <StatCard label="Passive Perception" value={passivePerception} />
      <StatCard label="Carry Capacity" value={`${carryingCapacity} lb`} />
    </div>
  );
}

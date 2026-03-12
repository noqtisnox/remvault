import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../store/AuthContext.jsx'

function StatBox({ label, score, modifier }) {
  return (
    <div className="card" style={{ textAlign: 'center', padding: '0.75rem' }}>
      <p className="muted" style={{ fontSize: '0.7rem', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.25rem' }}>{label}</p>
      <p style={{ fontSize: '1.5rem', fontWeight: 700 }}>{score}</p>
      <p style={{ fontSize: '0.9rem', color: 'var(--primary)' }}>
        {modifier >= 0 ? `+${modifier}` : modifier}
      </p>
    </div>
  )
}

function SkillRow({ skill, modifier }) {
  const dotColor = skill.proficiency === 'EXPERT' ? 'var(--primary)' :
                   skill.proficiency === 'PROFICIENT' ? 'var(--success)' : 'var(--border)'
  return (
    <div className="flex-between" style={{ padding: '0.3rem 0', borderBottom: '1px solid var(--border)' }}>
      <div className="flex" style={{ gap: '0.5rem' }}>
        <span style={{ width: 10, height: 10, borderRadius: '50%', background: dotColor, display: 'inline-block', flexShrink: 0 }} />
        <span style={{ fontSize: '0.9rem' }}>{skill.skillName}</span>
        <span className="muted" style={{ fontSize: '0.75rem' }}>({skill.stat.slice(0,3)})</span>
      </div>
      <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--primary)' }}>
        {modifier >= 0 ? `+${modifier}` : modifier}
      </span>
    </div>
  )
}

export default function CharacterSheet() {
  const { id }     = useParams()
  const { user }   = useAuth()
  const navigate   = useNavigate()

  const [sheet,    setSheet]    = useState(null)
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState(null)

  // HP editor
  const [editingHp, setEditingHp] = useState(false)
  const [hpValue,   setHpValue]   = useState('')
  const [hpLoading, setHpLoading] = useState(false)

  // XP editor
  const [editingXp, setEditingXp] = useState(false)
  const [xpValue,   setXpValue]   = useState('')
  const [xpLoading, setXpLoading] = useState(false)

  useEffect(() => {
    api.characters.get(id)
      .then(s => { setSheet(s); setHpValue(s.hitPoints.current); setXpValue(s.character.experiencePoints) })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  const saveHp = async () => {
    setHpLoading(true)
    try {
      const hp = await api.characters.updateHp(id, { current: parseInt(hpValue) })
      setSheet(s => ({ ...s, hitPoints: hp }))
      setEditingHp(false)
    } catch (err) {
      setError(err.message)
    } finally {
      setHpLoading(false)
    }
  }

  const saveXp = async () => {
    setXpLoading(true)
    try {
      const updated = await api.characters.update(id, { experiencePoints: parseInt(xpValue) })
      setSheet(updated)
      setEditingXp(false)
    } catch (err) {
      setError(err.message)
    } finally {
      setXpLoading(false)
    }
  }

  const deleteCharacter = async () => {
    if (!confirm(`Delete ${sheet.character.name}? This cannot be undone.`)) return
    try {
      await api.characters.delete(id)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message)
    }
  }

  const statModifier = (stat) => {
    const map = {
      strength: sheet.strModifier, dexterity: sheet.dexModifier,
      constitution: sheet.conModifier, intelligence: sheet.intModifier,
      wisdom: sheet.wisModifier, charisma: sheet.chaModifier
    }
    return map[stat] ?? 0
  }

  if (loading) return <div className="page muted">Loading…</div>
  if (error)   return <div className="page error">{error}</div>
  if (!sheet)  return <div className="page muted">Character not found.</div>

  const { character, stats, hitPoints, proficiencyBonus, passivePerception, carryingCapacity, skills } = sheet
  const isOwner = character.userId === user?.id

  return (
    <div className="page">

      {/* ── Header ──────────────────────────────────────────────────── */}
      <div className="flex-between" style={{ marginBottom: '1.5rem' }}>
        <div>
          <div className="flex" style={{ gap: '0.75rem', alignItems: 'center' }}>
            <h1>{character.name}</h1>
            <span className={`badge ${character.status === 'ALIVE' ? 'badge-green' : 'badge-red'}`}>
              {character.status}
            </span>
          </div>
          <p className="muted" style={{ marginTop: '0.25rem' }}>
            Level {character.level} {character.race} {character.characterClass}
            {character.background && ` · ${character.background}`}
            {character.alignment && ` · ${character.alignment}`}
          </p>
        </div>
        <div className="flex">
          <button className="btn-ghost" onClick={() => navigate('/dashboard')}>← Back</button>
          {isOwner && <button className="btn-danger" onClick={deleteCharacter}>Delete</button>}
        </div>
      </div>

      {/* ── Quick Stats ──────────────────────────────────────────────── */}
      <div className="grid-3" style={{ marginBottom: '1.5rem', gridTemplateColumns: 'repeat(4, 1fr)' }}>
        <div className="card" style={{ textAlign: 'center' }}>
          <p className="muted" style={{ fontSize: '0.75rem', textTransform: 'uppercase' }}>Hit Points</p>
          {editingHp ? (
            <div style={{ marginTop: '0.5rem' }}>
              <input
                type="number" value={hpValue}
                onChange={e => setHpValue(e.target.value)}
                min={0} max={hitPoints.maximum}
                style={{ textAlign: 'center', marginBottom: '0.5rem' }}
              />
              <div className="flex" style={{ justifyContent: 'center' }}>
                <button className="btn-primary" style={{ fontSize: '0.8rem', padding: '0.3rem 0.75rem' }} onClick={saveHp} disabled={hpLoading}>
                  {hpLoading ? '…' : 'Save'}
                </button>
                <button className="btn-ghost" style={{ fontSize: '0.8rem', padding: '0.3rem 0.75rem' }} onClick={() => setEditingHp(false)}>
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <>
              <p style={{ fontSize: '1.4rem', fontWeight: 700 }}>
                {hitPoints.current}<span className="muted">/{hitPoints.maximum}</span>
              </p>
              {hitPoints.temporary > 0 && <p className="muted" style={{ fontSize: '0.8rem' }}>+{hitPoints.temporary} temp</p>}
              {isOwner && (
                <button className="btn-ghost" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', marginTop: '0.4rem' }} onClick={() => setEditingHp(true)}>
                  Edit
                </button>
              )}
            </>
          )}
        </div>

        <div className="card" style={{ textAlign: 'center' }}>
          <p className="muted" style={{ fontSize: '0.75rem', textTransform: 'uppercase' }}>Proficiency</p>
          <p style={{ fontSize: '1.4rem', fontWeight: 700 }}>+{proficiencyBonus}</p>
        </div>

        <div className="card" style={{ textAlign: 'center' }}>
          <p className="muted" style={{ fontSize: '0.75rem', textTransform: 'uppercase' }}>Passive Perception</p>
          <p style={{ fontSize: '1.4rem', fontWeight: 700 }}>{passivePerception}</p>
        </div>

        <div className="card" style={{ textAlign: 'center' }}>
          <p className="muted" style={{ fontSize: '0.75rem', textTransform: 'uppercase' }}>Carry Capacity</p>
          <p style={{ fontSize: '1.4rem', fontWeight: 700 }}>{carryingCapacity} lb</p>
        </div>
      </div>

      {/* ── XP ───────────────────────────────────────────────────────── */}
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="flex-between">
          <div className="flex">
            <span style={{ fontSize: '0.9rem' }}>Experience Points</span>
            <span className="badge badge-purple">{character.experiencePoints} XP</span>
            <span className="muted" style={{ fontSize: '0.85rem' }}>→ Level {character.level}</span>
          </div>
          {isOwner && !editingXp && (
            <button className="btn-ghost" style={{ fontSize: '0.8rem', padding: '0.3rem 0.75rem' }} onClick={() => setEditingXp(true)}>
              Add XP
            </button>
          )}
        </div>
        {editingXp && (
          <div className="flex" style={{ marginTop: '0.75rem' }}>
            <input
              type="number" value={xpValue}
              onChange={e => setXpValue(e.target.value)}
              min={0} style={{ maxWidth: 140 }}
            />
            <button className="btn-primary" style={{ fontSize: '0.85rem' }} onClick={saveXp} disabled={xpLoading}>
              {xpLoading ? '…' : 'Save'}
            </button>
            <button className="btn-ghost" style={{ fontSize: '0.85rem' }} onClick={() => setEditingXp(false)}>
              Cancel
            </button>
          </div>
        )}
      </div>

      <div className="grid-2" style={{ gap: '1.5rem' }}>

        {/* ── Ability Scores ───────────────────────────────────────── */}
        <div>
          <h2 style={{ marginBottom: '0.75rem' }}>Ability Scores</h2>
          <div className="grid-3">
            <StatBox label="STR" score={stats.strength}     modifier={sheet.strModifier} />
            <StatBox label="DEX" score={stats.dexterity}    modifier={sheet.dexModifier} />
            <StatBox label="CON" score={stats.constitution} modifier={sheet.conModifier} />
            <StatBox label="INT" score={stats.intelligence} modifier={sheet.intModifier} />
            <StatBox label="WIS" score={stats.wisdom}       modifier={sheet.wisModifier} />
            <StatBox label="CHA" score={stats.charisma}     modifier={sheet.chaModifier} />
          </div>
        </div>

        {/* ── Skills ───────────────────────────────────────────────── */}
        <div>
          <h2 style={{ marginBottom: '0.75rem' }}>Skills</h2>
          <div className="card" style={{ padding: '0.75rem' }}>
            {skills.map(skill => (
              <SkillRow
                key={skill.skillName}
                skill={skill}
                modifier={skillModifier(skill, sheet)}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

function skillModifier(skill, sheet) {
  const statMap = {
    strength: sheet.strModifier, dexterity: sheet.dexModifier,
    constitution: sheet.conModifier, intelligence: sheet.intModifier,
    wisdom: sheet.wisModifier, charisma: sheet.chaModifier
  }
  const base = statMap[skill.stat] ?? 0
  const prof = sheet.proficiencyBonus
  switch (skill.proficiency) {
    case 'EXPERT':     return base + prof * 2
    case 'PROFICIENT': return base + prof
    default:           return base
  }
}
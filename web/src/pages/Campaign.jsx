import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../store/AuthContext.jsx'

const SESSION_STATUSES = ['PLANNED', 'ONGOING', 'FINISHED']

function statusBadgeClass(status) {
  switch (status) {
    case 'ACTIVE':   case 'ALIVE': case 'ONGOING':  return 'badge-green'
    case 'PLANNED':                                  return 'badge-purple'
    case 'ARCHIVED': case 'DEAD':  case 'FINISHED': return ''
    default: return ''
  }
}

export default function Campaign() {
  const { id }   = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [campaign,  setCampaign]  = useState(null)
  const [members,   setMembers]   = useState([])
  const [sessions,  setSessions]  = useState([])
  const [loading,   setLoading]   = useState(true)
  const [error,     setError]     = useState(null)

  const isMaster = campaign?.masterId === user?.id

  // ── Add member form ────────────────────────────────────────────────────
  const [showMemberForm, setShowMemberForm] = useState(false)
  const [memberForm,     setMemberForm]     = useState({ userId: '', characterId: '' })
  const [memberError,    setMemberError]    = useState(null)
  const [memberLoading,  setMemberLoading]  = useState(false)

  // ── Create session form ────────────────────────────────────────────────
  const [showSessionForm, setShowSessionForm] = useState(false)
  const [sessionDate,     setSessionDate]     = useState('')
  const [sessionError,    setSessionError]    = useState(null)
  const [sessionLoading,  setSessionLoading]  = useState(false)

  // ── Notes editor ───────────────────────────────────────────────────────
  const [editingNotes,  setEditingNotes]  = useState(null) // sessionId
  const [notesValue,    setNotesValue]    = useState('')
  const [notesLoading,  setNotesLoading]  = useState(false)

  useEffect(() => {
    Promise.all([
      api.campaigns.get(id),
      api.campaigns.getMembers(id),
      api.campaigns.getSessions(id)
    ])
      .then(([camp, mem, sess]) => {
        setCampaign(camp)
        setMembers(mem)
        setSessions(sess.sort((a, b) => b.date - a.date))
      })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  // ── Handlers ───────────────────────────────────────────────────────────

  const addMember = async (e) => {
    e.preventDefault()
    setMemberError(null)
    setMemberLoading(true)
    try {
      const member = await api.campaigns.addMember(id, {
        userId:      memberForm.userId,
        characterId: memberForm.characterId || null
      })
      setMembers(prev => [...prev, member])
      setShowMemberForm(false)
      setMemberForm({ userId: '', characterId: '' })
    } catch (err) {
      setMemberError(err.message)
    } finally {
      setMemberLoading(false)
    }
  }

  const removeMember = async (userId) => {
    if (!confirm('Remove this member from the campaign?')) return
    try {
      await api.campaigns.removeMember(id, userId)
      setMembers(prev => prev.filter(m => m.userId !== userId))
    } catch (err) {
      setError(err.message)
    }
  }

  const createSession = async (e) => {
    e.preventDefault()
    setSessionError(null)
    setSessionLoading(true)
    try {
      const date    = new Date(sessionDate).getTime()
      const session = await api.campaigns.createSession(id, { date })
      setSessions(prev => [session, ...prev])
      setShowSessionForm(false)
      setSessionDate('')
    } catch (err) {
      setSessionError(err.message)
    } finally {
      setSessionLoading(false)
    }
  }

  const updateStatus = async (sessionId, status) => {
    try {
      const updated = await api.campaigns.updateStatus(id, sessionId, { status })
      setSessions(prev => prev.map(s => s.id === sessionId ? updated : s))
    } catch (err) {
      setError(err.message)
    }
  }

  const saveNotes = async (sessionId) => {
    setNotesLoading(true)
    try {
      const updated = await api.campaigns.updateNotes(id, sessionId, { notes: notesValue })
      setSessions(prev => prev.map(s => s.id === sessionId ? updated : s))
      setEditingNotes(null)
    } catch (err) {
      setError(err.message)
    } finally {
      setNotesLoading(false)
    }
  }

  const archiveCampaign = async () => {
    if (!confirm('Archive this campaign? It will become read-only.')) return
    try {
      const updated = await api.campaigns.archive(id)
      setCampaign(updated)
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <div className="page muted">Loading…</div>
  if (error)   return <div className="page error">{error}</div>
  if (!campaign) return <div className="page muted">Campaign not found.</div>

  return (
    <div className="page">

      {/* ── Header ──────────────────────────────────────────────────── */}
      <div className="flex-between" style={{ marginBottom: '1.5rem' }}>
        <div>
          <div className="flex" style={{ alignItems: 'center' }}>
            <h1>{campaign.name}</h1>
            <span className={`badge ${statusBadgeClass(campaign.status)}`}>{campaign.status}</span>
          </div>
          <p className="muted" style={{ marginTop: '0.25rem' }}>
            {[campaign.setting, campaign.description].filter(Boolean).join(' · ')}
          </p>
        </div>
        <div className="flex">
          <button className="btn-ghost" onClick={() => navigate('/dashboard')}>← Back</button>
          {isMaster && campaign.status === 'ACTIVE' && (
            <button className="btn-danger" onClick={archiveCampaign}>Archive</button>
          )}
        </div>
      </div>

      <div className="grid-2" style={{ gap: '1.5rem', alignItems: 'start' }}>

        {/* ── Members ─────────────────────────────────────────────── */}
        <div>
          <div className="flex-between" style={{ marginBottom: '0.75rem' }}>
            <h2>Members</h2>
            {isMaster && campaign.status === 'ACTIVE' && (
              <button className="btn-primary" style={{ fontSize: '0.85rem', padding: '0.35rem 0.75rem' }}
                onClick={() => setShowMemberForm(v => !v)}>
                {showMemberForm ? 'Cancel' : '+ Add Member'}
              </button>
            )}
          </div>

          {showMemberForm && (
            <div className="card" style={{ marginBottom: '0.75rem' }}>
              <form onSubmit={addMember} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <label style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', fontSize: '0.9rem', color: 'var(--muted)' }}>
                  Player User ID *
                  <input
                    value={memberForm.userId}
                    onChange={e => setMemberForm(p => ({ ...p, userId: e.target.value }))}
                    placeholder="Paste user ID"
                    required
                  />
                </label>
                <label style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', fontSize: '0.9rem', color: 'var(--muted)' }}>
                  Character ID (optional)
                  <input
                    value={memberForm.characterId}
                    onChange={e => setMemberForm(p => ({ ...p, characterId: e.target.value }))}
                    placeholder="Paste character ID"
                  />
                </label>
                {memberError && <p className="error">{memberError}</p>}
                <button className="btn-primary" type="submit" disabled={memberLoading}>
                  {memberLoading ? 'Adding…' : 'Add Member'}
                </button>
              </form>
            </div>
          )}

          <div className="card" style={{ padding: '0.5rem 0.75rem' }}>
            {/* Master row */}
            <div className="flex-between" style={{ padding: '0.5rem 0', borderBottom: '1px solid var(--border)' }}>
              <div className="flex">
                <span style={{ fontSize: '0.9rem' }}>{user?.id === campaign.masterId ? user.username : campaign.masterId}</span>
                <span className="badge badge-purple">DM</span>
              </div>
            </div>

            {members.length === 0 ? (
              <p className="muted" style={{ padding: '0.75rem 0', fontSize: '0.9rem' }}>No players yet.</p>
            ) : (
              members.map(m => (
                <div key={m.userId} className="flex-between" style={{ padding: '0.5rem 0', borderBottom: '1px solid var(--border)' }}>
                  <div className="flex">
                    <span style={{ fontSize: '0.9rem' }}>{m.userId}</span>
                    {m.characterId && <span className="badge">Has character</span>}
                  </div>
                  {isMaster && (
                    <button className="btn-ghost" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem' }}
                      onClick={() => removeMember(m.userId)}>
                      Remove
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
        </div>

        {/* ── Sessions ────────────────────────────────────────────── */}
        <div>
          <div className="flex-between" style={{ marginBottom: '0.75rem' }}>
            <h2>Sessions</h2>
            {isMaster && campaign.status === 'ACTIVE' && (
              <button className="btn-primary" style={{ fontSize: '0.85rem', padding: '0.35rem 0.75rem' }}
                onClick={() => setShowSessionForm(v => !v)}>
                {showSessionForm ? 'Cancel' : '+ New Session'}
              </button>
            )}
          </div>

          {showSessionForm && (
            <div className="card" style={{ marginBottom: '0.75rem' }}>
              <form onSubmit={createSession} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <label style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', fontSize: '0.9rem', color: 'var(--muted)' }}>
                  Session Date *
                  <input type="datetime-local" value={sessionDate}
                    onChange={e => setSessionDate(e.target.value)} required />
                </label>
                {sessionError && <p className="error">{sessionError}</p>}
                <button className="btn-primary" type="submit" disabled={sessionLoading}>
                  {sessionLoading ? 'Creating…' : 'Create Session'}
                </button>
              </form>
            </div>
          )}

          {sessions.length === 0 ? (
            <p className="muted">No sessions yet.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {sessions.map(session => (
                <div key={session.id} className="card">
                  <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                    <div className="flex">
                      <span style={{ fontWeight: 600 }}>
                        {new Date(session.date).toLocaleDateString('en-GB', {
                          day: 'numeric', month: 'short', year: 'numeric'
                        })}
                      </span>
                      <span className={`badge ${statusBadgeClass(session.status)}`}>{session.status}</span>
                    </div>

                    {isMaster && campaign.status === 'ACTIVE' && (
                      <select
                        value={session.status}
                        onChange={e => updateStatus(session.id, e.target.value)}
                        style={{ width: 'auto', fontSize: '0.8rem', padding: '0.2rem 0.4rem' }}
                      >
                        {SESSION_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                      </select>
                    )}
                  </div>

                  {/* Notes */}
                  {editingNotes === session.id ? (
                    <div style={{ marginTop: '0.5rem' }}>
                      <textarea
                        value={notesValue}
                        onChange={e => setNotesValue(e.target.value)}
                        rows={4}
                        style={{ marginBottom: '0.5rem' }}
                        placeholder="Session notes…"
                      />
                      <div className="flex">
                        <button className="btn-primary" style={{ fontSize: '0.85rem' }}
                          onClick={() => saveNotes(session.id)} disabled={notesLoading}>
                          {notesLoading ? '…' : 'Save'}
                        </button>
                        <button className="btn-ghost" style={{ fontSize: '0.85rem' }}
                          onClick={() => setEditingNotes(null)}>
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div style={{ marginTop: '0.25rem' }}>
                      {session.rawNotes ? (
                        <p style={{ fontSize: '0.88rem', opacity: 0.8, whiteSpace: 'pre-wrap' }}>{session.rawNotes}</p>
                      ) : (
                        <p className="muted" style={{ fontSize: '0.85rem' }}>No notes yet.</p>
                      )}
                      {isMaster && (
                        <button className="btn-ghost" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', marginTop: '0.5rem' }}
                          onClick={() => { setEditingNotes(session.id); setNotesValue(session.rawNotes || '') }}>
                          {session.rawNotes ? 'Edit notes' : 'Add notes'}
                        </button>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
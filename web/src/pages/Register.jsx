import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../store/AuthContext.jsx'

export default function Register() {
  const { login }   = useAuth()
  const navigate    = useNavigate()

  const [form, setForm]       = useState({ username: '', email: '', password: '', role: 'PLAYER' })
  const [error, setError]     = useState(null)
  const [loading, setLoading] = useState(false)

  const set = (field) => (e) => setForm(f => ({ ...f, [field]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setError(null)
    if (form.password.length < 6) return setError('Password must be at least 6 characters')
    setLoading(true)
    try {
      const res = await api.auth.register(form)
      login(res.token, { id: res.userId, username: res.username, role: res.role })
      navigate('/dashboard')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div className="card" style={{ width: '100%', maxWidth: 400 }}>
        <div style={{ marginBottom: '1.5rem', textAlign: 'center' }}>
          <h1 style={{ color: 'var(--primary)', marginBottom: '0.25rem' }}>RemVault</h1>
          <p className="muted">Create your account</p>
        </div>

        <form className="form" onSubmit={submit}>
          <label>
            Username
            <input value={form.username} onChange={set('username')} required />
          </label>
          <label>
            Email
            <input type="email" value={form.email} onChange={set('email')} required />
          </label>
          <label>
            Password
            <input type="password" value={form.password} onChange={set('password')} required />
          </label>
          <label>
            Role
            <select value={form.role} onChange={set('role')}>
              <option value="PLAYER">Player</option>
              <option value="MASTER">Dungeon Master</option>
            </select>
          </label>

          {error && <p className="error">{error}</p>}

          <button className="btn-primary" type="submit" disabled={loading}>
            {loading ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="muted" style={{ marginTop: '1rem', textAlign: 'center' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--primary)' }}>Sign in</Link>
        </p>
      </div>
    </div>
  )
}
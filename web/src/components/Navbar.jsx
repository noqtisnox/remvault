import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../store/AuthContext.jsx'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav style={{
      background: 'var(--surface)',
      borderBottom: '1px solid var(--border)',
      padding: '0.75rem 1.5rem',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      position: 'sticky',
      top: 0,
      zIndex: 100
    }}>
      <Link to="/dashboard" style={{ color: 'var(--primary)', fontWeight: 700, fontSize: '1.1rem', textDecoration: 'none' }}>
        RemVault
      </Link>

      <div className="flex">
        <span className="muted" style={{ fontSize: '0.9rem' }}>
          {user?.username}
        </span>
        <span className={`badge ${user?.role === 'MASTER' ? 'badge-purple' : 'badge-green'}`}>
          {user?.role === 'MASTER' ? 'DM' : 'Player'}
        </span>
        <button className="btn-ghost" style={{ padding: '0.35rem 0.75rem', fontSize: '0.85rem' }} onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  )
}
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/use-auth'

function getInitials(user) {
  const firstName = user?.firstName ?? ''
  const lastName = user?.lastName ?? ''
  if (firstName || lastName) {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
  }
  const identifier = user?.identifier ?? '?'
  return identifier.charAt(0).toUpperCase()
}

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <NavLink to="/contacts" className="navbar-brand">
          <span className="auth-brand-mark" aria-hidden="true">
            C
          </span>
          <span>Contacts</span>
        </NavLink>

        <nav className="navbar-links" aria-label="Main navigation">
          <NavLink
            to="/contacts"
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            Contacts
          </NavLink>
          <NavLink
            to="/profile"
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            Profile
          </NavLink>
        </nav>

        <div className="navbar-actions">
          <span className="badge" title="Signed in account">
            <span className="badge-dot" aria-hidden="true" />
            <span className="avatar-init" aria-hidden="true">
              {getInitials(user)}
            </span>
            {user?.identifier}
          </span>
          <button type="button" className="btn btn-ghost" onClick={handleLogout}>
            Log out
          </button>
        </div>
      </div>
    </header>
  )
}
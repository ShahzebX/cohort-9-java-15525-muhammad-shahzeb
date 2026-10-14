import { useCallback, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/use-auth'
import { useToast } from '../context/use-toast'
import ChangePasswordModal from '../components/ChangePasswordModal'

function getInitials(user) {
  const firstName = user?.firstName ?? ''
  const lastName = user?.lastName ?? ''
  if (firstName || lastName) {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
  }
  return (user?.identifier ?? '?').charAt(0).toUpperCase()
}

function DetailRow({ label, value }) {
  const hasValue = Boolean(value && value.trim())
  return (
    <div className="profile-row">
      <span className="profile-row-label">{label}</span>
      <span className={`profile-row-value${hasValue ? '' : ' profile-row-value-empty'}`}>
        {hasValue ? value : 'Not provided'}
      </span>
    </div>
  )
}

export default function ProfilePage() {
  const { user, logout } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const [changeOpen, setChangeOpen] = useState(false)

  function handlePasswordChanged() {
    setChangeOpen(false)
    toast.success('Your password has been updated.')
  }

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  // Stable identity so ChangePasswordModal's focus/close effect does not re-run
  // while the modal is open (the modal effect depends on `onClose`).
  const handleCloseChangePassword = useCallback(() => setChangeOpen(false), [])

  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ')

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Your profile</h1>
          <p className="page-desc">Manage your account details and security.</p>
        </div>
      </div>

      <div className="profile-grid">
        <section className="card" aria-label="Account details">
          <div className="card-header">
            <h2 className="card-title">Account details</h2>
          </div>
          <div className="card-body">
            <DetailRow label="Full name" value={fullName} />
            <DetailRow label="Email" value={user?.email} />
            <DetailRow label="Phone number" value={user?.phone} />
            <DetailRow label="Signed in as" value={user?.identifier} />
          </div>
        </section>

        <section className="card account-card" aria-label="Account actions">
          <div className="account-meta">
            <span className="avatar" aria-hidden="true">
              {getInitials(user)}
            </span>
            <div>
              <strong>{fullName || user?.identifier || 'Account'}</strong>
            </div>
            <div className="text-muted mono">{user?.identifier}</div>
          </div>
          <div className="account-actions">
            <button type="button" className="btn btn-primary btn-block" onClick={() => setChangeOpen(true)}>
              Change password
            </button>
            <button type="button" className="btn btn-ghost btn-block" onClick={handleLogout}>
              Log out
            </button>
          </div>
        </section>
      </div>

      <ChangePasswordModal open={changeOpen} onClose={handleCloseChangePassword} onSuccess={handlePasswordChanged} />
    </>
  )
}
import { useEffect, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/use-auth'
import { useToast } from '../context/use-toast'
import { getApiError } from '../lib/errors'
import { validateLogin } from '../lib/validators'
import TextField from '../components/TextField'

export default function LoginPage() {
  const { login } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const location = useLocation()

  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const identifierRef = useRef(null)

  const from = location.state?.from?.pathname || '/contacts'

  useEffect(() => {
    identifierRef.current?.focus()
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    const validation = validateLogin({ identifier, password })
    setErrors(validation.errors)
    if (!validation.valid) return

    setSubmitting(true)
    try {
      await login({ identifier: identifier.trim(), password })
      toast.success('Signed in.')
      navigate(from, { replace: true })
    } catch (error) {
      toast.error(getApiError(error))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <span className="auth-brand-mark" aria-hidden="true">
            C
          </span>
          <span className="auth-brand-name">Contact Management System</span>
        </div>

        <h1 className="auth-title">Sign in</h1>
        <p className="auth-subtitle">Use your email or phone number to continue.</p>

        <form className="form" onSubmit={handleSubmit} noValidate>
          <TextField
            label="Email or phone"
            type="text"
            autoComplete="username"
            placeholder="you@example.com or +1 555 123 4567"
            value={identifier}
            onChange={(event) => setIdentifier(event.target.value)}
            error={errors.identifier}
            inputRef={identifierRef}
          />
          <TextField
            label="Password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            error={errors.password}
          />

          <div className="form-actions">
            <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
              {submitting && <span className="spinner" aria-hidden="true" />}
              {submitting ? 'Signing in\u2026' : 'Sign in'}
            </button>
          </div>
        </form>

        <p className="auth-alt">
          New here? <Link to="/register">Create an account</Link>
        </p>
      </div>
    </div>
  )
}
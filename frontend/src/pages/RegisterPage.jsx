import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/use-auth'
import { getApiError } from '../lib/errors'
import { validateRegistration } from '../lib/validators'
import TextField from '../components/TextField'
import Alert from '../components/Alert'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const firstNameRef = useRef(null)

  useEffect(() => {
    firstNameRef.current?.focus()
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    const payload = {
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      email: email.trim() || null,
      phone: phone.trim() || null,
      password,
      confirmPassword,
    }
    const validation = validateRegistration(payload)
    setErrors(validation.errors)
    setFormError(null)
    if (!validation.valid) return

    setSubmitting(true)
    try {
      await register(payload)
      navigate('/contacts', { replace: true })
    } catch (error) {
      setFormError(getApiError(error))
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

        <h1 className="auth-title">Create your account</h1>
        <p className="auth-subtitle">Register with an email, a phone number, or both.</p>

        <form className="form" onSubmit={handleSubmit} noValidate>
          {formError && <Alert variant="error" className="form-error">{formError}</Alert>}
          <TextField
            label="First name"
            type="text"
            autoComplete="given-name"
            value={firstName}
            onChange={(event) => setFirstName(event.target.value)}
            error={errors.firstName}
            inputRef={firstNameRef}
          />
          <TextField
            label="Last name"
            type="text"
            autoComplete="family-name"
            value={lastName}
            onChange={(event) => setLastName(event.target.value)}
            error={errors.lastName}
          />
          <TextField
            label="Email"
            type="email"
            autoComplete="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            error={errors.email}
            hint="Optional if you provide a phone number."
          />
          <TextField
            label="Phone number"
            type="tel"
            autoComplete="tel"
            placeholder="+1 555 123 4567"
            value={phone}
            onChange={(event) => setPhone(event.target.value)}
            error={errors.phone}
            hint="Optional if you provide an email."
          />
          <TextField
            label="Password"
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            error={errors.password}
            hint="At least 8 characters, with letters and numbers."
          />
          <TextField
            label="Confirm password"
            type="password"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            error={errors.confirmPassword}
          />

          <div className="form-actions">
            <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
              {submitting && <span className="spinner" aria-hidden="true" />}
              {submitting ? 'Creating account\u2026' : 'Create account'}
            </button>
          </div>
        </form>

        <p className="auth-alt">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
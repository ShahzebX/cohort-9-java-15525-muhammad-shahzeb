const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export const MIN_PASSWORD_LENGTH = 8

export function isValidEmail(value) {
  return typeof value === 'string' && EMAIL_RE.test(value.trim())
}

export function isValidPhone(value) {
  if (typeof value !== 'string') return false
  const trimmed = value.trim()
  if (!trimmed) return false
  if (!/^[+\d\s().-]+$/.test(trimmed)) return false
  const digits = trimmed.replace(/\D/g, '')
  return digits.length >= 7 && digits.length <= 15
}

export function hasPasswordDigits(value) {
  return /\d/.test(value)
}

export function hasPasswordLetters(value) {
  return /[a-zA-Z]/.test(value)
}

export function isStrongPassword(value) {
  return (
    typeof value === 'string' &&
    value.length >= MIN_PASSWORD_LENGTH &&
    hasPasswordLetters(value) &&
    hasPasswordDigits(value)
  )
}

export function identifierLooksLikeEmail(value) {
  return typeof value === 'string' && value.includes('@')
}

export function validateLogin(values) {
  const errors = {}

  const identifier = (values.identifier ?? '').trim()
  if (!identifier) {
    errors.identifier = 'Enter your email or phone number.'
  } else if (identifierLooksLikeEmail(identifier) && !isValidEmail(identifier)) {
    errors.identifier = 'Enter a valid email address.'
  }

  if (!values.password) {
    errors.password = 'Enter your password.'
  }

  return { errors, valid: Object.keys(errors).length === 0 }
}

export function validateRegistration(values) {
  const errors = {}

  const email = (values.email ?? '').trim()
  const phone = (values.phone ?? '').trim()

  if (email && !isValidEmail(email)) {
    errors.email = 'Enter a valid email address.'
  }
  if (phone && !isValidPhone(phone)) {
    errors.phone = 'Enter a valid phone number (7 to 15 digits).'
  }
  if (!email && !phone) {
    errors.email = 'Provide an email, a phone number, or both.'
  }

  if (!(values.firstName ?? '').trim()) {
    errors.firstName = 'Enter a first name.'
  }

  const password = values.password ?? ''
  if (!password) {
    errors.password = 'Enter a password.'
  } else if (!isStrongPassword(password)) {
    errors.password = `Use at least ${MIN_PASSWORD_LENGTH} characters with letters and numbers.`
  }

  if (values.confirmPassword !== password) {
    errors.confirmPassword = 'Passwords do not match.'
  }

  return { errors, valid: Object.keys(errors).length === 0 }
}

export function validateChangePassword(values) {
  const errors = {}

  if (!values.oldPassword) {
    errors.oldPassword = 'Enter your current password.'
  }

  const newPassword = values.newPassword ?? ''
  if (!newPassword) {
    errors.newPassword = 'Enter a new password.'
  } else if (!isStrongPassword(newPassword)) {
    errors.newPassword = `Use at least ${MIN_PASSWORD_LENGTH} characters with letters and numbers.`
  } else if (newPassword === values.oldPassword) {
    errors.newPassword = 'New password must be different from the current one.'
  }

  if (values.confirmPassword !== newPassword) {
    errors.confirmPassword = 'Passwords do not match.'
  }

  return { errors, valid: Object.keys(errors).length === 0 }
}
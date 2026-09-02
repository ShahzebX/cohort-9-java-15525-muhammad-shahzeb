const USER_KEY = 'cms.user'

// ---------------------------------------------------------------------------
// In-memory token slot.
// The JWT returned by the server lives here only — never in localStorage.
// It is lost on tab close, which is the desired security posture for a bearer
// token that cannot be revoked before expiry.
// ---------------------------------------------------------------------------
let _memoryToken = null

/** Store the bearer token in memory only. */
export function setMemoryToken(token) {
  _memoryToken = token ?? null
}

/** Return the current in-memory bearer token, or null when not authenticated. */
export function getMemoryToken() {
  return _memoryToken
}

export function getToken() {
  // The presence of the non-sensitive session marker below
  // reflects an active session; the actual JWT is held in _memoryToken.
  return hasStoredUser() ? 'active' : null
}

function hasStoredUser() {
  try {
    return localStorage.getItem(USER_KEY) != null
  } catch {
    return false
  }
}

export function getStoredUser() {
  let raw
  try {
    raw = localStorage.getItem(USER_KEY)
  } catch {
    return null
  }
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function setSession(token, user) {
  // Persist only the non-sensitive user profile for UI state.
  // The bearer token is kept in _memoryToken via setMemoryToken().
  try {
    localStorage.setItem(USER_KEY, JSON.stringify(user ?? null))
  } catch {
    /* ignore */
  }
}

export function clearSession() {
  _memoryToken = null
  try {
    localStorage.removeItem(USER_KEY)
  } catch {
    /* ignore */
  }
}

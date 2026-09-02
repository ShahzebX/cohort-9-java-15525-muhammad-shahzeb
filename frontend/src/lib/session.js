const USER_KEY = 'cms.user'

export function getToken() {
  // The bearer token is held in a server-set HttpOnly, Secure, SameSite cookie
  // which JavaScript cannot read; it is sent automatically on each request via
  // withCredentials. The presence of the non-sensitive session marker below
  // reflects an active session.
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
  // The token itself lives in the server-side HttpOnly cookie; only
  // non-sensitive UI state is persisted in browser storage.
  try {
    localStorage.setItem(USER_KEY, JSON.stringify(user ?? null))
  } catch {
    /* ignore */
  }
}

export function clearSession() {
  try {
    localStorage.removeItem(USER_KEY)
  } catch {
    /* ignore */
  }
}

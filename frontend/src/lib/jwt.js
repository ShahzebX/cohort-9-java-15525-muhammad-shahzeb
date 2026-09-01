export function getTokenExpiry(token) {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = JSON.parse(atob(normalized))
    return typeof decoded.exp === 'number' ? decoded.exp * 1000 : null
  } catch {
    return null
  }
}

export function isTokenExpired(token) {
  if (!token) return true
  const expiry = getTokenExpiry(token)
  if (expiry === null) return false
  return expiry <= Date.now()
}
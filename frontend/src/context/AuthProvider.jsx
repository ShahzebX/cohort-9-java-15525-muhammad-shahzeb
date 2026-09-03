import { useCallback, useEffect, useMemo, useState } from 'react'
import { loginUser, registerUser } from '../api/auth'
import { setUnauthorizedHandler } from '../api/client'
import { AuthContext } from './auth-context'
import { clearSession, getMemoryToken, getStoredUser, setMemoryToken, setSession } from '../lib/session'

function buildUser(identifier, extra = {}) {
  const isEmail = typeof identifier === 'string' && identifier.includes('@')
  return {
    identifier,
    email: extra.email ?? (isEmail ? identifier : null),
    phone: extra.phone ?? (isEmail ? null : identifier),
    firstName: extra.firstName ?? null,
    lastName: extra.lastName ?? null,
  }
}

export function AuthProvider({ children }) {
  // Initialize from the in-memory token only — it is null on every fresh page
  // load.  Using a localStorage marker here produced a false authenticated state
  // after reload: isAuthenticated was true but no bearer token existed, so every
  // subsequent API call would fail with 401.
  const [token, setToken] = useState(() => getMemoryToken())
  const [user, setUser] = useState(() => getStoredUser())

  const saveSession = useCallback((nextToken, nextUser) => {
    // The JWT is delivered as an HttpOnly cookie by the server; the token
    // argument is unused for storage. Only non-sensitive UI state is kept.
    setSession(nextToken, nextUser)
    setToken(getToken())
    setUser(nextUser)
  }, [])

  const logout = useCallback(() => {
    clearSession()
    setToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
    return () => setUnauthorizedHandler(null)
  }, [logout])

  const login = useCallback(
    async (credentials) => {
      const data = await loginUser(credentials)
      const nextUser = buildUser(data.identifier, data)
      setMemoryToken(data.token)
      saveSession(data.token, nextUser)
      return data
    },
    [saveSession]
  )

  const register = useCallback(
    async (payload) => {
      const data = await registerUser(payload)
      const nextUser = buildUser(data.identifier, payload)
      setMemoryToken(data.token)
      saveSession(data.token, nextUser)
      return data
    },
    [saveSession]
  )

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [token, user, login, register, logout]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
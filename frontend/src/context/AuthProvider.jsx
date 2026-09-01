import { useCallback, useEffect, useMemo, useState } from 'react'
import { loginUser, registerUser } from '../api/auth'
import { setUnauthorizedHandler } from '../api/client'
import { AuthContext } from './auth-context'
import { clearSession, getStoredUser, getToken, setSession } from '../lib/session'
import { isTokenExpired } from '../lib/jwt'

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
  const [token, setToken] = useState(() => getToken())
  const [user, setUser] = useState(() => {
    const storedToken = getToken()
    if (storedToken && isTokenExpired(storedToken)) {
      clearSession()
      return null
    }
    return getStoredUser()
  })

  const saveSession = useCallback((nextToken, nextUser) => {
    setSession(nextToken, nextUser)
    setToken(nextToken)
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
      saveSession(data.token, nextUser)
      return data
    },
    [saveSession]
  )

  const register = useCallback(
    async (payload) => {
      const data = await registerUser(payload)
      const nextUser = buildUser(data.identifier, payload)
      saveSession(data.token, nextUser)
      return data
    },
    [saveSession]
  )

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token) && !isTokenExpired(token),
      login,
      register,
      logout,
    }),
    [token, user, login, register, logout]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
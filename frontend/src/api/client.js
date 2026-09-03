import axios from 'axios'
import { clearSession, getMemoryToken } from '../lib/session'

const configuredBase = import.meta.env.VITE_API_BASE

let baseURL = configuredBase || '/api'
if (configuredBase) {
  let parsedBase
  try {
    parsedBase = new URL(configuredBase, window.location.origin)
  } catch {
    throw new Error('VITE_API_BASE is not a valid URL. Check your environment configuration.')
  }
  if (parsedBase.protocol === 'http:') {
    throw new Error('VITE_API_BASE must use https: (insecure http base disallowed)')
  }
} else {
  // No explicit base configured — we fall back to the relative /api path.
  // In development the Vite proxy forwards /api to localhost:8080, which is
  // acceptable only when the dev server itself is localhost.
  // In production the browser resolves /api against the page origin; if that
  // origin is plain HTTP (non-localhost) bearer tokens would travel in the
  // clear, so we reject the configuration early.
  const isLocalhost =
    window.location.hostname === 'localhost' ||
    window.location.hostname === '127.0.0.1' ||
    window.location.hostname === '[::1]'
  if (window.location.protocol === 'http:' && !isLocalhost) {
    throw new Error(
      'Credentialed API requests require HTTPS in production. ' +
      'Set VITE_API_BASE to an https:// URL or serve the app over HTTPS.'
    )
  }
}

const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

let onUnauthorized = null

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

// Attach the in-memory bearer token (set after login/register) to every request.
api.interceptors.request.use((config) => {
  const token = getMemoryToken()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const url = error?.config?.url || ''
    const isAuthAttempt = url.includes('/auth/login') || url.includes('/auth/register')
    if ((status === 401 || status === 403) && !isAuthAttempt) {
      clearSession()
      if (onUnauthorized) {
        onUnauthorized()
      } else {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default api
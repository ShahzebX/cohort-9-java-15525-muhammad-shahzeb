import axios from 'axios'
import { clearSession } from '../lib/session'

const configuredBase = import.meta.env.VITE_API_BASE

let baseURL = configuredBase || '/api'
if (configuredBase) {
  const { protocol } = new URL(configuredBase, window.location.origin)
  if (protocol === 'http:') {
    throw new Error('VITE_API_BASE must use https: (insecure http base disallowed)')
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
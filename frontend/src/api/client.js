import axios from 'axios'
import { getToken, clearSession } from '../lib/session'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

let onUnauthorized = null

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const hadToken = Boolean(error?.config?.headers?.Authorization)
    if (status === 401 && hadToken) {
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
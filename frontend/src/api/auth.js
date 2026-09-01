import api from './client'

export async function registerUser(payload) {
  const response = await api.post('/auth/register', payload)
  return response.data
}

export async function loginUser({ identifier, password }) {
  const response = await api.post('/auth/login', { identifier, password })
  return response.data
}

export async function changePassword({ oldPassword, newPassword }) {
  const response = await api.post('/auth/change-password', { oldPassword, newPassword })
  return response.data
}
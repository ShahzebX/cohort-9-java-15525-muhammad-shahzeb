import api from './client'

export async function getContacts() {
  const response = await api.get('/contacts')
  return response.data
}
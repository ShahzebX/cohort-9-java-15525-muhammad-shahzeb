import api from './client'

export async function getContacts() {
  const response = await api.get('/contacts')
  return response.data
}

export async function getContactsPage(page = 0, size = 10) {
  const response = await api.get('/contacts/paginated', {
    params: { page, size },
  })
  return response.data
}
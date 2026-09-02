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

export async function searchContacts(query) {
  const response = await api.get('/contacts/search', {
    params: { query },
  })
  return response.data
}

export async function createContact(contact) {
  const response = await api.post('/contacts', contact)
  return response.data
}

export async function updateContact(id, contact) {
  const response = await api.put(`/contacts/${id}`, contact)
  return response.data
}

export async function deleteContact(id) {
  await api.delete(`/contacts/${id}`)
}
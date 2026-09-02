import { useEffect, useState } from 'react'
import { getContacts } from '../api/contacts'
import Alert from '../components/Alert'
import { getApiError } from '../lib/errors'

function ContactRow({ contact }) {
  const fullName =
    [contact.firstName, contact.lastName].filter(Boolean).join(' ') || 'Unnamed contact'
  const emails = contact.emails ?? []
  const phones = contact.phones ?? []
  const hasDetails = emails.length > 0 || phones.length > 0

  return (
    <li className="contact-row">
      <div className="contact-row-main">
        <span className="contact-name">{fullName}</span>
        {contact.title && <span className="contact-title">{contact.title}</span>}
      </div>
      {hasDetails && (
        <div className="contact-details">
          {emails.map((email) => (
            <span key={email.id} className="contact-detail">
              {email.label ? `${email.label}: ` : ''}
              {email.email}
            </span>
          ))}
          {phones.map((phone) => (
            <span key={phone.id} className="contact-detail">
              {phone.label ? `${phone.label}: ` : ''}
              {phone.phoneNumber}
            </span>
          ))}
        </div>
      )}
    </li>
  )
}

export default function ContactsPage() {
  const [contacts, setContacts] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [reloadToken, setReloadToken] = useState(0)

  useEffect(() => {
    let active = true

    async function load() {
      try {
        const data = await getContacts()
        if (!active) return
        setContacts(data)
        setError(null)
      } catch (err) {
        if (!active) return
        setError(getApiError(err))
      } finally {
        if (active) setLoading(false)
      }
    }

    load()
    return () => {
      active = false
    }
  }, [reloadToken])

  function handleRetry() {
    setError(null)
    setLoading(true)
    setReloadToken((value) => value + 1)
  }

  const hasContacts = Array.isArray(contacts) && contacts.length > 0

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Contacts</h1>
          <p className="page-desc">Browse all your saved contacts.</p>
        </div>
        {Array.isArray(contacts) && (
          <span className="badge">
            {contacts.length} {contacts.length === 1 ? 'contact' : 'contacts'}
          </span>
        )}
      </div>

      {error && (
        <div className="banner-wrap">
          <Alert variant="error">{error}</Alert>
          <button type="button" className="btn btn-ghost retry-btn" onClick={handleRetry}>
            Try again
          </button>
        </div>
      )}

      {loading ? (
        <div className="loading-state" role="status">
          <span className="spinner" aria-hidden="true" />
          <span>Loading contacts…</span>
        </div>
      ) : error ? null : hasContacts ? (
        <ul className="card contact-list">
          {contacts.map((contact) => (
            <ContactRow key={contact.id} contact={contact} />
          ))}
        </ul>
      ) : (
        <div className="empty-state">
          <span className="empty-state-icon" aria-hidden="true">
            +
          </span>
          <h2 className="empty-state-title">No contacts yet</h2>
          <p className="empty-state-text">
            Contacts you add will appear here. The create form is coming in the next iteration.
          </p>
        </div>
      )}
    </>
  )
}
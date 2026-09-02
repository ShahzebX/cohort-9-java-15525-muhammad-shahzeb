import { useEffect, useState } from 'react'
import { getContactsPage } from '../api/contacts'
import Alert from '../components/Alert'
import { getApiError } from '../lib/errors'

const PAGE_SIZE = 10

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
  const [pageData, setPageData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [page, setPage] = useState(0)
  const [reloadToken, setReloadToken] = useState(0)

  useEffect(() => {
    let active = true

    async function load() {
      setLoading(true)
      try {
        const data = await getContactsPage(page, PAGE_SIZE)
        if (!active) return
        setPageData(data)
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
  }, [page, reloadToken])

  function handleRetry() {
    setError(null)
    setReloadToken((value) => value + 1)
  }

  function goTo(nextPage) {
    if (nextPage < 0 || nextPage >= (pageData?.totalPages ?? 0)) return
    setPage(nextPage)
  }

  const contacts = pageData?.content ?? []
  const hasContacts = contacts.length > 0
  const currentPage = pageData?.totalPages > 0 ? pageData.number + 1 : 0
  const isFirst = pageData?.first ?? true
  const isLast = pageData?.last ?? true

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Contacts</h1>
          <p className="page-desc">Browse all your saved contacts.</p>
        </div>
        {pageData && (
          <span className="badge">
            {pageData.totalElements}{' '}
            {pageData.totalElements === 1 ? 'contact' : 'contacts'}
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
        <>
          <ul className="card contact-list">
            {contacts.map((contact) => (
              <ContactRow key={contact.id} contact={contact} />
            ))}
          </ul>

          <nav className="pagination" aria-label="Contact list pagination">
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => goTo(page - 1)}
              disabled={isFirst || pageData.totalPages <= 1}
            >
              Previous
            </button>
            <span className="pagination-status">
              Page {currentPage} of {pageData.totalPages}
            </span>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => goTo(page + 1)}
              disabled={isLast || pageData.totalPages <= 1}
            >
              Next
            </button>
          </nav>
        </>
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
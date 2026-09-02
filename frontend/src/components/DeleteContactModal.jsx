import { useEffect, useRef, useState } from 'react'
import { deleteContact } from '../api/contacts'
import { getApiError } from '../lib/errors'
import Alert from './Alert'

export default function DeleteContactModal({ open, contact = null, onClose, onDeleted }) {
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const cancelRef = useRef(null)

  useEffect(() => {
    if (!open) return undefined

    const previouslyFocused = document.activeElement
    const focusTimer = window.setTimeout(() => {
      setFormError(null)
      cancelRef.current?.focus()
    }, 10)

    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    document.addEventListener('keydown', handleKeyDown)

    return () => {
      window.clearTimeout(focusTimer)
      document.removeEventListener('keydown', handleKeyDown)
      if (previouslyFocused && typeof previouslyFocused.focus === 'function') {
        previouslyFocused.focus()
      }
    }
  }, [open, onClose])

  if (!open) return null

  async function handleDelete() {
    if (!contact) return
    setFormError(null)
    setSubmitting(true)
    try {
      await deleteContact(contact.id)
      onDeleted()
    } catch (error) {
      setFormError(getApiError(error))
    } finally {
      setSubmitting(false)
    }
  }

  const fullName =
    contact
      ? [contact.firstName, contact.lastName].filter(Boolean).join(' ') || 'this contact'
      : 'this contact'

  return (
    <div
      className="modal-overlay"
      onClick={(event) => {
        if (event.target === event.currentTarget && !submitting) onClose()
      }}
    >
      <div
        className="modal"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="delete-contact-title"
      >
        <div className="modal-header">
          <h2 className="modal-title" id="delete-contact-title">
            Delete contact
          </h2>
          <button
            type="button"
            className="modal-close"
            aria-label="Close"
            onClick={onClose}
            disabled={submitting}
          >
            &times;
          </button>
        </div>

        <div className="modal-body">
          {formError && <Alert variant="error">{formError}</Alert>}
          <p className="text-muted">
            Are you sure you want to delete “{fullName}”? This cannot be undone.
          </p>
        </div>

        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={onClose}
            disabled={submitting}
            ref={cancelRef}
          >
            Cancel
          </button>
          <button type="button" className="btn btn-danger" onClick={handleDelete} disabled={submitting}>
            {submitting ? 'Deleting\u2026' : 'Delete'}
          </button>
        </div>
      </div>
    </div>
  )
}

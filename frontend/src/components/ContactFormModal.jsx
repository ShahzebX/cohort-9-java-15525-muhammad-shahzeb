import { useEffect, useRef, useState } from 'react'
import { createContact, updateContact } from '../api/contacts'
import { getApiError } from '../lib/errors'
import { isValidEmail, isValidPhone } from '../lib/validators'
import TextField from './TextField'
import Alert from './Alert'

function emptyEmail() {
  return { email: '', label: '' }
}

function emptyPhone() {
  return { phoneNumber: '', label: '' }
}

function ContactFormModal({ open, mode = 'create', contact = null, onClose, onSaved }) {
  const [firstName, setFirstName] = useState(contact?.firstName ?? '')
  const [lastName, setLastName] = useState(contact?.lastName ?? '')
  const [title, setTitle] = useState(contact?.title ?? '')
  const [emails, setEmails] = useState(() =>
    (contact?.emails?.length ? contact.emails : [emptyEmail()]).map((email) => ({
      id: email.id,
      email: email.emailAddress ?? email.email ?? '',
      label: email.label ?? '',
    }))
  )
  const [phones, setPhones] = useState(() =>
    (contact?.phones?.length ? contact.phones : [emptyPhone()]).map((phone) => ({
      id: phone.id,
      phoneNumber: phone.phoneNumber ?? '',
      label: phone.label ?? '',
    }))
  )
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const submittingRef = useRef(false)
  const firstNameRef = useRef(null)

  useEffect(() => {
    if (!open) return undefined

    const previouslyFocused = document.activeElement
    const focusTimer = window.setTimeout(() => {
      firstNameRef.current?.focus()
    }, 10)

    function handleKeyDown(event) {
      if (event.key === 'Escape' && !submittingRef.current) {
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

  function updateEmail(index, patch) {
    setEmails((prev) => prev.map((item, i) => (i === index ? { ...item, ...patch } : item)))
  }

  function removeEmail(index) {
    setEmails((prev) =>
      prev.length === 1 ? [emptyEmail()] : prev.filter((_, i) => i !== index)
    )
  }

  function updatePhone(index, patch) {
    setPhones((prev) => prev.map((item, i) => (i === index ? { ...item, ...patch } : item)))
  }

  function removePhone(index) {
    setPhones((prev) =>
      prev.length === 1 ? [emptyPhone()] : prev.filter((_, i) => i !== index)
    )
  }

  function validate() {
    const nextErrors = {}

    if (!firstName.trim()) {
      nextErrors.firstName = 'Enter a first name.'
    }

    emails.forEach((item, index) => {
      const value = item.email.trim()
      if (value && !isValidEmail(value)) {
        nextErrors[`email-${index}`] = 'Enter a valid email address.'
      }
    })

    phones.forEach((item, index) => {
      const value = item.phoneNumber.trim()
      if (value && !isValidPhone(value)) {
        nextErrors[`phone-${index}`] = 'Enter a valid phone number (7 to 15 digits).'
      }
    })

    return nextErrors
  }

  function buildPayload() {
    return {
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      title: title.trim(),
      emails: emails
        .map(({ email, label }) => ({ email: email.trim(), label: label.trim() }))
        .filter((item) => item.email),
      phones: phones
        .map(({ phoneNumber, label }) => ({ phoneNumber: phoneNumber.trim(), label: label.trim() }))
        .filter((item) => item.phoneNumber),
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()

    const nextErrors = validate()
    setErrors(nextErrors)
    setFormError(null)
    if (Object.keys(nextErrors).length > 0) return

    const payload = buildPayload()
    setSubmitting(true)
    submittingRef.current = true
    try {
      if (mode === 'edit' && contact) {
        await updateContact(contact.id, payload)
      } else {
        await createContact(payload)
      }
      onSaved()
    } catch (error) {
      setFormError(getApiError(error))
    } finally {
      setSubmitting(false)
      submittingRef.current = false
    }
  }

  function handleClose() {
    if (submitting) return
    onClose()
  }

  const modalTitle = mode === 'edit' ? 'Update contact' : 'New contact'

  return (
    <div
      className="modal-overlay"
      onClick={(event) => {
        if (event.target === event.currentTarget) handleClose()
      }}
    >
      <div
        className="modal modal-wide"
        role="dialog"
        aria-modal="true"
        aria-labelledby="contact-form-title"
      >
        <div className="modal-header">
          <h2 className="modal-title" id="contact-form-title">
            {modalTitle}
          </h2>
          <button
            type="button"
            className="modal-close"
            aria-label="Close"
            onClick={handleClose}
            disabled={submitting}
          >
            &times;
          </button>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <div className="modal-body">
            {formError && <Alert variant="error">{formError}</Alert>}
            <div className={`modal-stack${formError ? ' modal-stack-with-error' : ''}`}>
              <TextField
                label="First name"
                value={firstName}
                onChange={(event) => setFirstName(event.target.value)}
                error={errors.firstName}
                inputRef={firstNameRef}
              />
              <TextField
                label="Last name"
                value={lastName}
                onChange={(event) => setLastName(event.target.value)}
                error={errors.lastName}
              />
              <TextField
                label="Title"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                error={errors.title}
              />

              <fieldset className="contact-subsection">
                <legend className="contact-subsection-title">Emails</legend>
                {emails.map((item, index) => (
                  <div className="contact-sub-row" key={index}>
                    <div className="contact-sub-fields">
                      <input
                        className="input"
                        type="text"
                        placeholder="Email address"
                        aria-label={`Email address ${index + 1}`}
                        value={item.email}
                        onChange={(event) => updateEmail(index, { email: event.target.value })}
                        aria-invalid={errors[`email-${index}`] ? 'true' : undefined}
                      />
                      <input
                        className="input"
                        type="text"
                        placeholder="Label (e.g. work)"
                        aria-label={`Email label ${index + 1}`}
                        value={item.label}
                        onChange={(event) => updateEmail(index, { label: event.target.value })}
                      />
                    </div>
                    <div className="contact-sub-actions">
                      <button
                        type="button"
                        className="btn btn-ghost btn-sm"
                        onClick={() => removeEmail(index)}
                      >
                        Remove
                      </button>
                    </div>
                    {errors[`email-${index}`] && (
                      <p className="field-error">{errors[`email-${index}`]}</p>
                    )}
                  </div>
                ))}
                <button
                  type="button"
                  className="btn btn-ghost btn-sm contact-add-btn"
                  onClick={() => setEmails((prev) => [...prev, emptyEmail()])}
                >
                  Add email
                </button>
              </fieldset>

              <fieldset className="contact-subsection">
                <legend className="contact-subsection-title">Phones</legend>
                {phones.map((item, index) => (
                  <div className="contact-sub-row" key={index}>
                    <div className="contact-sub-fields">
                      <input
                        className="input"
                        type="text"
                        placeholder="Phone number"
                        aria-label={`Phone number ${index + 1}`}
                        value={item.phoneNumber}
                        onChange={(event) => updatePhone(index, { phoneNumber: event.target.value })}
                        aria-invalid={errors[`phone-${index}`] ? 'true' : undefined}
                      />
                      <input
                        className="input"
                        type="text"
                        placeholder="Label (e.g. mobile)"
                        aria-label={`Phone label ${index + 1}`}
                        value={item.label}
                        onChange={(event) => updatePhone(index, { label: event.target.value })}
                      />
                    </div>
                    <div className="contact-sub-actions">
                      <button
                        type="button"
                        className="btn btn-ghost btn-sm"
                        onClick={() => removePhone(index)}
                      >
                        Remove
                      </button>
                    </div>
                    {errors[`phone-${index}`] && (
                      <p className="field-error">{errors[`phone-${index}`]}</p>
                    )}
                  </div>
                ))}
                <button
                  type="button"
                  className="btn btn-ghost btn-sm contact-add-btn"
                  onClick={() => setPhones((prev) => [...prev, emptyPhone()])}
                >
                  Add phone
                </button>
              </fieldset>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={handleClose} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving\u2026' : mode === 'edit' ? 'Save changes' : 'Create contact'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ContactFormModal

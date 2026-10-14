import { useCallback, useEffect, useRef, useState } from 'react'
import { changePassword } from '../api/auth'
import { useToast } from '../context/use-toast'
import { getApiError } from '../lib/errors'
import { validateChangePassword } from '../lib/validators'
import TextField from './TextField'

export default function ChangePasswordModal({ open, onClose, onSuccess }) {
  const toast = useToast()
  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const submittingRef = useRef(false)
  const oldPasswordRef = useRef(null)

  const resetFields = useCallback(() => {
    setOldPassword('')
    setNewPassword('')
    setConfirmPassword('')
    setErrors({})
  }, [])

  const handleClose = useCallback(() => {
    if (submittingRef.current) return
    resetFields()
    onClose()
  }, [resetFields, onClose])

  useEffect(() => {
    if (!open) return undefined

    const previouslyFocused = document.activeElement
    const focusTimer = window.setTimeout(() => {
      oldPasswordRef.current?.focus()
    }, 10)

    // Selector for all standard interactive elements that are not disabled.
    const FOCUSABLE = [
      'a[href]',
      'button:not([disabled])',
      'input:not([disabled])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])',
    ].join(',')

    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        handleClose()
        return
      }

      if (event.key === 'Tab') {
        // Re-query on every keydown so the list reflects current disabled state.
        const modalEl = document.querySelector('[role="dialog"][aria-modal="true"]')
        if (!modalEl) return

        const focusable = Array.from(modalEl.querySelectorAll(FOCUSABLE))
        if (focusable.length === 0) return

        const first = focusable[0]
        const last  = focusable[focusable.length - 1]

        if (event.shiftKey) {
          // Shift+Tab: if focus is on the first element, wrap to the last.
          if (document.activeElement === first) {
            event.preventDefault()
            last.focus()
          }
        } else {
          // Tab: if focus is on the last element, wrap to the first.
          if (document.activeElement === last) {
            event.preventDefault()
            first.focus()
          }
        }
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
  }, [open, handleClose])

  if (!open) return null

  async function handleSubmit(event) {
    event.preventDefault()
    const validation = validateChangePassword({ oldPassword, newPassword, confirmPassword })
    setErrors(validation.errors)
    if (!validation.valid) return

    setSubmitting(true)
    submittingRef.current = true
    try {
      await changePassword({ oldPassword, newPassword })
      resetFields()
      onSuccess()
    } catch (error) {
      toast.error(getApiError(error))
    } finally {
      setSubmitting(false)
      submittingRef.current = false
    }
  }

  return (
    <div
      className="modal-overlay"
      onClick={(event) => {
        if (event.target === event.currentTarget) handleClose()
      }}
    >
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="change-password-title"
      >
        <div className="modal-header">
          <h2 className="modal-title" id="change-password-title">
            Change password
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
            <div className="modal-stack">
              <TextField
                label="Current password"
                type="password"
                autoComplete="current-password"
                value={oldPassword}
                onChange={(event) => setOldPassword(event.target.value)}
                error={errors.oldPassword}
                inputRef={oldPasswordRef}
              />
              <TextField
                label="New password"
                type="password"
                autoComplete="new-password"
                value={newPassword}
                onChange={(event) => setNewPassword(event.target.value)}
                error={errors.newPassword}
                hint="At least 8 characters, with letters and numbers."
              />
              <TextField
                label="Confirm new password"
                type="password"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                error={errors.confirmPassword}
              />
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={handleClose} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Updating\u2026' : 'Update password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
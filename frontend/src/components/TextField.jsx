import { useId } from 'react'

export default function TextField({ label, hint, error, inputRef, ...inputProps }) {
  const autoId = useId()
  const id = inputProps.id ?? autoId
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined

  return (
    <div className="field">
      <label className="label" htmlFor={id}>
        {label}
      </label>
      <input
        ref={inputRef}
        id={id}
        className="input"
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={describedBy}
        {...inputProps}
      />
      {hint && !error && (
        <p className="hint" id={`${id}-hint`}>
          {hint}
        </p>
      )}
      {error && (
        <p className="field-error" id={`${id}-error`}>
          {error}
        </p>
      )}
    </div>
  )
}
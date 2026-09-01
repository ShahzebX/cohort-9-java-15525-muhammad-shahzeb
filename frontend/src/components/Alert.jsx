const ICONS = {
  error: '!',
  success: '\u2713',
  info: 'i',
}

export default function Alert({ variant = 'error', children, className = '' }) {
  if (!children) return null

  return (
    <div role={variant === 'error' ? 'alert' : 'status'} className={`alert alert-${variant} ${className}`}>
      <span className="alert-icon" aria-hidden="true">
        {ICONS[variant]}
      </span>
      <span>{children}</span>
    </div>
  )
}
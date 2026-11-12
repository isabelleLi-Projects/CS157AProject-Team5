// Small banner for ?message= / ?error= set by a JSP after a form post.
export default function FlashBanner() {
  const message = window.flashMessage ?? ''
  const error = window.flashError ?? ''
  if (!message && !error) return null

  return (
    <p
      role={error ? 'alert' : 'status'}
      className={`text-sm rounded-xl px-4 py-3 mt-4 ${error ? 'flash-error' : 'flash-success'}`}
    >
      {error || message}
    </p>
  )
}

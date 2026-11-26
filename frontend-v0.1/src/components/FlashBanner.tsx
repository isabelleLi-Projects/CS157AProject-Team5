import { useEffect, useState } from 'react'

export default function FlashBanner() {
  const [message] = useState((window.flashMessage ?? '').trim())
  const [error] = useState((window.flashError ?? '').trim())
  const [visible, setVisible] = useState(true)

  useEffect(() => {
    // Remove old messages from the URL.
    const url = new URL(window.location.href)
    url.searchParams.delete('message')
    url.searchParams.delete('error')
    window.history.replaceState(null, '', url.toString())

    window.flashMessage = ''
    window.flashError = ''

    // Hide the banner after 5 seconds.
    const timer = window.setTimeout(() => {
      setVisible(false)
    }, 5000)

    return () => window.clearTimeout(timer)
  }, [])
  if (!visible || (!message && !error)) return null
  return (
    <p
      role={error ? 'alert' : 'status'}
      className={`text-sm rounded-xl px-4 py-3 mt-4 ${error ? 'flash-error' : 'flash-success'}`}
    >
      {error || message}
    </p>
  )
}

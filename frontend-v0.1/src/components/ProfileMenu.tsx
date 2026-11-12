import { useEffect, useRef, useState } from 'react'
import EditProfile from './EditProfile'

// Round profile icon with the user's initials. Clicking it opens a menu with the
// user's name/email, Edit profile, and Log out. Used by both the student and admin headers.
export default function ProfileMenu() {
  const currentUser = window.currentUser
  const name = currentUser?.name || 'Account'
  const initials = name
    .split(/\s+/)
    .filter(Boolean)
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
  const [open, setOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function close(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', close)
    return () => document.removeEventListener('mousedown', close)
  }, [])

  // logout.jsp only accepts POST, so build a small form and submit it.
  const logout = () => {
    const form = document.createElement('form')
    form.method = 'post'
    form.action = `${window.studyFinderContext ?? ''}/logout.jsp`
    document.body.appendChild(form)
    form.submit()
  }

  return (
    <div ref={menuRef} className="relative flex items-center gap-3 text-sm">
      <span className="hidden text-secondary sm:inline">{name}</span>
      <button
        type="button"
        aria-label="Open profile menu"
        aria-expanded={open}
        className="primary-background flex h-10 w-10 items-center justify-center rounded-full font-semibold"
        onClick={() => setOpen(!open)}
      >
        {initials}
      </button>
      {open && (
        <div className="surface-background standard-border absolute right-0 top-12 z-20 w-56 rounded-xl p-2 shadow-lg">
          <p className="px-3 py-2 text-sm font-semibold text-main">{name}</p>
          <p className="px-3 pb-2 text-xs text-muted">{currentUser?.email ?? ''}</p>
          <button
            type="button"
            className="w-full rounded-lg px-3 py-2 text-left text-sm text-secondary hover:bg-[#f7f4ef]"
            onClick={() => {
              setEditOpen(true)
              setOpen(false)
            }}
          >
            Edit profile
          </button>
          <button
            type="button"
            className="w-full rounded-lg px-3 py-2 text-left text-sm text-secondary hover:bg-[#f7f4ef]"
            onClick={logout}
          >
            Log out
          </button>
        </div>
      )}
      {editOpen && <EditProfile onClose={() => setEditOpen(false)} />}
    </div>
  )
}

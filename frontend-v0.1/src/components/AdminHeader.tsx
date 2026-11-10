import type { RefObject } from 'react'
import spartan from '../assets/logo/spartan.png'

type AdminHeaderProps = {
  profileOpen: boolean
  setProfileOpen: (open: boolean) => void
  profileRef: RefObject<HTMLDivElement | null>
  onEditProfile: () => void
}

export default function AdminHeader({ profileOpen, setProfileOpen, profileRef, onEditProfile }: AdminHeaderProps) {
  const currentUser = window.currentUser
  const initials = (currentUser?.name ?? 'Administrator').split(/\s+/).map((part) => part[0]).join('').slice(0, 2).toUpperCase()

  return (
    <header className="surface-background standard-border border-x-0 border-t-0 px-5 py-3">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4">
        <div>
          <img src={spartan} alt="Spartan logo" className="logo-image" />
          <p className="font-display text-xl text-main">SJSU StudyFinder</p>
        </div>
        <div ref={profileRef} className="relative flex items-center gap-3 text-sm">
          <span className="hidden text-secondary sm:inline">{currentUser?.name ?? 'Administrator'}</span>
          <button aria-label="Open administrator profile" className="primary-background flex h-10 w-10 items-center justify-center rounded-full font-semibold" onClick={() => setProfileOpen(!profileOpen)}>{initials}</button>
          {profileOpen && <div className="surface-background standard-border absolute right-0 top-12 z-20 w-56 rounded-xl p-2 shadow-lg"><p className="px-3 py-2 text-sm font-semibold text-main">{currentUser?.name ?? 'Administrator'}</p><p className="px-3 pb-2 text-xs text-muted">{currentUser?.email ?? ''}</p><button className="w-full rounded-lg px-3 py-2 text-left text-sm text-secondary hover:bg-[#f7f4ef]" onClick={onEditProfile}>Edit profile</button><button className="w-full rounded-lg px-3 py-2 text-left text-sm text-secondary hover:bg-[#f7f4ef]" onClick={() => { window.location.href = `${window.studyFinderContext ?? ''}/login.jsp` }}>Log out</button></div>}
        </div>
      </div>
    </header>
  )
}

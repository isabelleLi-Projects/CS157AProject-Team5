import spartan from '../assets/logo/spartan.png'
import ProfileMenu from './ProfileMenu'

// Same look as the student Header (font, colors, height, sticky bar), with the profile menu on the right.
export default function AdminHeader() {
  return (
    <nav className="surface-background standard-border sticky top-0 border-x-0 border-t-0" style={{ zIndex: 1000 }}>
      <div className="px-6 h-14 flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <img src={spartan} alt="Spartan logo" className="logo-image" />
          <span className="font-display text-2xl text-primary">SJSU Study Finder</span>
        </div>

        <ProfileMenu />
      </div>
    </nav>
  )
}

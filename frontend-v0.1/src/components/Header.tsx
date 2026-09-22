import spartan from '../assets/logo/spartan.png'

type HeaderProps = {
  isLoggedIn: boolean
  activeNav: string
  onNavChange: (nav: string) => void
  onLoginToggle: () => void
}

const NAV_ITEMS = ['Home', 'Find Spots', 'Rewards']

export default function Header({
  isLoggedIn,
  activeNav,
  onNavChange,
  onLoginToggle,
}: HeaderProps) {
  return (
    <nav className="surface-background standard-border sticky top-0 z-50 border-x-0 border-t-0">
      <div className="px-6 h-14 flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <img src={spartan} alt="Spartan logo" className="logo-image" />
          <span className="font-display text-lg text-primary">SJSU Study Finder</span>
        </div>

        <div className="flex items-center gap-1">
          {NAV_ITEMS.map((link) => (
            <button
              key={link}
              onClick={() => onNavChange(link)}
              className={`px-6 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                activeNav === link
                  ? 'primary-background'
                  : 'text-secondary hover:bg-[#f7f4ef]'
              }`}
            >
              {link}
            </button>
          ))}
        </div>

        <button onClick={onLoginToggle} className="btn-link text-sm">
          {isLoggedIn ? 'Log out' : 'Log in'}
        </button>
      </div>
    </nav>
  )
}

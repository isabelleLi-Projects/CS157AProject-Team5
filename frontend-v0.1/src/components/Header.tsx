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
  const logout = () => {
    const form = document.createElement('form')
    form.method = 'post'
    form.action = `${window.studyFinderContext ?? ''}/logout.jsp`
    document.body.appendChild(form)
    form.submit()
  }
  return (
    <nav className="surface-background standard-border sticky top-0 border-x-0 border-t-0" style={{zIndex: 1000,}}>
      <div className="px-6 h-14 flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <img src={spartan} alt="Spartan logo" className="logo-image" />
          <span className="font-display text-2xl text-primary">SJSU Study Finder</span>
        </div>

        <div className="flex items-center gap-18">
          {NAV_ITEMS.map((link) => (
            <button
              key={link}
              onClick={() => onNavChange(link)}
              className={`px-6 py-1.5 rounded-lg text-lg font-medium transition-colors ${
                activeNav === link
                  ? 'primary-background'
                  : 'text-secondary hover:bg-[#f7f4ef]'
              }`}
            >
              {link}
            </button>
          ))}
        </div>

        <button onClick={isLoggedIn ? logout : onLoginToggle} className="btn-link text-base">
          {isLoggedIn ? 'Log out' : 'Log in'}
        </button>
      </div>
    </nav>
  )
}

import { useMemo, useState } from 'react'
import { FILTERS, RECENT_SPOTS, REWARDS, STUDY_SPOTS } from './data/studySpots'

import SpotCard from './components/SpotCard'
import Header from './components/Header'
import HeroSection from './components/HeroSection'
import LocationBanner from './components/LocationBanner'
import SearchBar from './components/SearchBar'
import QuickActions from './components/QuickActions'
import RewardsPreview from './components/RewardsPreview'
import SpotFilters from './components/SpotFilters'
import SpotSection from './components/SpotSection'

export default function MainLayout() {
  const [isLoggedIn, setIsLoggedIn] = useState(true)
  const [locationDismissed, setLocationDismissed] = useState(false)
  const [search, setSearch] = useState('')
  const [activeNav, setActiveNav] = useState('Home')

  const filteredSpots = useMemo(() => {
    const query = search.toLowerCase().trim()

    if (!query) {
      return STUDY_SPOTS
    }

    return STUDY_SPOTS.filter((spot) =>
      [spot.name, spot.location, ...spot.tags].some((value) =>
        value.toLowerCase().includes(query),
      ),
    )
  }, [search])

  const navigateTo = (nav: string) => {
    setActiveNav(nav)

    // Optional: scroll back to the top when changing pages
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
  }

  return (
    <div className="app-background min-h-screen">
      <Header
        isLoggedIn={isLoggedIn}
        activeNav={activeNav}
        onNavChange={navigateTo}
        onLoginToggle={() => setIsLoggedIn((value) => !value)}
      />

      <main className="px-6 pb-16">

        {/* =========================
            HOME PAGE
        ========================== */}
        {activeNav === 'Home' && (
          <>
            <HeroSection isLoggedIn={isLoggedIn} />

            {!locationDismissed && (
              <LocationBanner
                onDismiss={() => setLocationDismissed(true)}
              />
            )}

            <SearchBar
              search={search}
              onSearchChange={setSearch}
              onSearch={() => navigateTo('Find Spots')}
            />

            <QuickActions onNavigate={navigateTo} />

            {isLoggedIn && (
              <RewardsPreview
                points={340}
                rewards={REWARDS}
                onViewRewards={() => navigateTo('Rewards')}
              />
            )}

            {isLoggedIn && (
              <SpotSection
                title="Recently Visited"
                spots={RECENT_SPOTS}
                compact
                onViewAll={() => navigateTo('Find Spots')}
              />
            )}

            <section>
              <div className="flex items-center justify-between mb-3">
                <h2 className="font-display text-xl text-main">
                  {isLoggedIn
                    ? 'Recommended for You'
                    : 'Popular Spots'}
                </h2>

                <button
                  onClick={() => navigateTo('Find Spots')}
                  className="btn-link text-xs"
                >
                  View all →
                </button>
              </div>

              <SpotFilters filters={FILTERS} />

              <div className="grid grid-cols-1 gap-4">
                {filteredSpots.map((spot) => (
                  <SpotCard
                    key={spot.id}
                    spot={spot}
                  />
                ))}
              </div>
            </section>

            <div className="mt-10 text-center">
              <button
                onClick={() => navigateTo('Find Spots')}
                className="btn-primary inline-flex items-center gap-2 px-6 py-3 rounded-2xl text-sm shadow-md hover:shadow-lg"
              >
                🗺️ Explore All Study Spots
              </button>

              <p className="text-xs text-muted mt-2">
                {STUDY_SPOTS.length} spots available on campus
              </p>
            </div>
          </>
        )}


        {/* =========================
            FIND STUDY SPOTS PAGE
        ========================== */}
        {activeNav === 'Find Spots' && (
          <section className="pt-8">
            <div className="mb-6">
              <h1 className="font-display text-3xl text-main mb-2">
                Find Study Spots
              </h1>

              <p className="text-sm text-muted">
                Search and filter study spaces around the SJSU campus.
              </p>
            </div>

            {!locationDismissed && (
              <LocationBanner
                onDismiss={() => setLocationDismissed(true)}
              />
            )}

            <SearchBar
              search={search}
              onSearchChange={setSearch}
              onSearch={() => {}}
            />

            <div className="mt-5">
              <SpotFilters filters={FILTERS} />
            </div>

            <div className="mt-6 mb-3 flex items-center justify-between">
              <h2 className="font-display text-xl text-main">
                Available Study Spots
              </h2>

              <p className="text-xs text-muted">
                {filteredSpots.length} results
              </p>
            </div>

            <div className="grid grid-cols-1 gap-4">
              {filteredSpots.length > 0 ? (
                filteredSpots.map((spot) => (
                  <SpotCard
                    key={spot.id}
                    spot={spot}
                  />
                ))
              ) : (
                <div className="surface-background standard-border rounded-2xl p-8 text-center">
                  <p className="text-main font-medium">
                    No study spots found.
                  </p>

                  <p className="text-sm text-muted mt-1">
                    Try searching for another location.
                  </p>
                </div>
              )}
            </div>
          </section>
        )}


        {/* =========================
            REWARDS PAGE
        ========================== */}
        {activeNav === 'Rewards' && (
          <section className="pt-8">
            <div className="mb-6">
              <h1 className="font-display text-3xl text-main mb-2">
                Rewards
              </h1>

              <p className="text-sm text-muted">
                Earn points by helping other students and redeem
                them for rewards.
              </p>
            </div>

            {isLoggedIn ? (
              <>
                <div className="surface-background standard-border rounded-2xl p-6 mb-6">
                  <p className="text-sm text-muted">
                    Your Points
                  </p>

                  <p className="font-display text-4xl text-primary mt-1">
                    340
                  </p>

                  <p className="text-xs text-muted mt-2">
                    Submit reports and verify other students'
                    reports to earn more points.
                  </p>
                </div>

                <RewardsPreview
                  points={340}
                  rewards={REWARDS}
                  onViewRewards={() => {}}
                />
              </>
            ) : (
              <div className="surface-background standard-border rounded-2xl p-8 text-center">
                <h2 className="font-display text-xl text-main">
                  Log in to view your rewards
                </h2>

                <p className="text-sm text-muted mt-2">
                  Only registered SJSU students can earn and
                  redeem points.
                </p>
              </div>
            )}
          </section>
        )}

      </main>
    </div>
  )
}
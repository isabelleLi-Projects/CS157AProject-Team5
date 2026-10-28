import {useEffect, useMemo, useState } from 'react'
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
import MapView from './components/MapView'
import FlashBanner from './components/FlashBanner'
import { distanceInMiles } from './utils/distance'

type UserLocation = {
  lat: number
  lng: number
}
// Tabs that can be opened directly with study-spots.jsp?nav=... (used by the Study Spot page header).
const NAV_TABS = ['Home', 'Find Spots', 'Rewards']

export default function MainLayout() {
  const currentUser = window.currentUser
  const isGuest = currentUser?.role?.toLowerCase() == 'guest'
  const [isLoggedIn, setIsLoggedIn] = useState(Boolean(currentUser && !isGuest))
  const [userLocation, setUserLocation] = useState<UserLocation | null>(null)
  const [showLocationBanner, setShowLocationBanner] = useState(true)
  const [locationError, setLocationError] = useState('')
  // const [locationDismissed, setLocationDismissed] = useState(false)
  const [search, setSearch] = useState('')
  const [activeFilter, setActiveFilter] = useState('All')
  const [activeNav, setActiveNav] = useState(() => {
    const nav = new URLSearchParams(window.location.search).get('nav')
    return nav && NAV_TABS.includes(nav) ? nav : 'Home'
  })
  const requestUserLocation = () => {
    if (!navigator.geolocation) {
      setLocationError('Location is not supported by your browser.')
      return
    }
  
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setUserLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        })
  
        setLocationError('')
      },
      () => {
        setLocationError(
          'Please allow location access to use Near Me.'
        )
      }
    )
  }
  
  const handleFilterChange = (filter: string) => {
    setActiveFilter(filter)
  
    if (filter === 'Near Me' && !userLocation) {
      requestUserLocation()
    }
  }
  const filteredSpots = useMemo(() => {
    const query = search.toLowerCase().trim()

    return STUDY_SPOTS.filter((spot) => {
      const accessType = spot.accessType.toLowerCase().replace(/[\s-]+/g, '_')

    const canAccess = !isGuest || accessType === 'public_access'
      const matchesSearch = !query || [spot.name, spot.location, ...spot.tags].some((value) => value.toLowerCase().includes(query))
      const matchesFilter = 
      activeFilter === 'All' 
      || (activeFilter === 'Quiet' && spot.noiseLevel === 'low') 
      || (activeFilter === 'Open Now' && spot.open) 
      || (activeFilter === 'Outlets' && spot.outlets === 'Available') 
      || activeFilter === 'Near Me' &&
      userLocation !== null &&
      // user is within a 0.5 mile radius of the study spot
      distanceInMiles(userLocation.lat, userLocation.lng, spot.latitude, spot.longitude) <= 0.5
      return canAccess && matchesSearch && matchesFilter
    })
  }, [search, activeFilter, isGuest, userLocation])

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
        onLoginToggle={() => {
          window.location.href =`${window.studyFinderContext ?? ''}/login.jsp`
        }}
      />

      <main className="px-6 pb-16">
        <FlashBanner />

        {/* =========================
            HOME PAGE
        ========================== */}
        {activeNav === 'Home' && (
          <>
            <HeroSection isLoggedIn={isLoggedIn} />

            {showLocationBanner && (
              <LocationBanner
                onDismiss={() => setShowLocationBanner(false)}
                onLocationChange={setUserLocation}
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
                points={window.studentPoints ?? 0}
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

              <SpotFilters filters={FILTERS} activeFilter={activeFilter} onFilterChange={handleFilterChange} />

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

            {/* {!locationDismissed && (
              <LocationBanner
                onDismiss={() => setLocationDismissed(true)}
              />
            )} */}

            <SearchBar
              search={search}
              onSearchChange={setSearch}
              onSearch={() => {}}
            />

            <div className="mt-5">
            <SpotFilters filters={FILTERS} activeFilter={activeFilter} onFilterChange={handleFilterChange} />
            </div>

            <div className='mt-6'>
              <MapView spots={filteredSpots} userLocation={userLocation ?? undefined} activeFilter={activeFilter}/>
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
                    {window.studentPoints ?? 0}
                  </p>

                  <p className="text-xs text-muted mt-2">
                    Submit reports and verify other students'
                    reports to earn more points.
                  </p>
                </div>

                <RewardsPreview
                  points={window.studentPoints ?? 0}
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

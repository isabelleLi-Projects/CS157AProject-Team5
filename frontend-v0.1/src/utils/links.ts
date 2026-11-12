const context = () => window.studyFinderContext ?? ''

// The Study Spot page for one spot (study-spot.jsp).
export const spotDetailUrl = (spotId: number) => `${context()}/study-spot.jsp?id=${spotId}`

// The main page, opened on a given tab (Home, Find Spots, Rewards).
export const mainPageUrl = (nav?: string) =>
  `${context()}/study-spots.jsp${nav ? `?nav=${encodeURIComponent(nav)}` : ''}`

// Google Maps walking directions from the user's location. No API key needed.
export const walkingDirectionsUrl = (latitude: number, longitude: number) =>
  `https://www.google.com/maps/dir/?api=1&destination=${latitude},${longitude}&travelmode=walking`

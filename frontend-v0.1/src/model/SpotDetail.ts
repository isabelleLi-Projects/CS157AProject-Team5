// Shape of window.spotDetail, written by study-spot.jsp.

export interface SpotAmenity {
  name: string
  description: string
  notes: string
}

export interface SpotReport {
  reportId: number
  authorName: string
  noise: string
  crowdedness: string
  outlets: number | null
  smell: string
  // From reports.wifi_quality (added by sql/alter_reports_add_wifi.sql). Empty for older reports.
  wifi: string
  notes: string
  createdAt: string
  minutesAgo: number
  confirmed: number
  disputed: number
}

export interface SpotReview {
  firstName: string
  text: string
  createdAt: string
}

export interface SpotDetail {
  spotId: number
  name: string
  buildingName: string
  buildingAcronym: string
  locationDetails: string
  description: string
  capacity: number
  accessType: string
  img: string
  latitude: number
  longitude: number
  hours: { today: string; opening: string | null; closing: string | null; openNow: boolean | null }
  amenities: SpotAmenity[]
  // Newest first. reports[0] is the "current conditions" students rate.
  reports: SpotReport[]
  reviews: SpotReview[]
  currentUserRating: 'confirmed' | 'disputed' | null
  // Combined rating: 70% review stars + 30% report accuracy (SpotRatingService)
  overall?: { score: number | null; avgStars: number; reviewCount: number; accuracyPercent: number | null; ratingCount: number } | null
  // Photos students added (spot_photos), newest first
  photos?: Array<{ url: string; firstName: string; uploadedAt: string }>
  // Most common conditions per time of day, from past reports
  timeOfDay?: Array<{ block: string; reports: number; noise: string; crowdedness: string }>
  isOwnLatestReport: boolean
}
export interface SpotReview {
  reviewId: number
  mine: boolean // written by the logged-in student
  firstName: string
  rating: number
  text: string
  createdAt: string
}

declare global {
  interface Window {
    spotDetail?: SpotDetail
    flashMessage?: string
    flashError?: string
  }
}

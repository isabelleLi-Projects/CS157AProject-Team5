import type { Reward, StudySpot } from '../model/StudySpot'



type DatabaseStudySpot = {
  spotId: number
  name: string
  building: string
  location: string
  capacity: number
  open?: boolean | null
  latitude: number
  longitude: number
  accessType: string
  photoPath?: string
  img?: string
  noise?: string
  crowdedness?: string
  outlets?: string
  updated?: string
  amenities?: string[]
  averageRating?: number // average review stars only (Isabelle); the card shows the combined rating
  rating?: number | null // combined rating out of 5 (null = no reviews or ratings yet)
  reviewCount?: number
  ratingCount?: number
}

declare global {
  interface Window {
    studySpots?: DatabaseStudySpot[]
    rewards?: Array<{ id: number; label: string; description?: string; pts: number; stock: number }>
    studentPoints?: number
    studyFinderContext?: string
    currentUser?: {id: number | null; name: string; email:string; role: string}
  }
}

const context = window.studyFinderContext ?? ''


const level = (value?: string): 'low' | 'med' | 'high' => {
  const text = (value ?? '').toLowerCase()
  return text.includes('quiet') || text === 'low' || text === '1' ? 'low' : text.includes('high') || text === 'high' || text === '3' ? 'high' : 'med'
}

//getting the data from the jsp
export const STUDY_SPOTS: StudySpot[] = (window.studySpots ?? []).map(
  (spot) => ({
    id: spot.spotId,
    name: spot.name,
    location: spot.location,
    latitude: spot.latitude,
    longitude: spot.longitude,
    open: spot.open ?? false,
    accessType: spot.accessType,
    noise: spot.noise ?? 'Unknown',
    noiseLevel: level(spot.noise),
    crowded: spot.crowdedness ?? 'Unknown',
    crowdLevel: level(spot.crowdedness),
    outlets: spot.outlets === '1' ? 'Available' : spot.outlets === '0' ? 'None' : (spot.outlets ?? 'Unknown'),
    updated: spot.updated ?? 'No reports',
    rating: spot.rating ?? null,
    reviewCount: Number(spot.reviewCount ?? 0),
    ratingCount: Number(spot.ratingCount ?? 0),
    img: spot.img ?? '',
    tags: [spot.accessType, ...(spot.amenities ?? [])].filter(Boolean),
    building: spot.building,
    capacity: spot.capacity,
    photoPath: spot.photoPath,
  })
)

export const RECENT_SPOTS = STUDY_SPOTS.slice(0, 2)

export const REWARDS: Reward[] = (window.rewards ?? []).map((reward) => ({ ...reward, icon: '🎁' }))

// Not Crowded = latest report says "Available"; Has Printer = Printers amenity;
// Recently Updated = every spot, newest report first.
export const FILTERS = ['All', 'Quiet', 'Not Crowded', 'Open Now', 'Near Me', 'Outlets', 'Has Printer', 'Recently Updated']

//temp code
console.log('Database study spots:', window.studySpots)
console.log('React study spots:', STUDY_SPOTS)

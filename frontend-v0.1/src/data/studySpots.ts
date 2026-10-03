import type { Reward, StudySpot } from '../model/StudySpot'



type DatabaseStudySpot = {
  spotId: number
  name: string
  building: string
  location: string
  capacity: number
  latitude: number
  longitude: number
  accessType: string
  photoPath?: string
  noise?: string
  crowdedness?: string
  outlets?: string
  updated?: string
  amenities?: string[]
}

declare global {
  interface Window {
    studySpots?: DatabaseStudySpot[]
    rewards?: Array<{ id: number; label: string; description?: string; pts: number; stock: number }>
    studentPoints?: number
    studyFinderContext?: string
  }
}

const context = window.studyFinderContext ?? ''
const photoUrl = (path?: string) => {
  if (!path) return ''
  const clean = path.replace(/^\//, '')
  return `${context}/${clean.startsWith('assets/') ? clean : `assets/${clean}`}`
}
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
    noise: spot.noise ?? 'Unknown',
    noiseLevel: level(spot.noise),
    crowded: spot.crowdedness ?? 'Unknown',
    crowdLevel: level(spot.crowdedness),
    outlets: spot.outlets === '1' ? 'Available' : spot.outlets === '0' ? 'None' : (spot.outlets ?? 'Unknown'),
    updated: spot.updated ?? 'No reports',
    rating: 0,
    img: photoUrl(spot.photoPath),
    tags: [spot.accessType, ...(spot.amenities ?? [])].filter(Boolean),
    open: true,
    building: spot.building,
    capacity: spot.capacity,
    photoPath: spot.photoPath,
  })
)

export const RECENT_SPOTS = STUDY_SPOTS.slice(0, 2)

export const REWARDS: Reward[] = (window.rewards ?? []).map((reward) => ({ ...reward, icon: '🎁' }))

export const FILTERS = ['All', 'Quiet', 'Open Now', 'Near Me', 'Outlets']

//temp code
console.log('Database study spots:', window.studySpots)
console.log('React study spots:', STUDY_SPOTS)

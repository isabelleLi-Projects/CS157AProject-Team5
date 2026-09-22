import MLK from '../assets/study_spots/MLK.jpg'
import STUDENTUNION from '../assets/study_spots/StudentUnion.jpeg'
import MCQ from '../assets/study_spots/MCQ.png'
import ISB from '../assets/study_spots/ISB.jpeg'

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
}

declare global {
  interface Window {
    studySpots?: DatabaseStudySpot[]
  }
}

const images: Record<string, string> = {
  'MLK Library': MLK,
  'Student Union': STUDENTUNION,
  'Macquerrie Hall': MCQ,
  'Interdisciplinary Science Building': ISB,
}

//getting the data from the jsp
export const STUDY_SPOTS: StudySpot[] = (window.studySpots ?? []).map(
  (spot) => ({
    id: spot.spotId,
    name: spot.name,
    location: spot.location,
    noise: 'Unknown',
    noiseLevel: 'med',
    crowded: 'Unknown',
    crowdLevel: 'med',
    outlets: 'Unknown',
    updated: 'Just now',
    rating: 0,
    img: images[spot.name],
    tags: [spot.accessType],
    open: true,
  })
)

export const RECENT_SPOTS = STUDY_SPOTS.slice(0, 2)

export const REWARDS: Reward[] = [
  { icon: '🎁', label: 'Free Amazon Gift Card', pts: 1000 },
  { icon: '🛒', label: 'Target Gift Card', pts: 800 },
  { icon: '🍕', label: 'DoorDash Gift Card', pts: 500 },
]

export const FILTERS = ['All', 'Quiet', 'Open Now', 'Near Me', 'Outlets']

//temp code
console.log('Database study spots:', window.studySpots)
console.log('React study spots:', STUDY_SPOTS)
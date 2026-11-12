export type NoiseLevel = 'low' | 'med' | 'high'
export type CrowdLevel = 'low' | 'med' | 'high'

export interface StudySpot {
  id: number
  name: string
  location: string
  latitude: number
  longitude: number
  accessType: string
  noise: string
  noiseLevel: NoiseLevel
  crowded: string
  crowdLevel: CrowdLevel
  outlets: string
  updated: string
  rating: number | null // combined rating out of 5; null = not rated yet
  reviewCount: number
  ratingCount: number // accuracy ratings on the spot's reports
  img: string
  tags: string[]
  open: boolean
  building?: string
  capacity?: number
  photoPath?: string
}

export interface UserLocation {
  lat: number;
  lng: number;
}

export interface Reward {
  id?: number
  icon: string
  label: string
  pts: number
  description?: string
  stock?: number
  img?: string
}

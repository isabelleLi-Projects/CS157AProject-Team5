export type NoiseLevel = 'low' | 'med' | 'high'
export type CrowdLevel = 'low' | 'med' | 'high'

export interface StudySpot {
  id: number
  name: string
  location: string
  noise: string
  noiseLevel: NoiseLevel
  crowded: string
  crowdLevel: CrowdLevel
  outlets: string
  updated: string
  rating: number
  img: string
  tags: string[]
  open: boolean
  building?: string
  capacity?: number
  photoPath?: string
}

export interface Reward {
  id?: number
  icon: string
  label: string
  pts: number
  description?: string
  stock?: number
}

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
}

export interface Reward {
  icon: string
  label: string
  pts: number
}

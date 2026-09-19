import MLK from '../assets/study_spots/MLK.jpg'
import STUDENTUNION from '../assets/study_spots/StudentUnion.jpeg'
import MCQ from '../assets/study_spots/MCQ.png'
import ISB from '../assets/study_spots/ISB.jpeg'
import type { Reward, StudySpot } from '../model/StudySpot'

export const STUDY_SPOTS: StudySpot[] = [
  {
    id: 1,
    name: 'MLK Library',
    location: 'Floor 5',
    noise: 'Quiet',
    noiseLevel: 'low',
    crowded: 'Moderate',
    crowdLevel: 'med',
    outlets: 'Plenty',
    updated: '5 min ago',
    rating: 4.8,
    img: MLK,
    tags: ['WiFi', 'AC', 'Whiteboards'],
    open: true,
  },
  {
    id: 2,
    name: 'Student Union',
    location: 'Ground Floor',
    noise: 'Moderate',
    noiseLevel: 'med',
    crowded: 'Busy',
    crowdLevel: 'high',
    outlets: 'Some',
    updated: '12 min ago',
    rating: 4.2,
    img: STUDENTUNION,
    tags: ['Café', 'WiFi', 'Group Tables'],
    open: true,
  },
  {
    id: 3,
    name: 'Macquerrie Hall',
    location: 'Room 227',
    noise: 'Silent',
    noiseLevel: 'low',
    crowded: 'Light',
    crowdLevel: 'low',
    outlets: 'Limited',
    updated: '2 min ago',
    rating: 4.9,
    img: MCQ,
    tags: ['Silent Zone', 'AC'],
    open: true,
  },
  {
    id: 4,
    name: 'Interdisciplinary Science Building',
    location: 'Floor 5',
    noise: 'Lively',
    noiseLevel: 'high',
    crowded: 'Moderate',
    crowdLevel: 'med',
    outlets: 'Some',
    updated: '30 min ago',
    rating: 4.0,
    img: ISB,
    tags: ['Outdoor', 'Views', 'WiFi'],
    open: false,
  },
]

export const RECENT_SPOTS = [STUDY_SPOTS[0], STUDY_SPOTS[2]]

export const REWARDS: Reward[] = [
  { icon: '🎁', label: 'Free Amazon Gift Card', pts: 1000 },
  { icon: '🛒', label: 'Target Gift Card', pts: 800 },
  { icon: '🍕', label: 'DoorDash Gift Card', pts: 500 },
]

export const FILTERS = ['All', 'Quiet', 'Open Now', 'Near Me', 'Outlets']

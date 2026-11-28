// Shared by the admin Add Study Spot form and the student Suggest a Study Spot form.

// Amenity names; they match amenities.amenity_name in the database.
export const amenityNames = [
  'Electrical Outlets',
  'Wi-Fi',
  'Whiteboards',
  'Computers',
  'Printers',
  'Air Conditioning',
  'Natural Lighting',
  'Quiet Area',
  'Group Tables',
  'Food Nearby',
]

// Same rule as the server (decimal(9,6) columns): "lat, lng", each with at most 6 decimal places.
export function coordinateError(text: string): string {
  const parts = text.split(',').map((part) => part.trim())
  if (parts.length !== 2 || !parts[0] || !parts[1]) return 'Enter coordinates as: latitude, longitude'
  const number = /^-?\d+(\.\d{1,6})?$/
  if (!number.test(parts[0]) || !number.test(parts[1])) {
    return 'Latitude and longitude must be numbers with at most 6 decimal places.'
  }
  if (Math.abs(Number(parts[0])) > 90) return 'Latitude must be between -90 and 90.'
  if (Math.abs(Number(parts[1])) > 180) return 'Longitude must be between -180 and 180.'
  if (!isOnCampus(Number(parts[0]), Number(parts[1]))) return 'Coordinates must be on the SJSU main campus.'
  return ''
}

// Outline of the SJSU main campus as [latitude, longitude] corners (from OpenStreetMap).
// Same points as SJSU_CAMPUS in AdminDashboardService.java, which checks again on the server.
const SJSU_CAMPUS: [number, number][] = [
  [37.335805, -121.885877], // 4th St & San Fernando St (King Library corner)
  [37.333161, -121.883917],
  [37.331614, -121.882799], // 4th St & San Salvador St
  [37.334576, -121.876568], // 10th St & San Salvador St
  [37.338646, -121.879621], // 10th St & San Fernando St
  [37.337289, -121.882765],
]

// True when the point is inside the campus outline (ray casting test).
export function isOnCampus(lat: number, lng: number) {
  let inside = false
  for (let i = 0, j = SJSU_CAMPUS.length - 1; i < SJSU_CAMPUS.length; j = i++) {
    const [latI, lngI] = SJSU_CAMPUS[i]
    const [latJ, lngJ] = SJSU_CAMPUS[j]
    if (latI > lat !== latJ > lat && lng < ((lngJ - lngI) * (lat - latI)) / (latJ - latI) + lngI) inside = !inside
  }
  return inside
}

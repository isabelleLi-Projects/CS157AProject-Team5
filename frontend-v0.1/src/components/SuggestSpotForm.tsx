import { useState } from 'react'
import { amenityNames, coordinateError } from '../utils/campus'

declare global {
  interface Window {
    suggestionBuildings?: Array<{ id: number; acronym: string; name: string }>
  }
}

// "Suggest a study spot" window for students. Posts to suggest-spot.jsp. The spot stays hidden
// until an administrator approves it; then the student earns 15 points.
export default function SuggestSpotForm({ onClose }: { onClose: () => void }) {
  const [coordinates, setCoordinates] = useState('')
  const [touched, setTouched] = useState(false)
  const [locationNote, setLocationNote] = useState('')
  const coordError = coordinateError(coordinates)

  // Fill the coordinates from where the student is standing, rounded to 6 decimal places.
  const useMyLocation = () => {
    if (!navigator.geolocation) {
      setLocationNote('Your browser cannot share its location. Type the coordinates instead.')
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoordinates(`${position.coords.latitude.toFixed(6)}, ${position.coords.longitude.toFixed(6)}`)
        setTouched(true)
        setLocationNote('')
      },
      () => setLocationNote('Location is off. Allow location access or type the coordinates.'),
    )
  }

  return (
    <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 p-4">
      <form
        method="post"
        action={`${window.studyFinderContext ?? ''}/suggest-spot.jsp`}
        onSubmit={(event) => {
          if (coordError) {
            event.preventDefault()
            setTouched(true)
          }
        }}
        className="surface-background max-h-full w-full max-w-xl overflow-y-auto rounded-2xl p-6 shadow-xl"
      >
        <div className="flex items-center justify-between gap-3">
          <h2 className="font-display text-2xl text-main">Suggest a study spot</h2>
          <button type="button" className="text-secondary" onClick={onClose}>
            Close
          </button>
        </div>
        <p className="mt-1 text-sm text-muted">
          An admin reviews every suggestion. You earn 15 points if your spot is approved.
        </p>

        <div className="mt-5 grid gap-4 sm:grid-cols-2">
          <label className="text-sm font-medium text-main">
            Spot name
            <input name="spotName" required maxLength={100} placeholder="e.g. MLK 5th floor window desks" className="search-input mt-1 p-3" />
          </label>
          <label className="text-sm font-medium text-main">
            Building
            <select name="buildingId" required defaultValue="" className="search-input mt-1 p-3">
              <option value="" disabled>
                Select a building
              </option>
              {(window.suggestionBuildings ?? []).map((building) => (
                <option key={building.id} value={building.id}>
                  {building.acronym} · {building.name}
                </option>
              ))}
            </select>
          </label>
          <label className="text-sm font-medium text-main">
            Where in the building
            <input name="locationDetails" required maxLength={100} placeholder="Floor, room, or area" className="search-input mt-1 p-3" />
          </label>
          <label className="text-sm font-medium text-main">
            Seats
            <input type="number" name="capacity" required min={1} max={1000} className="search-input mt-1 p-3" />
          </label>
          <label className="text-sm font-medium text-main">
            Access type
            <select name="accessType" className="search-input mt-1 p-3">
              <option>Public access</option>
              <option>Student access</option>
            </select>
          </label>
          <label className="text-sm font-medium text-main">
            Coordinates
            <input
              name="coordinates"
              required
              value={coordinates}
              onChange={(event) => setCoordinates(event.target.value)}
              onBlur={() => setTouched(true)}
              placeholder="37.335500, -121.881500"
              aria-invalid={touched && Boolean(coordError)}
              className="search-input mt-1 p-3"
            />
            {touched && coordError ? (
              <span role="alert" className="mt-1 block text-xs text-[#b83c2a]">
                {coordError}
              </span>
            ) : (
              <button type="button" onClick={useMyLocation} className="btn-link mt-1 block text-xs">
                Use my current location
              </button>
            )}
            {locationNote && <span className="mt-1 block text-xs text-muted">{locationNote}</span>}
          </label>
          <label className="text-sm font-medium text-main sm:col-span-2">
            Description (optional)
            <textarea name="description" maxLength={255} rows={3} placeholder="What is it like to study here?" className="search-input mt-1 resize-none p-3" />
          </label>
          <fieldset className="sm:col-span-2">
            <legend className="text-sm font-medium text-main">Amenities</legend>
            <div className="mt-1 grid grid-cols-2 gap-2 text-sm">
              {amenityNames.map((amenity) => (
                <label key={amenity}>
                  <input type="checkbox" name="amenities" value={amenity} className="mr-2" />
                  {amenity}
                </label>
              ))}
            </div>
          </fieldset>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <button type="button" className="btn-secondary px-4 py-2" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn-primary px-4 py-2">
            Send for review
          </button>
        </div>
      </form>
    </div>
  )
}

import { useState } from 'react'

const amenities = [
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

const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
const weekdays = days.slice(0, 5)

// Times every 30 minutes for the dropdowns. Values are "HH:mm" like <input type="time">.
const TIME_OPTIONS = Array.from({ length: 48 }, (_, i) => {
  const value = `${String(Math.floor(i / 2)).padStart(2, '0')}:${i % 2 ? '30' : '00'}`
  return { value, label: formatTime(value) }
})

function formatTime(value: string) {
  const [h, m] = value.split(':').map(Number)
  return `${h % 12 === 0 ? 12 : h % 12}:${String(m).padStart(2, '0')} ${h < 12 ? 'AM' : 'PM'}`
}

type DayHours = { opening: string; closing: string; closed: boolean }
type Hours = Record<string, DayHours>

const DEFAULT_OPENING = '08:00'
const DEFAULT_CLOSING = '22:00'
const defaultHours = (): Hours =>
  Object.fromEntries(days.map((day) => [day, { opening: DEFAULT_OPENING, closing: DEFAULT_CLOSING, closed: false }]))

// Same rule as the server (decimal(9,6) columns): "lat, lng", each with at most 6 decimal places.
function coordinateError(text: string): string {
  const parts = text.split(',').map((part) => part.trim())
  if (parts.length !== 2 || !parts[0] || !parts[1]) return 'Enter coordinates as: latitude, longitude'
  const number = /^-?\d+(\.\d{1,6})?$/
  if (!number.test(parts[0]) || !number.test(parts[1])) {
    return 'Latitude and longitude must be numbers with at most 6 decimal places.'
  }
  if (Math.abs(Number(parts[0])) > 90) return 'Latitude must be between -90 and 90.'
  if (Math.abs(Number(parts[1])) > 180) return 'Longitude must be between -180 and 180.'
  return ''
}

export default function SpotForm() {
  const [fileName, setFileName] = useState('')
  const [coordinates, setCoordinates] = useState('')
  const [coordinatesTouched, setCoordinatesTouched] = useState(false)
  const [hours, setHours] = useState<Hours>(defaultHours)
  const [hoursNote, setHoursNote] = useState('')
  const [buildingPhoto, setBuildingPhoto] = useState('')
  const [useBuildingPhoto, setUseBuildingPhoto] = useState(false)
  const [quickOpening, setQuickOpening] = useState(DEFAULT_OPENING)
  const [quickClosing, setQuickClosing] = useState(DEFAULT_CLOSING)

  const coordError = coordinateError(coordinates)

  // Autofill hours and photo from what is already saved for the chosen building.
  function chooseBuilding(id: string) {
    const building = (window.adminBuildings ?? []).find((b) => String(b.id) === id)
    const saved = building?.hours ?? {}
    if (Object.keys(saved).length > 0) {
      // Days with no saved row are shown as closed.
      setHours(
        Object.fromEntries(
          days.map((day) => [
            day,
            saved[day]
              ? { opening: saved[day][0], closing: saved[day][1], closed: false }
              : { opening: DEFAULT_OPENING, closing: DEFAULT_CLOSING, closed: true },
          ]),
        ),
      )
      setHoursNote(`Filled in from ${building?.name}'s saved hours.`)
    } else {
      setHours(defaultHours())
      setHoursNote(building ? `${building.name} has no saved hours yet. Using default hours.` : '')
    }
    setBuildingPhoto(building?.photoUrl ?? '')
    setUseBuildingPhoto(Boolean(building?.photoUrl))
  }

  function setDay(day: string, change: Partial<DayHours>) {
    setHours((current) => ({ ...current, [day]: { ...current[day], ...change } }))
  }

  function applyToDays(targetDays: string[]) {
    setHours((current) => {
      const next = { ...current }
      targetDays.forEach((day) => (next[day] = { opening: quickOpening, closing: quickClosing, closed: false }))
      return next
    })
  }

  return (
    <form
      className="grid gap-4 sm:grid-cols-2"
      method="post"
      action={`${window.studyFinderContext ?? ''}/admin-action`}
      encType="multipart/form-data"
      onSubmit={(event) => {
        if (coordError) {
          event.preventDefault()
          setCoordinatesTouched(true)
        }
      }}
    >
      <input type="hidden" name="action" value="addSpot" />
      <label>
        Study spot name
        <input name="spotName" required className="search-input mt-1 p-3" />
      </label>

      <label>
        Building
        <select
          name="buildingId"
          required
          defaultValue=""
          onChange={(event) => chooseBuilding(event.target.value)}
          className="search-input mt-1 p-3"
        >
          <option value="" disabled>
            Select a building
          </option>

          {(window.adminBuildings ?? []).map((building) => (
            <option key={building.id} value={building.id}>
              {building.name}
            </option>
          ))}
        </select>
      </label>

      <label>
        Location details
        <input name="locationDetails" required placeholder="Room, floor, or area" className="search-input mt-1 p-3" />
      </label>

      <label>
        Coordinates
        <input
          name="coordinates"
          required
          value={coordinates}
          onChange={(event) => setCoordinates(event.target.value)}
          onBlur={() => setCoordinatesTouched(true)}
          placeholder="37.335200, -121.881100"
          aria-invalid={coordinatesTouched && Boolean(coordError)}
          className="search-input mt-1 p-3"
        />
        {coordinatesTouched && coordError ? (
          <span role="alert" className="mt-1 block text-xs text-[#b83c2a]">
            {coordError}
          </span>
        ) : (
          <span className="mt-1 block text-xs text-muted">Up to 6 decimal places each.</span>
        )}
      </label>

      <label>
        Access type
        <select name="accessType" className="search-input mt-1 p-3">
          <option>Public access</option>
          <option>Student access</option>
        </select>
      </label>

      <label>
        Capacity
        <input type="number" name="capacity" required min={1} className="search-input mt-1 p-3" />
      </label>

      <div className="text-sm font-medium text-main">
        Study Spot image
        {buildingPhoto && useBuildingPhoto && !fileName ? (
          <div className="mt-1 flex items-center gap-3 rounded-xl border border-[#e2ddd5] p-2">
            <img src={buildingPhoto} alt="Photo of another spot in this building" className="h-14 w-20 rounded-lg object-cover" />
            <div className="text-xs text-muted">
              Using this building's photo.
              <button type="button" className="btn-link ml-2 text-xs" onClick={() => setUseBuildingPhoto(false)}>
                Don't use it
              </button>
            </div>
          </div>
        ) : (
          buildingPhoto &&
          !fileName && (
            <button type="button" className="btn-link mt-1 block text-xs" onClick={() => setUseBuildingPhoto(true)}>
              Use this building's photo
            </button>
          )
        )}
        <input type="hidden" name="useBuildingPhoto" value={useBuildingPhoto && buildingPhoto ? '1' : '0'} />

        <label className="mt-1 flex cursor-pointer items-center justify-between rounded-xl border-2 border-dashed border-[#2d5a3d] bg-[#edf5ee] px-4 py-3 text-sm text-[#2d5a3d] hover:bg-[#dcecdf]">
          <span>{fileName || (buildingPhoto ? 'Or upload a different photo' : 'Choose file')}</span>

          <input
            type="file"
            name="photo"
            accept=".jpg,.jpeg,.png,.gif"
            className="hidden"
            onChange={(event) => {
              const file = event.target.files?.[0]
              setFileName(file?.name ?? '')
            }}
          />
        </label>
      </div>

      <fieldset className="sm:col-span-2">
        <legend>Amenities</legend>

        <div className="grid grid-cols-2 gap-2 md:grid-cols-3">
          {amenities.map((amenity) => (
            <label key={amenity} className="text-sm">
              <input type="checkbox" name="amenities" value={amenity} className="mr-2" />
              {amenity}
            </label>
          ))}
        </div>
      </fieldset>

      <fieldset className="sm:col-span-2">
        <legend>Building hours</legend>
        {hoursNote && <p className="mt-1 text-xs text-muted">{hoursNote}</p>}

        {/* Quick fill: pick one time range and apply it to many days at once. */}
        <div className="mt-2 flex flex-wrap items-center gap-2 rounded-xl bg-[#f7f4ef] p-3 text-sm">
          <span>Set</span>
          <TimeSelect value={quickOpening} onChange={setQuickOpening} label="Quick fill opening time" />
          <span>to</span>
          <TimeSelect value={quickClosing} onChange={setQuickClosing} label="Quick fill closing time" />
          <button type="button" className="btn-secondary px-3 py-1.5 text-xs" onClick={() => applyToDays(weekdays)}>
            Apply to Mon–Fri
          </button>
          <button type="button" className="btn-secondary px-3 py-1.5 text-xs" onClick={() => applyToDays(days)}>
            Apply to all days
          </button>
        </div>

        {days.map((day) => {
          const dayHours = hours[day]
          return (
            <div key={day} className="mt-2 flex flex-wrap items-center gap-3">
              <span className="w-24">{day}</span>
              <TimeSelect
                name={`${day}Opening`}
                value={dayHours.opening}
                onChange={(opening) => setDay(day, { opening })}
                disabled={dayHours.closed}
                label={`${day} opening time`}
              />
              <span className="text-sm text-muted">to</span>
              <TimeSelect
                name={`${day}Closing`}
                value={dayHours.closing}
                onChange={(closing) => setDay(day, { closing })}
                disabled={dayHours.closed}
                label={`${day} closing time`}
              />
              <label className="text-sm">
                <input
                  type="checkbox"
                  checked={dayHours.closed}
                  onChange={(event) => setDay(day, { closed: event.target.checked })}
                  className="mr-1"
                />
                Closed
              </label>
            </div>
          )
        })}
      </fieldset>

      <button type="submit" className="btn-primary sm:col-span-2 px-4 py-2">
        Save study spot
      </button>
    </form>
  )
}

// Dropdown of times in 30-minute steps. A saved time that is not on a half hour
// (for example 23:59) is added so it still shows correctly.
function TimeSelect({
  name,
  value,
  onChange,
  disabled,
  label,
}: {
  name?: string
  value: string
  onChange: (value: string) => void
  disabled?: boolean
  label: string
}) {
  const options = TIME_OPTIONS.some((option) => option.value === value)
    ? TIME_OPTIONS
    : [...TIME_OPTIONS, { value, label: formatTime(value) }].sort((a, b) => a.value.localeCompare(b.value))
  return (
    <select
      name={disabled ? undefined : name}
      aria-label={label}
      value={value}
      disabled={disabled}
      onChange={(event) => onChange(event.target.value)}
      className="search-input !w-32 p-2 text-sm disabled:opacity-50"
    >
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  )
}

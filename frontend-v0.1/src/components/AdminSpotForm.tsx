import { useState } from 'react'

export default function SpotForm() {
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
  
    const days = [
      'Monday',
      'Tuesday',
      'Wednesday',
      'Thursday',
      'Friday',
      'Saturday',
      'Sunday',
    ]
    const [fileName, setFileName] = useState('')
  
    return (
      <form
        className="grid gap-4 sm:grid-cols-2"
        method="post"
        action={`${window.studyFinderContext ?? ''}/admin-action.jsp`}
        encType="multipart/form-data"
      >
        <input type="hidden" name="action" value="addSpot" />
        <label>
          Study spot name
          <input name="spotName" required className="search-input mt-1 p-3" />
        </label>
  
        <label>
          Building
          <input name="building" required className="search-input mt-1 p-3" />
        </label>
  
        <label>
          Location details
          <input name="locationDetails" required placeholder="Room, floor, or area" className="search-input mt-1 p-3" />
        </label>

        <label>
          Coordinates
          <input name="coordinates" required placeholder="37.3352, -121.8811" className="search-input mt-1 p-3" />
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
          <input
            type="number"
            name="capacity" required className="search-input mt-1 p-3"
          />
        </label>
  
        <label className="text-sm font-medium text-main">
          Study Spot image

          <label className="mt-1 flex cursor-pointer items-center justify-between rounded-xl border-2 border-dashed border-[#2d5a3d] bg-[#edf5ee] px-4 py-3 text-sm text-[#2d5a3d] hover:bg-[#dcecdf]">
            <span>
              {fileName || 'Choose file'}
            </span>

            <input
              type="file"
              accept="image/*"
              className="hidden"
              onChange={(event) => {
                const file = event.target.files?.[0]
                setFileName(file?.name ?? '')
              }}
            />
          </label>
        </label>
  
        <fieldset className="sm:col-span-2">
          <legend>Amenities</legend>
  
          <div className="grid grid-cols-2 gap-2 md:grid-cols-3">
            {amenities.map((amenity) => (
              <label key={amenity} className="text-sm">
                <input
                  type="checkbox"
                  name="amenities"
                  value={amenity}
                  className="mr-2"
                />
                {amenity}
              </label>
            ))}
          </div>
        </fieldset>
  
        <fieldset className="sm:col-span-2">
          <legend>Building hours</legend>
  
          {days.map((day) => (
            <div key={day} className="mt-2 flex gap-3">
              <span className="w-24">{day}</span>
  
              <input
                type="time"
                name={`${day}Opening`}
                className="search-input p-2"
              />
  
              <input
                type="time"
                name={`${day}Closing`}
                className="search-input p-2"
              />
            </div>
          ))}
        </fieldset>
  
        <button
          type="submit"
          className="btn-primary sm:col-span-2 px-4 py-2"
        >
          Save study spot
        </button>
      </form>
    )
  }

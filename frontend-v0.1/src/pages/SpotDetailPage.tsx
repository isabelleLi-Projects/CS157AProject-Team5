import { useEffect, useState, type ReactNode } from 'react'
import Header from '../components/Header'
import FlashBanner from '../components/FlashBanner'
import NoiseChip from '../components/NoiseChip'
import SpotDetailMap from '../components/SpotDetailMap'
import type { SpotDetail, SpotReport, SpotReview } from '../model/SpotDetail'
import type { UserLocation } from '../model/StudySpot'
import { distanceInMiles } from '../utils/distance'
import { mainPageUrl, walkingDirectionsUrl } from '../utils/links'

// Study Spot page (Functional Requirement #4). All data comes from window.spotDetail (study-spot.jsp).

const timeAgo = (minutes: number) => {
  if (minutes < 1) return 'just now'
  if (minutes < 60) return `${minutes} min ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} hr ago`
  const days = Math.floor(hours / 24)
  return `${days} day${days === 1 ? '' : 's'} ago`
}

const outletsLabel = (outlets: number | null) =>
  outlets === 1 ? 'Available' : outlets === 0 ? 'None' : 'Unknown'

const noiseLevel = (noise: string) => {
  const text = noise.toLowerCase()
  return text.includes('quiet') ? 'low' : text.includes('loud') ? 'high' : 'med'
}

const students = (count: number) => `${count} student${count === 1 ? '' : 's'}`

// Same limit the server checks (SpotDetailDAO): students must be within 0.3 miles to rate, report, or review.
const MAX_ACTION_MILES = 0.3

export default function SpotDetailPage({ spot }: { spot: SpotDetail }) {
  const currentUser = window.currentUser
  const role = currentUser?.role?.toLowerCase() ?? 'guest'
  const isGuest = role === 'guest'
  const [userLocation, setUserLocation] = useState<UserLocation | null>(null)
  const [locationError, setLocationError] = useState('')

  const requestLocation = () => {
    if (!navigator.geolocation) {
      setLocationError('Location is not supported by your browser.')
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setUserLocation({ lat: position.coords.latitude, lng: position.coords.longitude })
        setLocationError('')
      },
      () => setLocationError('Allow location access to see how far away this spot is.')
    )
  }

  // Show the user's position right away only if they already granted location; don't prompt on load.
  useEffect(() => {
    navigator.permissions
      ?.query({ name: 'geolocation' })
      .then((status) => {
        if (status.state === 'granted') requestLocation()
      })
      .catch(() => {})
  }, [])

  const latest = spot.reports[0]
  const distance = userLocation
    ? distanceInMiles(userLocation.lat, userLocation.lng, spot.latitude, spot.longitude)
    : null
  // Turn off rating, reporting, and reviewing when the student is too far away: either the page
  // knows their distance, or the server just sent back its "within 0.3 miles" message.
  const tooFar = distance !== null ? distance > MAX_ACTION_MILES : (window.flashError ?? '').includes('0.3 miles')

  return (
    <div className="app-background min-h-screen">
      <Header
        isLoggedIn={!isGuest}
        activeNav=""
        onNavChange={(nav) => {
          window.location.href = mainPageUrl(nav)
        }}
        onLoginToggle={() => {
          window.location.href = `${window.studyFinderContext ?? ''}/login.jsp`
        }}
      />

      <main className="px-6 pb-16 max-w-6xl mx-auto">
        <a href={mainPageUrl('Find Spots')} className="btn-link text-sm inline-block mt-6">
          ← Back to spots
        </a>

        <FlashBanner />
        {tooFar && role === 'student' && (
          <div role="status" className="flash-error text-sm rounded-xl px-4 py-3 mt-3 flex flex-wrap items-center justify-between gap-2">
            <span>
              You're more than {MAX_ACTION_MILES} miles from this spot, so rating, reporting, and reviewing are turned off.
            </span>
            <button type="button" onClick={requestLocation} className="btn-link text-sm">
              I'm closer now, check again
            </button>
          </div>
        )}
        <div className="grid grid-cols-1 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)] gap-6 mt-4">
          {/* Left column: about, conditions + rating, recent reports, reviews */}
          <div className="space-y-6">
            <article className="spot-card rounded-2xl">
              {spot.img && <img src={spot.img} alt={spot.name} className="spot-image" style={{ height: 280 }} />}
              <div className="p-5">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h1 className="font-display text-3xl text-main leading-tight">{spot.name}</h1>
                    <p className="text-sm text-secondary mt-1">
                      {spot.buildingName}
                      {spot.buildingAcronym && <span className="text-muted"> ({spot.buildingAcronym})</span>}
                      {' · '}
                      {spot.locationDetails}
                    </p>
                  </div>
                  <OpenBadge openNow={spot.hours.openNow} />
                </div>
                <OverallRating overall={spot.overall} />
                {spot.description && <p className="text-sm text-main mt-3 leading-relaxed">{spot.description}</p>}
                <div className="flex flex-wrap gap-2 mt-4">
                  <Tag>{spot.accessType}</Tag>
                  <Tag>Seats {spot.capacity}</Tag>
                </div>
              </div>
            </article>

            <Card title="Current conditions">
              {latest ? (
                <>
                  <p className="text-xs text-muted -mt-2 mb-4">
                    Reported by {latest.authorName} · updated {timeAgo(latest.minutesAgo)}
                  </p>
                  <ConditionsGrid report={latest} />
                  {latest.notes && <p className="text-sm text-secondary mt-4 italic">“{latest.notes}”</p>}

                  <div className="mt-5 pt-4" style={{ borderTop: '1px solid var(--color-border)' }}>
                    <p className="text-sm font-medium text-main">Is this still accurate?</p>
                    <p className="text-xs text-muted mt-1 mb-3">
                      {students(latest.confirmed)} said accurate, {latest.disputed} said inaccurate.
                    </p>
                    <RatingControls spot={spot} report={latest} role={role} locked={tooFar} />
                  </div>
                </>
              ) : (
                <p className="text-sm text-muted">No reports yet. Be the first to report conditions.</p>
              )}
            </Card>

            <Card title="Report conditions">
              <ReportForm spotId={spot.spotId} role={role} locked={tooFar} />
            </Card>

            {spot.reports.length > 0 && (
              <Card title="Recent reports">
                <ul className="divide-y divide-[#e2ddd5]">
                  {spot.reports.map((report) => (
                    <li key={report.reportId} className="py-3 first:pt-0 last:pb-0">
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-center gap-2 flex-wrap text-xs">
                          <NoiseChip level={noiseLevel(report.noise)} label={report.noise} />
                          <span className="text-secondary">{report.crowdedness}</span>
                          <span className="text-muted">· Outlets {outletsLabel(report.outlets).toLowerCase()}</span>
                          {report.wifi && <span className="text-muted">· Wi-Fi {report.wifi.toLowerCase()}</span>}
                        </div>
                        <span className="text-[11px] text-muted shrink-0">{timeAgo(report.minutesAgo)}</span>
                      </div>
                      {report.notes && <p className="text-sm text-main mt-1.5">{report.notes}</p>}
                      <p className="text-[11px] text-muted mt-1">
                        by {report.authorName} · ✓ {report.confirmed} confirmed · ✗ {report.disputed} disputed
                      </p>
                    </li>
                  ))}
                </ul>
              </Card>
            )}
            <Card title="Photos from students">
              {spot.photos && spot.photos.length > 0 ? (
                <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                  {spot.photos.map((photo) => (
                    <a key={photo.url} href={photo.url} target="_blank" rel="noopener noreferrer" className="group block">
                      <img src={photo.url} alt={`Photo of ${spot.name} by ${photo.firstName}`} className="aspect-square w-full rounded-lg object-cover" loading="lazy" />
                      <p className="mt-1 text-[11px] text-muted">
                        {photo.firstName} · {photo.uploadedAt}
                      </p>
                    </a>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-muted">No photos yet.</p>
              )}
              <AddPhotoForm spotId={spot.spotId} role={role} locked={tooFar} />
            </Card>

            <Card title="Reviews">
              {spot.reviews.length > 0 ? (
                <>
                  <ul className="space-y-3">
                    {/* Your own review first, with Edit / Delete */}
                    {[...spot.reviews].sort((a, b) => Number(b.mine) - Number(a.mine)).slice(0, 3).map((review) => (
                      <li key={review.reviewId}>
                        <div className="text-yellow-500">
                          {'★'.repeat(review.rating)}
                          <span className="text-gray-300">
                            {'★'.repeat(5 - review.rating)}
                          </span>
                        </div>

                        <p className="text-sm text-main mt-1">{review.text}</p>

                        <p className="text-[11px] text-muted mt-0.5">
                          {review.mine ? 'You' : review.firstName} · {review.createdAt}
                        </p>
                        {review.mine && <MyReviewControls spotId={spot.spotId} review={review} />}
                      </li>
                    ))}
                  </ul>

                  {spot.reviews.length > 3 && (
                    <button type="button" className="btn-link text-sm mt-4">
                      View all {spot.reviews.length} reviews
                    </button>
                  )}
                </>
              ) : (
                <p className="text-sm text-muted">No reviews yet.</p>
              )}
            </Card>
            
          </div>

          {/* Right column: directions, hours, amenities */}
          <div className="space-y-6">
            <Card title="Directions">
              <div className="rounded-xl overflow-hidden standard-border">
                <SpotDetailMap
                  name={spot.name}
                  latitude={spot.latitude}
                  longitude={spot.longitude}
                  userLocation={userLocation}
                />
              </div>
              <p className="text-sm text-main mt-3">
                <span className="font-medium">{spot.buildingName}</span> → {spot.locationDetails}
              </p>
              {distance !== null ? (
                <p className="text-sm text-muted mt-1">About {distance.toFixed(2)} miles away (straight line)</p>
              ) : (
                <button type="button" onClick={requestLocation} className="btn-link text-xs mt-1">
                  Show my distance
                </button>
              )}
              {locationError && <p className="text-xs text-muted mt-1">{locationError}</p>}
              <a
                href={walkingDirectionsUrl(spot.latitude, spot.longitude)}
                target="_blank"
                rel="noopener noreferrer"
                className="btn-primary block text-center w-full py-3 text-sm mt-4"
              >
                Get directions
              </a>
            </Card>

            <Card title="Today's hours">
              {spot.hours.opening && spot.hours.closing ? (
                <p className="text-sm text-main">
                  {spot.hours.today}: {spot.hours.opening} – {spot.hours.closing}
                </p>
              ) : (
                <p className="text-sm text-muted">Hours not available</p>
              )}
            </Card>

            <Card title="Usually by time of day">
              {spot.timeOfDay && spot.timeOfDay.length > 0 ? (
                <>
                  <ul className="space-y-2">
                    {spot.timeOfDay.map((row) => (
                      <li key={row.block} className="flex items-center justify-between gap-3 text-sm">
                        <span className="font-medium text-main">{row.block}</span>
                        <span className="text-right text-secondary">
                          {row.noise} · {row.crowdedness}
                          <span className="ml-1 text-[11px] text-muted">({row.reports} report{row.reports === 1 ? '' : 's'})</span>
                        </span>
                      </li>
                    ))}
                  </ul>
                  <p className="mt-3 text-[11px] text-muted">Most common noise and crowdedness in past reports. Morning 6–11, Afternoon 12–4, Evening 5–9, Night 10–5.</p>
                </>
              ) : (
                <p className="text-sm text-muted">Not enough reports yet.</p>
              )}
            </Card>

            <Card title="Amenities">
              {spot.amenities.length > 0 ? (
                <ul className="space-y-2">
                  {spot.amenities.map((amenity) => (
                    <li key={amenity.name}>
                      <p className="text-sm font-medium text-main">{amenity.name}</p>
                      {(amenity.notes || amenity.description) && (
                        <p className="text-sm text-muted">{amenity.notes || amenity.description}</p>
                      )}
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="text-sm text-muted">No amenities listed.</p>
              )}
            </Card>
            
            <Card title="Leave a review">
              {spot.reviews.some((review) => review.mine) ? (
                <p className="text-sm text-muted">You already reviewed this spot. You can edit or delete it under Reviews.</p>
              ) : (
                <ReviewForm spotId={spot.spotId} role={role} locked={tooFar} />
              )}
            </Card>
          </div>
        </div>
      </main>
    </div>
  )
}

// "Add a photo" for students at the spot. The browser shrinks the photo to at most 1280px and
// sends it as a JPEG to upload-photo.jsp (the location is added by the study-spot.jsp script).
function AddPhotoForm({ spotId, role, locked }: { spotId: number; role: string; locked: boolean }) {
  const [preview, setPreview] = useState('')
  const [note, setNote] = useState('')

  if (role !== 'student') return null

  const shrink = (file: File) => {
    setNote('')
    const reader = new FileReader()
    reader.onload = () => {
      const image = new Image()
      image.onload = () => {
        const scale = Math.min(1, 1280 / Math.max(image.width, image.height))
        const canvas = document.createElement('canvas')
        canvas.width = Math.round(image.width * scale)
        canvas.height = Math.round(image.height * scale)
        canvas.getContext('2d')?.drawImage(image, 0, 0, canvas.width, canvas.height)
        let dataUrl = canvas.toDataURL('image/jpeg', 0.8)
        if (dataUrl.length > 1_400_000) dataUrl = canvas.toDataURL('image/jpeg', 0.6)
        if (dataUrl.length > 1_900_000) {
          setNote('That photo is too large. Try a smaller one.')
          setPreview('')
          return
        }
        setPreview(dataUrl)
      }
      image.onerror = () => setNote('That file is not a photo we can read. Try a JPG or PNG.')
      image.src = String(reader.result)
    }
    reader.readAsDataURL(file)
  }

  return (
    <form method="post" action={`${window.studyFinderContext ?? ''}/upload-photo.jsp`} className="mt-4 border-t border-[#e2ddd5] pt-4">
      <input type="hidden" name="spotId" value={spotId} />
      <input type="hidden" name="photo" value={preview} />
      <label className={`inline-flex items-center gap-2 text-sm ${locked ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'}`}>
        <span className="btn-secondary px-3 py-1.5 text-xs">{preview ? 'Choose a different photo' : 'Add a photo'}</span>
        <input
          type="file"
          accept="image/*"
          disabled={locked}
          className="hidden"
          onChange={(event) => {
            const file = event.target.files?.[0]
            if (file) shrink(file)
          }}
        />
      </label>
      {note && <p className="mt-2 text-xs text-[#b83c2a]">{note}</p>}
      {preview && (
        <div className="mt-3 flex items-end gap-3">
          <img src={preview} alt="Photo to upload" className="h-24 w-24 rounded-lg object-cover" />
          <button type="submit" disabled={locked} className="btn-primary px-3 py-1.5 text-xs disabled:opacity-50">
            Upload photo
          </button>
        </div>
      )}
      <p className="mt-2 text-[11px] text-muted">You need to be at the spot (within 0.3 miles). Up to 5 photos per spot.</p>
    </form>
  )
}

// Combined rating: 70% average review stars + 30% how often reports are rated accurate.
function OverallRating({ overall }: { overall?: SpotDetail['overall'] }) {
  if (!overall || overall.score == null) {
    return <p className="mt-3 text-sm text-muted">No ratings yet. Review this spot or rate its latest report.</p>
  }
  const parts = [
    overall.reviewCount > 0 ? `${overall.avgStars.toFixed(1)}★ from ${overall.reviewCount} review${overall.reviewCount === 1 ? '' : 's'}` : null,
    overall.accuracyPercent != null ? `${overall.accuracyPercent}% of ${overall.ratingCount} rating${overall.ratingCount === 1 ? '' : 's'} say reports are accurate` : null,
  ].filter(Boolean)
  return (
    <div className="mt-3 flex flex-wrap items-baseline gap-x-2 gap-y-1">
      <span className="font-display text-2xl text-main">★ {overall.score.toFixed(1)}</span>
      <span className="text-xs text-muted">overall · {parts.join(' · ')}</span>
    </div>
  )
}

// Edit / Delete for the logged-in student's own review. Posts to edit-review.jsp.
function MyReviewControls({ spotId, review }: { spotId: number; review: SpotReview }) {
  const [mode, setMode] = useState<'view' | 'edit' | 'confirmDelete'>('view')
  const [rating, setRating] = useState(review.rating)
  const action = `${window.studyFinderContext ?? ''}/edit-review.jsp`

  if (mode === 'view') {
    return (
      <div className="mt-1 flex gap-3">
        <button type="button" onClick={() => setMode('edit')} className="btn-link text-xs">
          Edit
        </button>
        <button type="button" onClick={() => setMode('confirmDelete')} className="btn-link text-xs">
          Delete
        </button>
      </div>
    )
  }

  if (mode === 'confirmDelete') {
    return (
      <form method="post" action={action} className="mt-2 flex flex-wrap items-center gap-2 rounded-xl bg-[#f7f4ef] p-3 text-xs">
        <input type="hidden" name="action" value="delete" />
        <input type="hidden" name="spotId" value={spotId} />
        <input type="hidden" name="reviewId" value={review.reviewId} />
        <span className="text-main">Delete your review? The 5 points it earned will be removed.</span>
        <button type="submit" className="btn-primary px-3 py-1 text-xs">
          Delete review
        </button>
        <button type="button" onClick={() => setMode('view')} className="btn-secondary px-3 py-1 text-xs">
          Keep it
        </button>
      </form>
    )
  }

  return (
    <form method="post" action={action} className="mt-2 space-y-2 rounded-xl bg-[#f7f4ef] p-3">
      <input type="hidden" name="action" value="edit" />
      <input type="hidden" name="spotId" value={spotId} />
      <input type="hidden" name="reviewId" value={review.reviewId} />
      <input type="hidden" name="rating" value={rating} />
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            onClick={() => setRating(star)}
            className={`text-2xl ${star <= rating ? 'text-yellow-500' : 'text-gray-300'}`}
            aria-label={`${star} stars`}
          >
            ★
          </button>
        ))}
      </div>
      <textarea
        name="reviewText"
        required
        maxLength={255}
        rows={3}
        defaultValue={review.text}
        className="search-input w-full resize-none px-3 py-2 text-sm"
      />
      <div className="flex gap-2">
        <button type="submit" className="btn-primary px-3 py-1.5 text-xs">
          Save review
        </button>
        <button type="button" onClick={() => setMode('view')} className="btn-secondary px-3 py-1.5 text-xs">
          Cancel
        </button>
      </div>
    </form>
  )
}

function ReviewForm({
  spotId,
  role,
  locked,
}: {
  spotId: number
  role: string
  locked: boolean
}) {
  const [rating, setRating] = useState(0)

  if (role === 'guest') {
    return (
      <a
        href={`${window.studyFinderContext ?? ''}/login.jsp`}
        className="btn-link text-sm"
      >
        Log in to leave a review
      </a>
    )
  }

  if (role !== 'student') {
    return <p className="text-sm text-muted">Only students can leave reviews.</p>
  }

  return (
    <form
      method="post"
      action={`${window.studyFinderContext ?? ''}/review-spot.jsp`}
      className="space-y-4"
    >
      <input type="hidden" name="spotId" value={spotId} />
      <input type="hidden" name="rating" value={rating} />

      <div>
        <p className="text-sm text-main mb-2">Your rating</p>

        <div className="flex gap-1">
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              key={star}
              type="button"
              disabled={locked}
              onClick={() => setRating(star)}
              className={`text-3xl ${
                star <= rating ? 'text-yellow-500' : 'text-gray-300'
              }`}
              aria-label={`${star} stars`}
            >
              ★
            </button>
          ))}
        </div>
      </div>

      <textarea
        name="reviewText"
        required
        disabled={locked}
        maxLength={255}
        rows={4}
        placeholder="Share your experience at this study spot..."
        className="search-input w-full px-3 py-2 text-sm resize-none"
      />
      <button type="submit" disabled={locked || rating === 0} className="btn-primary px-4 py-2.5 text-sm disabled:opacity-50">
        Submit review
      </button>
    </form>
  )
}

// Accurate / Inaccurate buttons. The server (rate-spot.jsp) re-checks every rule shown here.
function RatingControls({ spot, report, role, locked }: { spot: SpotDetail; report: SpotReport; role: string; locked: boolean }) {
  if (role === 'guest') {
    return (
      <a href={`${window.studyFinderContext ?? ''}/login.jsp`} className="btn-link text-sm">
        Log in to rate
      </a>
    )
  }
  if (role !== 'student') {
    return <p className="text-sm text-muted">Only students can rate spots.</p>
  }
  if (spot.isOwnLatestReport) {
    return <p className="text-sm text-muted">This is your report, so you can't rate it.</p>
  }

  const rated = spot.currentUserRating
  return (
    <form method="post" action={`${window.studyFinderContext ?? ''}/rate-spot.jsp`}>
      <input type="hidden" name="spotId" value={spot.spotId} />
      <input type="hidden" name="reportId" value={report.reportId} />
      <div className="flex gap-3">
        <RateButton value="confirmed" chosen={rated} locked={locked} label="Accurate ✓" />
        <RateButton value="disputed" chosen={rated} locked={locked} label="Inaccurate ✗" />
      </div>
      {rated ? (
        <p className="text-xs text-muted mt-2">You rated this {rated === 'confirmed' ? 'accurate' : 'inaccurate'}.</p>
      ) : (
        <p className="text-xs text-muted mt-2">Earn 2 points for each rating.</p>
      )}
    </form>
  )
}

// Choices match the allowed values in SpotService.submitReport.
const REPORT_FIELDS = [
  { name: 'noise', label: 'Noise', options: ['Quiet', 'Moderate', 'Loud'] },
  { name: 'crowdedness', label: 'Crowdedness', options: ['Available', 'Moderate', 'Crowded'] },
  { name: 'outlets', label: 'Outlets', options: [['1', 'Available'], ['0', 'None']] },
  { name: 'wifi', label: 'Wi-Fi', options: ['Strong', 'Weak', 'None'] },
  { name: 'smell', label: 'Smell', options: ['None', 'Low', 'Strong'] },
] as const

// Form for a student to report what the spot is like right now. Posts to report-spot.jsp.
function ReportForm({ spotId, role, locked }: { spotId: number; role: string; locked: boolean }) {
  const [open, setOpen] = useState(false)

  if (role === 'guest') {
    return (
      <a href={`${window.studyFinderContext ?? ''}/login.jsp`} className="btn-link text-sm">
        Log in to report conditions
      </a>
    )
  }
  if (role !== 'student') {
    return <p className="text-sm text-muted">Only students can report conditions.</p>
  }
  if (!open) {
    return (
      <>
        <p className="text-sm text-muted mb-3">Here now? Tell other students what it's like.</p>
        <button type="button" disabled={locked} onClick={() => setOpen(true)} className="btn-secondary px-4 py-2.5 text-sm disabled:opacity-50 disabled:cursor-not-allowed">
          Report current conditions
        </button>
      </>
    )
  }

  return (
    <form method="post" action={`${window.studyFinderContext ?? ''}/report-spot.jsp`}>
      <input type="hidden" name="spotId" value={spotId} />
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
        {REPORT_FIELDS.map((field) => (
          <label key={field.name} className="block">
            <span className="text-[10px] text-muted mb-1 uppercase tracking-wide block">{field.label}</span>
            <select name={field.name} required defaultValue="" className="search-input px-3 py-2 text-sm">
              <option value="" disabled>
                Choose…
              </option>
              {field.options.map((option) => {
                const [value, text] = typeof option === 'string' ? [option, option] : option
                return (
                  <option key={value} value={value}>
                    {text}
                  </option>
                )
              })}
            </select>
          </label>
        ))}
      </div>
      <label className="block mt-3">
        <span className="text-[10px] text-muted mb-1 uppercase tracking-wide block">Notes (optional)</span>
        <textarea
          name="notes"
          maxLength={255}
          rows={2}
          placeholder="e.g. Back tables are open, AC is cold"
          className="search-input px-3 py-2 text-sm resize-none"
        />
      </label>
      <div className="flex gap-3 mt-3">
        <button type="submit" disabled={locked} className="btn-primary px-4 py-2.5 text-sm disabled:opacity-50">
          Submit report
        </button>
        <button type="button" onClick={() => setOpen(false)} className="btn-secondary px-4 py-2.5 text-sm">
          Cancel
        </button>
      </div>
    </form>
  )
}

function RateButton({ value, chosen, locked, label }: { value: string; chosen: string | null; locked: boolean; label: string }) {
  const isChosen = chosen === value
  return (
    <button
      type="submit"
      name="result"
      value={value}
      disabled={locked || chosen !== null}
      aria-pressed={isChosen}
      className={`flex-1 py-2.5 text-sm ${isChosen ? 'btn-primary' : 'btn-secondary'} ${
        (chosen !== null && !isChosen) || locked ? 'opacity-50' : ''
      } disabled:cursor-not-allowed`}
    >
      {label}
    </button>
  )
}

function ConditionsGrid({ report }: { report: SpotReport }) {
  return (
    <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
      <Stat label="Noise">
        <NoiseChip level={noiseLevel(report.noise)} label={report.noise} />
      </Stat>
      <Stat label="Crowd">{report.crowdedness}</Stat>
      <Stat label="Outlets">{outletsLabel(report.outlets)}</Stat>
      <Stat label="Smell">{report.smell || 'Unknown'}</Stat>
      <Stat label="Wi-Fi">{report.wifi || 'Unknown'}</Stat>
    </div>
  )
}

function Stat({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div>
      <p className="text-[10px] text-muted mb-1 uppercase tracking-wide">{label}</p>
      <div className="text-sm font-medium text-primary">{children}</div>
    </div>
  )
}

function Card({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="surface-background standard-border rounded-2xl p-5">
      <h2 className="font-display text-xl text-main mb-3">{title}</h2>
      {children}
    </section>
  )
}

function Tag({ children }: { children: ReactNode }) {
  return (
    <span className="text-xs tag-background text-secondary px-2.5 py-0.5 rounded-full standard-border">{children}</span>
  )
}

function OpenBadge({ openNow }: { openNow: boolean | null }) {
  if (openNow === null) return null
  return (
    <span className={`text-xs px-2.5 py-0.5 rounded-full shrink-0 ${openNow ? 'badge-open' : 'badge-closed'}`}>
      {openNow ? 'Open now' : 'Closed'}
    </span>
  )
}

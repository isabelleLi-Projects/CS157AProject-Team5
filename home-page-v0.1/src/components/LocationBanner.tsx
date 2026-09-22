type LocationBannerProps = {
  onDismiss: () => void
}

export default function LocationBanner({ onDismiss }: LocationBannerProps) {
  const enableLocation = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser.')
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        console.log('Latitude:', position.coords.latitude)
        console.log('Longitude:', position.coords.longitude)

        alert('Location access enabled!')
        onDismiss()
      },
      (error) => {
        console.error('Location error:', error)

        if (error.code === error.PERMISSION_DENIED) {
          alert('Location permission was denied.')
        } else {
          alert('Unable to get your location.')
        }
      }
    )
  }

  return (
    <div className="mb-5 alert-background border border-[#f0d080] rounded-2xl p-4 flex items-start gap-3">
      <span className="text-xl mt-0.5">📍</span>

      <div className="flex-1">
        <p className="text-sm font-semibold text-[#5a3a00] mb-0.5">
          Enable location for better results
        </p>

        <p className="text-xs text-[#8a6020]">
          We&apos;ll show study spots nearest to you and give real-time crowdedness updates.
        </p>
      </div>

      <div className="flex flex-col gap-1.5 shrink-0">
        <button
          onClick={enableLocation}
          className="btn-primary bg-[#e8a838] hover:bg-[#d49428] text-xs px-3 py-1.5"
        >
          Enable
        </button>

        <button
          onClick={onDismiss}
          className="text-xs text-[#8a6020] hover:underline text-center"
        >
          Not now
        </button>
      </div>
    </div>
  )
}
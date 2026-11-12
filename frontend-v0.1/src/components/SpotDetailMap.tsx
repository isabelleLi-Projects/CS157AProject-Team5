import { MapContainer, TileLayer, Marker, Popup, CircleMarker, useMap } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import { useEffect } from 'react'
import L from 'leaflet'
import type { UserLocation } from '../model/StudySpot'

type SpotDetailMapProps = {
  name: string
  latitude: number
  longitude: number
  userLocation: UserLocation | null
}

// Same marker style as MapView.
const studySpotIcon = L.divIcon({
  html: '<span style="font-size: 28px;">📍</span>',
  className: '',
  iconSize: [28, 28],
  iconAnchor: [14, 28],
  popupAnchor: [0, -28],
})

// Zoom to fit both the spot and the user once the user's location is known.
function FitBoth({ latitude, longitude, userLocation }: { latitude: number; longitude: number; userLocation: UserLocation | null }) {
  const map = useMap()
  useEffect(() => {
    if (!userLocation) return
    map.fitBounds(L.latLngBounds([[latitude, longitude], [userLocation.lat, userLocation.lng]]), { padding: [40, 40], maxZoom: 17 })
  }, [map, latitude, longitude, userLocation])
  return null
}

export default function SpotDetailMap({ name, latitude, longitude, userLocation }: SpotDetailMapProps) {
  const spot: [number, number] = [latitude, longitude]
  return (
    <MapContainer center={spot} zoom={17} style={{ height: '260px', width: '100%' }}>
      <FitBoth latitude={latitude} longitude={longitude} userLocation={userLocation} />
      <TileLayer url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap contributors" />
      <Marker position={spot} icon={studySpotIcon}>
        <Popup>{name}</Popup>
      </Marker>
      {userLocation && (
        <CircleMarker
          center={[userLocation.lat, userLocation.lng]}
          radius={5}
          pathOptions={{ color: 'red', fillColor: 'red', fillOpacity: 1 }}
        >
          <Popup>Your current location</Popup>
        </CircleMarker>
      )}
    </MapContainer>
  )
}

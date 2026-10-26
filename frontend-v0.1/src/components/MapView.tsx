import { MapContainer, TileLayer, Marker, Popup, CircleMarker, useMap, }  from "react-leaflet";
import 'leaflet/dist/leaflet.css'
import type { StudySpot } from "@/model/StudySpot";
import { useEffect, useState } from 'react'

type MapViewProps = {spots: StudySpot[]}
type Coordinates = {lat: number; lng: number}

function RecenterMap({ location }: { location?: Coordinates }) {
    const map = useMap()
    useEffect(() => {
      if (location) {
        map.setView([location.lat, location.lng], 25)
      }
    }, [location, map])
    return null
  }

function distanceInMiles(latitude1: number, longitude1: number, latitude2: number, longitude2: number) {
    const earthRadiusMiles = 3959

    const latitudeDifference =
        ((latitude2 - latitude1) * Math.PI) / 180

    const longitudeDifference =
        ((longitude2 - longitude1) * Math.PI) / 180

    const a =
        Math.sin(latitudeDifference / 2) ** 2 +
        Math.cos((latitude1 * Math.PI) / 180) *
        Math.cos((latitude2 * Math.PI) / 180) *
        Math.sin(longitudeDifference / 2) ** 2

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadiusMiles * c
}

const SJSU_CENTER: [number, number] = [37.3352, -121.8811]
export default function MapView({ spots }: MapViewProps) {
  const [userLocation, setUserLocation] = useState<Coordinates>()
  const nearbySpot = userLocation
  ? spots.some((spot) => {
      const distance = distanceInMiles(
        userLocation.lat,
        userLocation.lng,
        spot.latitude,
        spot.longitude
      )
      return distance <= 5
    })
  : false
  useEffect(() => {
    if (!navigator.geolocation) {
      console.error('Geolocation is not supported.')
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setUserLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        })
      },
      (error) => {console.error('Location permission error:', error)}
    )
  }, [])
    const center: [number, number] = userLocation
    ? [userLocation.lat, userLocation.lng] : SJSU_CENTER
    return (
        // display map
        <MapContainer center={SJSU_CENTER} zoom={25} style={{height: '450px', width: '100%'}}>
            <RecenterMap location={nearbySpot ? userLocation : undefined} />
            <TileLayer url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap contributors"/>
            {spots.map((spot) => (
                <Marker key={spot.id} position={[spot.latitude, spot.longitude]}>
                    <Popup> 
                        <strong>{spot.name}</strong>
                        <br />{spot.building}<br />
                        {spot.accessType}
                    </Popup>
                </Marker>))}
            {userLocation && (
                <CircleMarker 
                    center={[userLocation.lat, userLocation.lng]} radius={5} pathOptions={{color: 'red', fillColor: 'red', fillOpacity: 1,}}>
                    <Popup> Your current location </Popup>
                </CircleMarker>
                
            )}
        </MapContainer>
    )
            
}


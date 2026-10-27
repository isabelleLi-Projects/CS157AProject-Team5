import { MapContainer, TileLayer, Marker, Popup, CircleMarker, useMap, }  from "react-leaflet";
import 'leaflet/dist/leaflet.css'
import type { StudySpot } from "@/model/StudySpot";
import type { UserLocation } from "@/model/StudySpot";
import { use, useEffect, useState } from 'react'
import L, { bounds } from 'leaflet'
import { spotDetailUrl } from '../utils/links'

type MapViewProps = {spots: StudySpot[]; userLocation?: UserLocation; activeFilter: string}
type Coordinates = {lat: number; lng: number}
// default center coordinates of sjsu
const SJSU_CENTER: [number, number] = [37.3352, -121.8811]
// give each study spot the location emoji
const studySpotIcon = L.divIcon({html: '<span style="font-size: 28px;">📍</span>',
    className: '',
    iconSize: [28, 28],
    iconAnchor: [14, 28],
    popupAnchor: [0, -28],
  })

function RecenterMap({ spots, userLocation, showUserLocation }: { spots: StudySpot[]; userLocation?: Coordinates; showUserLocation: boolean }) {
    const map = useMap()
    useEffect(() => {
      if (showUserLocation && userLocation) {
        map.setView([userLocation.lat, userLocation.lng], 50)
      }
      if (spots.length === 0) {
        map.setView(SJSU_CENTER, 16)
        return
      }
      const spotBounds = L.latLngBounds(
        spots.map((spot) => [spot.latitude, spot.longitude] as [number, number])
      )
      map.fitBounds(spotBounds, {
        padding: [40, 40],
        maxZoom: 16,
      })
    }, [map, spots, userLocation, showUserLocation])
    return null
  }
function FitStudySpots({spots, activeFilter,}: 
    {spots: StudySpot[]; activeFilter: string})
    {
        const map = useMap()
    useEffect(()=> {
        if (activeFilter !== 'All' || spots.length === 0) {
            return
        }
        const bounds = L.latLngBounds(
            spots.map((spot) => [spot.latitude, spot.longitude])
        )
      
        map.fitBounds(bounds, {
            padding: [40, 40],
            maxZoom: 16,
        })
    }, [spots, activeFilter, map])
    return null
    }

export default function MapView({ spots, userLocation, activeFilter }: MapViewProps) {
    const showUserLocation = activeFilter === 'Near Me' && userLocation !== undefined && spots.length > 0
    const center: [number, number] = showUserLocation && userLocation
  ? [userLocation.lat, userLocation.lng]
  : SJSU_CENTER
    return (
        // display map
        <MapContainer center={center} zoom={50} style={{height: '450px', width: '100%'}}>
            <FitStudySpots spots={spots} activeFilter={activeFilter}/>
            <RecenterMap spots={spots} userLocation={userLocation} showUserLocation={showUserLocation} />
            <TileLayer url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap contributors"/>
            {spots.map((spot) => (
                <Marker key={spot.id} position={[spot.latitude, spot.longitude,]}icon={studySpotIcon}>
                    <Popup> 
                        <strong>{spot.name}</strong>
                        <br />{spot.building}<br />
                        {spot.accessType}
                        <br /><a href={spotDetailUrl(spot.id)}>View details →</a>
                    </Popup>
                </Marker>))}
            {showUserLocation && userLocation &&(
                <CircleMarker 
                    center={[userLocation.lat, userLocation.lng]} radius={5} pathOptions={{color: 'red', fillColor: 'red', fillOpacity: 1,}}>
                    <Popup> Your current location </Popup>
                </CircleMarker>
                
            )}
        </MapContainer>
    )
            
}


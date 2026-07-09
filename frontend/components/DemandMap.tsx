"use client"

import "leaflet/dist/leaflet.css"
import { MapContainer, TileLayer, CircleMarker, Tooltip } from "react-leaflet"
import { useRouter } from "next/navigation"
import { CityDemandSummary, CityWeather } from "@/types"
import { demandColor, demandRadius } from "@/lib/demand"
import { weatherEmoji } from "@/lib/weather"

interface Props {
  data: CityDemandSummary[]
  date: string
  weather: Map<string, CityWeather> | null
}

export default function DemandMap({ data, date, weather }: Props) {
  const router = useRouter()

  return (
    <MapContainer
      center={[39.8283, -98.5795]}
      zoom={4}
      scrollWheelZoom={true}
      className="h-[500px] w-full rounded-lg"
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {data.map((c) => {
        const w = weather?.get(c.city)
        return (
          <CircleMarker
            key={c.city}
            center={[c.lat, c.lng]}
            radius={demandRadius(c.demandScore)}
            pathOptions={{
              color: demandColor[c.demandLabel],
              fillColor: demandColor[c.demandLabel],
              fillOpacity: 0.6,
              weight: 1,
            }}
            eventHandlers={{
              click: () =>
                router.push(
                  `/results/${encodeURIComponent(c.city)}?from=${date}&to=${date}&highOnly=false`
                ),
            }}
          >
            <Tooltip>
              <div className="text-sm space-y-0.5">
                <p className="font-semibold">{c.city}</p>
                <p>{c.demandLabel} · {c.demandScore}/10</p>
                <p>{c.eventCount} significant event{c.eventCount !== 1 ? "s" : ""}</p>
                {w && (
                  <p className="text-xs text-gray-500 pt-0.5 border-t border-gray-200 mt-1">
                    {weatherEmoji(w.weatherCode)} {w.tempHighF}°F · {w.precipPct}% precip
                  </p>
                )}
              </div>
            </Tooltip>
          </CircleMarker>
        )
      })}
    </MapContainer>
  )
}

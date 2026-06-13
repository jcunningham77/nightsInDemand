"use client"

import "leaflet/dist/leaflet.css"
import { MapContainer, TileLayer, CircleMarker, Tooltip } from "react-leaflet"
import { useRouter } from "next/navigation"
import { CityDemandSummary } from "@/types"
import { demandColor, demandRadius } from "@/lib/demand"

interface Props {
  data: CityDemandSummary[]
  date: string
}

export default function DemandMap({ data, date }: Props) {
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
      {data.map((c) => (
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
            <div className="text-sm">
              <p className="font-semibold">{c.city}</p>
              <p>{c.demandLabel} · {c.demandScore}/10</p>
              <p>{c.eventCount} significant event{c.eventCount !== 1 ? "s" : ""}</p>
            </div>
          </Tooltip>
        </CircleMarker>
      ))}
    </MapContainer>
  )
}

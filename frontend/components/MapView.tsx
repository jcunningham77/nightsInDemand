"use client"

import { useEffect, useState } from "react"
import dynamic from "next/dynamic"
import { getCityDemandMap } from "@/lib/api"
import { CityDemandSummary, CityWeather } from "@/types"
import { demandColor } from "@/lib/demand"
import { fetchWeatherForCities } from "@/lib/weather"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

const DemandMap = dynamic(() => import("./DemandMap"), {
  ssr: false,
  loading: () => (
    <div className="h-[500px] w-full animate-pulse rounded-lg bg-muted" />
  ),
})

const demandLabels = ["LOW", "MEDIUM", "HIGH", "EXTREME"] as const

function today() {
  return new Date().toISOString().split("T")[0]
}

function shiftDate(date: string, days: number) {
  const d = new Date(date + "T12:00:00")
  d.setDate(d.getDate() + days)
  return d.toISOString().split("T")[0]
}

function isWithinDays(date: string, days: number) {
  const todayStr = today()
  const target = new Date(date + "T00:00:00")
  const cutoff = new Date(todayStr + "T00:00:00")
  cutoff.setDate(cutoff.getDate() + days)
  return date >= todayStr && target <= cutoff
}

export function MapView() {
  const [date, setDate] = useState(today())
  const [data, setData] = useState<CityDemandSummary[]>([])
  const [weather, setWeather] = useState<Map<string, CityWeather> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      setWeather(null)
      try {
        const result = await getCityDemandMap(date)
        if (!cancelled) {
          setData(result)
          if (isWithinDays(date, 10) && result.length > 0) {
            try {
              const w = await fetchWeatherForCities(result, date)
              if (!cancelled) setWeather(w)
            } catch {
              // weather is optional — fail silently
            }
          }
        }
      } catch {
        if (!cancelled) setError("Failed to load demand map. Make sure the backend is running.")
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [date])

  const hottest = [...data].sort((a, b) => b.demandScore - a.demandScore)[0]

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end gap-3">
        <div className="space-y-1">
          <Label htmlFor="map-date">Date</Label>
          <Input
            id="map-date"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
        </div>
        <Button variant="outline" size="sm" onClick={() => setDate(shiftDate(date, -1))}>
          ← Prev day
        </Button>
        <Button variant="outline" size="sm" onClick={() => setDate(shiftDate(date, 1))}>
          Next day →
        </Button>
        <Button variant="outline" size="sm" onClick={() => setDate(today())}>
          Today
        </Button>
        {loading && <span className="text-sm text-muted-foreground">Loading…</span>}
      </div>

      {error && (
        <div className="rounded-md bg-red-50 border border-red-200 p-4 text-red-700 text-sm">
          {error}
        </div>
      )}

      {!error && (
        <>
          <DemandMap data={data} date={date} weather={weather} />

          <div className="flex flex-wrap items-center gap-4 text-xs text-muted-foreground">
            <span className="font-medium uppercase tracking-wide">Demand:</span>
            {demandLabels.map((label) => (
              <span key={label} className="flex items-center gap-1.5">
                <span
                  className="inline-block h-3 w-3 rounded-full"
                  style={{ backgroundColor: demandColor[label] }}
                />
                {label}
              </span>
            ))}
          </div>

          {hottest && (
            <p className="text-sm text-muted-foreground">
              Hottest city: <span className="font-medium text-foreground">{hottest.city}</span>{" "}
              ({hottest.demandLabel}, {hottest.eventCount} event{hottest.eventCount !== 1 ? "s" : ""}) —
              click any city on the map for details.
            </p>
          )}
        </>
      )}
    </div>
  )
}

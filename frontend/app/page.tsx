"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs"
import { MapView } from "@/components/MapView"

function today() {
  return new Date().toISOString().split("T")[0]
}

function ninetyDaysOut() {
  const d = new Date()
  d.setDate(d.getDate() + 90)
  return d.toISOString().split("T")[0]
}

export default function Home() {
  const router = useRouter()
  const [city, setCity] = useState("New York")
  const [from, setFrom] = useState(today())
  const [to, setTo] = useState(ninetyDaysOut())
  const [highOnly, setHighOnly] = useState(true)

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const params = new URLSearchParams({ from, to, highOnly: String(highOnly) })
    router.push(`/results/${encodeURIComponent(city)}?${params}`)
  }

  return (
    <main className="min-h-screen bg-background px-4 py-8">
      <div className="w-full max-w-3xl mx-auto space-y-8">
        <div className="text-center space-y-2">
          <h1 className="text-4xl font-bold tracking-tight">Nights in Demand</h1>
          <p className="text-muted-foreground text-sm">
            Find high-demand nights in your city — sports, concerts, and events combined.
          </p>
        </div>

        <Tabs defaultValue="map" className="items-center">
          <TabsList>
            <TabsTrigger value="map">Map</TabsTrigger>
            <TabsTrigger value="search">City Search</TabsTrigger>
          </TabsList>

          <TabsContent value="map" className="w-full">
            <MapView />
          </TabsContent>

          <TabsContent value="search" className="w-full max-w-md mx-auto">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-1">
                <Label htmlFor="city">City</Label>
                <Input
                  id="city"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                  placeholder="e.g. New York, Chicago, Los Angeles"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label htmlFor="from">From</Label>
                  <Input
                    id="from"
                    type="date"
                    value={from}
                    onChange={(e) => setFrom(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-1">
                  <Label htmlFor="to">To</Label>
                  <Input
                    id="to"
                    type="date"
                    value={to}
                    onChange={(e) => setTo(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="flex items-center gap-2">
                <input
                  id="highOnly"
                  type="checkbox"
                  checked={highOnly}
                  onChange={(e) => setHighOnly(e.target.checked)}
                  className="h-4 w-4 rounded border-gray-300"
                />
                <Label htmlFor="highOnly" className="cursor-pointer">
                  High demand nights only (2+ events)
                </Label>
              </div>

              <Button type="submit" className="w-full">
                Search
              </Button>
            </form>
          </TabsContent>
        </Tabs>
      </div>
    </main>
  )
}

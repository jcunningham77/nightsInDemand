export interface Event {
  id: string
  name: string
  date: string
  city: string
  venue: string
  league: string | null
  category: string
  estimatedAttendance: number | null
  source: string
}

export interface CityNightReport {
  date: string
  city: string
  events: Event[]
  eventCount: number
  demandScore: number
  demandLabel: "LOW" | "MEDIUM" | "HIGH" | "EXTREME"
}

export interface CityDemandSummary {
  city: string
  lat: number
  lng: number
  date: string
  eventCount: number
  demandScore: number
  demandLabel: "LOW" | "MEDIUM" | "HIGH" | "EXTREME"
}

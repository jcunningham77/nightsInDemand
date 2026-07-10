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
  minPrice: number | null
  maxPrice: number | null
  priceCurrency: string | null
}

export interface PriceQuote {
  source: string
  minPrice: number | null
  maxPrice: number | null
  currency: string
  url: string | null
  available: boolean
}

export interface PriceComparison {
  eventId: string
  quotes: PriceQuote[]
  cheapestSource: string | null
  fetchedAt: number
}

export interface CityNightReport {
  date: string
  city: string
  events: Event[]
  eventCount: number
  demandScore: number
  demandLabel: "LOW" | "MEDIUM" | "HIGH" | "EXTREME"
}

export interface CityWeather {
  tempHighF: number
  precipPct: number
  weatherCode: number
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

import { CityNightReport } from "@/types"

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"

export async function getNights(
  city: string,
  from: string,
  to: string
): Promise<CityNightReport[]> {
  const res = await fetch(
    `${BASE_URL}/api/events/${encodeURIComponent(city)}?from=${from}&to=${to}`,
    { cache: "no-store" }
  )
  if (!res.ok) throw new Error(`API error: ${res.status}`)
  return res.json()
}

export async function getHighDemandNights(
  city: string,
  from: string,
  to: string,
  threshold = 2
): Promise<CityNightReport[]> {
  const res = await fetch(
    `${BASE_URL}/api/events/${encodeURIComponent(city)}/highdemand?from=${from}&to=${to}&threshold=${threshold}`,
    { cache: "no-store" }
  )
  if (!res.ok) throw new Error(`API error: ${res.status}`)
  return res.json()
}

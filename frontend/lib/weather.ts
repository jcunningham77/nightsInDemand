import { CityDemandSummary, CityWeather } from "@/types"

// WMO weather interpretation codes → simple category
export type WeatherIcon = "sunny" | "partly-cloudy" | "cloudy" | "rainy" | "snowy" | "stormy" | "foggy"

export function weatherIcon(code: number): WeatherIcon {
  if (code === 0) return "sunny"
  if (code <= 3) return "partly-cloudy"
  if (code <= 48) return "foggy"
  if (code <= 67) return "rainy"
  if (code <= 77) return "snowy"
  if (code <= 82) return "rainy"
  if (code <= 86) return "snowy"
  return "stormy"
}

export function weatherEmoji(code: number): string {
  const icon = weatherIcon(code)
  const map: Record<WeatherIcon, string> = {
    sunny: "☀️",
    "partly-cloudy": "⛅",
    cloudy: "☁️",
    foggy: "🌫️",
    rainy: "🌧️",
    snowy: "🌨️",
    stormy: "⛈️",
  }
  return map[icon]
}

// Returns celsius → fahrenheit
function cToF(c: number) {
  return Math.round(c * 9 / 5 + 32)
}

export async function fetchWeatherForCities(
  cities: CityDemandSummary[],
  date: string
): Promise<Map<string, CityWeather>> {
  const lats = cities.map((c) => c.lat).join(",")
  const lngs = cities.map((c) => c.lng).join(",")

  const url =
    `https://api.open-meteo.com/v1/forecast` +
    `?latitude=${lats}&longitude=${lngs}` +
    `&daily=temperature_2m_max,precipitation_probability_max,weathercode` +
    `&temperature_unit=celsius&timezone=America%2FNew_York&forecast_days=16`

  const res = await fetch(url)
  if (!res.ok) throw new Error(`Weather API error: ${res.status}`)
  const json = await res.json()

  // Open-Meteo returns an array when multiple locations are requested
  const results: CityWeather[] = []
  const items = Array.isArray(json) ? json : [json]

  for (const item of items) {
    const dateIndex = (item.daily.time as string[]).indexOf(date)
    if (dateIndex === -1) {
      results.push({ tempHighF: 0, precipPct: 0, weatherCode: 0 })
    } else {
      results.push({
        tempHighF: cToF(item.daily.temperature_2m_max[dateIndex]),
        precipPct: Math.round(item.daily.precipitation_probability_max[dateIndex] ?? 0),
        weatherCode: item.daily.weathercode[dateIndex],
      })
    }
  }

  const map = new Map<string, CityWeather>()
  cities.forEach((c, i) => {
    if (results[i]) map.set(c.city, results[i])
  })
  return map
}

import { CityNightReport } from "@/types"

export const demandColor: Record<CityNightReport["demandLabel"], string> = {
  LOW:     "#94a3b8",
  MEDIUM:  "#fbbf24",
  HIGH:    "#f97316",
  EXTREME: "#ef4444",
}

/** Marker radius in pixels, scaled by demand score (1-10). */
export function demandRadius(score: number) {
  return 6 + score * 2
}

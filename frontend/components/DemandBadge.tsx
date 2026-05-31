import { Badge } from "@/components/ui/badge"
import { CityNightReport } from "@/types"

const styles: Record<CityNightReport["demandLabel"], string> = {
  LOW:     "bg-slate-100 text-slate-700 hover:bg-slate-100",
  MEDIUM:  "bg-yellow-100 text-yellow-800 hover:bg-yellow-100",
  HIGH:    "bg-orange-100 text-orange-800 hover:bg-orange-100",
  EXTREME: "bg-red-100 text-red-800 hover:bg-red-100",
}

const icons: Record<CityNightReport["demandLabel"], string> = {
  LOW:     "–",
  MEDIUM:  "⚡",
  HIGH:    "🔥",
  EXTREME: "🚨",
}

export function DemandBadge({ label, score }: { label: CityNightReport["demandLabel"]; score: number }) {
  return (
    <Badge className={`text-sm font-semibold px-3 py-1 ${styles[label]}`}>
      {icons[label]} {label} {score}/10
    </Badge>
  )
}

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { CityNightReport } from "@/types"
import { DemandBadge } from "./DemandBadge"

function formatDate(iso: string) {
  return new Date(iso + "T12:00:00").toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
  })
}

function formatAttendance(n: number | null) {
  if (!n) return null
  return `~${n.toLocaleString()} attendees`
}

const categoryIcon: Record<string, string> = {
  sports:  "🏟️",
  concert: "🎵",
  other:   "📅",
}

const sourceLabel: Record<string, string> = {
  espn:         "ESPN",
  ticketmaster: "Ticketmaster",
}

export function NightCard({ report }: { report: CityNightReport }) {
  return (
    <Card className="w-full">
      <CardHeader className="flex flex-row items-start justify-between gap-4 pb-2">
        <CardTitle className="text-lg font-bold leading-snug">
          {formatDate(report.date)}
        </CardTitle>
        <DemandBadge label={report.demandLabel} score={report.demandScore} />
      </CardHeader>
      <CardContent className="space-y-2">
        {report.events.map((event) => (
          <div key={event.id} className="flex items-start gap-3 text-sm">
            <span className="mt-0.5 text-base">{categoryIcon[event.category] ?? "📅"}</span>
            <div className="flex-1 min-w-0">
              <p className="font-medium truncate">{event.name}</p>
              <p className="text-muted-foreground text-xs">
                {event.venue}
                {event.league ? ` · ${event.league}` : ""}
                {event.estimatedAttendance ? ` · ${formatAttendance(event.estimatedAttendance)}` : ""}
                <span className="ml-1 opacity-50">({sourceLabel[event.source] ?? event.source})</span>
              </p>
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  )
}

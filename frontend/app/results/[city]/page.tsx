import { Suspense } from "react"
import Link from "next/link"
import { getNights, getHighDemandNights } from "@/lib/api"
import { CityNightReport } from "@/types"
import { NightCard } from "@/components/NightCard"
import { DemandChart } from "@/components/DemandChart"
import { Button } from "@/components/ui/button"

interface Props {
  params: Promise<{ city: string }>
  searchParams: Promise<{ from: string; to: string; highOnly?: string }>
}

export default async function ResultsPage({ params, searchParams }: Props) {
  const { city } = await params
  const { from, to, highOnly } = await searchParams
  const decodedCity = decodeURIComponent(city)
  const isHighOnly = highOnly !== "false"

  let reports: CityNightReport[] = []
  let error: string | null = null

  try {
    reports = isHighOnly
      ? await getHighDemandNights(decodedCity, from, to)
      : await getNights(decodedCity, from, to)

    // Sort by demand score descending
    reports = reports.sort((a, b) => b.demandScore - a.demandScore)
  } catch (e) {
    error = "Failed to load events. Make sure the backend is running."
  }

  return (
    <main className="min-h-screen bg-background px-4 py-8">
      <div className="max-w-2xl mx-auto space-y-6">

        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">{decodedCity}</h1>
            <p className="text-muted-foreground text-sm">{from} → {to}</p>
          </div>
          <Link href="/">
            <Button variant="outline" size="sm">← New Search</Button>
          </Link>
        </div>

        {/* Toggle */}
        <div className="flex gap-2">
          <Link href={`/results/${city}?from=${from}&to=${to}&highOnly=true`}>
            <Button variant={isHighOnly ? "default" : "outline"} size="sm">
              High Demand Only
            </Button>
          </Link>
          <Link href={`/results/${city}?from=${from}&to=${to}&highOnly=false`}>
            <Button variant={!isHighOnly ? "default" : "outline"} size="sm">
              All Nights
            </Button>
          </Link>
        </div>

        {error && (
          <div className="rounded-md bg-red-50 border border-red-200 p-4 text-red-700 text-sm">
            {error}
          </div>
        )}

        {!error && reports.length === 0 && (
          <div className="text-center py-12 text-muted-foreground">
            No {isHighOnly ? "high-demand " : ""}nights found for {decodedCity} in this date range.
          </div>
        )}

        {reports.length > 0 && (
          <>
            <div className="rounded-lg border bg-card p-4">
              <p className="text-xs text-muted-foreground mb-3 font-medium uppercase tracking-wide">
                Demand Score by Night
              </p>
              <Suspense fallback={<div className="h-[180px] animate-pulse bg-muted rounded" />}>
                <DemandChart reports={[...reports].sort((a, b) => a.date.localeCompare(b.date))} />
              </Suspense>
            </div>

            <p className="text-sm text-muted-foreground">
              {reports.length} night{reports.length !== 1 ? "s" : ""} found — sorted by demand
            </p>

            <div className="space-y-3">
              {reports.map((report) => (
                <NightCard key={report.date} report={report} />
              ))}
            </div>
          </>
        )}
      </div>
    </main>
  )
}

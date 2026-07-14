"use client"

import { useState } from "react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Badge } from "@/components/ui/badge"
import { getPriceComparison } from "@/lib/api"
import { Event, PriceComparison } from "@/types"

const sourceLabel: Record<string, string> = {
  ticketmaster: "Ticketmaster",
  seatgeek: "SeatGeek",
  stubhub: "StubHub",
  vividseats: "Vivid Seats",
}

function formatShortDate(iso: string) {
  return new Date(iso + "T12:00:00").toLocaleDateString("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
  })
}

function formatPrice(quote: PriceComparison["quotes"][number]) {
  if (!quote.available || quote.minPrice == null) return "Not available"
  const min = `$${quote.minPrice.toFixed(0)}`
  if (quote.maxPrice != null && quote.maxPrice !== quote.minPrice) {
    return `${min} – $${quote.maxPrice.toFixed(0)}`
  }
  return min
}

export function PriceComparisonDialog({ event }: { event: Event }) {
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [comparison, setComparison] = useState<PriceComparison | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleOpenChange(nextOpen: boolean) {
    setOpen(nextOpen)
    if (nextOpen && !comparison && !loading) {
      setLoading(true)
      setError(null)
      try {
        const result = await getPriceComparison(event.id)
        setComparison(result)
      } catch (e) {
        setError("Couldn't load prices right now.")
      } finally {
        setLoading(false)
      }
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger className="text-xs font-semibold text-red-600 hover:underline">
        compare prices here: primary vs secondary market
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Price Comparison · {formatShortDate(event.date)}
          </p>
          <DialogTitle>{event.name}</DialogTitle>
        </DialogHeader>
        <div className="space-y-2">
          {loading && (
            <p className="text-sm text-muted-foreground">Loading prices…</p>
          )}
          {error && <p className="text-sm text-destructive">{error}</p>}
          {comparison && (
            <ul className="space-y-2">
              {comparison.quotes.map((quote) => {
                const isCheapest =
                  quote.available && quote.source === comparison.cheapestSource
                return (
                  <li
                    key={quote.source}
                    className={
                      "flex items-center justify-between rounded-md border px-3 py-2 text-sm" +
                      (quote.available ? "" : " opacity-50")
                    }
                  >
                    <span className="font-medium">
                      {sourceLabel[quote.source] ?? quote.source}
                      {!quote.available && !quote.url && (
                        <span className="ml-1 text-xs text-muted-foreground">
                          (not yet connected)
                        </span>
                      )}
                    </span>
                    <span className="flex items-center gap-2">
                      {formatPrice(quote)}
                      {isCheapest && <Badge variant="default">cheapest</Badge>}
                      {!quote.available && quote.url && (
                        <a
                          href={quote.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-xs font-medium text-primary opacity-100 hover:underline"
                        >
                          View on {sourceLabel[quote.source] ?? quote.source} ↗
                        </a>
                      )}
                    </span>
                  </li>
                )
              })}
            </ul>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}

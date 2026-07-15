"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"

export function DateRangeEditor({
  city,
  from,
  to,
  highOnly,
}: {
  city: string
  from: string
  to: string
  highOnly: boolean
}) {
  const router = useRouter()
  const [localFrom, setLocalFrom] = useState(from)
  const [localTo, setLocalTo] = useState(to)

  const isDirty = localFrom !== from || localTo !== to

  function handleUpdate() {
    const params = new URLSearchParams({ from: localFrom, to: localTo, highOnly: String(highOnly) })
    router.push(`/results/${encodeURIComponent(city)}?${params}`)
  }

  return (
    <div className="flex items-center gap-2">
      <Input
        type="date"
        value={localFrom}
        onChange={(e) => setLocalFrom(e.target.value)}
        className="h-8 w-auto"
      />
      <span className="text-muted-foreground text-sm">→</span>
      <Input
        type="date"
        value={localTo}
        onChange={(e) => setLocalTo(e.target.value)}
        className="h-8 w-auto"
      />
      {isDirty && (
        <Button size="sm" onClick={handleUpdate}>
          Update
        </Button>
      )}
    </div>
  )
}

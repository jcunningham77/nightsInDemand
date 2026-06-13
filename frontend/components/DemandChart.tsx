"use client"

import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from "recharts"
import { CityNightReport } from "@/types"
import { demandColor } from "@/lib/demand"

function shortDate(iso: string) {
  const d = new Date(iso + "T12:00:00")
  return d.toLocaleDateString("en-US", { month: "short", day: "numeric" })
}

function shortDateWithWeekday(iso: string) {
  const d = new Date(iso + "T12:00:00")
  const weekday = d.toLocaleDateString("en-US", { weekday: "short" })
  const date = d.toLocaleDateString("en-US", { month: "short", day: "numeric" })
  return `${date} (${weekday})`
}

export function DemandChart({ reports }: { reports: CityNightReport[] }) {
  const data = reports.map((r) => ({
    date: shortDate(r.date),
    fullDate: shortDateWithWeekday(r.date),
    score: r.demandScore,
    label: r.demandLabel,
    events: r.eventCount,
  }))

  return (
    <ResponsiveContainer width="100%" height={180}>
      <BarChart data={data} margin={{ top: 4, right: 8, left: -20, bottom: 0 }}>
        <XAxis dataKey="date" tick={{ fontSize: 11 }} />
        <YAxis domain={[0, 10]} tick={{ fontSize: 11 }} />
        <Tooltip
          formatter={(value, _, props) =>
            [`Score: ${value}/10 · ${props.payload.events} events`, props.payload.label]
          }
          labelFormatter={(_, payload) => payload?.[0]?.payload?.fullDate ?? ""}
        />
        <Bar dataKey="score" radius={[4, 4, 0, 0]}>
          {data.map((entry, i) => (
            <Cell key={i} fill={demandColor[entry.label]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}

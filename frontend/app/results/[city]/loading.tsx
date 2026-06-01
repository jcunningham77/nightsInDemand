export default function Loading() {
  return (
    <main className="min-h-screen bg-background px-4 py-8">
      <div className="max-w-2xl mx-auto space-y-6">

        {/* Header skeleton */}
        <div className="flex items-center justify-between">
          <div className="space-y-2">
            <div className="h-7 w-36 rounded-md bg-muted animate-pulse" />
            <div className="h-4 w-48 rounded-md bg-muted animate-pulse" />
          </div>
          <div className="h-9 w-28 rounded-md bg-muted animate-pulse" />
        </div>

        {/* Toggle skeleton */}
        <div className="flex gap-2">
          <div className="h-9 w-36 rounded-md bg-muted animate-pulse" />
          <div className="h-9 w-24 rounded-md bg-muted animate-pulse" />
        </div>

        {/* Chart skeleton */}
        <div className="rounded-lg border bg-card p-4">
          <div className="h-4 w-40 rounded-md bg-muted animate-pulse mb-3" />
          <div className="h-[180px] rounded-md bg-muted animate-pulse" />
        </div>

        <div className="h-4 w-32 rounded-md bg-muted animate-pulse" />

        {/* Card skeletons */}
        {[...Array(4)].map((_, i) => (
          <div key={i} className="rounded-lg border bg-card p-4 space-y-3">
            <div className="flex items-center justify-between">
              <div className="h-5 w-56 rounded-md bg-muted animate-pulse" />
              <div className="h-7 w-28 rounded-full bg-muted animate-pulse" />
            </div>
            <div className="space-y-2">
              {[...Array(3)].map((_, j) => (
                <div key={j} className="flex gap-3">
                  <div className="h-4 w-4 rounded bg-muted animate-pulse mt-0.5" />
                  <div className="flex-1 space-y-1">
                    <div className="h-4 w-3/4 rounded bg-muted animate-pulse" />
                    <div className="h-3 w-1/2 rounded bg-muted animate-pulse" />
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </main>
  )
}

"use client"

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"

export function ScoreInfoDialog() {
  return (
    <Dialog>
      <DialogTrigger className="inline-flex items-center justify-center w-4 h-4 rounded-full bg-muted text-muted-foreground text-[10px] font-bold hover:bg-muted/80 transition-colors" aria-label="How is the demand score calculated?">
        i
      </DialogTrigger>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>How is the demand score calculated?</DialogTitle>
        </DialogHeader>
        <div className="space-y-4 text-sm text-muted-foreground">
          <p>
            Each night is scored out of <span className="font-semibold text-foreground">10</span> based
            on the number of significant events and their estimated attendance.
          </p>
          <div className="rounded-md bg-muted p-3 space-y-1 font-mono text-xs">
            <p>+2 per significant event</p>
            <p>+3 if attendance &gt; 50,000</p>
            <p>+2 if attendance &gt; 20,000</p>
            <p>+1 otherwise</p>
            <p className="text-muted-foreground/60 pt-1">capped at 10</p>
          </div>
          <p>
            <span className="font-semibold text-foreground">Significant events</span>{" "}are sports games
            (any size) and concerts at venues with 10,000+ capacity. Smaller venue
            shows are shown for context but don&apos;t affect the score.
          </p>
        </div>
      </DialogContent>
    </Dialog>
  )
}

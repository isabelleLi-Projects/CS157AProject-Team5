import type { ReactNode } from 'react'

// Historical trends for the admin dashboard. Data comes from TrendsDAO via study-spots.jsp.
export type Trends = {
  reportsPerDay?: Array<{ label: string; value: number }>
  crowdednessByTime?: Array<{ label: string; available: number; moderate: number; crowded: number; total: number }>
  mostActiveSpots?: Array<{ label: string; visits: number; reports: number; reviews: number }>
  redemptionsPerWeek?: Array<{ label: string; value: number; points: number }>
}

const GREEN = '#2d5a3d'
const AMBER = '#e8a838'
const RED = '#b83c2a'
const BLUE = '#6289a8'

export default function AdminTrends({ trends }: { trends?: Trends }) {
  const t = trends ?? {}
  return (
    <div className="grid gap-5 xl:grid-cols-2">
      <Panel title="Reports per day" note="How many condition reports students sent each day (most recent 30 days with reports)">
        <Columns rows={(t.reportsPerDay ?? []).map((r) => ({ label: r.label, value: r.value, title: `${r.value} reports` }))} color={GREEN} />
      </Panel>

      <Panel title="Crowdedness by time of day" note="Share of reports that said Available, Moderate, or Crowded">
        {(t.crowdednessByTime ?? []).length === 0 ? (
          <Empty />
        ) : (
          <div className="space-y-3">
            {(t.crowdednessByTime ?? []).map((row) => (
              <div key={row.label}>
                <div className="mb-1 flex justify-between text-xs">
                  <span className="text-secondary">{row.label}</span>
                  <span className="text-muted">
                    {Math.round((row.crowded / Math.max(row.total, 1)) * 100)}% crowded · {row.total} reports
                  </span>
                </div>
                <div className="flex h-3 overflow-hidden rounded-full bg-[#edf0ea]">
                  <div style={{ width: `${(row.available / Math.max(row.total, 1)) * 100}%`, background: GREEN }} title={`${row.available} available`} />
                  <div style={{ width: `${(row.moderate / Math.max(row.total, 1)) * 100}%`, background: AMBER }} title={`${row.moderate} moderate`} />
                  <div style={{ width: `${(row.crowded / Math.max(row.total, 1)) * 100}%`, background: RED }} title={`${row.crowded} crowded`} />
                </div>
              </div>
            ))}
            <Legend items={[['Available', GREEN], ['Moderate', AMBER], ['Crowded', RED]]} />
            <p className="text-[11px] text-muted">Morning 6–11, Afternoon 12–4, Evening 5–9, Night 10–5.</p>
          </div>
        )}
      </Panel>

      <Panel title="Most active study spots" note="Visits, reports, and reviews per spot">
        {(t.mostActiveSpots ?? []).length === 0 ? (
          <Empty />
        ) : (
          <div className="space-y-3">
            {(() => {
              const rows = t.mostActiveSpots ?? []
              const max = Math.max(...rows.map((r) => r.visits + r.reports + r.reviews), 1)
              return rows.map((row) => (
                <div key={row.label}>
                  <div className="mb-1 flex justify-between gap-3 text-xs">
                    <span className="truncate text-secondary">{row.label}</span>
                    <span className="shrink-0 tabular-nums text-muted">
                      {row.visits} visits · {row.reports} reports · {row.reviews} reviews
                    </span>
                  </div>
                  <div className="flex h-2 overflow-hidden rounded-full bg-[#edf0ea]">
                    <div style={{ width: `${(row.visits / max) * 100}%`, background: BLUE }} />
                    <div style={{ width: `${(row.reports / max) * 100}%`, background: GREEN }} />
                    <div style={{ width: `${(row.reviews / max) * 100}%`, background: AMBER }} />
                  </div>
                </div>
              ))
            })()}
            <Legend items={[['Visits', BLUE], ['Reports', GREEN], ['Reviews', AMBER]]} />
          </div>
        )}
      </Panel>

      <Panel title="Rewards redeemed per week" note="Number of rewards redeemed each week (week starting Monday)">
        <Columns
          rows={(t.redemptionsPerWeek ?? []).map((r) => ({ label: r.label, value: r.value, title: `${r.value} rewards, ${r.points} points` }))}
          color={AMBER}
        />
      </Panel>
    </div>
  )
}

function Panel({ title, note, children }: { title: string; note: string; children: ReactNode }) {
  return (
    <section className="surface-background standard-border rounded-2xl p-5">
      <h2 className="font-display text-xl text-main">{title}</h2>
      <p className="mb-4 text-xs text-muted">{note}</p>
      {children}
    </section>
  )
}

// Vertical bars; the value is printed on each bar so no axis is needed.
function Columns({ rows, color }: { rows: Array<{ label: string; value: number; title: string }>; color: string }) {
  if (!rows.length) return <Empty />
  const max = Math.max(...rows.map((r) => r.value), 1)
  return (
    <div className="flex h-44 items-end gap-1.5 overflow-x-auto pb-1">
      {rows.map((row) => (
        <div key={row.label} className="flex min-w-[28px] flex-1 flex-col items-center justify-end gap-1" title={row.title}>
          <span className="text-[10px] font-semibold tabular-nums text-main">{row.value}</span>
          <div className="w-full rounded-t" style={{ height: `${(row.value / max) * 110}px`, minHeight: 3, background: color }} />
          <span className="text-[10px] tabular-nums text-muted">{row.label}</span>
        </div>
      ))}
    </div>
  )
}

function Legend({ items }: { items: Array<[string, string]> }) {
  return (
    <div className="flex flex-wrap gap-3 text-[11px] text-muted">
      {items.map(([label, color]) => (
        <span key={label} className="flex items-center gap-1">
          <span className="inline-block h-2 w-2 rounded-full" style={{ background: color }} />
          {label}
        </span>
      ))}
    </div>
  )
}

function Empty() {
  return <p className="rounded-xl bg-[#f7f4ef] p-4 text-center text-sm text-muted">No data yet.</p>
}

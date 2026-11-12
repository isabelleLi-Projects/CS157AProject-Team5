import type { ReactNode } from 'react'
import Header from '../components/Header'
import FlashBanner from '../components/FlashBanner'
import { mainPageUrl, spotDetailUrl } from '../utils/links'

// My history (Functional Requirement #8). All data comes from window.studentHistory (history.jsp).

export type PointsEvent = { at: string; what: string; item: string; points: number }
export type SpotHistory = {
  spotId: number
  name: string
  building: string
  active: number
  visits: number
  reports: number
  ratings: number
  reviews: number
  lastAt: string
}
export type Suggestion = { name: string; building: string; status: string; at: string }
export type StudentHistory = {
  points: number
  earned: number
  spent: number
  activity: PointsEvent[]
  spots: SpotHistory[]
  suggestions: Suggestion[]
}

declare global {
  interface Window {
    studentHistory?: StudentHistory
  }
}

const SUGGESTION_LABELS: Record<string, string> = { pending: 'Waiting for review', approved: 'Approved', rejected: 'Not approved' }

export default function StudentHistoryPage({ history }: { history: StudentHistory }) {
  // Sample data can give students points from before this page existed.
  const otherPoints = history.points - (history.earned - history.spent)

  return (
    <div className="app-background min-h-screen">
      <Header
        isLoggedIn
        activeNav=""
        onNavChange={(nav) => {
          window.location.href = mainPageUrl(nav)
        }}
        onLoginToggle={() => {}}
      />

      <main className="mx-auto max-w-5xl px-6 pb-16">
        <a href={mainPageUrl('Home')} className="btn-link mt-6 inline-block text-sm">
          ← Back to home
        </a>
        <FlashBanner />
        <h1 className="mt-4 font-display text-3xl text-main">My history</h1>
        <p className="mt-1 text-sm text-muted">The study spots you've been active at, and the points you've earned and spent.</p>

        <section className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
          <Stat label="Current points" value={history.points} />
          <Stat label="Points earned" value={history.earned} sign="+" />
          <Stat label="Points spent" value={history.spent} sign="−" />
        </section>
        {otherPoints !== 0 && (
          <p className="mt-2 text-xs text-muted">
            Your balance also includes {Math.abs(otherPoints)} points {otherPoints > 0 ? 'given' : 'removed'} outside these activities (for
            example, starting points).
          </p>
        )}

        <Card title="Your study spots" note="Spots you visited, reported, rated, or reviewed">
          {history.spots.length === 0 ? (
            <Empty text="Open a study spot, report its conditions, rate a report, or leave a review, and it will show up here." />
          ) : (
            <ul className="divide-y divide-[#e2ddd5]">
              {history.spots.map((spot) => (
                <li key={spot.spotId} className="flex flex-wrap items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                  <div className="min-w-0">
                    {spot.active ? (
                      <a href={spotDetailUrl(spot.spotId)} className="text-sm font-medium text-main hover:underline">
                        {spot.name}
                      </a>
                    ) : (
                      <span className="text-sm font-medium text-muted">{spot.name} (no longer listed)</span>
                    )}
                    <p className="text-xs text-muted">
                      {spot.building} · last activity {spot.lastAt}
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    <Chip count={spot.visits} label="Visited" />
                    <Chip count={spot.reports} label="Reported" />
                    <Chip count={spot.ratings} label="Rated" />
                    <Chip count={spot.reviews} label="Reviewed" />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card title="Points activity" note="+2 per rating, +5 per review, +15 per approved spot suggestion">
          {history.activity.length === 0 ? (
            <Empty text="Rate reports, write reviews, or suggest study spots to start earning points." />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-xs text-muted">
                    <th className="pb-2 pr-4 font-medium">When</th>
                    <th className="pb-2 pr-4 font-medium">What</th>
                    <th className="pb-2 pr-4 font-medium">Spot or reward</th>
                    <th className="pb-2 text-right font-medium">Points</th>
                  </tr>
                </thead>
                <tbody>
                  {history.activity.map((event, index) => (
                    <tr key={index} className="border-t border-[#e2ddd5]">
                      <td className="whitespace-nowrap py-2 pr-4 text-muted">{event.at}</td>
                      <td className="py-2 pr-4 text-main">{event.what}</td>
                      <td className="py-2 pr-4 text-secondary">{event.item}</td>
                      <td className={`py-2 text-right font-semibold tabular-nums ${event.points < 0 ? 'text-[#b83c2a]' : 'text-[#2d5a3d]'}`}>
                        {event.points > 0 ? `+${event.points}` : `−${Math.abs(event.points)}`}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>

        <Card title="Spots you suggested" note="You earn 15 points when an admin approves one">
          {history.suggestions.length === 0 ? (
            <Empty text="Know a good study spot that's missing? Suggest it on Find Study Spots." />
          ) : (
            <ul className="divide-y divide-[#e2ddd5]">
              {history.suggestions.map((suggestion, index) => (
                <li key={index} className="flex flex-wrap items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                  <div>
                    <p className="text-sm font-medium text-main">{suggestion.name}</p>
                    <p className="text-xs text-muted">
                      {suggestion.building} · suggested {suggestion.at}
                    </p>
                  </div>
                  <span
                    className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${
                      suggestion.status === 'approved'
                        ? 'bg-[#d1ead9] text-[#2d5a3d]'
                        : suggestion.status === 'rejected'
                          ? 'bg-[#f6dcd7] text-[#8c2418]'
                          : 'bg-[#f7ecd0] text-[#7a5a12]'
                    }`}
                  >
                    {SUGGESTION_LABELS[suggestion.status] ?? suggestion.status}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </main>
    </div>
  )
}

function Stat({ label, value, sign }: { label: string; value: number; sign?: string }) {
  return (
    <div className="surface-background standard-border rounded-2xl p-4">
      <p className="text-xs text-muted">{label}</p>
      <p className="mt-1 font-display text-3xl tabular-nums text-main">
        {sign && value > 0 ? sign : ''}
        {value.toLocaleString()}
      </p>
    </div>
  )
}

function Card({ title, note, children }: { title: string; note: string; children: ReactNode }) {
  return (
    <section className="surface-background standard-border mt-6 rounded-2xl p-5">
      <div className="mb-4">
        <h2 className="font-display text-xl text-main">{title}</h2>
        <p className="text-xs text-muted">{note}</p>
      </div>
      {children}
    </section>
  )
}

function Chip({ count, label }: { count: number; label: string }) {
  if (!count) return null
  return (
    <span className="rounded-full bg-[#edf5ee] px-2 py-0.5 text-[11px] font-medium text-[#2d5a3d]">
      {label} {count > 1 ? `×${count}` : ''}
    </span>
  )
}

function Empty({ text }: { text: string }) {
  return <p className="rounded-xl bg-[#f7f4ef] p-4 text-center text-sm text-muted">{text}</p>
}

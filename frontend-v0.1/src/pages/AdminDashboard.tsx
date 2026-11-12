import { useEffect, useMemo, useState, type ReactNode } from 'react'
import MapView from '../components/MapView'
import { STUDY_SPOTS } from '../data/studySpots'
import type { StudySpot } from '../model/StudySpot'
import AdminHeader from '../components/AdminHeader'
import AdminSidebar, { type AdminTab } from '../components/AdminSidebar'
import SpotForm from '../components/AdminSpotForm'
import RewardForm from '../components/AdminRewardForm'

type AdminRow = {
  id: number
  label: string
  secondary: string
  status?: string
  meta?: string
  active?: boolean // present when the admin can make this row active/inactive
  details?: string // extra line shown under a row (Suggested Spots)
}

// What a Deactivate/Reactivate button changes, and which tab to come back to.
type ToggleTarget = { type: 'student' | 'spot' | 'review' | 'report'; tab: AdminTab }

type AdminDashboardData = {
  spotClicks?: Array<{ spot: string; clicks: number }>
  reports?: AdminRow[]
  reviews?: AdminRow[]
  activityLogs?: AdminRow[]
  recentRedemptions?: AdminRow[]
  users?: AdminRow[]
  spots?: AdminRow[]
  suggestions?: AdminRow[]
  studySpots?: StudySpot[]
  counts?: { reports?: number; reviews?: number; users?: number; activeSpots?: number; redemptions?: number }
}
type AdminBuilding = {
  id: number
  acronym: string // doesn't dropdown display name 
  name: string
  photoUrl?: string // photo of another spot in this building ("" if none)
  hours?: Record<string, [string, string]> // saved building_hours, e.g. Monday: ["07:00", "01:00"]
}

declare global {
  interface Window {
    adminDashboard?: AdminDashboardData
    adminBuildings?: AdminBuilding[]
    adminMessage?: string
    adminError?: string
    adminTab?: string
  }
}

const emptyData: AdminDashboardData = { reports: [], reviews: [], activityLogs: [], recentRedemptions: [], counts: {} }

const tabs: AdminTab[] = ['Dashboard', 'Add Study Spot', 'Suggested Spots', 'Reported Students', 'Add Rewards', 'Reports', 'Reviews', 'All Users', 'Historical Trends']

export default function AdminDashboard() {
  // admin-action can send the admin back to a tab (for example after a form error)
  const [activeTab, setActiveTab] = useState<AdminTab>(() => tabs.find((tab) => tab === window.adminTab) ?? 'Dashboard')
  // "Saved" / error banner from admin-action. It is shown once, then cleared when the admin moves to another tab.
  const [banner, setBanner] = useState(() => ({ error: window.adminError ?? '', message: window.adminMessage ?? '' }))
  const goToTab = (tab: AdminTab) => {
    setActiveTab(tab)
    setBanner({ error: '', message: '' })
  }

  // Remove ?adminMessage=... from the address bar so refreshing the page doesn't show the banner again.
  useEffect(() => {
    if (window.location.search) window.history.replaceState(null, '', window.location.pathname)
  }, [])
  const data = useMemo(() => ({ ...emptyData, ...window.adminDashboard, studySpots: window.adminDashboard?.studySpots ?? STUDY_SPOTS }), [])
  const counts = { ...emptyData.counts, ...data.counts }

  return (
    <div className="app-background min-h-screen">
      <AdminHeader />

      <div className="mx-auto grid max-w-7xl gap-6 px-5 py-6 lg:grid-cols-[220px_minmax(0,1fr)]">
        <AdminSidebar tabs={tabs} activeTab={activeTab} onTabChange={goToTab} />

        <main className="min-w-0">
          {(banner.error || banner.message) && (
            <p role={banner.error ? 'alert' : 'status'} className={`mb-4 rounded-xl px-4 py-3 text-sm ${banner.error ? 'flash-error' : 'flash-success'}`}>
              {banner.error || banner.message}
            </p>
          )}
          {activeTab === 'Dashboard' ? <DashboardHome data={data} counts={counts} onNavigate={goToTab} /> : <AdminSection tab={activeTab} data={data} />}
        </main>
      </div>
    </div>
  )
}

// includes the hero section
function DashboardHome({ data, counts, onNavigate }: { data: AdminDashboardData; counts: Record<string, number | undefined>; onNavigate: (tab: AdminTab) => void }) {
  const clickTotal = data.spotClicks?.reduce((sum, spot) => sum + spot.clicks, 0)
  return (
    <>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div><p className="text-sm text-muted">Overview</p><h1 className="font-display text-3xl text-main">Administrator dashboard</h1></div>
      </div>

      <section className="mb-6 grid grid-cols-2 gap-3 xl:grid-cols-4">
        <MetricCard label="Active study spots" value={counts.activeSpots ?? 0} tone="green" />
        <MetricCard label="Reports to review" value={counts.reports ?? 0} tone="amber" />
        <MetricCard label="Flagged reviews" value={counts.reviews ?? 0} tone="red" />
        <MetricCard label="Registered users" value={counts.users ?? 0} tone="blue" />
      </section>

      <div className="grid gap-5 xl:grid-cols-2">
        <Panel title="Student clicks on study spots" action="Historical Trends" onAction={() => onNavigate('Historical Trends')}>
          {data.spotClicks?.length ? <MiniBars values={data.spotClicks} /> : <EmptyState title="Click tracking is not configured" text="Add a study_spot_views table before displaying this metric." />}
        </Panel>
        <Panel title="User reports, reviews, and activity logs" action="View activity" onAction={() => onNavigate('Reports')}>
          <ActivityList rows={[...(data.reports ?? []).slice(0, 2), ...(data.reviews ?? []).slice(0, 1), ...(data.activityLogs ?? []).slice(0, 2)]} />
        </Panel>
        <Panel title="Recently redeemed rewards" action="Add rewards" onAction={() => onNavigate('Add Rewards')}>
          <ActivityList rows={data.recentRedemptions ?? []} showStatus />
        </Panel>
        <Panel title="Map study spots" action="Add study spots" onAction={() => onNavigate('Add Study Spot')}>
          {data.studySpots?.length ? <MapView spots={data.studySpots} activeFilter="All" /> : <EmptyState title="No study spots available" text="Add a study spot to display it on the map." />}
        </Panel>
      </div>
    </>
  )
}

function MetricCard({ label, value, tone }: { label: string; value: number; tone: string }) {
  return <div className="surface-background standard-border rounded-2xl p-4"><div className={`mb-3 h-2 w-10 rounded-full ${tone === 'green' ? 'bg-[#2d5a3d]' : tone === 'amber' ? 'bg-[#e8a838]' : tone === 'red' ? 'bg-[#b83c2a]' : 'bg-[#6289a8]'}`} /><p className="text-xs text-muted">{label}</p><p className="mt-1 font-display text-3xl text-main">{value.toLocaleString()}</p></div>
}

function Panel({ title, action, onAction, children }: { title: string; action: string; onAction: () => void; children: ReactNode }) {
  return <section className="surface-background standard-border rounded-2xl p-5"><div className="mb-4 flex items-center justify-between gap-3"><h2 className="font-display text-xl text-main">{title}</h2><button onClick={onAction} className="btn-link text-xs">{action}</button></div>{children}</section>
}

function ActivityList({ rows, showStatus = false, toggle }: { rows: AdminRow[]; showStatus?: boolean; toggle?: ToggleTarget }) {
  if (!rows.length) return <EmptyState title="Nothing to display" text="New activity will appear here." />
  return <div className="divide-y divide-[#e2ddd5]">{rows.map((row) => <div key={`${row.id}-${row.label}`} className="flex items-center justify-between gap-3 py-3 first:pt-0 last:pb-0"><div className="min-w-0"><p className={`truncate text-sm font-medium ${row.active === false ? 'text-muted line-through' : 'text-main'}`}>{row.label}</p><p className="truncate text-xs text-muted">{row.secondary} · {row.meta}</p></div><div className="flex shrink-0 items-center gap-2">{showStatus && row.status && <span className={`rounded-full px-2 py-1 text-[11px] font-semibold ${row.active === false ? 'bg-[#f6dcd7] text-[#8c2418]' : 'bg-[#d1ead9] text-[#2d5a3d]'}`}>{row.status}</span>}{toggle && row.active !== undefined && <ActiveToggle row={row} target={toggle} />}</div></div>)}</div>
}

// Student suggestions waiting for review, with Approve (spot goes live, student +15 points) and Reject (stays hidden).
function SuggestionList({ rows }: { rows: AdminRow[] }) {
  if (!rows.length) return <EmptyState title="No suggestions to review" text="When a student suggests a study spot, it shows up here." />
  return (
    <div className="divide-y divide-[#e2ddd5]">
      {rows.map((row) => (
        <div key={row.id} className="flex flex-wrap items-start justify-between gap-3 py-4 first:pt-0 last:pb-0">
          <div className="min-w-0 flex-1">
            <p className="text-sm font-medium text-main">{row.label}</p>
            <p className="text-xs text-muted">{row.secondary}</p>
            {row.details && <p className="mt-1 text-xs text-secondary">{row.details}</p>}
            <p className="mt-1 text-xs text-muted">{row.meta}</p>
          </div>
          <div className="flex shrink-0 gap-2">
            {(['approve', 'reject'] as const).map((decision) => (
              <form key={decision} method="post" action={`${window.studyFinderContext ?? ''}/admin-action`}>
                <input type="hidden" name="action" value="reviewSuggestion" />
                <input type="hidden" name="id" value={row.id} />
                <input type="hidden" name="decision" value={decision} />
                <input type="hidden" name="returnTab" value="Suggested Spots" />
                <button type="submit" className={`${decision === 'approve' ? 'btn-primary' : 'btn-secondary'} px-3 py-1.5 text-xs`}>
                  {decision === 'approve' ? 'Approve' : 'Reject'}
                </button>
              </form>
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}

// Deactivate / Reactivate button. Posts to the admin-action servlet, which saves the change and logs it.
function ActiveToggle({ row, target }: { row: AdminRow; target: ToggleTarget }) {
  return (
    <form method="post" action={`${window.studyFinderContext ?? ''}/admin-action`}>
      <input type="hidden" name="action" value="setActive" />
      <input type="hidden" name="type" value={target.type} />
      <input type="hidden" name="id" value={row.id} />
      <input type="hidden" name="active" value={row.active ? '0' : '1'} />
      <input type="hidden" name="returnTab" value={target.tab} />
      <button type="submit" className={`${row.active ? 'btn-secondary' : 'btn-primary'} px-3 py-1 text-xs`}>
        {row.active ? 'Deactivate' : 'Reactivate'}
      </button>
    </form>
  )
}

function MiniBars({ values }: { values: Array<{ spot: string; clicks: number }> }) {
  const max = Math.max(...values.map((value) => value.clicks), 1)
  return <div className="space-y-3">{values.slice(0, 5).map((value) => <div key={value.spot}><div className="mb-1 flex justify-between text-xs"><span className="text-secondary">{value.spot}</span><span className="font-semibold text-main">{value.clicks}</span></div><div className="h-2 rounded-full bg-[#edf0ea]"><div className="h-2 rounded-full bg-[#2d5a3d]" style={{ width: `${(value.clicks / max) * 100}%` }} /></div></div>)}</div>
}

function EmptyState({ title, text }: { title: string; text: string }) {
  return <div className="rounded-xl bg-[#f7f4ef] p-6 text-center"><p className="text-sm font-medium text-main">{title}</p><p className="mt-1 text-xs text-muted">{text}</p></div>
}

function AdminSection({ tab, data }: { tab: AdminTab; data: AdminDashboardData }) {
  const titles: Record<AdminTab, string> = { Dashboard: 'Dashboard', 'Add Study Spot': 'Manage study spots', 'Suggested Spots': 'Spots suggested by students', 'Reported Students': 'Reported students', 'Add Rewards': 'Manage rewards', Reports: 'Reports', Reviews: 'Reviews', 'All Users': 'All users', 'Historical Trends': 'Historical trends' }
  const rows = tab === 'Reports' ? data.reports : tab === 'Reviews' ? data.reviews : tab === 'Reported Students' ? data.activityLogs : tab === 'All Users' ? data.users : []
  // Tabs whose rows can be made active/inactive.
  const toggle: ToggleTarget | undefined = tab === 'Reports' ? { type: 'report', tab } : tab === 'Reviews' ? { type: 'review', tab } : tab === 'All Users' ? { type: 'student', tab } : undefined
  return <section><div className="mb-6"><p className="text-sm text-muted">Administration</p><h1 className="font-display text-3xl text-main">{titles[tab]}</h1></div><div className="surface-background standard-border rounded-2xl p-5">{tab === 'Add Study Spot' ? <SpotForm /> : tab === 'Suggested Spots' ? <SuggestionList rows={data.suggestions ?? []} /> : tab === 'Add Rewards' ? <RewardForm /> : tab === 'Historical Trends' ? <EmptyState title="Historical trends" text="Charts will use preserved reports and the future study_spot_views table." /> : <ActivityList rows={rows ?? []} showStatus toggle={toggle} />}</div>{tab === 'Add Study Spot' && <div className="surface-background standard-border mt-6 rounded-2xl p-5"><h2 className="mb-4 font-display text-xl text-main">Existing study spots</h2><ActivityList rows={data.spots ?? []} showStatus toggle={{ type: 'spot', tab }} /></div>}</section>
}

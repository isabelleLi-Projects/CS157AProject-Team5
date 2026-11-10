import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import MapView from '../components/MapView'
import { STUDY_SPOTS } from '../data/studySpots'
import type { StudySpot } from '../model/StudySpot'
import AdminHeader from '../components/AdminHeader'
import AdminSidebar, { type AdminTab } from '../components/AdminSidebar'
import EditProfile from '../components/EditProfile'
import SpotForm from '../components/AdminSpotForm'
import RewardForm from '../components/AdminRewardForm'

type AdminRow = {
  id: number
  label: string
  secondary: string
  status?: string
  meta?: string
}

type AdminDashboardData = {
  spotClicks?: Array<{ spot: string; clicks: number }>
  reports?: AdminRow[]
  reviews?: AdminRow[]
  activityLogs?: AdminRow[]
  recentRedemptions?: AdminRow[]
  users?: AdminRow[]
  studySpots?: StudySpot[]
  counts?: { reports?: number; reviews?: number; users?: number; activeSpots?: number; redemptions?: number }
}

declare global {
  interface Window {
    adminDashboard?: AdminDashboardData
  }
}

const emptyData: AdminDashboardData = { reports: [], reviews: [], activityLogs: [], recentRedemptions: [], counts: {} }

const tabs: AdminTab[] = ['Dashboard', 'Add Study Spot', 'Reported Students', 'Add Rewards', 'Reports', 'Reviews', 'All Users', 'Historical Trends']

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState<AdminTab>('Dashboard')
  const [profileOpen, setProfileOpen] = useState(false)
  const [editProfileOpen, setEditProfileOpen] = useState(false)
  const data = useMemo(() => ({ ...emptyData, ...window.adminDashboard, studySpots: window.adminDashboard?.studySpots ?? STUDY_SPOTS }), [])
  const counts = { ...emptyData.counts, ...data.counts }
  const profileRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function closeProfile(event: MouseEvent) {
      if (profileRef.current && !profileRef.current.contains(event.target as Node)) setProfileOpen(false)
    }
    document.addEventListener('mousedown', closeProfile)
    return () => document.removeEventListener('mousedown', closeProfile)
  }, [])

  return (
    <div className="app-background min-h-screen">
      <AdminHeader profileOpen={profileOpen} setProfileOpen={setProfileOpen} profileRef={profileRef} onEditProfile={() => { setEditProfileOpen(true); setProfileOpen(false) }} />

      <div className="mx-auto grid max-w-7xl gap-6 px-5 py-6 lg:grid-cols-[220px_minmax(0,1fr)]">
        <AdminSidebar tabs={tabs} activeTab={activeTab} onTabChange={setActiveTab} />

        <main className="min-w-0">
          {activeTab === 'Dashboard' ? <DashboardHome data={data} counts={counts} onNavigate={setActiveTab} /> : <AdminSection tab={activeTab} data={data} />}
        </main>
      </div>
      {editProfileOpen && <EditProfile onClose={() => setEditProfileOpen(false)} />}
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

function ActivityList({ rows, showStatus = false }: { rows: AdminRow[]; showStatus?: boolean }) {
  if (!rows.length) return <EmptyState title="Nothing to display" text="New activity will appear here." />
  return <div className="divide-y divide-[#e2ddd5]">{rows.map((row) => <div key={`${row.id}-${row.label}`} className="flex items-center justify-between gap-3 py-3 first:pt-0 last:pb-0"><div className="min-w-0"><p className="truncate text-sm font-medium text-main">{row.label}</p><p className="truncate text-xs text-muted">{row.secondary} · {row.meta}</p></div>{showStatus && row.status && <span className="rounded-full bg-[#d1ead9] px-2 py-1 text-[11px] font-semibold text-[#2d5a3d]">{row.status}</span>}</div>)}</div>
}

function MiniBars({ values }: { values: Array<{ spot: string; clicks: number }> }) {
  const max = Math.max(...values.map((value) => value.clicks), 1)
  return <div className="space-y-3">{values.slice(0, 5).map((value) => <div key={value.spot}><div className="mb-1 flex justify-between text-xs"><span className="text-secondary">{value.spot}</span><span className="font-semibold text-main">{value.clicks}</span></div><div className="h-2 rounded-full bg-[#edf0ea]"><div className="h-2 rounded-full bg-[#2d5a3d]" style={{ width: `${(value.clicks / max) * 100}%` }} /></div></div>)}</div>
}

function EmptyState({ title, text }: { title: string; text: string }) {
  return <div className="rounded-xl bg-[#f7f4ef] p-6 text-center"><p className="text-sm font-medium text-main">{title}</p><p className="mt-1 text-xs text-muted">{text}</p></div>
}

function AdminSection({ tab, data }: { tab: AdminTab; data: AdminDashboardData }) {
  const titles: Record<AdminTab, string> = { Dashboard: 'Dashboard', 'Add Study Spot': 'Manage study spots', 'Reported Students': 'Reported students', 'Add Rewards': 'Manage rewards', Reports: 'Reports', Reviews: 'Reviews', 'All Users': 'All users', 'Historical Trends': 'Historical trends' }
  const rows = tab === 'Reports' ? data.reports : tab === 'Reviews' ? data.reviews : tab === 'Reported Students' ? data.activityLogs : tab === 'All Users' ? data.users : []
  return <section><div className="mb-6"><p className="text-sm text-muted">Administration</p><h1 className="font-display text-3xl text-main">{titles[tab]}</h1></div><div className="surface-background standard-border rounded-2xl p-5">{tab === 'Add Study Spot' ? <SpotForm /> : tab === 'Add Rewards' ? <RewardForm /> : tab === 'Historical Trends' ? <EmptyState title="Historical trends" text="Charts will use preserved reports and the future study_spot_views table." /> : <ActivityList rows={rows ?? []} showStatus />}</div></section>
}

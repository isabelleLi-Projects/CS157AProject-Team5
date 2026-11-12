export type AdminTab = 'Dashboard' | 'Add Study Spot' | 'Suggested Spots' | 'Student Photos' | 'Reported Students' | 'Add Rewards' | 'Reports' | 'Reviews' | 'All Users' | 'Historical Trends'

type AdminSidebarProps = { tabs: AdminTab[]; activeTab: AdminTab; onTabChange: (tab: AdminTab) => void }

export default function AdminSidebar({ tabs, activeTab, onTabChange }: AdminSidebarProps) {
  return <aside className="surface-background standard-border h-fit rounded-2xl p-3"><p className="px-3 pb-2 text-xs font-semibold uppercase tracking-wider text-muted">Admin tools</p><nav className="space-y-1">{tabs.map((tab) => <button key={tab} onClick={() => onTabChange(tab)} className={`w-full rounded-xl px-3 py-2.5 text-left text-sm transition ${activeTab === tab ? 'primary-background font-semibold' : 'text-secondary hover:bg-[#f7f4ef]'}`}>{tab}</button>)}</nav></aside>
}

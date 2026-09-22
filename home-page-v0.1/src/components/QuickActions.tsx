type QuickActionsProps = {
  onNavigate: (nav: string) => void
}

const ACTIONS = [
  { icon: '🗺️', label: 'Find Spots', nav: 'Find Spots', desc: 'Browse all' },
  { icon: '⭐', label: 'Rewards', nav: 'Rewards', desc: 'Redeem pts' },
]

export default function QuickActions({ onNavigate }: QuickActionsProps) {
  return (
    <div className="grid grid-cols-2 gap-3 mb-8">
      {ACTIONS.map((action) => (
        <button
          key={action.label}
          onClick={() => onNavigate(action.nav)}
          className="card p-4 flex flex-col items-center gap-1 hover:border-[#2d5a3d] hover:shadow-md transition-all duration-150"
        >
          <span className="text-2xl">{action.icon}</span>
          <span className="text-xs font-semibold text-main">{action.label}</span>
          <span className="text-[10px] text-muted">{action.desc}</span>
        </button>
      ))}
    </div>
  )
}

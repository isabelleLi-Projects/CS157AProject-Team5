import type { StudySpot } from '../model/StudySpot'
import SpotCard from './SpotCard'

type SpotSectionProps = {
  title: string
  spots: StudySpot[]
  compact?: boolean
  onViewAll: () => void
}

export default function SpotSection({ title, spots, compact = false, onViewAll }: SpotSectionProps) {
  return (
    <section className="mb-8">
      <div className="flex items-center justify-between mb-3">
        <h2 className="font-display text-xl text-main">{title}</h2>
        <button onClick={onViewAll} className="btn-link text-xs">View all →</button>
      </div>
      <div className={compact ? 'grid grid-cols-2 gap-3' : 'grid grid-cols-1 gap-4'}>
        {spots.map((spot) => <SpotCard key={spot.id} spot={spot} compact={compact} />)}
      </div>
    </section>
  )
}

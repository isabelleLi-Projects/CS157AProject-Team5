import type { StudySpot } from '../model/StudySpot'
import CrowdBar from './CrowdBar'
import NoiseChip from './NoiseChip'

type SpotCardProps = {
  spot: StudySpot
  compact?: boolean
}

export default function SpotCard({ spot }: SpotCardProps) {
  return (
    <article className="spot-card rounded-2xl">
      <div className="relative">
        <img src={spot.img} alt={spot.name} className="spot-image" />
        <div className="absolute top-2 right-2">
          <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${spot.open ? 'badge-open' : 'badge-closed'}`}>
            {spot.open ? 'Open' : 'Closed'}
          </span>
        </div>
      </div>

      <div className="p-4">
        <div className="flex items-start justify-between gap-2 mb-1">
          <h3 className="font-semibold text-main text-sm leading-tight">{spot.name}</h3>
          <span className="text-xs text-secondary shrink-0">★ {spot.rating}</span>
        </div>
        <p className="text-xs text-muted mb-3">{spot.location}</p>

        <div className="grid grid-cols-3 gap-2 mb-3">
          <div>
            <p className="text-[10px] text-muted mb-1 uppercase tracking-wide">Noise</p>
            <NoiseChip level={spot.noiseLevel} label={spot.noise} />
          </div>
          <div>
            <p className="text-[10px] text-muted mb-1 uppercase tracking-wide">Crowd</p>
            <CrowdBar level={spot.crowdLevel} />
          </div>
          <div>
            <p className="text-[10px] text-muted mb-1 uppercase tracking-wide">Outlets</p>
            <span className="text-xs font-medium text-primary">{spot.outlets}</span>
          </div>
        </div>

        <div className="flex items-center justify-between gap-2">
          <div className="flex gap-1 flex-wrap">
            {spot.tags.slice(0, 2).map((tag) => (
              <span key={tag} className="text-[10px] tag-background text-secondary px-2 py-0.5 rounded-full standard-border">
                {tag}
              </span>
            ))}
          </div>
          <span className="text-[10px] text-muted shrink-0">Updated {spot.updated}</span>
        </div>
      </div>
    </article>
  )
}

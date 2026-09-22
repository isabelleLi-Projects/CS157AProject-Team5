import type { CrowdLevel } from '../model/StudySpot'

export default function CrowdBar({ level }: { level: CrowdLevel }) {
  const fill = level === 'low' ? 1 : level === 'med' ? 2 : 3

  return (
    <div className="flex gap-0.5 items-center" aria-label={`${level} crowd level`}>
      {[1, 2, 3].map((item) => (
        <div
          key={item}
          className={`h-2 w-4 rounded-sm ${item <= fill ? 'primary-background' : 'bg-[#e2ddd5]'}`}
        />
      ))}
    </div>
  )
}

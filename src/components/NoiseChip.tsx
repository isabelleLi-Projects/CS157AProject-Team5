import type { NoiseLevel } from '../model/StudySpot'

type NoiseChipProps = {
  level: NoiseLevel
  label: string
}

export default function NoiseChip({ level, label }: NoiseChipProps) {
  return <span className={`text-xs font-medium px-2 py-0.5 rounded-full noise-${level}`}>{label}</span>
}

import type { Reward } from '../model/StudySpot'

type RewardsPreviewProps = {
  points: number
  rewards: Reward[]
  onViewRewards: () => void
}

export default function RewardsPreview({ points, rewards, onViewRewards }: RewardsPreviewProps) {
  const nextReward = rewards[0]
  const ptsToNext = nextReward.pts - points
  const progress = Math.min((points / nextReward.pts) * 100, 100)

  return (
    <section className="mb-8 primary-background rounded-2xl p-5 text-white relative overflow-hidden">
      <div className="absolute right-4 top-4 text-6xl opacity-10 select-none">🎓</div>
      <div className="flex items-start justify-between mb-3">
        <div>
          <p className="text-xs text-white/70 uppercase tracking-wide mb-1">Your Points</p>
          <p className="font-display text-4xl">{points.toLocaleString()}</p>
        </div>
        <button onClick={onViewRewards} className="reward-button-background text-xs font-semibold px-3 py-1.5 rounded-xl">
          View Rewards →
        </button>
      </div>

      <div className="mb-3">
        <div className="flex justify-between text-xs text-white/70 mb-1.5">
          <span>Next: {nextReward.label}</span>
          <span>{ptsToNext} pts away</span>
        </div>
        <div className="progress-track h-1.5">
          <div className="progress-fill h-full transition-all" style={{ width: `${progress}%` }} />
        </div>
      </div>

      <div className="flex gap-2">
        {rewards.map((reward) => (
          <div key={reward.label} className="reward-background flex-1 rounded-xl p-2 text-center">
            <span className="text-base">{reward.icon}</span>
            <p className="text-[9px] text-white/70 mt-0.5">{reward.pts} pts</p>
          </div>
        ))}
      </div>
    </section>
  )
}

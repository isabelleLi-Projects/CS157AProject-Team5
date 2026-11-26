import type { Reward } from '../model/StudySpot'

type RewardDetailsProps = {
    rewards: Reward[]
    points: number
}

export default function RewardDetails({
    rewards,
    points,
}: RewardDetailsProps) {
    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {rewards.map((reward) => {
                const pointsNeeded = Math.max(reward.pts - points, 0)
                const progress = reward.pts > 0
                    ? Math.min(Math.max((points / reward.pts) * 100, 0), 100)
                    : 100

                const rewardName = reward.faceValue != null
                    ? `$${Number(reward.faceValue).toLocaleString('en-US')} ${reward.label}`
                    : reward.label

                return (
                    <div
                        key={reward.id ?? reward.label}
                        className="surface-background standard-border rounded-2xl p-5 flex flex-col shadow-sm hover:shadow-lg transition-shadow"
                    >
                        {/* Gift card image */}
                        <div className="w-full aspect-[8/5] mb-4 flex items-center justify-center">
                            {reward.img ? (
                                <img
                                    src={reward.img}
                                    alt={rewardName}
                                    className="w-full h-full rounded-xl object-contain"
                                />
                            ) : (
                                <span className="text-6xl">
                                    {reward.icon}
                                </span>
                            )}
                        </div>

                        {/* Reward name and description */}
                        <h2 className="font-display text-xl text-main">
                            {rewardName}
                        </h2>

                        <p className="text-sm text-muted mt-2">
                            {reward.description || 'No description available.'}
                        </p>

                        {/* Required points and stock */}
                        <p className="font-display text-2xl text-primary mt-4">
                            {reward.pts.toLocaleString()} pts
                        </p>

                        <p className="text-sm text-muted mt-1">
                            Available stock: {reward.stock ?? 'Unknown'}
                        </p>

                        {/* Reward progress */}
                        <div className="mt-auto pt-5">
                            <div className="flex justify-between text-xs text-muted mb-2">
                                <span>
                                    {points.toLocaleString()} / {reward.pts.toLocaleString()} pts
                                </span>
                                <span>{Math.floor(progress)}%</span>
                            </div>

                            <div className="progress-track h-2 rounded-full overflow-hidden">
                                <div
                                    className="progress-fill h-full rounded-full transition-all"
                                    style={{ width: `${progress}%` }}
                                />
                            </div>

                            {/* Redemption status */}
                            <div className="mt-3 min-h-[24px]">
                                {reward.stock === 0 ? (
                                    <p className="text-sm text-red-600 font-medium">
                                        Out of stock
                                    </p>
                                ) : pointsNeeded > 0 ? (
                                    <p className="text-sm text-amber-600 font-medium">
                                        You need {pointsNeeded.toLocaleString()} more points.
                                    </p>
                                ) : (
                                    <p className="text-sm text-green-600 font-medium">
                                        Ready to redeem!
                                    </p>
                                )}
                            </div>

                            {/* Redeem button */}
                            <form
                                method="post"
                                action={`${window.studyFinderContext ?? ''}/redeem.jsp`}
                                className="mt-4"
                            >
                                <input
                                    type="hidden"
                                    name="rewardId"
                                    value={reward.id ?? ''}
                                />

                                <button
                                    type="submit"
                                    disabled={!reward.id}
                                    className="btn-primary w-full py-3 rounded-xl text-sm font-semibold"
                                >
                                    Redeem
                                </button>
                            </form>
                        </div>
                    </div>
                )
            })}
        </div>
    )
}
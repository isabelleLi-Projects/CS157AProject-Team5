import type { Reward } from '../model/StudySpot'

type RewardDetailsProps = {
    rewards: Reward[]
    points: number
    redeemedRewardIds: number[]
}

export default function RewardDetails({
    rewards,
    points,
    redeemedRewardIds
}: RewardDetailsProps) {
    return (
        <div className="grid grid-cols-1 gap-4">
            {rewards.map((reward) => (
                <div
                    key={reward.id ?? reward.label}
                    className="surface-background standard-border rounded-2xl p-5"
                >
                    {reward.img ? (
                        <img
                            src={reward.img}
                            alt={reward.label}
                            className="w-24 h-24 rounded-xl object-cover mb-4"
                        />
                    ) : (
                        <span className="text-4xl">{reward.icon}</span>
                    )}

                    <h2 className="font-display text-xl text-main">
                        {reward.label}
                    </h2>

                    <p className="text-sm text-muted mt-2">
                        {reward.description || 'No description available.'}
                    </p>

                    <p className="text-main mt-3">
                        Points required: {reward.pts}
                    </p>

                    <p className="text-muted text-sm mt-1">
                        Available stock: {reward.stock ?? 'Unknown'}
                    </p>

                    <p className="text-muted text-sm mt-1">
                        Your points: {points}
                    </p>

                    {reward.id != null &&
                        redeemedRewardIds.includes(reward.id) ? (
                        <p className="mt-3">Already redeemed</p>
                    ) : reward.stock === 0 ? (
                        <p className="mt-3">Out of stock</p>
                    ) : points < reward.pts ? (
                        <p className="mt-3">
                            You need {reward.pts - points} more points.
                        </p>
                    ) : (
                        <form
                            method="post"
                            action={`${window.studyFinderContext ?? ''}/redeem.jsp`}
                            className="mt-3"
                        >
                            <input
                                type="hidden"
                                name="rewardId"
                                value={reward.id ?? ''}
                            />

                            <button
                                type="submit"
                                disabled={!reward.id}
                                className="btn-primary px-4 py-2 rounded-xl text-sm"
                            >
                                Redeem
                            </button>
                        </form>
                    )}
                </div>
            ))}
        </div>
    )
}
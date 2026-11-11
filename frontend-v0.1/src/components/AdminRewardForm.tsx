import { useState } from "react"

export default function RewardForm() {
  const [rewardFileName, setRewardFileName] = useState('')
    return (
      <form
        className="grid gap-4 sm:grid-cols-2"
        method="post"
        action={`${window.studyFinderContext ?? ''}/admin-action`}
        encType="multipart/form-data"
      >
        <input type="hidden" name="action" value="addReward" />
        <label className="text-sm font-medium text-main">
          Reward name
          <input
            type="text"
            name="rewardName" required className="search-input mt-1 p-3"
          />
        </label>
  
        <label className="text-sm font-medium text-main">
          Points required
          <input
            type="number"
            min="1"
            name="pointsRequired" required className="search-input mt-1 p-3"
          />
        </label>
  
        <label className="text-sm font-medium text-main">
          Stock quantity
          <input
            type="number"
            min="0"
            name="stockQuantity" required className="search-input mt-1 p-3"
          />
        </label>
  
        <label
          htmlFor="rewardPhoto"
          className="mt-1 flex cursor-pointer justify-between rounded-xl border-2 border-dashed border-[#2d5a3d] bg-[#edf5ee] px-4 py-3 text-sm text-[#2d5a3d]"
        >
          <span>{rewardFileName || 'Choose file'}</span>

          <input
            id="rewardPhoto"
            type="file"
            name="rewardPhoto"
            accept=".jpg,.jpeg,.png,.gif"
            className="hidden"
            onChange={(event) => {
              const file = event.target.files?.[0]
              setRewardFileName(file?.name ?? '')
            }}
          />
        </label>
  
        <label className="text-sm font-medium text-main sm:col-span-2">
          Description
          <textarea
            name="description" className="search-input mt-1 min-h-24 p-3"
          />
        </label>
  
        <div className="flex justify-end sm:col-span-2">
          <button
            type="submit"
            className="btn-primary px-4 py-2"
          >
            Save reward
          </button>
        </div>
      </form>
    )
  }

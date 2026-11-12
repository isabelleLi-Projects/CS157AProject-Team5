import { useState } from 'react'

type EditProfileProps = { onClose: () => void }

// Edit profile window. Posts to edit-profile.jsp, which saves the name and, if a new password
// is entered, checks the current password and saves the new one.
export default function EditProfile({ onClose }: EditProfileProps) {
  const currentUser = window.currentUser
  const [newPassword, setNewPassword] = useState('')
  // Come back to the same study spot page after saving; otherwise the main page.
  const returnTo = window.spotDetail ? `study-spot.jsp?id=${window.spotDetail.spotId}` : ''

  return (
    <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/40 p-4">
      <form
        method="post"
        action={`${window.studyFinderContext ?? ''}/edit-profile.jsp`}
        className="surface-background w-full max-w-md rounded-2xl p-6 shadow-xl"
      >
        <input type="hidden" name="returnTo" value={returnTo} />
        <div className="flex items-center justify-between">
          <h2 className="font-display text-2xl text-main">Edit profile</h2>
          <button type="button" className="text-secondary" onClick={onClose}>
            Close
          </button>
        </div>
        <label className="mt-5 block text-sm font-medium text-main">
          Full name
          <input name="fullName" required maxLength={100} className="search-input mt-1 p-3" defaultValue={currentUser?.name ?? ''} />
        </label>
        <label className="mt-4 block text-sm font-medium text-main">
          Email
          <input className="search-input mt-1 bg-gray-100 p-3" value={currentUser?.email ?? ''} readOnly />
        </label>

        <p className="mt-5 text-sm font-medium text-main">Change password</p>
        <p className="text-xs text-muted">Leave these blank to keep your current password.</p>
        <label className="mt-2 block text-sm text-main">
          New password
          <input
            type="password"
            name="newPassword"
            minLength={8}
            maxLength={128}
            autoComplete="new-password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            className="search-input mt-1 p-3"
          />
        </label>
        {newPassword && (
          <>
            <label className="mt-3 block text-sm text-main">
              Confirm new password
              <input type="password" name="confirmPassword" required autoComplete="new-password" className="search-input mt-1 p-3" />
            </label>
            <label className="mt-3 block text-sm text-main">
              Current password
              <input type="password" name="currentPassword" required autoComplete="current-password" className="search-input mt-1 p-3" />
            </label>
          </>
        )}

        <div className="mt-6 flex justify-end gap-3">
          <button type="button" className="btn-secondary px-4 py-2" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn-primary px-4 py-2">
            Save changes
          </button>
        </div>
      </form>
    </div>
  )
}

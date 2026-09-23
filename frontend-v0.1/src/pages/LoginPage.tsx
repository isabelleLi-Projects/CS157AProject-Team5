import { useState, type FormEvent, type ReactNode } from 'react'
import spartan from '../assets/logo/spartan.png'
import {
  AuthError,
  continueAsGuest,
  logIn,
  signUp,
  type AuthField,
  type SessionUser,
} from '../api/auth'

type Mode = 'login' | 'signup'
type FieldName = AuthField | 'confirmPassword'
type FieldErrors = Partial<Record<FieldName, string>>

type LoginPageProps = {
  onAuthenticated: (user: SessionUser) => void
}

// Same rules the server enforces in AuthValidator.java.
const SJSU_EMAIL = /^[^\s@]+@sjsu\.edu$/i
const MIN_PASSWORD_LENGTH = 8
const FIELD_ORDER: FieldName[] = ['fullName', 'email', 'password', 'confirmPassword']

export default function LoginPage({ onAuthenticated }: LoginPageProps) {
  const [mode, setMode] = useState<Mode>('login')
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState('')
  const [pending, setPending] = useState<'submit' | 'guest' | null>(null)

  const isSignup = mode === 'signup'
  const busy = pending !== null

  const switchMode = (next: Mode) => {
    setMode(next)
    setFieldErrors({})
    setFormError('')
    setPassword('')
    setConfirmPassword('')
  }

  const clearFieldError = (field: FieldName) => {
    if (fieldErrors[field]) {
      setFieldErrors((current) => ({ ...current, [field]: undefined }))
    }
  }

  const validate = (): FieldErrors => {
    const errors: FieldErrors = {}
    const trimmedEmail = email.trim()

    if (isSignup && !fullName.trim()) {
      errors.fullName = 'Enter your full name.'
    }

    if (!trimmedEmail) {
      errors.email = 'Enter your SJSU email.'
    } else if (!SJSU_EMAIL.test(trimmedEmail)) {
      errors.email = 'Use your SJSU email address, ending in @sjsu.edu.'
    }

    if (!password) {
      errors.password = isSignup ? 'Create a password.' : 'Enter your password.'
    } else if (isSignup && password.length < MIN_PASSWORD_LENGTH) {
      errors.password = `Use at least ${MIN_PASSWORD_LENGTH} characters.`
    }

    if (isSignup && password && confirmPassword !== password) {
      errors.confirmPassword = "Passwords don't match."
    }

    return errors
  }

  const focusFirstError = (errors: FieldErrors) => {
    const first = FIELD_ORDER.find((field) => errors[field])
    if (first) {
      document.getElementById(`auth-${first}`)?.focus()
    }
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setFormError('')

    const errors = validate()
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) {
      focusFirstError(errors)
      return
    }

    setPending('submit')
    try {
      const user = isSignup
        ? await signUp(fullName.trim(), email.trim(), password)
        : await logIn(email.trim(), password)
      onAuthenticated(user)
    } catch (error) {
      if (error instanceof AuthError && error.field) {
        const serverErrors = { [error.field]: error.message }
        setFieldErrors(serverErrors)
        focusFirstError(serverErrors)
      } else {
        setFormError(error instanceof Error ? error.message : 'Something went wrong. Try again.')
      }
      setPending(null)
    }
  }

  const handleGuest = async () => {
    setFormError('')
    setFieldErrors({})
    setPending('guest')
    try {
      onAuthenticated(await continueAsGuest())
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Something went wrong. Try again.')
      setPending(null)
    }
  }

  const passwordToggle = (
    <button
      type="button"
      onClick={() => setShowPassword((value) => !value)}
      aria-label={showPassword ? 'Hide password' : 'Show password'}
      aria-pressed={showPassword}
      className="auth-reveal absolute inset-y-0 right-0 px-3.5 text-xs font-semibold text-primary"
    >
      {showPassword ? 'Hide' : 'Show'}
    </button>
  )

  return (
    <div className="app-background min-h-screen md:grid md:grid-cols-[minmax(0,5fr)_minmax(0,6fr)]">
      {/* Brand panel, shown from tablet width up */}
      <aside className="primary-background hidden md:flex flex-col justify-between p-10 lg:p-14">
        <Brand light />

        <div className="max-w-md">
          <p className="font-display text-5xl lg:text-6xl leading-[1.05] tracking-tight">
            Know before you walk over.
          </p>
          <p className="mt-5 text-base leading-relaxed text-white/80 max-w-sm">
            Students report noise, crowds, and outlets from where they&apos;re sitting, so you can
            pick a spot that fits how you study.
          </p>
        </div>
      </aside>

      <main className="flex items-start md:items-center justify-center px-6 py-10 md:py-16">
        <div className="w-full max-w-sm">
          <div className="md:hidden mb-10">
            <Brand />
          </div>

          <h1 className="font-display text-3xl text-main leading-tight">
            {isSignup ? 'Create your account' : 'Log in'}
          </h1>
          <p className="text-sm text-muted mt-2 mb-7">
            {isSignup
              ? "Only @sjsu.edu emails can sign up. You'll be logged in right away."
              : 'Use your SJSU email to see every study spot and add reports.'}
          </p>

          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            {isSignup && (
              <TextField
                id="auth-fullName"
                label="Full name"
                value={fullName}
                onChange={(value) => {
                  setFullName(value)
                  clearFieldError('fullName')
                }}
                autoComplete="name"
                error={fieldErrors.fullName}
              />
            )}

            <TextField
              id="auth-email"
              label="SJSU email"
              type="email"
              value={email}
              onChange={(value) => {
                setEmail(value)
                clearFieldError('email')
              }}
              autoComplete="email"
              placeholder="you@sjsu.edu"
              error={fieldErrors.email}
            />

            <TextField
              id="auth-password"
              label="Password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(value) => {
                setPassword(value)
                clearFieldError('password')
              }}
              autoComplete={isSignup ? 'new-password' : 'current-password'}
              hint={isSignup ? `At least ${MIN_PASSWORD_LENGTH} characters.` : undefined}
              error={fieldErrors.password}
              trailing={passwordToggle}
            />

            {isSignup && (
              <TextField
                id="auth-confirmPassword"
                label="Confirm password"
                type={showPassword ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(value) => {
                  setConfirmPassword(value)
                  clearFieldError('confirmPassword')
                }}
                autoComplete="new-password"
                error={fieldErrors.confirmPassword}
              />
            )}

            {formError && (
              <p role="alert" className="auth-alert text-sm rounded-xl px-4 py-3">
                {formError}
              </p>
            )}

            <button
              type="submit"
              disabled={busy}
              className="btn-primary w-full py-3 text-sm mt-2"
            >
              {pending === 'submit'
                ? isSignup
                  ? 'Creating account…'
                  : 'Logging in…'
                : isSignup
                  ? 'Create account'
                  : 'Log in'}
            </button>
          </form>

          <p className="text-sm text-secondary mt-5">
            {isSignup ? 'Already have an account? ' : 'New to StudyFinder? '}
            <button
              type="button"
              onClick={() => switchMode(isSignup ? 'login' : 'signup')}
              disabled={busy}
              className="btn-link"
            >
              {isSignup ? 'Log in' : 'Create an account'}
            </button>
          </p>

          <div className="auth-divider my-7" role="separator">
            <span>or</span>
          </div>

          <button
            type="button"
            onClick={handleGuest}
            disabled={busy}
            className="btn-secondary w-full py-3 text-sm"
          >
            {pending === 'guest' ? 'Opening guest view…' : 'Continue as guest'}
          </button>
          <p className="text-xs text-muted mt-3 leading-relaxed">
            Guests can browse public spots like the Student Union and King Library. Log in to see
            every building and add your own reports.
          </p>
        </div>
      </main>
    </div>
  )
}

function Brand({ light = false }: { light?: boolean }) {
  return (
    <div className="flex items-center gap-2">
      <span className={light ? 'rounded-lg bg-white p-1' : ''}>
        <img src={spartan} alt="" className="logo-image" />
      </span>
      <span className={`font-display text-xl ${light ? 'text-white' : 'text-primary'}`}>
        SJSU Study Finder
      </span>
    </div>
  )
}

type TextFieldProps = {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
  autoComplete?: string
  placeholder?: string
  hint?: string
  error?: string
  trailing?: ReactNode
}

function TextField({
  id,
  label,
  value,
  onChange,
  type = 'text',
  autoComplete,
  placeholder,
  hint,
  error,
  trailing,
}: TextFieldProps) {
  const hintId = hint && !error ? `${id}-hint` : undefined
  const errorId = error ? `${id}-error` : undefined

  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-main mb-1.5">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          name={id.replace('auth-', '')}
          type={type}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          autoComplete={autoComplete}
          placeholder={placeholder}
          aria-invalid={error ? true : undefined}
          aria-describedby={errorId ?? hintId}
          className={`auth-input ${trailing ? 'pr-16' : ''}`}
        />
        {trailing}
      </div>
      {hintId && (
        <p id={hintId} className="text-xs text-muted mt-1.5">
          {hint}
        </p>
      )}
      {errorId && (
        <p id={errorId} className="auth-field-error text-xs mt-1.5">
          {error}
        </p>
      )}
    </div>
  )
}

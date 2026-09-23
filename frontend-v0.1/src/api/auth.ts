// Talks to the Java servlets under /SJSUStudyFinder/api/auth.
// The session lives in Tomcat's HttpOnly JSESSIONID cookie, which the browser
// sends automatically, so no tokens are stored in JavaScript.

export type Role = 'student' | 'admin' | 'guest'

export interface SessionUser {
  id: number | null
  name: string
  email: string | null
  role: Role
}

export type AuthField = 'fullName' | 'email' | 'password'

export class AuthError extends Error {
  field?: AuthField

  constructor(message: string, field?: AuthField) {
    super(message)
    this.name = 'AuthError'
    this.field = field
  }
}

type ApiResponse = {
  ok?: boolean
  error?: string
  field?: AuthField
  authenticated?: boolean
  user?: SessionUser
}

// BASE_URL comes from `base` in vite.config.ts ('/SJSUStudyFinder/').
const AUTH_API = `${import.meta.env.BASE_URL}api/auth`

async function request(path: string, init?: RequestInit): Promise<ApiResponse> {
  let response: Response

  try {
    response = await fetch(`${AUTH_API}/${path}`, {
      credentials: 'same-origin',
      headers: { Accept: 'application/json' },
      ...init,
    })
  } catch {
    throw new AuthError(
      "Can't reach the StudyFinder server. Make sure Tomcat is running, then try again.",
    )
  }

  let body: ApiResponse = {}
  try {
    body = await response.json()
  } catch {
    // Not JSON, e.g. a Tomcat error page. The status check below handles it.
  }

  if (!response.ok || body.ok === false) {
    throw new AuthError(
      body.error ?? `The server returned an error (${response.status}). Try again.`,
      body.field,
    )
  }

  return body
}

// Form-encoded so the servlets can read fields with request.getParameter().
function post(path: string, fields: Record<string, string> = {}) {
  return request(path, { method: 'POST', body: new URLSearchParams(fields) })
}

function requireUser(body: ApiResponse): SessionUser {
  if (!body.user) {
    throw new AuthError('The server did not return an account. Try again.')
  }
  return body.user
}

export async function logIn(email: string, password: string): Promise<SessionUser> {
  return requireUser(await post('login', { email, password }))
}

export async function signUp(fullName: string, email: string, password: string): Promise<SessionUser> {
  return requireUser(await post('signup', { fullName, email, password }))
}

export async function continueAsGuest(): Promise<SessionUser> {
  return requireUser(await post('guest'))
}

export async function logOut(): Promise<void> {
  await post('logout')
}

/** Returns the logged-in user, or null if there is no session. */
export async function getCurrentUser(): Promise<SessionUser | null> {
  const body = await request('me')
  return body.authenticated && body.user ? body.user : null
}

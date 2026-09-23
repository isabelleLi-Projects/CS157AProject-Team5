package com.studyfinder.util;

import com.studyfinder.model.SessionUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Starts, reads, and ends the logged-in session.
 * Tomcat tracks the session with the JSESSIONID cookie, which is HttpOnly,
 * so JavaScript in the page cannot read it.
 */
public final class AuthSession {

    /** Sessions end after 30 minutes without a request. */
    public static final int TIMEOUT_SECONDS = 30 * 60;

    private AuthSession() {
    }

    public static void start(HttpServletRequest request, SessionUser user) {
        HttpSession session = request.getSession(true);

        // Give the session a new ID at login so an attacker can't plant
        // a session ID beforehand and reuse it afterward (session fixation).
        request.changeSessionId();

        session.setMaxInactiveInterval(TIMEOUT_SECONDS);
        session.setAttribute(SessionUser.SESSION_KEY, user);
    }

    /** Returns the current user, or null if nobody is logged in. */
    public static SessionUser current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SessionUser.SESSION_KEY);
        return value instanceof SessionUser user ? user : null;
    }

    public static void end(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}

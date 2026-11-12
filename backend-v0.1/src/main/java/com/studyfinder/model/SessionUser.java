package com.studyfinder.model;

import java.io.Serializable;

/**
 * The signed-in person, stored in the HttpSession.
 *
 * role is "student", "admin", or "guest". Guests have no id or email.
 * Serializable so Tomcat can keep sessions across a server restart.
 */
public record SessionUser(Integer id, String name, String email, String role) implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Key used to store this object in the HttpSession. */
    public static final String SESSION_KEY = "currentUser";

    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_GUEST = "guest";

    public static SessionUser guest() {
        return new SessionUser(null, "Guest", null, ROLE_GUEST);
    }

    public static SessionUser from(User user) {
        return new SessionUser(user.getUserId(), user.getFullName(), user.getEmail(), user.getRoleName());
    }

    public boolean isGuest() {
        return ROLE_GUEST.equals(role);
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }
}

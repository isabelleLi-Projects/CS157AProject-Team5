package com.studyfinder.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Server-side input rules for login and sign-up.
 * The React page checks the same things, but the browser can be bypassed,
 * so the server always checks again.
 */
public final class AuthValidator {

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern SJSU_EMAIL = Pattern.compile("^[a-z0-9._%+-]+@sjsu\\.edu$");

    private AuthValidator() {
    }

    /** Trims and lowercases an email so "Name@SJSU.edu " and "name@sjsu.edu" are the same account. */
    public static String normalizeEmail(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    /** Trims a name and collapses repeated spaces. */
    public static String normalizeName(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }

    public static boolean isSjsuEmail(String normalizedEmail) {
        return normalizedEmail.length() <= MAX_EMAIL_LENGTH && SJSU_EMAIL.matcher(normalizedEmail).matches();
    }

    /** Returns a message describing what's wrong with the name, or null if it's fine. */
    public static String nameProblem(String normalizedName) {
        if (normalizedName.isEmpty()) {
            return "Enter your full name.";
        }
        if (normalizedName.length() > MAX_NAME_LENGTH) {
            return "Keep your name under " + MAX_NAME_LENGTH + " characters.";
        }
        return null;
    }

    /** Returns a message describing what's wrong with the password, or null if it's fine. */
    public static String passwordProblem(String password) {
        if (password == null || password.isEmpty()) {
            return "Enter a password.";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Use at least " + MIN_PASSWORD_LENGTH + " characters.";
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "Use " + MAX_PASSWORD_LENGTH + " characters or fewer.";
        }
        return null;
    }
}

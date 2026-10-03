package com.studyfinder.service;

import com.studyfinder.dao.UserDAO;
import com.studyfinder.model.SessionUser;
import com.studyfinder.model.User;
import com.studyfinder.util.AuthSession;
import com.studyfinder.util.AuthValidator;
import com.studyfinder.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.sql.SQLException;
import java.security.SecureRandom;

/** Login and signup business logic used by login.jsp; it does not return JSON. */
public class AuthService {
    private static final String PENDING_NAME = "studyfinder.pending.name";
    private static final String PENDING_EMAIL = "studyfinder.pending.email";
    private static final String PENDING_HASH = "studyfinder.pending.hash";
    private static final String PENDING_CODE = "studyfinder.pending.code";
    private static final String PENDING_EXPIRES = "studyfinder.pending.expires";
    private static final long CODE_LIFETIME_MS = 10 * 60 * 1000L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserDAO userDAO = new UserDAO();
    private final EmailVerificationService emailService = new EmailVerificationService();

    public SessionUser login(HttpServletRequest request, String email, String password) throws Exception {
        User user = userDAO.findByEmail(AuthValidator.normalizeEmail(email));
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("That email and password don't match.");
        }
        if (!user.isEmailVerified()) throw new IllegalArgumentException("Verify your SJSU email before logging in.");
        if ("suspended".equalsIgnoreCase(user.getStatus())) throw new IllegalArgumentException("This account is suspended.");
        SessionUser sessionUser = SessionUser.from(user);
        AuthSession.start(request, sessionUser);
        return sessionUser;
    }

    public void beginSignup(HttpServletRequest request, String fullName, String email, String password) throws Exception {
        String normalizedName = AuthValidator.normalizeName(fullName);
        String normalizedEmail = AuthValidator.normalizeEmail(email);
        String nameError = AuthValidator.nameProblem(normalizedName);
        if (nameError != null) throw new IllegalArgumentException(nameError);
        if (!AuthValidator.isSjsuEmail(normalizedEmail)) throw new IllegalArgumentException("Use your SJSU email address ending in @sjsu.edu.");
        String passwordError = AuthValidator.passwordProblem(password);
        if (passwordError != null) throw new IllegalArgumentException(passwordError);
        if (userDAO.emailExists(normalizedEmail)) throw new IllegalArgumentException("That email is already registered.");

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        HttpSession session = request.getSession(true);
        session.setAttribute(PENDING_NAME, normalizedName);
        session.setAttribute(PENDING_EMAIL, normalizedEmail);
        session.setAttribute(PENDING_HASH, PasswordUtil.hash(password));
        session.setAttribute(PENDING_CODE, code);
        session.setAttribute(PENDING_EXPIRES, System.currentTimeMillis() + CODE_LIFETIME_MS);
        emailService.sendCode(normalizedEmail, code);
    }

    public SessionUser verifySignup(HttpServletRequest request, String submittedCode) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) throw new IllegalArgumentException("Your verification session expired. Start signup again.");
        String expected = (String) session.getAttribute(PENDING_CODE);
        Long expires = (Long) session.getAttribute(PENDING_EXPIRES);
        if (expected == null || expires == null || System.currentTimeMillis() > expires) {
            clearPending(session);
            throw new IllegalArgumentException("That code expired. Start signup again.");
        }
        if (submittedCode == null || !expected.equals(submittedCode.trim())) {
            throw new IllegalArgumentException("That access code is incorrect.");
        }
        String name = (String) session.getAttribute(PENDING_NAME);
        String email = (String) session.getAttribute(PENDING_EMAIL);
        String hash = (String) session.getAttribute(PENDING_HASH);
        int userId = userDAO.createStudent(name, email, hash);
        clearPending(session);
        SessionUser user = new SessionUser(userId, name, email, SessionUser.ROLE_STUDENT);
        AuthSession.start(request, user);
        return user;
    }

    public boolean hasPendingSignup(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(PENDING_CODE) != null;
    }

    public SessionUser continueAsGuest(HttpServletRequest request) {
        SessionUser guest = SessionUser.guest();
        AuthSession.start(request, guest);
        return guest;
    }

    private void clearPending(HttpSession session) {
        session.removeAttribute(PENDING_NAME);
        session.removeAttribute(PENDING_EMAIL);
        session.removeAttribute(PENDING_HASH);
        session.removeAttribute(PENDING_CODE);
        session.removeAttribute(PENDING_EXPIRES);
    }
}

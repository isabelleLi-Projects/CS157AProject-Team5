package com.studyfinder.servlet;

import java.io.IOException;
import java.sql.SQLException;

import com.studyfinder.dao.UserDAO;
import com.studyfinder.model.SessionUser;
import com.studyfinder.model.User;
import com.studyfinder.util.AuthSession;
import com.studyfinder.util.AuthValidator;
import com.studyfinder.util.JsonResponse;
import com.studyfinder.util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * POST /api/auth/login
 * Form fields: email, password
 *
 * 200 {"ok":true,"user":{...}}      logged in
 * 400 {"ok":false,"error":"..."}    a field was empty
 * 401 {"ok":false,"error":"..."}    email or password is wrong
 * 403 {"ok":false,"error":"..."}    account suspended or not verified
 */
@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Same message for "no such email" and "wrong password",
    // so the page doesn't reveal which SJSU emails have accounts.
    private static final String BAD_CREDENTIALS =
            "That email and password don't match. Check both and try again.";

    private final transient UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String email = AuthValidator.normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");

        if (email.isEmpty()) {
            JsonResponse.fieldError(response, 400, "email", "Enter your SJSU email.");
            return;
        }
        if (password == null || password.isEmpty()) {
            JsonResponse.fieldError(response, 400, "password", "Enter your password.");
            return;
        }

        try {
            User user = userDAO.findByEmail(email);

            if (user == null) {
                PasswordUtil.simulateVerify(password);
                JsonResponse.error(response, 401, BAD_CREDENTIALS);
                return;
            }

            if (!PasswordUtil.verify(password, user.getPasswordHash())) {
                JsonResponse.error(response, 401, BAD_CREDENTIALS);
                return;
            }

            // Status is checked only after the password matches,
            // so strangers can't learn which accounts are suspended.
            if ("suspended".equals(user.getStatus())) {
                JsonResponse.error(response, 403,
                        "This account is suspended. Contact a StudyFinder administrator.");
                return;
            }
            if ("unverified".equals(user.getStatus())) {
                JsonResponse.error(response, 403,
                        "Verify your email before logging in. Check your SJSU inbox for the link.");
                return;
            }

            userDAO.recordLogin(user.getUserId());

            SessionUser sessionUser = SessionUser.from(user);
            AuthSession.start(request, sessionUser);
            JsonResponse.sendUser(response, 200, sessionUser);

        } catch (SQLException e) {
            log("Login failed because of a database error", e);
            JsonResponse.error(response, 500, "The server couldn't reach the database. Try again in a moment.");
        }
    }
}

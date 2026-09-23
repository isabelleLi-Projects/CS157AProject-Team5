package com.studyfinder.servlet;

import java.io.IOException;
import java.sql.SQLException;

import com.studyfinder.dao.UserDAO;
import com.studyfinder.model.SessionUser;
import com.studyfinder.util.AuthSession;
import com.studyfinder.util.AuthValidator;
import com.studyfinder.util.JsonResponse;
import com.studyfinder.util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * POST /api/auth/signup
 * Form fields: fullName, email, password
 *
 * Creates a student account and logs the new student in.
 * Admin accounts are never created here; they are added directly in MySQL.
 *
 * 201 {"ok":true,"user":{...}}                     account created
 * 400 {"ok":false,"error":"...","field":"..."}     invalid input
 * 409 {"ok":false,"error":"...","field":"email"}   email already registered
 */
@WebServlet("/api/auth/signup")
public class SignupServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String EMAIL_TAKEN = "An account with this email already exists. Log in instead.";

    private final transient UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String fullName = AuthValidator.normalizeName(request.getParameter("fullName"));
        String email = AuthValidator.normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");

        String nameProblem = AuthValidator.nameProblem(fullName);
        if (nameProblem != null) {
            JsonResponse.fieldError(response, 400, "fullName", nameProblem);
            return;
        }
        if (!AuthValidator.isSjsuEmail(email)) {
            JsonResponse.fieldError(response, 400, "email", "Use your SJSU email address, ending in @sjsu.edu.");
            return;
        }
        String passwordProblem = AuthValidator.passwordProblem(password);
        if (passwordProblem != null) {
            JsonResponse.fieldError(response, 400, "password", passwordProblem);
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                JsonResponse.fieldError(response, 409, "email", EMAIL_TAKEN);
                return;
            }

            int userId = userDAO.createStudent(fullName, email, PasswordUtil.hash(password));

            SessionUser sessionUser = new SessionUser(userId, fullName, email, SessionUser.ROLE_STUDENT);
            AuthSession.start(request, sessionUser);
            JsonResponse.sendUser(response, 201, sessionUser);

        } catch (SQLException e) {
            // Two sign-ups with the same email at the same moment: the UNIQUE index catches the second.
            if (UserDAO.isDuplicateEntry(e)) {
                JsonResponse.fieldError(response, 409, "email", EMAIL_TAKEN);
                return;
            }
            log("Sign-up failed because of a database error", e);
            JsonResponse.error(response, 500, "The server couldn't reach the database. Try again in a moment.");
        }
    }
}

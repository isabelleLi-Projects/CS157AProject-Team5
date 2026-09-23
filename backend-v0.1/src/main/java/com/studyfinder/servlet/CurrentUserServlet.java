package com.studyfinder.servlet;

import java.io.IOException;

import com.studyfinder.model.SessionUser;
import com.studyfinder.util.AuthSession;
import com.studyfinder.util.JsonResponse;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * GET /api/auth/me
 *
 * Tells the React app who is logged in when the page first loads,
 * so a refresh doesn't send the user back to the login page.
 *
 * 200 {"ok":true,"authenticated":true,"user":{...}}
 * 200 {"ok":true,"authenticated":false}
 */
@WebServlet("/api/auth/me")
public class CurrentUserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SessionUser user = AuthSession.current(request);

        if (user == null) {
            JsonResponse.send(response, 200, "{\"ok\":true,\"authenticated\":false}");
            return;
        }
        JsonResponse.send(response, 200,
                "{\"ok\":true,\"authenticated\":true,\"user\":" + JsonResponse.user(user) + "}");
    }
}

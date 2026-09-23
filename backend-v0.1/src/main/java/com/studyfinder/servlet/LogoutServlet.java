package com.studyfinder.servlet;

import java.io.IOException;

import com.studyfinder.util.AuthSession;
import com.studyfinder.util.JsonResponse;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * POST /api/auth/logout
 *
 * Ends the session for students, admins, and guests alike.
 * It's a POST rather than a GET so a link or image on another site
 * can't log someone out.
 *
 * 200 {"ok":true}
 */
@WebServlet("/api/auth/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthSession.end(request);
        JsonResponse.send(response, 200, "{\"ok\":true}");
    }
}

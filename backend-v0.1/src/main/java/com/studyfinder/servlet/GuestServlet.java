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
 * POST /api/auth/guest
 *
 * Starts a guest session. Guests can browse public study spots only,
 * and cannot submit reports, verify reports, or earn points. Storing the
 * guest role on the server lets other servlets enforce that.
 *
 * 200 {"ok":true,"user":{"id":null,"name":"Guest","email":null,"role":"guest"}}
 */
@WebServlet("/api/auth/guest")
public class GuestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SessionUser guest = SessionUser.guest();
        AuthSession.start(request, guest);
        JsonResponse.sendUser(response, 200, guest);
    }
}

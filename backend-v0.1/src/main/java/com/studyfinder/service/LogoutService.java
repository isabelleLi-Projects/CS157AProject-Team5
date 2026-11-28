package com.studyfinder.service;

import com.studyfinder.util.AuthSession;
import jakarta.servlet.http.HttpServletRequest;

/** Application service for ending the current JSP session. */
public class LogoutService {
    public void logout(HttpServletRequest request) {
        AuthSession.end(request);
    }
}

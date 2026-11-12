<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.AuthService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%
    // Saves the Edit profile form (name, and password when a new one is entered), then goes back.
    if (!"POST".equalsIgnoreCase(request.getMethod())) { response.sendError(405); return; }

    String context = request.getContextPath();
    SessionUser current = AuthSession.current(request);
    if (current == null || current.id() == null || current.isGuest()) {
        response.sendRedirect(context + "/login.jsp");
        return;
    }

    // Go back to the page the form was opened on. Only these two pages are allowed.
    String returnTo = request.getParameter("returnTo");
    String back = returnTo != null && returnTo.matches("study-spot\\.jsp\\?id=\\d+")
            ? context + "/" + returnTo + "&"
            : context + "/study-spots.jsp?";
    // The admin dashboard shows adminMessage/adminError; student pages show message/error.
    String messageKey = current.isAdmin() ? "adminMessage" : "message";
    String errorKey = current.isAdmin() ? "adminError" : "error";

    try {
        new AuthService().updateProfile(request,
                request.getParameter("fullName"),
                request.getParameter("currentPassword"),
                request.getParameter("newPassword"),
                request.getParameter("confirmPassword"));
        response.sendRedirect(back + messageKey + "=Profile+saved");
    } catch (IllegalArgumentException exception) {
        response.sendRedirect(back + errorKey + "=" + URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8));
    } catch (Exception exception) {
        log("Profile update failed", exception);
        response.sendRedirect(back + errorKey + "=Your+profile+could+not+be+saved");
    }
%>

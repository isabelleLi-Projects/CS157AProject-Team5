<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studyfinder.service.AuthService" %>
<%!
    private String js(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n")
                .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
    }
%>
<%
    AuthService authService = new AuthService();
    String error = null;
    String success = null;
    String requestMode = request.getParameter("mode");
    String authMode = authService.hasPendingSignup(request) || "verify".equals(requestMode)
            ? "verify" : ("signup".equals(request.getParameter("view")) || "signup".equals(requestMode) ? "signup" : "login");

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        try {
            if ("login".equals(requestMode)) {
                authService.login(request, request.getParameter("email"), request.getParameter("password"));
                response.sendRedirect(request.getContextPath() + "/study-spots.jsp");
                return;
            } else if ("signup".equals(requestMode)) {
                String password = request.getParameter("password");
                if (password == null || !password.equals(request.getParameter("confirmPassword"))) {
                    throw new IllegalArgumentException("Passwords don't match.");
                }
                authService.beginSignup(request, request.getParameter("fullName"), request.getParameter("email"), password);
                authMode = "verify";
                success = "A verification code was sent to your SJSU email.";
            } else if ("verify".equals(requestMode)) {
                authService.verifySignup(request, request.getParameter("code"));
                response.sendRedirect(request.getContextPath() + "/study-spots.jsp");
                return;
            } else if ("guest".equals(requestMode)) {
                authService.continueAsGuest(request);
                response.sendRedirect(request.getContextPath() + "/study-spots.jsp");
                return;
            }
        } catch (Exception exception) {
            error = exception instanceof IllegalArgumentException ? exception.getMessage() : "The request could not be completed. Check the database, email settings, and deployed libraries.";
            log("JSP authentication request failed", exception);
            if ("verify".equals(requestMode)) authMode = "verify";
            else if ("signup".equals(requestMode)) authMode = "signup";
            else authMode = "login";
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>StudyFinder Login</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/index-DEVZBIfi.css">
  <style>
    .auth-input { width:100%; border:0; border-bottom:1px solid #e2ddd5; border-radius:0; background:transparent; color:#1c1a16; outline:none; padding:.65rem 0; font:inherit; }
    .auth-input::placeholder { color:#8a8070; }
    .auth-input:focus { border-bottom:2px solid #2d5a3d; }
    .auth-reveal { background:transparent; border:0; cursor:pointer; }
    .auth-alert { background:#fdd9d3; color:#8c2418; }
    .auth-success { background:#e7f5eb; color:#245b35; }
    .auth-divider { display:flex; align-items:center; gap:1rem; color:#8a8070; }
    .auth-divider::before, .auth-divider::after { content:''; height:1px; flex:1; background:#e2ddd5; }
  </style>
</head>
<body class="app-background min-h-screen">
  <div id="root"></div>
  <script>
    window.authPage = true;
    window.authMode = "<%= js(authMode) %>";
    window.authError = "<%= js(error) %>";
    window.authSuccess = "<%= js(success) %>";
    window.studyFinderContext = "<%= js(request.getContextPath()) %>";
  </script>
  <script type="module" src="${pageContext.request.contextPath}/assets/index-DnrjtiLu.js"></script>
</body>
</html>

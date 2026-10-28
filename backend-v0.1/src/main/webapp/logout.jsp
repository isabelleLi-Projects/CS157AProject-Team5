<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.studyfinder.service.LogoutService" %>
<%
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
        response.sendError(405, "Logout must use POST.");
        return;
    }
    new LogoutService().logout(request);
    response.sendRedirect(request.getContextPath() + "/login.jsp");
%>

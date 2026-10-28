<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.StudyFinderPageService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%
    if (!"POST".equalsIgnoreCase(request.getMethod())) { response.sendError(405); return; }
    SessionUser current = AuthSession.current(request);
    if (current == null || current.id() == null) { response.sendRedirect(request.getContextPath() + "/login.jsp"); return; }
    try {
        int rewardId = Integer.parseInt(request.getParameter("rewardId"));
        new StudyFinderPageService().redeemReward(current.id(), rewardId);
        response.sendRedirect(request.getContextPath() + "/study-spots.jsp?message=Reward+redeemed");
    } catch (Exception exception) {
        log("Reward redemption failed", exception);
        response.sendRedirect(request.getContextPath() + "/study-spots.jsp?error=Reward+could+not+be+redeemed");
    }
%>

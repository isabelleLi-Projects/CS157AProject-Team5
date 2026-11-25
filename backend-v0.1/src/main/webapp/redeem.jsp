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
        // Saves the redemption, then emails the gift code to the student's SJSU email.
        boolean emailed = new StudyFinderPageService().redeemReward(current.id(), current.email(), current.name(), rewardId);
        response.sendRedirect(request.getContextPath() + "/study-spots.jsp?nav=Rewards&message=" + (emailed
                ? "Reward+redeemed.+Check+your+SJSU+email+for+your+gift."
                : "Reward+redeemed,+but+the+gift+email+could+not+be+sent.+Contact+an+administrator."));
    } catch (Exception exception) {
    log("Reward redemption failed", exception);

    String error = "Reward could not be redeemed";

    if ("You do not have enough points.".equals(exception.getMessage())) {
    error = "You do not have enough points.";
    } else if ("Reward is out of stock.".equals(exception.getMessage())) {
    error = "Reward is out of stock.";
    }

    response.sendRedirect(
        request.getContextPath() + "/study-spots.jsp?nav=Rewards&error="
        + java.net.URLEncoder.encode(error, "UTF-8")
    ); 
    }
%>

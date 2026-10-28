<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.SpotService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%
    // Saves a student's "Accurate" / "Inaccurate" rating of a spot's latest report, then goes back to the spot page.
    if (!"POST".equalsIgnoreCase(request.getMethod())) { response.sendError(405); return; }

    String context = request.getContextPath();
    SessionUser current = AuthSession.current(request);
    if (current == null || current.id() == null || current.isGuest()) {
        response.sendRedirect(context + "/login.jsp?notice=rate");
        return;
    }

    int spotId;
    int reportId;
    try {
        spotId = Integer.parseInt(request.getParameter("spotId"));
        reportId = Integer.parseInt(request.getParameter("reportId"));
    } catch (NumberFormatException exception) {
        response.sendRedirect(context + "/study-spots.jsp?error=Spot+not+found");
        return;
    }
    String back = context + "/study-spot.jsp?id=" + spotId;

    // Only students rate. Admins get a friendly message instead.
    if (!SessionUser.ROLE_STUDENT.equals(current.role())) {
        response.sendRedirect(back + "&error=Only+students+can+rate+spots");
        return;
    }

    try {
        String error = new SpotService().rateLatestReport(current.id(), spotId, reportId, request.getParameter("result"));
        if (error == null) {
            response.sendRedirect(back + "&message=Thanks+for+rating");
        } else {
            response.sendRedirect(back + "&error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
    } catch (Exception exception) {
        log("Spot rating failed", exception);
        response.sendRedirect(back + "&error=Your+rating+could+not+be+saved");
    }
%>

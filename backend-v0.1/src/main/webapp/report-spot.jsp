<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.SpotService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%
    // Saves a student's report of a spot's current conditions (noise, crowdedness, outlets, smell, Wi-Fi, notes),
    // then goes back to the spot page.
    if (!"POST".equalsIgnoreCase(request.getMethod())) { response.sendError(405); return; }
    request.setCharacterEncoding("UTF-8");

    String context = request.getContextPath();
    SessionUser current = AuthSession.current(request);
    if (current == null || current.id() == null || current.isGuest()) {
        response.sendRedirect(context + "/login.jsp?notice=report");
        return;
    }

    int spotId;
    try {
        spotId = Integer.parseInt(request.getParameter("spotId"));
    } catch (NumberFormatException exception) {
        response.sendRedirect(context + "/study-spots.jsp?error=Spot+not+found");
        return;
    }
    String back = context + "/study-spot.jsp?id=" + spotId;

    // Only students report. Admins get a friendly message instead.
    if (!SessionUser.ROLE_STUDENT.equals(current.role())) {
        response.sendRedirect(back + "&error=Only+students+can+report+conditions");
        return;
    }

    try {
        String error = new SpotService().submitReport(current.id(), spotId,
                request.getParameter("noise"), request.getParameter("crowdedness"),
                request.getParameter("outlets"), request.getParameter("smell"),
                request.getParameter("wifi"), request.getParameter("notes"));
        if (error == null) {
            response.sendRedirect(back + "&message=Thanks+for+reporting+conditions");
        } else {
            response.sendRedirect(back + "&error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
    } catch (Exception exception) {
        log("Spot report failed", exception);
        response.sendRedirect(back + "&error=Your+report+could+not+be+saved");
    }
%>

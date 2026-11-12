<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.SpotPhotoService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%
    // Saves a student's photo of a study spot. The browser sends the (already shrunk) JPEG as a
    // data URL, plus the student's location, which the study-spot.jsp script adds.
    if (!"POST".equalsIgnoreCase(request.getMethod())) { response.sendError(405); return; }
    String context = request.getContextPath();
    SessionUser current = AuthSession.current(request);
    if (current == null || current.id() == null || current.isGuest()) {
        response.sendRedirect(context + "/login.jsp");
        return;
    }
    int spotId;
    double latitude;
    double longitude;
    try {
        spotId = Integer.parseInt(request.getParameter("spotId"));
        latitude = Double.parseDouble(request.getParameter("latitude"));
        longitude = Double.parseDouble(request.getParameter("longitude"));
    } catch (NumberFormatException | NullPointerException exception) {
        String id = request.getParameter("spotId");
        response.sendRedirect(id != null && id.matches("\\d+")
                ? context + "/study-spot.jsp?id=" + id + "&error=Turn+on+location+to+add+a+photo"
                : context + "/study-spots.jsp?error=Spot+not+found");
        return;
    }
    String back = context + "/study-spot.jsp?id=" + spotId;
    if (!SessionUser.ROLE_STUDENT.equals(current.role())) {
        response.sendRedirect(back + "&error=Only+students+can+add+photos");
        return;
    }
    try {
        String error = new SpotPhotoService().upload(current.id(), spotId, request.getParameter("photo"),
                latitude, longitude, application.getRealPath("/assets/spot_photos"));
        if (error == null) {
            response.sendRedirect(back + "&message=Thanks!+Your+photo+was+added.");
        } else {
            response.sendRedirect(back + "&error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
    } catch (Exception exception) {
        log("Photo upload failed", exception);
        response.sendRedirect(back + "&error=Your+photo+could+not+be+saved");
    }
%>

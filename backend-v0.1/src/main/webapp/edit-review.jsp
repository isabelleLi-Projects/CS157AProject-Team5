<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.SpotService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%
    // Edit or delete the logged-in student's own review, then go back to the spot page.
    if (!"POST".equalsIgnoreCase(request.getMethod())) { response.sendError(405); return; }
    request.setCharacterEncoding("UTF-8");
    String context = request.getContextPath();
    SessionUser current = AuthSession.current(request);
    if (current == null || current.id() == null || current.isGuest()) {
        response.sendRedirect(context + "/login.jsp");
        return;
    }
    int spotId;
    int reviewId;
    try {
        spotId = Integer.parseInt(request.getParameter("spotId"));
        reviewId = Integer.parseInt(request.getParameter("reviewId"));
    } catch (NumberFormatException exception) {
        response.sendRedirect(context + "/study-spots.jsp?error=Review+not+found");
        return;
    }
    String back = context + "/study-spot.jsp?id=" + spotId;
    try {
        SpotService service = new SpotService();
        boolean delete = "delete".equals(request.getParameter("action"));
        String error;
        if (delete) {
            error = service.deleteReview(current.id(), reviewId);
        } else {
            int rating;
            try {
                rating = Integer.parseInt(request.getParameter("rating"));
            } catch (NumberFormatException exception) {
                rating = 0;
            }
            error = service.editReview(current.id(), reviewId, rating, request.getParameter("reviewText"));
        }
        if (error == null) {
            response.sendRedirect(back + "&message=" + (delete
                    ? "Your+review+was+deleted.+The+5+points+it+earned+were+removed."
                    : "Your+review+was+updated."));
        } else {
            response.sendRedirect(back + "&error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
    } catch (Exception exception) {
        log("Review edit failed", exception);
        response.sendRedirect(back + "&error=Your+review+could+not+be+changed");
    }
%>

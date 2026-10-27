<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.SpotService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>

<%
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
        response.sendError(405);
        return;
    }

    request.setCharacterEncoding("UTF-8");

    String context = request.getContextPath();
    SessionUser current = AuthSession.current(request);

    if (current == null || current.id() == null || current.isGuest()) {
        response.sendRedirect(context + "/login.jsp");
        return;
    }

    int spotId;
    int rating;

    try {
        spotId = Integer.parseInt(request.getParameter("spotId"));
        rating = Integer.parseInt(request.getParameter("rating"));
    } catch (NumberFormatException exception) {
        response.sendRedirect(context + "/study-spots.jsp?error=Invalid+review");
        return;
    }

    String back = context + "/study-spot.jsp?id=" + spotId;

    try {
        String error = new SpotService().submitReview(
                current.id(),
                spotId,
                rating,
                request.getParameter("reviewText")
        );

        if (error == null) {
            String message = URLEncoder.encode(
                "Thank you for your review! You earned 5 points.",
                StandardCharsets.UTF_8
        );
            response.sendRedirect(back + "&message=" + message);
        } else {
            response.sendRedirect(
                    back + "&error="
                    + URLEncoder.encode(error, StandardCharsets.UTF_8)
            );
        }
    } catch (Exception exception) {
        log("Review submission failed", exception);
        response.sendRedirect(back + "&error=Review+could+not+be+saved");
    }
%>
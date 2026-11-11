<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="jakarta.servlet.http.HttpServletResponse" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.SpotService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>

<%
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    request.setCharacterEncoding("UTF-8");

    String context = request.getContextPath();
    SessionUser currentUser = AuthSession.current(request);

    if (currentUser == null
            || currentUser.id() == null
            || currentUser.isGuest()) {
        response.sendRedirect(context + "/login.jsp");
        return;
    }

    int spotId;
    int rating;
    double latitude;
    double longitude;

    try {
        spotId = Integer.parseInt(request.getParameter("spotId"));
        rating = Integer.parseInt(request.getParameter("rating"));
        latitude = Double.parseDouble(request.getParameter("latitude"));
        longitude = Double.parseDouble(request.getParameter("longitude"));
    } catch (NumberFormatException exception) {
        response.sendRedirect(
                context + "/study-spots.jsp?error=Invalid+review+or+location");
        return;
    }

    if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
        response.sendRedirect(
                context + "/study-spots.jsp?error=Valid+location+is+required");
        return;
    }

    String back = context + "/study-spot.jsp?id=" + spotId;

    try {
        String error = new SpotService().submitReview(
                currentUser.id(),
                spotId,
                rating,
                request.getParameter("reviewText"),
                latitude,
                longitude
        );

        if (error == null) {
            String message = URLEncoder.encode(
                    "Thank you for your review! You earned 5 points.",
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(
                    back + "&message=" + message
            );
        } else {
            String encodedError = URLEncoder.encode(
                    error,
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(
                    back + "&error=" + encodedError
            );
        }

    } catch (Exception exception) {
        log("Review submission failed", exception);

        response.sendRedirect(
                back + "&error=Review+could+not+be+saved"
        );
    }
%>
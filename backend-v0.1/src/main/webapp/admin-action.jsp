<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.AdminDashboardService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>

<%
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
        response.sendError(405);
        return;
    }

    SessionUser current = AuthSession.current(request);

    if (current == null || !current.isAdmin()) {
        response.sendError(403);
        return;
    }

    String action = request.getParameter("action");

    try {
        AdminDashboardService service = new AdminDashboardService();

        if ("addReward".equals(action)) {
            service.addReward(
                request.getParameter("rewardName"),
                request.getParameter("description"),
                Integer.parseInt(
                    request.getParameter("pointsRequired")
                ),
                Integer.parseInt(
                    request.getParameter("stockQuantity")
                )
            );

        } else if ("addSpot".equals(action)) {

            String buildingText = request.getParameter("buildingId");
            Integer buildingId = null;
            // Empty building means outdoor spot
            if (buildingText != null && !buildingText.isBlank()) {
                buildingId = Integer.parseInt(buildingText);
            }

            String coordinateText = request.getParameter("coordinates");

            if (coordinateText == null || coordinateText.isBlank()) {
                response.sendError(400, "Coordinates are required");
                return;
            }

            String[] coordinates = coordinateText.split(",");

            if (coordinates.length != 2) {
                response.sendError(400, "Use latitude, longitude");
                return;
            }

            double latitude = Double.parseDouble(coordinates[0].trim());
            double longitude = Double.parseDouble(coordinates[1].trim());
            String photoPath = null;

            service.addStudySpot(
                request.getParameter("spotName"),
                buildingId,
                request.getParameter("locationDetails"),
                request.getParameter("accessType"),
                Integer.parseInt(request.getParameter("capacity")),
                latitude,
                longitude,
                photoPath
            );

        } else {
            response.sendError(400,"Invalid administrator action");
            return;
        }
        response.sendRedirect(
            request.getContextPath()
            + "/study-spots.jsp?adminMessage=Saved"
        );

    } catch (Exception exception) {
        log("Administrator action failed", exception);

        response.sendRedirect(
            request.getContextPath()
            + "/study-spots.jsp?adminError="
            + "The change could not be saved"
        );
    }
%>
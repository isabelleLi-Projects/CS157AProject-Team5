<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.List" %>
<%@ page import="com.studyfinder.model.StudySpot" %>
<%@ page import="com.studyfinder.service.StudySpotService" %>
<%!
    private String json(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029");
    }
%>
<%
    List<StudySpot> studySpots = List.of();
    String databaseError = null;

    try {
        studySpots = new StudySpotService().getActiveStudySpots();
    } catch (SQLException exception) {
        databaseError = "The study spots could not be loaded from the database.";
        log("Unable to load study spots", exception);
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>StudyFinder Study Spots</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/index-Cyl7Okye.css">
</head>

<body>

    <% if (databaseError != null) { %>
        <p style="padding: 1rem; color: #9b1c1c;">
            <%= databaseError %> Check that MySQL is running and the database credentials are configured.
        </p>
    <% } %>

    <div id="root"></div>

    <script>
        window.studySpots = [
            <% for (int i = 0; i < studySpots.size(); i++) {
                StudySpot spot = studySpots.get(i); %>
            {
                spotId: <%= spot.getSpotId() %>,
                name: "<%= json(spot.getName()) %>",
                building: "<%= json(spot.getBuilding()) %>",
                location: "<%= json(spot.getLocation()) %>",
                capacity: <%= spot.getCapacity() %>,
                latitude: <%= spot.getLatitude() %>,
                longitude: <%= spot.getLongitude() %>,
                accessType: "<%= json(spot.getAccessType()) %>"
            }
            <%= i < studySpots.size() - 1 ? "," : "" %>
            <% } %>
        ];
    </script>

    <script type="module"
            src="${pageContext.request.contextPath}/assets/index-CB_ZeGPu.js">
    </script>

</body>
</html>

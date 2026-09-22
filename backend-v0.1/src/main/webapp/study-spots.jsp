<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<p>Number of study spots: ${studySpots.size()}</p>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>StudyFinder Study Spots</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/index-oGkH73V0.css">
</head>

<body>

    <div id="root"></div>

    <script>
        window.studySpots = [
            <c:forEach var="spot" items="${studySpots}" varStatus="status">
            {
                spotId: ${spot.spotId},
                name: "${spot.name}",
                building: "${spot.building}",
                location: "${spot.location}",
                capacity: ${spot.capacity},
                latitude: ${spot.latitude},
                longitude: ${spot.longitude},
                accessType: "${spot.accessType}"
            }
            <c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];
    </script>

    <script type="module"
            src="${pageContext.request.contextPath}/assets/index-DT5zc6NT.js">
    </script>

</body>
</html>
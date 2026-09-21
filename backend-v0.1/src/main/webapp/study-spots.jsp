<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>


<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>StudyFinder Study Spots</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/index-D-QOtrnC.css">
</head>

<body>

    <div id="root"></div>

    <p>Number of study spots: ${studySpots.size()}</p>


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
            src="${pageContext.request.contextPath}/assets/index-yc6M9ay5.js">
    </script>

</body>
</html>
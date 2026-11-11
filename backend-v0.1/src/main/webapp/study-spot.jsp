<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.sql.SQLException" %>
        <%@ page import="java.util.List" %>
            <%@ page import="com.studyfinder.model.SessionUser" %>
                <%@ page import="com.studyfinder.model.SpotDetail" %>
                    <%@ page import="com.studyfinder.service.SpotService" %>
                        <%@ page import="com.studyfinder.util.AuthSession" %>
                            <%@ page import="com.studyfinder.dao.StudyFinderDAO" %>
                                <%! private String js(String value) { if (value==null) return "" ; return
                                    value.replace("\\", "\\\\" ).replace("\"", "\\\"")
                .replace(" \r", "\\r" ).replace("\n", "\\n" ) .replace("\u2028", "\\u2028"
                                    ).replace("\u2029", "\\u2029" ) .replace("<", "\\u003c" ); } /** A quoted, escaped
                                    JS string, or null. */ private String jsOrNull(String value) { return value==null
                                    ? "null" : "\"" + js(value) + " \""; } %>
                                    <% // Study Spot page (Functional Requirement #4): one spot's details, directions,
                                        and accurate/inaccurate rating. SessionUser
                                        currentUser=AuthSession.current(request); if (currentUser==null) {
                                        response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } int
                                        spotId; try { spotId=Integer.parseInt(request.getParameter("id")); } catch
                                        (NumberFormatException exception) {
                                        response.sendRedirect(request.getContextPath()
                                        + "/study-spots.jsp?error=Spot+not+found" ); return; } boolean
                                        isStudent=SessionUser.ROLE_STUDENT.equals(currentUser.role()) &&
                                        currentUser.id() !=null; SpotDetail spot; try { spot=new
                                        SpotService().getSpotDetail(spotId, isStudent ? currentUser.id() : null); }
                                        catch (SQLException exception) { log("Unable to load study spot " + spotId, exception);
        response.sendRedirect(request.getContextPath() + " /study-spots.jsp?error=The+spot+could+not+be+loaded");
                                        return; } if (spot==null) { response.sendRedirect(request.getContextPath()
                                        + "/study-spots.jsp?error=Spot+not+found" ); return; } // Guests can only see
                                        public spots. Enforced here, not just hidden in React. if (currentUser.isGuest()
                                        && !spot.isPublicAccess()) { response.sendRedirect(request.getContextPath()
                                        + "/study-spots.jsp?error=Log+in+to+view+this+spot" ); return; } // example:
                                        http://localhost:8080/SJSUStudyFinder/study-spot.jsp?id=1 // Every time someone
                                        successfully opens study-spot.jsp?id=1, one row will be added to
                                        study_spot_views new StudyFinderDAO().recordStudySpotView(spotId,
                                        currentUser.id()); String imageUrl="" ; if (spot.getPhotoPath() !=null &&
                                        !spot.getPhotoPath().isBlank()) { imageUrl=request.getContextPath()
                                        + "/assets/study_spots/" + spot.getPhotoPath(); } SpotDetail.Report
                                        latest=spot.getLatestReport(); List<SpotDetail.Report> reports =
                                        spot.getRecentReports();
                                        %>
                                        <!DOCTYPE html>
                                        <html>

                                        <head>
                                            <meta charset="UTF-8">
                                            <meta name="viewport" content="width=device-width, initial-scale=1">
                                            <title>
                                                <%= spot.getName().replace("<", "&lt;" ) %> | StudyFinder
                                            </title>
                                            <link rel="stylesheet"
                                                href="${pageContext.request.contextPath}/assets/index-CtfqJuxQ.css">
                                        </head>

    int spotId;
    try {
        spotId = Integer.parseInt(request.getParameter("id"));
    } catch (NumberFormatException exception) {
        response.sendRedirect(request.getContextPath() + "/study-spots.jsp?error=Spot+not+found");
        return;
    }

    boolean isStudent = SessionUser.ROLE_STUDENT.equals(currentUser.role()) && currentUser.id() != null;
    SpotDetail spot;
    try {
        spot = new SpotService().getSpotDetail(spotId, isStudent ? currentUser.id() : null);
    } catch (SQLException exception) {
        log("Unable to load study spot " + spotId, exception);
        response.sendRedirect(request.getContextPath() + "/study-spots.jsp?error=The+spot+could+not+be+loaded");
        return;
    }
    if (spot == null) {
        response.sendRedirect(request.getContextPath() + "/study-spots.jsp?error=Spot+not+found");
        return;
    }
    // Guests can only see public spots. Enforced here, not just hidden in React.
    if (currentUser.isGuest() && !spot.isPublicAccess()) {
        response.sendRedirect(request.getContextPath() + "/study-spots.jsp?error=Log+in+to+view+this+spot");
        return;
    }
    
    // example: http://localhost:8080/SJSUStudyFinder/study-spot.jsp?id=1
    // Every time someone successfully opens study-spot.jsp?id=1, one row will be added to study_spot_views
    new StudyFinderDAO().recordStudySpotView(spotId, currentUser.id());

    String imageUrl = "";
    if (spot.getPhotoPath() != null && !spot.getPhotoPath().isBlank()) {
        imageUrl = request.getContextPath() + "/assets/study_spots/" + spot.getPhotoPath();
    }
    SpotDetail.Report latest = spot.getLatestReport();
    List<SpotDetail.Report> reports = spot.getRecentReports();
%>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title><%= spot.getName().replace("<", "&lt;") %> | StudyFinder</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/index-B05CoEj-.css"></head>
<body>
<div id="root"></div>
<script>
window.studyFinderContext = "<%= js(request.getContextPath()) %>";
window.currentUser = { id: <%= currentUser.id() == null ? "null" : currentUser.id() %>, name: "<%= js(currentUser.name()) %>", email: "<%= js(currentUser.email()) %>", role: "<%= js(currentUser.role()) %>" };
window.flashMessage = "<%= js(request.getParameter("message")) %>";
window.flashError = "<%= js(request.getParameter("error")) %>";
window.spotDetail = {
  spotId: <%= spot.getSpotId() %>,
  name: "<%= js(spot.getName()) %>",
  buildingName: "<%= js(spot.getBuildingName()) %>",
  buildingAcronym: "<%= js(spot.getBuildingAcronym()) %>",
  locationDetails: "<%= js(spot.getLocationDetails()) %>",
  description: "<%= js(spot.getDescription()) %>",
  capacity: <%= spot.getCapacity() %>,
  accessType: "<%= js(spot.getAccessType()) %>",
  img: "<%= js(imageUrl) %>",
  latitude: <%= spot.getLatitude() %>,
  longitude: <%= spot.getLongitude() %>,
  hours: { today: "<%= js(spot.getToday()) %>", opening: <%= jsOrNull(spot.getOpeningTime()) %>, closing: <%= jsOrNull(spot.getClosingTime()) %>, openNow: <%= spot.getOpenNow() == null ? "null" : spot.getOpenNow() %> },
  amenities: [<% for (int i = 0; i < spot.getAmenities().size(); i++) { SpotDetail.Amenity a = spot.getAmenities().get(i); %>
    { name: "<%= js(a.name()) %>", description: "<%= js(a.description()) %>", notes: "<%= js(a.notes()) %>" }<%= i + 1 < spot.getAmenities().size() ? "," : "" %><% } %>

                                        <body>
                                            <div id="root"></div>
                                            <script>
                                                window.studyFinderContext = "<%= js(request.getContextPath()) %>";
                                                window.currentUser = { id: <%= currentUser.id() == null ? "null" : currentUser.id() %>, name: "<%= js(currentUser.name()) %>", email: "<%= js(currentUser.email()) %>", role: "<%= js(currentUser.role()) %>" };
                                                window.flashMessage = "<%= js(request.getParameter("message")) %>";
                                                window.flashError = "<%= js(request.getParameter("error")) %>";
                                                window.spotDetail = {
                                                    spotId: <%= spot.getSpotId() %>,
                                                    name: "<%= js(spot.getName()) %>",
                                                    buildingName: "<%= js(spot.getBuildingName()) %>",
                                                    buildingAcronym: "<%= js(spot.getBuildingAcronym()) %>",
                                                    locationDetails: "<%= js(spot.getLocationDetails()) %>",
                                                    description: "<%= js(spot.getDescription()) %>",
                                                    capacity: <%= spot.getCapacity() %>,
                                                    accessType: "<%= js(spot.getAccessType()) %>",
                                                    img: "<%= js(imageUrl) %>",
                                                    latitude: <%= spot.getLatitude() %>,
                                                    longitude: <%= spot.getLongitude() %>,
                                                    hours: { today: "<%= js(spot.getToday()) %>", opening: <%= jsOrNull(spot.getOpeningTime()) %>, closing: <%= jsOrNull(spot.getClosingTime()) %>, openNow: <%= spot.getOpenNow() == null ? "null" : spot.getOpenNow() %> },
                                                amenities: [<% for (int i = 0; i < spot.getAmenities().size(); i++) {
                                                    SpotDetail.Amenity a = spot.getAmenities().get(i); %>
                                                        { name: "<%= js(a.name()) %>", description: "<%= js(a.description()) %>", notes: "<%= js(a.notes()) %>" } <%= i + 1 < spot.getAmenities().size() ? "," : "" %><% } %>
  ],
                                                reports: [<% for (int i = 0; i < reports.size(); i++) {
                                                    SpotDetail.Report r = reports.get(i); %>
                                                        { reportId: <%= r.reportId() %>, authorName: "<%= js(r.authorFirstName()) %>", noise: "<%= js(r.noise()) %>", crowdedness: "<%= js(r.crowdedness()) %>", outlets: <%= r.outlets() == null ? "null" : r.outlets() %>, smell: "<%= js(r.smell()) %>", wifi: "<%= js(r.wifi()) %>", notes: "<%= js(r.notes()) %>", createdAt: "<%= js(r.createdAt()) %>", minutesAgo: <%= r.minutesAgo() %>, confirmed: <%= r.confirmed() %>, disputed: <%= r.disputed() %> } <%= i + 1 < reports.size() ? "," : "" %><% } %>
  ],
                                                reviews: [
        <%
            for (int i = 0; i < spot.getReviews().size(); i++) {
                                                    SpotDetail.Review rv = spot.getReviews().get(i);
        %>
                                                    {
                                                        firstName: "<%= js(rv.firstName()) %>",
                                                        rating: <%= rv.rating() %>,
                                                        text: "<%= js(rv.text()) %>",
                                                        createdAt: "<%= js(rv.createdAt()) %>"
                                                    } <%= i + 1 < spot.getReviews().size() ? "," : "" %>
        <%
        }
        %>
],
                                                currentUserRating: <%= jsOrNull(spot.getCurrentUserRating()) %>,
                                                    isOwnLatestReport: <%= latest != null && currentUser.id() != null && latest.authorId() == currentUser.id() %>
};
</script>
<script type="module" src="${pageContext.request.contextPath}/assets/index-DNUZbf2J.js"></script>
</body></html>
                                            </script>
                                            document.addEventListener("submit", function(event) {
                                            const form = event.target;

                                            // Only handle the review form
                                            if (!form.action.includes("review-spot.jsp")) {
                                            return;
                                            }

                                            // Stop the form temporarily while getting location
                                            event.preventDefault();

                                            if (!navigator.geolocation) {
                                            alert("Location is not supported by your browser.");
                                            return;
                                            }

                                            navigator.geolocation.getCurrentPosition(
                                            function(position) {
                                            const latitude = document.createElement("input");
                                            latitude.type = "hidden";
                                            latitude.name = "latitude";
                                            latitude.value = position.coords.latitude;

                                            const longitude = document.createElement("input");
                                            longitude.type = "hidden";
                                            longitude.name = "longitude";
                                            longitude.value = position.coords.longitude;

                                            form.appendChild(latitude);
                                            form.appendChild(longitude);

                                            form.submit();
                                            },
                                            function() {
                                            alert("Please turn on location permission to earn points.");
                                            }
                                            );
                                            });
                                            <script type="module"
                                                src="${pageContext.request.contextPath}/assets/index-DxaClr9S.js"></script>
                                        </body>

                                        </html>

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.studyfinder.model.Reward" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.model.StudySpot" %>
<%@ page import="com.studyfinder.service.StudyFinderPageService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%!
    private String js(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n")
                .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
    }
%>
<%
    SessionUser currentUser = AuthSession.current(request);
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<StudySpot> studySpots = List.of();
    List<Reward> rewards = List.of();
    List<Map<String, Object>> reviews = List.of();
    List<Map<String, Object>> activityLogs = List.of();
    List<Map<String, Object>> verifications = List.of();
    int points = 0;
    String databaseError = null;
    try {
        StudyFinderPageService service = new StudyFinderPageService();
        studySpots = service.getStudySpots();
        rewards = service.getRewards();
        reviews = service.getReviews();
        activityLogs = service.getActivityLogs();
        verifications = service.getVerifications();
        if (currentUser.id() != null) points = service.getStudentPoints(currentUser.id());
    } catch (SQLException exception) {
        databaseError = "The page could not load all database data.";
        log("Unable to load StudyFinder page data", exception);
    }
%>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>StudyFinder Study Spots</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/index-DEVZBIfi.css"></head>
<body>
<% if (databaseError != null) { %><p style="padding:1rem;color:#9b1c1c;"><%= databaseError %></p><% } %>
<div id="root"></div>
<script>
window.studyFinderContext = "<%= js(request.getContextPath()) %>";
window.currentUser = { id: <%= currentUser.id() == null ? "null" : currentUser.id() %>, name: "<%= js(currentUser.name()) %>", email: "<%= js(currentUser.email()) %>", role: "<%= js(currentUser.role()) %>" };
window.studentPoints = <%= points %>;
window.studySpots = [
<% for (int i = 0; i < studySpots.size(); i++) { StudySpot spot = studySpots.get(i); %>
{ spotId:<%= spot.getSpotId() %>, name:"<%= js(spot.getName()) %>", building:"<%= js(spot.getBuilding()) %>", location:"<%= js(spot.getLocation()) %>", capacity:<%= spot.getCapacity() %>, latitude:<%= spot.getLatitude() %>, longitude:<%= spot.getLongitude() %>, accessType:"<%= js(spot.getAccessType()) %>", photoPath:"<%= js(spot.getPhotoPath()) %>", noise:"<%= js(spot.getNoise()) %>", crowdedness:"<%= js(spot.getCrowdedness()) %>", outlets:"<%= js(spot.getOutlets()) %>", updated:"<%= js(spot.getUpdated()) %>", amenities:[<% for (int j=0;j<spot.getAmenities().size();j++) { %>"<%= js(spot.getAmenities().get(j)) %>"<%= j+1<spot.getAmenities().size()?",":"" %><% } %>] }<%= i+1<studySpots.size()?",":"" %>
<% } %>];
window.rewards = [
<% for (int i=0;i<rewards.size();i++) { Reward reward=rewards.get(i); %>
{ id:<%= reward.getRewardId() %>, label:"<%= js(reward.getName()) %>", description:"<%= js(reward.getDescription()) %>", pts:<%= reward.getPointCost() %>, stock:<%= reward.getStockQuantity() %> }<%= i+1<rewards.size()?",":"" %>
<% } %>];
window.reviews = [<% for (int i=0;i<reviews.size();i++) { Map<String,Object> row=reviews.get(i); %>{userId:<%= row.get("user_id") %>,spotId:<%= row.get("spot_id") %>,reviewText:"<%= js((String) row.get("review_text")) %>",visible:<%= row.get("is_visible") %>}<%= i+1<reviews.size()?",":"" %><% } %>];
window.activityLogs = [<% for (int i=0;i<activityLogs.size();i++) { Map<String,Object> row=activityLogs.get(i); %>{userId:<%= row.get("user_id") %>,actionType:"<%= js((String) row.get("action_type")) %>",targetType:"<%= js((String) row.get("target_type")) %>",targetId:<%= row.get("target_id") == null ? "null" : row.get("target_id") %>}<%= i+1<activityLogs.size()?",":"" %><% } %>];
window.verifications = [<% for (int i=0;i<verifications.size();i++) { Map<String,Object> row=verifications.get(i); %>{reportId:<%= row.get("report_id") %>,userId:<%= row.get("user_id") %>,result:"<%= js((String) row.get("result")) %>"}<%= i+1<verifications.size()?",":"" %><% } %>];
</script>
<script type="module" src="${pageContext.request.contextPath}/assets/index-DnrjtiLu.js"></script>
</body></html>

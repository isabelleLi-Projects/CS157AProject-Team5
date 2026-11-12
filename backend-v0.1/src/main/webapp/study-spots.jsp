<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.studyfinder.model.Reward" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.model.StudySpot" %>
<%@ page import="com.studyfinder.service.StudyFinderPageService" %>
<%@ page import="com.studyfinder.service.AdminDashboardService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%!
    private String js(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n")
                .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
    }

    private String adminRows(List<Map<String, Object>> rows) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            if (i > 0) json.append(',');
            json.append("{id:").append(row.get("id"))
                .append(",label:\"").append(js(String.valueOf(row.get("label")))).append("\"")
                .append(",secondary:\"").append(js(String.valueOf(row.get("secondary")))).append("\"")
                .append(",meta:\"").append(js(String.valueOf(row.get("meta")))).append("\"");
            if (row.get("status") != null) json.append(",status:\"").append(js(String.valueOf(row.get("status")))).append("\"");
            // 1/0 when the admin can turn this row on or off (null for administrators and log rows).
            // Extra text shown under a row (used by Suggested Spots).
            if (row.get("details") != null) json.append(",details:\"").append(js(String.valueOf(row.get("details")))).append("\"");
            if (row.get("active") != null) json.append(",active:").append(String.valueOf(row.get("active")).matches("1|true"));
            json.append('}');
        }
        return json.append(']').toString();
    }
%>
<%
    SessionUser currentUser = AuthSession.current(request);
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<StudySpot> studySpots = List.of();
    List<Map<String, Object>> adminBuildings = List.of();
    List<Reward> rewards = List.of();
    List<Map<String, Object>> reviews = List.of();
    List<Map<String, Object>> activityLogs = List.of();
    List<Map<String, Object>> verifications = List.of();
    
    int points = 0;
    String databaseError = null;
    Map<String, Integer> adminCounts = Map.of();
    List<Map<String, Object>> adminReports = List.of(), adminReviews = List.of(), adminUsers = List.of(), adminSpots = List.of(), adminSuggestions = List.of(), suggestionBuildings = List.of(), adminActivity = List.of(), adminRedemptions = List.of(), adminSpotClicks = List.of();
    try {
        StudyFinderPageService service = new StudyFinderPageService();
        studySpots = service.getStudySpots();
        rewards = service.getRewards();
        reviews = service.getReviews();
        activityLogs = service.getActivityLogs();
        verifications = service.getVerifications();
        if (currentUser.id() != null) points = service.getStudentPoints(currentUser.id());
        // Students get the building list for the "Suggest a study spot" form.
        if (SessionUser.ROLE_STUDENT.equals(currentUser.role())) {
            suggestionBuildings = new com.studyfinder.service.SpotSuggestionService().getBuildings();
        }
        if (currentUser.isAdmin()) {
            AdminDashboardService admin = new AdminDashboardService();
            adminBuildings = admin.getBuildings();
            adminCounts = admin.getCounts();
            adminReports = admin.getReports();
            adminReviews = admin.getReviews();
            adminUsers = admin.getUsers();
            adminSpots = admin.getAllSpots();
            adminSuggestions = new com.studyfinder.service.SpotSuggestionService().getPendingSuggestions();
            adminActivity = admin.getActivityLogs();
            adminRedemptions = admin.getRecentRedemptions();
            try { adminSpotClicks = admin.getSpotClicks(); } catch (SQLException ignored) { /* optional migration */ }
        }
    } catch (SQLException exception) {
        databaseError = "The page could not load all database data.";
        log("Unable to load StudyFinder page data", exception);
    }
%>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>StudyFinder Study Spots</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/index-6hFF44YQ.css"></head>
<body>
<% if (databaseError != null) { %><p style="padding:1rem;color:#9b1c1c;"><%= databaseError %></p><% } %>
<div id="root"></div>
<script>
window.studyFinderContext = "<%= js(request.getContextPath()) %>";
window.currentUser = { id: <%= currentUser.id() == null ? "null" : currentUser.id() %>, name: "<%= js(currentUser.name()) %>", email: "<%= js(currentUser.email()) %>", role: "<%= js(currentUser.role()) %>" };
window.studentPoints = <%= points %>;
// Buildings for the student "Suggest a study spot" form (empty for guests and admins).
window.suggestionBuildings = [<% for (int i = 0; i < suggestionBuildings.size(); i++) { Map<String, Object> b = suggestionBuildings.get(i); %>{ id: <%= b.get("id") %>, acronym: "<%= js(String.valueOf(b.get("acronym"))) %>", name: "<%= js(String.valueOf(b.get("name"))) %>" }<%= i + 1 < suggestionBuildings.size() ? "," : "" %><% } %>];
window.flashMessage = "<%= js(request.getParameter("message")) %>";
window.flashError = "<%= js(request.getParameter("error")) %>";
window.adminMessage = "<%= js(request.getParameter("adminMessage")) %>";
window.adminError = "<%= js(request.getParameter("adminError")) %>";
window.adminTab = "<%= js(request.getParameter("adminTab")) %>";
// admin 
<% if (currentUser.isAdmin()) { %>
window.adminDashboard = {
  counts: { activeSpots:<%= adminCounts.getOrDefault("activeSpots", 0) %>, reports:<%= adminCounts.getOrDefault("reports", 0) %>, reviews:<%= adminCounts.getOrDefault("reviews", 0) %>, users:<%= adminCounts.getOrDefault("users", 0) %>, redemptions:<%= adminCounts.getOrDefault("redemptions", 0) %> },
  spotClicks: [<% for (int i=0;i<adminSpotClicks.size();i++) { Map<String,Object> row=adminSpotClicks.get(i); %>{spot:"<%= js(String.valueOf(row.get("spot"))) %>",clicks:<%= row.get("clicks") %>}<%= i+1<adminSpotClicks.size()?",":"" %><% } %>],
  reports: <%= adminRows(adminReports) %>, reviews: <%= adminRows(adminReviews) %>, activityLogs: <%= adminRows(adminActivity) %>, recentRedemptions: <%= adminRows(adminRedemptions) %>, users: <%= adminRows(adminUsers) %>, spots: <%= adminRows(adminSpots) %>, suggestions: <%= adminRows(adminSuggestions) %>,
  studySpots: [<% for (int i=0;i<studySpots.size();i++) { StudySpot spot=studySpots.get(i); %>{id:<%=spot.getSpotId()%>,name:"<%=js(spot.getName())%>",location:"<%=js(spot.getLocation())%>",latitude:<%=spot.getLatitude()%>,longitude:<%=spot.getLongitude()%>,accessType:"<%=js(spot.getAccessType())%>",noise:"<%=js(spot.getNoise())%>",noiseLevel:"low",crowded:"<%=js(spot.getCrowdedness())%>",crowdLevel:"low",outlets:"<%=js(spot.getOutlets())%>",updated:"<%=js(spot.getUpdated())%>",
  rating:<%= spot.getAverageRating() %>, reviewCount:<%= spot.getReviewCount() %>, img:"",tags:[],open:<%=spot.getOpenNow()==null?"null":spot.getOpenNow()%>,building:"<%=js(spot.getBuilding())%>",capacity:<%=spot.getCapacity()%>}<%=i+1<studySpots.size()?",":""%><% } %>]
};
// provides a dicionary like this 1:BBC, 2:KING
window.adminBuildings = [
<%
for (int i = 0; i < adminBuildings.size(); i++) {
    Map<String, Object> building = adminBuildings.get(i);
%>
{
    id: <%= building.get("id") %>,
    acronym: "<%= js(String.valueOf(building.get("acronym"))) %>",
    name: "<%= js(String.valueOf(building.get("name"))) %>",
    // photo of another spot in this building, for autofill ("" when there is none)
    photoUrl: "<%= building.get("photoPath") == null ? "" : js(request.getContextPath() + "/assets/study_spots/" + building.get("photoPath")) %>",
    // saved building_hours, e.g. Monday: ["07:00", "01:00"]
    hours: {<% int d = 0; for (Map.Entry<?, ?> day : ((Map<?, ?>) building.get("hours")).entrySet()) { String[] times = (String[]) day.getValue(); %><%= d++ > 0 ? "," : "" %>"<%= js(String.valueOf(day.getKey())) %>":["<%= js(times[0]) %>","<%= js(times[1]) %>"]<% } %>}
}<%= i + 1 < adminBuildings.size() ? "," : "" %>
<%}%>]<% } %>;
window.studySpots = [
<% for (int i = 0; i < studySpots.size(); i++) {
    StudySpot spot = studySpots.get(i);

    String imageUrl = "";
    if (spot.getPhotoPath() != null && !spot.getPhotoPath().isBlank()) {
        imageUrl = request.getContextPath()
                + "/assets/study_spots/"
                + spot.getPhotoPath();
    }
%>
{ spotId:<%= spot.getSpotId() %>,
  name:"<%= js(spot.getName()) %>",
  building:"<%= js(spot.getBuilding()) %>",
  location:"<%= js(spot.getLocation()) %>",
  open:<%= spot.getOpenNow() == null ? "null" : spot.getOpenNow() %>,
  capacity:<%= spot.getCapacity() %>,
  latitude:<%= spot.getLatitude() %>,
  rating:<%= spot.getAverageRating() %>,
  reviewCount:<%= spot.getReviewCount() %>,
  longitude:<%= spot.getLongitude() %>,
  accessType:"<%= js(spot.getAccessType()) %>",
  photoPath:"<%= js(spot.getPhotoPath()) %>",
  img:"<%= js(imageUrl) %>", noise:"<%= js(spot.getNoise()) %>", crowdedness:"<%= js(spot.getCrowdedness()) %>", outlets:"<%= js(spot.getOutlets()) %>", updated:"<%= js(spot.getUpdated()) %>", amenities:[<% for (int j=0;j<spot.getAmenities().size();j++) { %>"<%= js(spot.getAmenities().get(j)) %>"<%= j+1<spot.getAmenities().size()?",":"" %><% } %>] }<%= i+1<studySpots.size()?",":"" %>
<% } %>];
window.rewards = [
<%
for (int i = 0; i < rewards.size(); i++) {
    Reward reward = rewards.get(i);
    String rewardImage = "";
    if (reward.getPhotoPath() != null && !reward.getPhotoPath().isBlank()) {
        rewardImage =request.getContextPath() +  "/assets/rewards/" + reward.getPhotoPath();
    }
%>
{
    id: <%= reward.getRewardId() %>,
    label: "<%= js(reward.getName()) %>",
    description: "<%= js(reward.getDescription()) %>",
    pts: <%= reward.getPointCost() %>,
    stock: <%= reward.getStockQuantity() %>,
    img: "<%= js(rewardImage) %>"
}<%= i + 1 < rewards.size() ? "," : "" %>
<%}%>];

window.reviews = [<% for (int i=0;i<reviews.size();i++) { Map<String,Object> row=reviews.get(i); %>{userId:<%= row.get("user_id") %>,spotId:<%= row.get("spot_id") %>,reviewText:"<%= js((String) row.get("review_text")) %>",visible:<%= row.get("is_visible") %>}<%= i+1<reviews.size()?",":"" %><% } %>];
window.activityLogs = [<% for (int i=0;i<activityLogs.size();i++) { Map<String,Object> row=activityLogs.get(i); %>{userId:<%= row.get("user_id") %>,actionType:"<%= js((String) row.get("action_type")) %>",targetType:"<%= js((String) row.get("target_type")) %>",targetId:<%= row.get("target_id") == null ? "null" : row.get("target_id") %>}<%= i+1<activityLogs.size()?",":"" %><% } %>];
window.verifications = [<% for (int i=0;i<verifications.size();i++) { Map<String,Object> row=verifications.get(i); %>{reportId:<%= row.get("report_id") %>,userId:<%= row.get("user_id") %>,result:"<%= js((String) row.get("result")) %>"}<%= i+1<verifications.size()?",":"" %><% } %>];
</script>
<script type="module" src="${pageContext.request.contextPath}/assets/index-C1_wAM6y.js"></script>
</body></html>

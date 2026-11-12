<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.StudentHistoryService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%!
    private String js(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n")
                .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")
                .replace("<", "\\u003c");
    }
    // Rows from StudentHistoryDAO as a JavaScript array; numbers stay numbers, everything else is a string.
    private String jsRows(List<Map<String, Object>> rows) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) out.append(',');
            out.append('{');
            int k = 0;
            for (Map.Entry<String, Object> e : rows.get(i).entrySet()) {
                if (k++ > 0) out.append(',');
                Object v = e.getValue();
                out.append(e.getKey()).append(':');
                if (v instanceof Number) out.append(((Number) v).longValue());
                else if (v instanceof Boolean) out.append(((Boolean) v) ? 1 : 0);
                else out.append('"').append(js(v == null ? "" : String.valueOf(v))).append('"');
            }
            out.append('}');
        }
        return out.append(']').toString();
    }
%>
<%
    // My history (Functional Requirement #8): spots the student visited, reported, rated, or
    // reviewed, plus points earned and spent. Students only.
    SessionUser currentUser = AuthSession.current(request);
    if (currentUser == null || currentUser.id() == null || currentUser.isGuest()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    if (!SessionUser.ROLE_STUDENT.equals(currentUser.role())) {
        response.sendRedirect(request.getContextPath() + "/study-spots.jsp");
        return;
    }

    StudentHistoryService service = new StudentHistoryService();
    List<Map<String, Object>> activity = List.of(), spots = List.of(), suggestions = List.of();
    int points = 0;
    String error = "";
    try {
        activity = service.getPointsActivity(currentUser.id());
        spots = service.getSpotHistory(currentUser.id());
        suggestions = service.getSuggestions(currentUser.id());
        points = service.getCurrentPoints(currentUser.id());
    } catch (SQLException exception) {
        log("Unable to load student history", exception);
        error = "Your history could not be loaded.";
    }
    int[] totals = service.totals(activity);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My history | StudyFinder</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/index-D29Bf_X6.css">
</head>
<body>
<div id="root"></div>
<script>
window.studyFinderContext = "<%= js(request.getContextPath()) %>";
window.currentUser = { id: <%= currentUser.id() %>, name: "<%= js(currentUser.name()) %>", email: "<%= js(currentUser.email()) %>", role: "<%= js(currentUser.role()) %>" };
window.flashMessage = "";
window.flashError = "<%= js(error) %>";
window.studentHistory = {
  points: <%= points %>,
  earned: <%= totals[0] %>,
  spent: <%= totals[1] %>,
  activity: <%= jsRows(activity) %>,
  spots: <%= jsRows(spots) %>,
  suggestions: <%= jsRows(suggestions) %>
};
</script>
<script type="module" src="${pageContext.request.contextPath}/assets/index-B_a3fuMC.js"></script>
</body>
</html>

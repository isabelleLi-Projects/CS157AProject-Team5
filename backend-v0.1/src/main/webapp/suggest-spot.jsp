<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.studyfinder.model.SessionUser" %>
<%@ page import="com.studyfinder.service.SpotSuggestionService" %>
<%@ page import="com.studyfinder.util.AuthSession" %>
<%
    // Saves a student's "Suggest a study spot" form for an administrator to review.
    if (!"POST".equalsIgnoreCase(request.getMethod())) { response.sendError(405); return; }

    String context = request.getContextPath();
    SessionUser current = AuthSession.current(request);
    if (current == null || current.id() == null || current.isGuest()) {
        response.sendRedirect(context + "/login.jsp");
        return;
    }
    String back = context + "/study-spots.jsp?nav=Find+Spots&";
    if (!SessionUser.ROLE_STUDENT.equals(current.role())) {
        response.sendRedirect(back + "error=Only+students+can+suggest+study+spots");
        return;
    }

    try {
        String error = new SpotSuggestionService().suggest(
            current.id(),
            request.getParameter("spotName"),
            request.getParameter("buildingId"),
            request.getParameter("locationDetails"),
            request.getParameter("accessType"),
            request.getParameter("capacity"),
            request.getParameter("coordinates"),
            request.getParameter("description"),
            request.getParameterValues("amenities")
        );
        if (error == null) {
            response.sendRedirect(back + "message=Thanks!+An+admin+will+review+your+spot.+You+earn+15+points+if+it+is+approved.");
        } else {
            response.sendRedirect(back + "error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
    } catch (Exception exception) {
        log("Spot suggestion failed", exception);
        response.sendRedirect(back + "error=Your+suggestion+could+not+be+saved");
    }
%>

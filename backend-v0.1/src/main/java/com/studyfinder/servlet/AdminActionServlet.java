package com.studyfinder.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.studyfinder.model.SessionUser;
import com.studyfinder.service.AdminDashboardService;
import com.studyfinder.util.AuthSession;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/admin-action")
@MultipartConfig(
    maxFileSize = 5 * 1024 * 1024,
    maxRequestSize = 10 * 1024 * 1024
)
public class AdminActionServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        SessionUser current = AuthSession.current(request);

        if (current == null || !current.isAdmin()) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Administrator access required"
            );
            return;
        }

        String action = request.getParameter("action");

        try {
            AdminDashboardService service =
                    new AdminDashboardService();

            if ("addReward".equals(action)) {
                addReward(request, service);

            } else if ("addSpot".equals(action)) {
                addStudySpot(request, service);

            } else {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid administrator action"
                );
                return;
            }

            response.sendRedirect(
                request.getContextPath()
                    + "/study-spots.jsp?adminMessage=Saved"
            );

        } catch (IllegalArgumentException exception) {
            // Bad form input (for example coordinates with too many decimals): go back with a message.
            String message = exception instanceof NumberFormatException
                    ? "Please enter whole numbers where a number is required."
                    : exception.getMessage();
            response.sendRedirect(request.getContextPath() + "/study-spots.jsp?adminTab=Add+Study+Spot&adminError="
                    + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception exception) {
            log("Administrator action failed", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,exception.getMessage());
        }
    }

    private void addReward(HttpServletRequest request, AdminDashboardService service) throws Exception {
        Part rewardPhoto = request.getPart("rewardPhoto");
        String photoPath = savePhoto(rewardPhoto, "rewards");
        
        service.addReward(
            request.getParameter("rewardName"),
            request.getParameter("description"),
            Integer.parseInt(request.getParameter("pointsRequired")),
            Integer.parseInt(request.getParameter("stockQuantity")),
            photoPath
        );
    }

    private void addStudySpot(HttpServletRequest request, AdminDashboardService service) throws Exception {

        String buildingText = request.getParameter("buildingId");

        Integer buildingId = null;

        if (buildingText != null && !buildingText.isBlank()) {
            buildingId = Integer.parseInt(buildingText);
        }

        // Checks the format and that each value has at most 6 decimal places (decimal(9,6)).
        double[] coordinates = service.parseCoordinates(request.getParameter("coordinates"));
        double latitude = coordinates[0];
        double longitude = coordinates[1];

        Part photo = request.getPart("photo");

        String photoPath = savePhoto(photo, "study_spots");

        // No new upload: reuse the photo of another spot in the same building, if the form asked for it.
        if (photoPath == null && "1".equals(request.getParameter("useBuildingPhoto"))) {
            photoPath = service.getBuildingPhoto(buildingId);
        }

        // Hours from the form: "MondayOpening" / "MondayClosing" etc. Closed days send nothing.
        java.util.Map<String, String> openingTimes = new java.util.HashMap<>();
        java.util.Map<String, String> closingTimes = new java.util.HashMap<>();
        for (String day : new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" }) {
            openingTimes.put(day, request.getParameter(day + "Opening"));
            closingTimes.put(day, request.getParameter(day + "Closing"));
        }

        service.addStudySpot(
            request.getParameter("spotName"),
            buildingId,
            request.getParameter("locationDetails"),
            request.getParameter("accessType"),
            Integer.parseInt(request.getParameter("capacity")),
            latitude,
            longitude,
            photoPath,
            request.getParameterValues("amenities"),
            openingTimes,
            closingTimes
        );
    }

    // saving photo to tomcat/WEBAPP/SJSUStudyFinder/backend-v0.1/.../assets/rewards
    // before rebuilding the war file, you will need to copy this into your project folder 
    private String savePhoto(Part photo, String folder) throws IOException {

        if (photo == null || photo.getSize() == 0) {
            return null;
        }

        String originalFileName =Paths.get(photo.getSubmittedFileName()).getFileName().toString();

        String lowerFileName = originalFileName.toLowerCase();

        String extension;

        if (lowerFileName.endsWith(".jpg")|| lowerFileName.endsWith(".jpeg")) {
            extension = ".jpg";
        } else if (lowerFileName.endsWith(".png")) {
            extension = ".png";
        } else if (lowerFileName.endsWith(".gif")) {
            extension = ".gif";
        } else {
            throw new IOException(
                "Only JPG, PNG, and GIF images are allowed" 
            );
        }

        String savedFileName =
                UUID.randomUUID() + extension;

        // should be the acutal path of where your project is stored 
        String realDirectory = getServletContext().getRealPath("/assets/" + folder);

        if (realDirectory == null) {
            throw new IOException(
                "Could not locate the study_spots directory"
            );
        }

        Path uploadDirectory = Paths.get(realDirectory);

        Files.createDirectories(uploadDirectory);

        Path destination = uploadDirectory.resolve(savedFileName);

        // test tomcat is saving to correct place
        System.out.println("Upload directory: " + uploadDirectory);
        System.out.println("Saving image to: " + destination);
        try (InputStream inputStream = photo.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return savedFileName;
    }
}
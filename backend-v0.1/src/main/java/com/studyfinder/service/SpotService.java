package com.studyfinder.service;

import com.studyfinder.dao.SpotDetailDAO;
import com.studyfinder.model.SpotDetail;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Business layer for the Study Spot page.
 * study-spot.jsp and rate-spot.jsp talk to this class instead of JDBC.
 */
public class SpotService {

    /** Campus time zone, so "open now" is right no matter where the server runs. */
    private static final ZoneId CAMPUS_ZONE = ZoneId.of("America/Los_Angeles");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    private final SpotDetailDAO spotDetailDAO = new SpotDetailDAO();

    /**
     * Loads everything the page shows. userId is the logged-in student or null for guests/admins.
     * Returns null when the spot does not exist or is inactive.
     */
    public SpotDetail getSpotDetail(int spotId, Integer userId) throws SQLException {
        SpotDetail spot = spotDetailDAO.getSpotDetail(spotId);
        if (spot == null) return null;

        addTodaysHours(spot, ZonedDateTime.now(CAMPUS_ZONE));

        SpotDetail.Report latest = spot.getLatestReport();
        if (latest != null && userId != null) {
            spot.setCurrentUserRating(spotDetailDAO.getUserRating(latest.reportId(), userId));
        }
        return spot;
    }

    /** @return null on success, otherwise a message to show the student */
    public String rateLatestReport(int userId, int spotId, int reportId, String result) throws SQLException {
        if (!"confirmed".equals(result) && !"disputed".equals(result)) {
            return "Choose accurate or inaccurate.";
        }
        return spotDetailDAO.rateLatestReport(userId, spotId, reportId, result);
    }

    // Choices offered by the report form. Anything else is rejected.
    private static final List<String> NOISE = List.of("Quiet", "Moderate", "Loud");
    private static final List<String> CROWDEDNESS = List.of("Available", "Moderate", "Crowded");
    private static final List<String> SMELL = List.of("None", "Low", "Strong");
    private static final List<String> WIFI = List.of("Strong", "Weak", "None");
    private static final int MAX_NOTES = 255;

    /** @return null on success, otherwise a message to show the student */
    public String submitReport(int userId, int spotId, String noise, String crowdedness, String outlets,
                               String smell, String wifi, String notes) throws SQLException {
        if (!NOISE.contains(noise) || !CROWDEDNESS.contains(crowdedness) || !SMELL.contains(smell)
                || !WIFI.contains(wifi) || !("1".equals(outlets) || "0".equals(outlets))) {
            return "Fill in every condition.";
        }
        String trimmedNotes = notes == null ? "" : notes.trim();
        if (trimmedNotes.length() > MAX_NOTES) {
            return "Notes can be at most " + MAX_NOTES + " characters.";
        }
        return spotDetailDAO.submitReport(userId, spotId, noise, crowdedness, Integer.parseInt(outlets),
                smell, wifi, trimmedNotes.isEmpty() ? null : trimmedNotes);
    }

    private void addTodaysHours(SpotDetail spot, ZonedDateTime now) throws SQLException {
        DayOfWeek todayDay = now.getDayOfWeek();
        String today = todayDay.getDisplayName(TextStyle.FULL, Locale.US);
        String yesterday = todayDay.minus(1).getDisplayName(TextStyle.FULL, Locale.US);
        spot.setToday(today);

        Map<String, LocalTime[]> hours = spotDetailDAO.getBuildingHours(spot.getBuildingId(), List.of(today, yesterday));
        LocalTime[] todayHours = hours.get(today);
        if (todayHours == null) return; // page shows "Hours not available"

        LocalTime open = todayHours[0];
        LocalTime close = todayHours[1];
        LocalTime time = now.toLocalTime();
        spot.setOpeningTime(open.format(TIME_FORMAT));
        spot.setClosingTime(close.format(TIME_FORMAT));

        boolean openNow;
        if (close.isAfter(open)) {
            openNow = !time.isBefore(open) && time.isBefore(close);
        } else {
            // Closes after midnight, e.g. 07:00 -> 01:00: open from opening time until midnight.
            openNow = !time.isBefore(open);
        }

        // Still inside yesterday's hours that ran past midnight, e.g. 00:30 when yesterday closed at 01:00.
        LocalTime[] yesterdayHours = hours.get(yesterday);
        if (!openNow && yesterdayHours != null && !yesterdayHours[1].isAfter(yesterdayHours[0])) {
            openNow = time.isBefore(yesterdayHours[1]);
        }
        spot.setOpenNow(openNow);
    }
}

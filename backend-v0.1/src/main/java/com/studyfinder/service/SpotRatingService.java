package com.studyfinder.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.studyfinder.dao.SpotRatingDAO;

/**
 * Combines reviews and reports into one rating per spot, and summarizes a spot's typical
 * conditions by time of day.
 *
 * Overall score (out of 5) = 70% average review stars + 30% report accuracy x 5, where
 * report accuracy = confirmed / (confirmed + disputed) ratings on the spot's reports.
 * If a spot has only reviews or only ratings, that one alone is used.
 */
public class SpotRatingService {

    private static final double REVIEW_WEIGHT = 0.7;
    private static final double ACCURACY_WEIGHT = 0.3;
    private static final List<String> BLOCKS = List.of("Morning", "Afternoon", "Evening", "Night");

    /** Combined rating for one spot. */
    public record Rating(Double overall, double avgStars, int reviewCount, Integer accuracyPercent, int ratingCount) {
    }

    /** Most common conditions in one time-of-day block. */
    public record TimeOfDay(String block, int reports, String noise, String crowdedness) {
    }

    private final SpotRatingDAO dao = new SpotRatingDAO();

    public Rating getRating(int spotId) throws SQLException {
        return combine(dao.getRatingData(spotId));
    }

    /** spot_id -> combined rating, for the spot list and map. */
    public Map<Integer, Rating> getAllRatings() throws SQLException {
        Map<Integer, Rating> ratings = new HashMap<>();
        for (Map.Entry<Integer, double[]> entry : dao.getAllRatingData().entrySet()) {
            ratings.put(entry.getKey(), combine(entry.getValue()));
        }
        return ratings;
    }

    /** data = {avgStars, reviewCount, confirmed, disputed} */
    static Rating combine(double[] data) {
        double avgStars = data[0];
        int reviewCount = (int) data[1];
        int confirmed = (int) data[2];
        int disputed = (int) data[3];
        int ratingCount = confirmed + disputed;
        Double accuracy = ratingCount == 0 ? null : (double) confirmed / ratingCount;

        Double overall;
        if (reviewCount > 0 && accuracy != null) {
            overall = REVIEW_WEIGHT * avgStars + ACCURACY_WEIGHT * accuracy * 5;
        } else if (reviewCount > 0) {
            overall = avgStars;
        } else if (accuracy != null) {
            overall = accuracy * 5;
        } else {
            overall = null; // no reviews and no ratings yet
        }
        if (overall != null) overall = Math.round(overall * 10) / 10.0;
        return new Rating(overall, Math.round(avgStars * 10) / 10.0, reviewCount,
                accuracy == null ? null : (int) Math.round(accuracy * 100), ratingCount);
    }

    /** For each time block that has reports: how many, and the most common noise and crowdedness. */
    public List<TimeOfDay> getTimeOfDay(int spotId) throws SQLException {
        Map<String, Map<String, Integer>> noise = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> crowd = new LinkedHashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        for (String[] row : dao.getReportsByTimeOfDay(spotId)) {
            counts.merge(row[0], 1, Integer::sum);
            if (row[1] != null) noise.computeIfAbsent(row[0], k -> new HashMap<>()).merge(row[1], 1, Integer::sum);
            if (row[2] != null) crowd.computeIfAbsent(row[0], k -> new HashMap<>()).merge(row[2], 1, Integer::sum);
        }
        List<TimeOfDay> result = new ArrayList<>();
        for (String block : BLOCKS) {
            if (!counts.containsKey(block)) continue;
            result.add(new TimeOfDay(block, counts.get(block), mostCommon(noise.get(block)), mostCommon(crowd.get(block))));
        }
        return result;
    }

    private static String mostCommon(Map<String, Integer> values) {
        if (values == null || values.isEmpty()) return "Unknown";
        return values.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }
}

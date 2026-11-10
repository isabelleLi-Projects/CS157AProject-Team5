USE studyfinder;

-- One row represents a study-spot detail-page view.
-- Do not use activity_logs for every click; this table keeps high-volume
-- analytics separate from meaningful user and administrator actions.
CREATE TABLE IF NOT EXISTS study_spot_views (
    view_id BIGINT NOT NULL AUTO_INCREMENT,
    spot_id INT NOT NULL,
    user_id INT NULL,
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (view_id),
    INDEX idx_spot_views_spot_time (spot_id, viewed_at),
    INDEX idx_spot_views_time (viewed_at),
    CONSTRAINT fk_spot_views_spot
        FOREIGN KEY (spot_id) REFERENCES study_spots (spot_id),
    CONSTRAINT fk_spot_views_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Example dashboard metric:
-- SELECT s.spot_name, COUNT(v.view_id) AS click_count
-- FROM study_spots s
-- LEFT JOIN study_spot_views v ON v.spot_id = s.spot_id
-- WHERE s.is_active = 1
-- GROUP BY s.spot_id, s.spot_name
-- ORDER BY click_count DESC;

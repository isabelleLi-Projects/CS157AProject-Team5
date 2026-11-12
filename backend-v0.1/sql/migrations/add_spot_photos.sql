-- =====================================================================
-- SJSU StudyFinder: students add photos of study spots
--
-- A student at a spot (within 0.3 miles) can upload a photo. It shows in a
-- gallery on the spot page. Admins can hide a photo (is_visible = 0).
-- The image file is saved in assets/spot_photos/; this table stores its name.
--
-- New relationship: Students "uploads" SpotPhotos, SpotPhotos "of" StudySpots.
--
-- Run this once, AFTER schema.sql and sample_data.sql.
-- =====================================================================

USE studyfinder;

CREATE TABLE IF NOT EXISTS spot_photos (
  photo_id INT NOT NULL AUTO_INCREMENT,
  spot_id INT NOT NULL,
  user_id INT NOT NULL,
  photo_path VARCHAR(255) NOT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_visible TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (photo_id),
  KEY idx_spot_photos_spot (spot_id, is_visible),
  CONSTRAINT fk_spot_photos_spot FOREIGN KEY (spot_id) REFERENCES study_spots (spot_id),
  CONSTRAINT fk_spot_photos_student FOREIGN KEY (user_id) REFERENCES students (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

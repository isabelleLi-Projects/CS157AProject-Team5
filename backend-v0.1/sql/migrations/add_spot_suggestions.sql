-- =====================================================================
-- SJSU StudyFinder: students suggest study spots, admins approve them
--
-- A student fills in "Suggest a study spot". The spot is saved hidden
-- (is_active = 0) with suggestion_status = 'pending'. An admin then:
--   Approve -> is_active = 1, suggestion_status = 'approved', student +15 points
--   Reject  -> stays hidden, suggestion_status = 'rejected'
--
-- New columns on study_spots:
--   suggested_by       the student who suggested it (NULL = added by an admin)
--   suggestion_status  'pending', 'approved', 'rejected' (NULL = added by an admin)
--
-- This adds the relationship Students "suggests" StudySpots (one student,
-- many spots). Spots added by admins keep both columns NULL.
--
-- Run this once, AFTER schema.sql and sample_data.sql.
-- =====================================================================

USE studyfinder;

ALTER TABLE study_spots
  ADD COLUMN suggested_by INT NULL AFTER photo_path,
  ADD COLUMN suggestion_status VARCHAR(10) NULL AFTER suggested_by,
  ADD CONSTRAINT fk_study_spots_suggested_by
    FOREIGN KEY (suggested_by) REFERENCES students (user_id);

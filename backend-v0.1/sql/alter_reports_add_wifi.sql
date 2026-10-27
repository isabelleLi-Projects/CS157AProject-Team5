-- =====================================================================
-- SJSU StudyFinder: add Wi-Fi quality to reports
-- Functional Requirement #4 (Study Spot Page): students report outlets,
-- crowdedness, Wi-Fi, noise level, and smell level for a spot.
--
-- reports had no column for Wi-Fi, so this adds one. It is NULL for the
-- existing sample reports, which the page shows as "Unknown".
-- Allowed values (checked in SpotService): 'Strong', 'Weak', 'None'.
--
-- Run this AFTER schema.sql and sample_data.sql.
-- =====================================================================

USE studyfinder;

ALTER TABLE reports
  ADD COLUMN wifi_quality VARCHAR(20) DEFAULT NULL AFTER smell_level;

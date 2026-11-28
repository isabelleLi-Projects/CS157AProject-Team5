USE studyfinder;

-- Add photo_path if it does not exist.
SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'rewards'
        AND COLUMN_NAME = 'photo_path'
    ),
    'SELECT 1',
    'ALTER TABLE rewards ADD COLUMN photo_path VARCHAR(255) DEFAULT NULL'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add face_value if it does not exist.
SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'rewards'
        AND COLUMN_NAME = 'face_value'
    ),
    'SELECT 1',
    'ALTER TABLE rewards ADD COLUMN face_value DECIMAL(6,2) DEFAULT NULL'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Set gift card values based on 50 points = $1.
UPDATE rewards
SET face_value = points_required / 50.0
WHERE reward_id BETWEEN 1 AND 10
AND face_value IS NULL;

-- Set gift card image filenames.
UPDATE rewards SET photo_path = 'amazon.png'
WHERE reward_id = 1 AND reward_name = 'Amazon Gift Card';

UPDATE rewards SET photo_path = 'target.png'
WHERE reward_id = 2 AND reward_name = 'Target Gift Card';

UPDATE rewards SET photo_path = 'starbucks.png'
WHERE reward_id = 3 AND reward_name = 'Starbucks Gift Card';

UPDATE rewards SET photo_path = 'safeway.png'
WHERE reward_id = 4 AND reward_name = 'Safeway Gift Card';

UPDATE rewards SET photo_path = 'chipotle.png'
WHERE reward_id = 5 AND reward_name = 'Chipotle Gift Card';

UPDATE rewards SET photo_path = 'in-n-out.png'
WHERE reward_id = 6 AND reward_name = 'In-N-Out Gift Card';

UPDATE rewards SET photo_path = 'barnes-noble.png'
WHERE reward_id = 7 AND reward_name = 'Barnes and Noble Gift Card';

UPDATE rewards SET photo_path = 'doordash.png'
WHERE reward_id = 8 AND reward_name = 'DoorDash Gift Card';

UPDATE rewards SET photo_path = 'jamba.png'
WHERE reward_id = 9 AND reward_name = 'Jamba Gift Card';

UPDATE rewards SET photo_path = 'safeway-coffee.png'
WHERE reward_id = 10 AND reward_name = 'Safeway Coffee Gift Card';

-- Verify the updated reward data.
SELECT reward_id, reward_name, points_required,
       face_value, photo_path
FROM rewards;
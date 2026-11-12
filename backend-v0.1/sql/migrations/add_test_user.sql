USE studyfinder;

INSERT INTO users
(full_name, email, password_hash, email_verified, account_status)
VALUES
('Test Student',
 'teststudent@sjsu.edu',
 'pbkdf2_sha256$600000$tIKaRVw9HRY1VyUaPuqCfA==$U7SvyXaK98ZJ/SNDXd+oAimKzlu3eKgJ9syALtC7IQI=',
 1,
 'active');

SET @user_id = LAST_INSERT_ID();

INSERT INTO students
(user_id, student_id)
VALUES
(@user_id, 'TEST001');
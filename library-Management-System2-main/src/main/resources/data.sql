-- Seed data. Uses INSERT IGNORE so it is safe to run on every startup (relies on unique constraints).

INSERT IGNORE INTO users (id, name, email, password, mobile, role, enrollment_number, department, course, year, semester, profile_photo, account_status, registration_date)
VALUES
(1, 'System Admin', 'admin@library.com', '$2b$10$XZit6c2rwl0zd0LayCvw1.AvQCfACVa7XnXevQ1c4.HTFPZFmQaLK', '9999999999', 'ADMIN', NULL, NULL, NULL, NULL, NULL, NULL, 'ACTIVE', CURRENT_DATE),
(2, 'Default Librarian', 'librarian@library.com', '$2b$10$dYVoFPWMmVm674nYVVNEzesKddmm.lgqvY0wtfY5O/TP0xmmQPvzS', '8888888888', 'LIBRARIAN', NULL, NULL, NULL, NULL, NULL, NULL, 'ACTIVE', CURRENT_DATE),
(3, 'Rahul Sharma', 'student@library.com', '$2b$10$gVGf4TqFhsA9ClKw/hlSM.9W4KHqvvF5NqVht0MUkofZuoAG2Tv0O', '7777777777', 'STUDENT', '2026CS1025', 'Computer Science', 'B.Tech', 4, 7, NULL, 'ACTIVE', CURRENT_DATE);

INSERT IGNORE INTO categories (id, name, description) VALUES
(1, 'Programming', 'Books related to programming languages'),
(2, 'Database', 'Database systems and management'),
(3, 'Computer Networks', 'Networking concepts and protocols'),
(4, 'Operating Systems', 'OS concepts and design'),
(5, 'Data Structures', 'Data structures and algorithms'),
(6, 'AI & Machine Learning', 'Artificial intelligence and ML'),
(7, 'Mathematics', 'Mathematics for engineering'),
(8, 'Physics', 'Physics books'),
(9, 'Chemistry', 'Chemistry books'),
(10, 'Management', 'Management and business books'),
(11, 'Competitive Exams', 'Books for competitive exam preparation'),
(12, 'General Knowledge', 'General knowledge and awareness');

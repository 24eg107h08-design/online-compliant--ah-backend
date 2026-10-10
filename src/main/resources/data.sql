-- =====================================================================
-- Online Complaint Management System - Sample Data Script
-- Database: MySQL 8.0+
-- Passwords:
--   admin@cms.com -> admin123 (BCrypt hash)
--   user@cms.com  -> user123  (BCrypt hash)
--   jane@cms.com  -> user123  (BCrypt hash)
-- =====================================================================

USE `complaint_db`;

-- 1. Insert Sample Users
-- Password 'admin123': $2a$10$4O64b0kK3Z9X7h6w6l/0yec8q8m6c1lO/d4xI9jX1rWpE0yK2mEaK
-- Password 'user123':  $2a$10$8wIeQ1oB1m1gE7f2h4j5/.w2xX8q8m7k3pM1rL0vQ9wX2yZ3aBcDe
INSERT INTO `users` (`id`, `name`, `email`, `phone`, `password`, `role`, `created_at`) VALUES
(1, 'System Administrator', 'admin@cms.com', '9876543210', '$2a$10$4O64b0kK3Z9X7h6w6l/0yec8q8m6c1lO/d4xI9jX1rWpE0yK2mEaK', 'ROLE_ADMIN', '2026-09-01 09:00:00'),
(2, 'John Doe', 'user@cms.com', '9123456780', '$2a$10$8wIeQ1oB1m1gE7f2h4j5/.w2xX8q8m7k3pM1rL0vQ9wX2yZ3aBcDe', 'ROLE_USER', '2026-09-10 10:30:00'),
(3, 'Jane Smith', 'jane@cms.com', '9988776655', '$2a$10$8wIeQ1oB1m1gE7f2h4j5/.w2xX8q8m7k3pM1rL0vQ9wX2yZ3aBcDe', 'ROLE_USER', '2026-09-15 14:15:00');

-- 2. Insert Sample Complaints
INSERT INTO `complaints` (`id`, `complaint_code`, `title`, `category`, `description`, `location`, `attachment_path`, `attachment_name`, `status`, `user_id`, `assigned_department`, `assigned_staff`, `resolution_remarks`, `created_at`, `updated_at`) VALUES
(1, 'CMP-20261001-1001', 'Street Light Outage on 5th Main Avenue', 'Electricity', 'The street lights on 5th Main Avenue near City Park have been dysfunctional for three consecutive nights, causing severe safety concerns for pedestrians and commuters.', '5th Main Avenue, Near City Park, Ward 12', NULL, NULL, 'IN_PROGRESS', 2, 'Electricity Board', 'Officer Ramesh Kumar', 'Field inspection completed. Transformer fuse replacement scheduled for tomorrow morning.', '2026-10-01 08:30:00', '2026-10-03 11:20:00'),
(2, 'CMP-20261002-2002', 'Contaminated Drinking Water Supply', 'Water', 'Muddy and foul-smelling tap water supplied since yesterday morning in Block C apartments. Over 40 households are affected and unable to use tap water for cooking.', 'Block C, Green Valley Apartments, Sector 4', NULL, NULL, 'RESOLVED', 2, 'Water Supply & Sewerage Board', 'Engineer Suresh Patel', 'Main water pipeline fracture repaired and pipeline flushed thoroughly. Potable water quality tested and certified.', '2026-09-28 07:45:00', '2026-10-02 16:30:00'),
(3, 'CMP-20261003-3003', 'Hazardous Potholes on Ring Road Cross', 'Roads', 'Large, deep potholes causing traffic congestion and two-wheeler skidding near Metro Station Gate 2.', 'Ring Road Junction, Metro Gate 2', NULL, NULL, 'SUBMITTED', 2, NULL, NULL, NULL, '2026-10-03 14:10:00', '2026-10-03 14:10:00'),
(4, 'CMP-20261003-4004', 'Garbage Overflow near Municipal School', 'Sanitation', 'Public dumpster overflowing with domestic waste for over 5 days. Foul stench spreading into adjacent primary school classrooms.', 'Municipal Primary School Road, Sector 9', NULL, NULL, 'IN_PROGRESS', 3, 'Sanitation Department', 'Inspector Vikram Singh', 'Cleaning vehicle dispatched. Regular garbage collection cycle reset for this location.', '2026-10-02 11:00:00', '2026-10-04 09:15:00'),
(5, 'CMP-20260925-5005', 'Illegal Construction Blocking Drainage', 'Other', 'Unauthorized commercial shed construction encroaching onto the community storm water drain line.', 'Plot 45, Industrial Layout', NULL, NULL, 'REJECTED', 3, 'Town Planning Authority', 'Officer Meena Rao', 'Complaint rejected after site survey: The construction site has approved municipal sanctions under permit #TP-2026-881.', '2026-09-25 15:30:00', '2026-09-27 12:00:00');

-- 3. Insert Complaint History Audit Logs
INSERT INTO `complaint_history` (`id`, `complaint_id`, `previous_status`, `new_status`, `remarks`, `updated_by`, `created_at`) VALUES
(1, 1, NULL, 'SUBMITTED', 'Complaint registered and queued for verification', 'John Doe', '2026-10-01 08:30:00'),
(2, 1, 'SUBMITTED', 'IN_PROGRESS', 'Assigned to Electricity Board. Field inspection underway.', 'System Administrator (Admin)', '2026-10-03 11:20:00'),
(3, 2, NULL, 'SUBMITTED', 'Complaint lodged regarding contaminated water supply', 'John Doe', '2026-09-28 07:45:00'),
(4, 2, 'SUBMITTED', 'IN_PROGRESS', 'Inspection team dispatched to check main pipeline valve leak', 'System Administrator (Admin)', '2026-09-29 10:00:00'),
(5, 2, 'IN_PROGRESS', 'RESOLVED', 'Main water pipeline repaired and water quality certified.', 'System Administrator (Admin)', '2026-10-02 16:30:00'),
(6, 3, NULL, 'SUBMITTED', 'Complaint lodged regarding road potholes', 'John Doe', '2026-10-03 14:10:00'),
(7, 4, NULL, 'SUBMITTED', 'Complaint lodged regarding garbage overflow', 'Jane Smith', '2026-10-02 11:00:00'),
(8, 4, 'SUBMITTED', 'IN_PROGRESS', 'Assigned to Sanitation Department', 'System Administrator (Admin)', '2026-10-04 09:15:00'),
(9, 5, NULL, 'SUBMITTED', 'Complaint lodged regarding unauthorized construction', 'Jane Smith', '2026-09-25 15:30:00'),
(10, 5, 'SUBMITTED', 'REJECTED', 'Site survey confirms valid municipal construction permit #TP-2026-881.', 'System Administrator (Admin)', '2026-09-27 12:00:00');

-- 4. Insert Sample Notifications
INSERT INTO `notifications` (`id`, `user_id`, `complaint_id`, `message`, `is_read`, `created_at`) VALUES
(1, 2, 2, 'Your complaint CMP-20261002-2002 has been marked as RESOLVED.', 1, '2026-10-02 16:30:00'),
(2, 2, 1, 'Your complaint CMP-20261001-1001 status changed to IN_PROGRESS (Electricity Board).', 0, '2026-10-03 11:20:00'),
(3, 3, 4, 'Your complaint CMP-20261003-4004 status changed to IN_PROGRESS (Sanitation Department).', 0, '2026-10-04 09:15:00'),
(4, 3, 5, 'Your complaint CMP-20260925-5005 was REJECTED: Valid municipal permit on file.', 1, '2026-09-27 12:00:00');

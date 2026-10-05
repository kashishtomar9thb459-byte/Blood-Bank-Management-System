-- ============================================================
-- Blood Bank Management System - Database Schema
-- ============================================================

DROP DATABASE IF EXISTS blood_bank_db;
CREATE DATABASE blood_bank_db;
USE blood_bank_db;

-- ------------------------------------------------------------
-- Table: admin
-- ------------------------------------------------------------
CREATE TABLE admin (
    admin_id   INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(50)  NOT NULL
);

INSERT INTO admin (username, password) VALUES ('admin', 'admin123');

-- ------------------------------------------------------------
-- Table: donor
-- ------------------------------------------------------------
CREATE TABLE donor (
    donor_id           INT AUTO_INCREMENT PRIMARY KEY,
    full_name           VARCHAR(100) NOT NULL,
    age                 INT NOT NULL,
    gender              VARCHAR(10)  NOT NULL,
    blood_group         VARCHAR(5)   NOT NULL,
    phone_number        VARCHAR(15)  NOT NULL,
    email                VARCHAR(100),
    address              VARCHAR(255),
    last_donation_date  DATE
);

-- ------------------------------------------------------------
-- Table: blood_stock
-- ------------------------------------------------------------
CREATE TABLE blood_stock (
    blood_group      VARCHAR(5) PRIMARY KEY,
    units_available  INT NOT NULL DEFAULT 0
);

INSERT INTO blood_stock (blood_group, units_available) VALUES
('A+', 10), ('A-', 5), ('B+', 10), ('B-', 5),
('AB+', 5), ('AB-', 2), ('O+', 15), ('O-', 8);

-- ------------------------------------------------------------
-- Table: donation
-- ------------------------------------------------------------
CREATE TABLE donation (
    donation_id     INT AUTO_INCREMENT PRIMARY KEY,
    donor_id        INT NOT NULL,
    blood_group     VARCHAR(5) NOT NULL,
    donation_date   DATE NOT NULL,
    units_donated   INT NOT NULL,
    CONSTRAINT fk_donation_donor FOREIGN KEY (donor_id) REFERENCES donor(donor_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_donation_bloodgroup FOREIGN KEY (blood_group) REFERENCES blood_stock(blood_group)
);

-- ------------------------------------------------------------
-- Table: blood_request
-- ------------------------------------------------------------
CREATE TABLE blood_request (
    request_id       INT AUTO_INCREMENT PRIMARY KEY,
    patient_name      VARCHAR(100) NOT NULL,
    hospital_name     VARCHAR(100) NOT NULL,
    contact_number    VARCHAR(15)  NOT NULL,
    blood_group       VARCHAR(5)   NOT NULL,
    units_required    INT NOT NULL,
    request_date      DATE NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'Pending',
    CONSTRAINT fk_request_bloodgroup FOREIGN KEY (blood_group) REFERENCES blood_stock(blood_group)
);

-- ------------------------------------------------------------
-- Sample donors
-- ------------------------------------------------------------
INSERT INTO donor (full_name, age, gender, blood_group, phone_number, email, address, last_donation_date) VALUES
('Rahul Sharma', 28, 'Male', 'O+', '9876543210', 'rahul.sharma@example.com', 'Indore, MP', '2026-06-15'),
('Priya Verma', 24, 'Female', 'A+', '9123456780', 'priya.verma@example.com', 'Bhopal, MP', '2026-05-10'),
('Amit Singh', 32, 'Male', 'B+', '9988776655', 'amit.singh@example.com', 'Ujjain, MP', '2026-07-01'),
('Sneha Patil', 27, 'Female', 'AB+', '9090909090', 'sneha.patil@example.com', 'Indore, MP', NULL);

-- ------------------------------------------------------------
-- Sample donations (also reflected in blood_stock inserts above)
-- ------------------------------------------------------------
INSERT INTO donation (donor_id, blood_group, donation_date, units_donated) VALUES
(1, 'O+', '2026-06-15', 1),
(2, 'A+', '2026-05-10', 1),
(3, 'B+', '2026-07-01', 1);

-- ------------------------------------------------------------
-- Sample blood requests
-- ------------------------------------------------------------
INSERT INTO blood_request (patient_name, hospital_name, contact_number, blood_group, units_required, request_date, status) VALUES
('Ramesh Chandra', 'City Hospital', '9871234560', 'O+', 2, '2026-09-01', 'Pending'),
('Kavita Joshi', 'Apollo Clinic', '9112233445', 'A+', 1, '2026-09-10', 'Approved');

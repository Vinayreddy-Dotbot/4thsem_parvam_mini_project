CREATE DATABASE IF NOT EXISTS hospital_patient_db;
USE hospital_patient_db;

CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age >= 0),
    gender VARCHAR(20) NOT NULL,
    blood VARCHAR(5) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    admitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    dept VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    ward VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'admitted',
    appointment_at DATETIME NULL,
    appointment_notes VARCHAR(255) NOT NULL DEFAULT '',
    CONSTRAINT fk_admissions_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_admissions_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE admissions
    ADD COLUMN IF NOT EXISTS appointment_at DATETIME NULL AFTER status;

ALTER TABLE admissions
    ADD COLUMN IF NOT EXISTS appointment_notes VARCHAR(255) NOT NULL DEFAULT '' AFTER appointment_at;

INSERT IGNORE INTO doctors (id, name, dept) VALUES
    (1, 'Dr. Meera Rao', 'Cardiology'),
    (2, 'Dr. Arjun Menon', 'General Medicine'),
    (3, 'Dr. Kavya Iyer', 'Neurology'),
    (4, 'Dr. Farhan Ali', 'Orthopedics'),
    (5, 'Dr. Nisha Reddy', 'Pediatrics'),
    (6, 'Dr. Rohan Das', 'Emergency');

INSERT IGNORE INTO patients (id, name, age, gender, blood, phone, address) VALUES
    (1, 'Aarav Sharma', 34, 'Male', 'B+', '9876543210', 'Delhi'),
    (2, 'Priya Nair', 28, 'Female', 'O+', '9876501234', 'Kochi'),
    (3, 'Sanjay Kumar', 67, 'Male', 'A-', '9988776655', 'Hyderabad');

INSERT IGNORE INTO admissions (id, patient_id, doctor_id, ward, status, appointment_at, appointment_notes) VALUES
    (1, 1, 1, 'A-101', 'admitted', DATE_ADD(NOW(), INTERVAL 1 DAY), 'Follow-up with cardiology team'),
    (2, 2, 2, 'B-204', 'under observation', DATE_ADD(NOW(), INTERVAL 2 DAY), 'Review fever and infection symptoms'),
    (3, 3, 6, 'ER-01', 'emergency', NOW(), 'Emergency priority review');

UPDATE admissions
SET appointment_at = COALESCE(appointment_at, DATE_ADD(NOW(), INTERVAL 1 DAY)),
    appointment_notes = IF(appointment_notes = '', 'Follow-up with assigned doctor', appointment_notes)
WHERE id IN (1, 2, 3);

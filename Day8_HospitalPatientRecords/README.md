# Hospital Patient Records

## Pages
Home, Register Patient, Patient List, Patient Detail, Edit, Discharge, Search

## Database Tables
patients(id, name, age, gender, blood, phone, address, admitted_at)

doctors(id, name, dept)

admissions(id, patient_id, doctor_id, ward, status)

## Core Features
Register patient, assign doctor, view records, edit details, discharge, search by name/ID.

## Rules
Patient ID auto-generates. Doctor dropdown is grouped by department. Admission status follows admitted, under observation, emergency, discharged.

## AI-Oriented Enhancements Description
1. AI Patient Health Summary Generator: Generates a summarized patient health report using patient details, admission history, and doctor information.
2. AI Disease Risk Prediction: Analyzes patient age, blood group, admission status, and health record signals to mark risk level.
3. AI Smart Patient Search: Supports natural language queries such as "Show admitted patients" and "Patients under Cardiology department".
4. AI Doctor Recommendation System: Suggests doctor/department suitability based on patient condition and current assignment.
5. AI Emergency Alert & Priority Detection: Marks critical patients using age and emergency admission status.
6. AI Hospital Analytics Dashboard: Shows department-wise patient count, admission status statistics, discharge count, and doctor workload.

## XAMPP Run Order
1. Start MySQL and Tomcat from XAMPP.
2. Run `run_migration.bat`.
3. Run `build_and_deploy.bat`. The script deploys to `%USERPROFILE%\Downloads\Xampp\tomcat` first when that XAMPP copy exists.
4. Open `http://localhost:8080/hospital_app/`.
5. Login with `admin` / `admin123`.

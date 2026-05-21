package controllers;

import models.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HospitalAiService {
    private HospitalAiService() {
    }

    public static int calculateRiskScore(Patient patient) {
        if (patient == null) {
            return 0;
        }
        if (patient.isDischarged()) {
            return 5;
        }

        int score = 10;
        String status = normalize(patient.getStatus());
        String department = normalize(patient.getDoctorDept());
        String blood = normalize(patient.getBlood());
        String ward = normalize(patient.getWard());
        int age = patient.getAge();

        if (status.contains("emergency")) {
            score += 50;
        } else if (status.contains("observation")) {
            score += 25;
        } else if (status.contains("admitted")) {
            score += 12;
        }

        if (age >= 75) {
            score += 30;
        } else if (age >= 65) {
            score += 22;
        } else if (age >= 55) {
            score += 12;
        } else if (age <= 5 && age > 0) {
            score += 20;
        } else if (age <= 12 && age > 0) {
            score += 15;
        }

        if (department.contains("emergency")) {
            score += 12;
        } else if (department.contains("cardiology") || department.contains("neurology")) {
            score += 10;
        } else if (department.contains("pediatrics") && age <= 12) {
            score += 8;
        }

        if (blood.endsWith("-")) {
            score += blood.equals("o-") ? 8 : 5;
        }

        if (ward.contains("icu") || ward.contains("critical")) {
            score += 15;
        }

        return Math.min(score, 100);
    }

    public static String getRiskLevel(Patient patient) {
        if (patient != null && patient.isDischarged()) {
            return "Closed (5/100)";
        }

        int score = calculateRiskScore(patient);
        if (score >= 75) {
            return "Critical (" + score + "/100)";
        }
        if (score >= 55) {
            return "High (" + score + "/100)";
        }
        if (score >= 35) {
            return "Moderate (" + score + "/100)";
        }
        return "Low (" + score + "/100)";
    }

    public static String getRiskClass(Patient patient) {
        if (patient != null && patient.isDischarged()) {
            return "muted";
        }

        int score = calculateRiskScore(patient);
        if (score >= 55) {
            return "danger";
        }
        if (score >= 35) {
            return "warning";
        }
        return "success";
    }

    public static String getPriorityLabel(Patient patient) {
        if (patient != null && patient.isDischarged()) {
            return "Closed";
        }

        int score = calculateRiskScore(patient);
        if (score >= 75) {
            return "Critical Priority";
        }
        if (score >= 55) {
            return "High Priority";
        }
        if (score >= 35) {
            return "Needs Monitoring";
        }
        return "Stable";
    }

    public static String getRiskReasons(Patient patient) {
        if (patient == null) {
            return "No patient record available for analysis.";
        }
        if (patient.isDischarged()) {
            return "Patient is already discharged, so active clinical risk is closed.";
        }

        List<String> reasons = new ArrayList<>();
        String status = normalize(patient.getStatus());
        String department = normalize(patient.getDoctorDept());
        String blood = normalize(patient.getBlood());
        String ward = normalize(patient.getWard());
        int age = patient.getAge();

        if (status.contains("emergency")) {
            reasons.add("emergency admission status");
        } else if (status.contains("observation")) {
            reasons.add("under-observation admission status");
        }

        if (age >= 65) {
            reasons.add("senior patient age");
        } else if (age <= 12 && age > 0) {
            reasons.add("child patient age");
        }

        if (department.contains("emergency") || department.contains("cardiology") || department.contains("neurology")) {
            reasons.add("specialist department workload");
        }

        if (blood.endsWith("-")) {
            reasons.add("negative blood group needs careful stock planning");
        }

        if (ward.contains("icu") || ward.contains("critical")) {
            reasons.add("critical-care ward indicator");
        }

        if (reasons.isEmpty()) {
            return "No critical indicators detected from the current record.";
        }
        return joinReasons(reasons);
    }

    public static String getHealthSummary(Patient patient) {
        if (patient == null) {
            return "No patient details are available for AI summary generation.";
        }

        String doctor = blank(patient.getDoctorName()) ? "an unassigned doctor" : patient.getDoctorName();
        String department = blank(patient.getDoctorDept()) ? "an unassigned department" : patient.getDoctorDept();
        String ward = blank(patient.getWard()) ? "N/A" : patient.getWard();
        String appointment = blank(patient.getAppointmentAt())
            ? "No appointment is scheduled yet"
            : "Next appointment is " + patient.getAppointmentAt();

        return patient.getName() + " is a " + patient.getAge() + "-year-old " + patient.getGender()
            + " patient with blood group " + patient.getBlood() + ". Latest admission is in ward "
            + ward + ", assigned to " + doctor + " from " + department + ". Current status is "
            + patient.getStatusDisplay() + ", with AI risk marked as " + getRiskLevel(patient) + ". "
            + appointment + ".";
    }

    public static String getDoctorRecommendation(Patient patient) {
        if (patient == null) {
            return "Select a patient record before generating a doctor recommendation.";
        }

        String suggestedDepartment = getRecommendedDepartment(patient);
        String currentDepartment = blank(patient.getDoctorDept()) ? "Unassigned" : patient.getDoctorDept();
        String currentDoctor = blank(patient.getDoctorName()) ? "No doctor assigned" : patient.getDoctorName();

        if (normalize(currentDepartment).equals(normalize(suggestedDepartment))) {
            return "Assigned doctor " + currentDoctor + " from " + currentDepartment
                + " fits the current AI triage. Suggested follow-up: " + getFollowUpAction(patient);
        }

        return "AI suggests review by " + suggestedDepartment + ". Current assignment is " + currentDoctor
            + " from " + currentDepartment + ". Suggested follow-up: " + getFollowUpAction(patient);
    }

    public static String getEmergencyAction(Patient patient) {
        if (patient == null) {
            return "No emergency action generated.";
        }
        if (patient.isDischarged()) {
            return "No emergency alert. Patient is already discharged.";
        }

        int score = calculateRiskScore(patient);
        if (score >= 75) {
            return "Move this case to the emergency queue, notify the duty doctor, and review vitals immediately.";
        }
        if (score >= 55) {
            return "Keep this case on high-priority rounds and confirm doctor review during the next cycle.";
        }
        if (score >= 35) {
            return "Monitor vitals and symptoms, then update status if the patient condition changes.";
        }
        return "No emergency alert from the current patient record.";
    }

    public static String getRecommendedDepartment(Patient patient) {
        if (patient == null) {
            return "General Medicine";
        }

        String status = normalize(patient.getStatus());
        String currentDepartment = blank(patient.getDoctorDept()) ? "" : patient.getDoctorDept();
        int age = patient.getAge();

        if (status.contains("emergency")) {
            return "Emergency";
        }
        if (age <= 12 && age > 0) {
            return "Pediatrics";
        }
        if (age >= 65 && (blank(currentDepartment) || normalize(currentDepartment).contains("general"))) {
            return "Cardiology";
        }
        if (!blank(currentDepartment)) {
            return currentDepartment;
        }
        return "General Medicine";
    }

    public static String getFollowUpAction(Patient patient) {
        if (patient == null) {
            return "complete triage first";
        }
        int score = calculateRiskScore(patient);
        if (score >= 75) {
            return blank(patient.getAppointmentAt()) ? "schedule immediate emergency review" : "keep emergency review appointment";
        }
        if (score >= 55) {
            return blank(patient.getAppointmentAt()) ? "schedule same-day specialist review" : "confirm scheduled specialist review";
        }
        if (score >= 35) {
            return blank(patient.getAppointmentAt()) ? "schedule observation follow-up" : "repeat assessment at appointment";
        }
        return "standard ward follow-up";
    }

    private static String joinReasons(List<String> reasons) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < reasons.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(reasons.get(i));
        }
        return builder.toString();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
